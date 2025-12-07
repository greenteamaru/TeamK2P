<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.EventDao.EventDetail" %>
<%@ page import="teamk2p.db.UserDao.LoginUser" %>

<%
    EventDetail ev = (EventDetail) request.getAttribute("detail");
    if (ev == null) {
%>
<html><body><p>이벤트 정보를 불러올 수 없습니다.</p></body></html>
<%
        return;
    }

    int remaining = ev.getCapacity() - ev.getCurrentRegistrations();

    String regResult          = (String) request.getAttribute("regResult");
    String reviewResult       = (String) request.getAttribute("reviewResult");
    String reviewDeleteResult = (String) request.getAttribute("reviewDeleteResult");

    // ❌ 여기 있던 LoginUser loginUser = ... 는 삭제 (header.jspf에서 이미 선언함)

    java.util.Date now = new java.util.Date();
    boolean isPast;
    if (ev.getEndTime() != null) {
        isPast = ev.getEndTime().before(now);
    } else {
        isPast = ev.getStartTime().before(now);
    }

    boolean canApply = (remaining > 0) && !isPast;
%>

<html>
<head>
    <meta charset="UTF-8">
    <title><%= ev.getTitle() %> - 이벤트 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <a href="<%= request.getContextPath() %>/events"
       class="btn btn-link mb-3">&larr; 목록으로</a>

    <%-- 신청 / 리뷰 관련 알림 --%>
    <% if (regResult != null) { %>
    <div class="alert alert-info">
        <% if ("OK".equals(regResult)) { %>
            신청이 완료되었습니다.
        <% } else if ("FULL".equals(regResult)) { %>
            정원이 이미 모두 찼습니다. (신청 불가)
        <% } else if ("DUP".equals(regResult)) { %>
            이미 이 이벤트에 신청한 사용자입니다.
        <% } else if ("NO_EVENT".equals(regResult)) { %>
            해당 이벤트를 찾을 수 없습니다.
        <% } else { %>
            신청 처리 중 오류가 발생했습니다.
        <% } %>
    </div>
    <% } %>

    <% if (reviewResult != null) { %>
    <div class="alert alert-success">
        <% if ("INSERTED".equals(reviewResult)) { %>
            리뷰가 등록되었습니다.
        <% } else if ("UPDATED".equals(reviewResult)) { %>
            기존 리뷰가 수정되었습니다.
        <% } else { %>
            리뷰 처리 중 문제가 발생했습니다.
        <% } %>
    </div>
    <% } %>

    <% if (reviewDeleteResult != null) { %>
    <div class="alert alert-warning">
        <% if ("DELETED".equals(reviewDeleteResult)) { %>
            리뷰가 삭제되었습니다.
        <% } else if ("NO_REVIEW".equals(reviewDeleteResult)) { %>
            삭제할 리뷰가 없습니다.
        <% } else { %>
            리뷰 삭제 중 오류가 발생했습니다.
        <% } %>
    </div>
    <% } %>

    <div class="card shadow-sm">
        <div class="card-body">
            <h2 class="card-title mb-3"><%= ev.getTitle() %></h2>

            <p class="text-muted mb-1">
                <strong>클럽:</strong> <%= ev.getClubName() %> /
                <strong>장소:</strong> <%= ev.getVenueName() %>
            </p>
            <p class="text-muted mb-3">
                <strong>시간:</strong>
                <%= ev.getStartTime() %> ~ <%= ev.getEndTime() %>
            </p>

            <p class="mb-2">
                <strong>정원:</strong>
                <%= ev.getCurrentRegistrations() %> / <%= ev.getCapacity() %>
                (<%= remaining %> 자리 남음)
            </p>
            <p class="mb-2">
                <strong>참가비:</strong> <%= ev.getFee() %>
            </p>
            <p class="mb-2">
                <strong>상태:</strong> <%= ev.getStatus() %>
            </p>

            <p class="mb-2">
                <strong>이벤트 평점:</strong>
                <%
                    Double avg = ev.getAvgRating();
                    int rc = ev.getReviewCount();
                    if (avg == null || rc == 0) {
                %>
                    아직 등록된 리뷰가 없습니다.
                <%
                    } else {
                %>
                    <%= String.format("%.1f", avg) %> / 5.0
                    (<%= rc %>개 리뷰)
                    &nbsp;
                    <a href="<%= request.getContextPath() %>/events/reviews?event_id=<%= ev.getEventId() %>"
                       class="btn btn-sm btn-outline-secondary">
                        전체 리뷰 보기
                    </a>
                <%
                    }
                %>
            </p>

            <hr/>

            <p class="mb-3">
                <strong>설명:</strong><br/>
                <%= ev.getDescription() %>
            </p>

            <%-- 참가 신청 폼 --%>
            <form action="<%= request.getContextPath() %>/events/register"
                  method="post"
                  class="mt-3 border-top pt-3">

                <input type="hidden" name="event_id" value="<%= ev.getEventId() %>">

                <%-- 여기서 loginUser는 header.jspf에서 선언된 변수 사용 --%>
                <% if (loginUser != null) { %>
                <p class="mb-2 small text-muted">
                    현재 로그인: <strong><%= loginUser.getName() %></strong>
                    (&lt;<%= loginUser.getEmail() %>&gt;)
                </p>
                <% } %>

                <button type="submit"
                        class="btn btn-success btn-sm"
                        <%= canApply ? "" : "disabled" %>>
                    참가 신청
                </button>

                <% if (isPast) { %>
                <p class="text-warning small mt-2">
                    이미 종료된 이벤트입니다. 새로운 신청은 불가합니다.
                </p>
                <% } else if (remaining <= 0) { %>
                <p class="text-danger small mt-2">
                    정원이 가득 찼습니다. 더 이상 신청할 수 없습니다.
                </p>
                <% } %>
            </form>

            <%-- 리뷰 작성/수정 폼: 로그인 + 종료된 이벤트일 때만 --%>
            <% if (loginUser != null && isPast) { %>
            <hr class="mt-4"/>
            <h5 class="mb-2">리뷰 작성 / 수정</h5>
            <form action="<%= request.getContextPath() %>/events/review/write"
                  method="post"
                  class="mb-3">

                <input type="hidden" name="event_id" value="<%= ev.getEventId() %>">

                <div class="mb-2">
                    <label class="form-label form-label-sm">평점 (1~5)</label>
                    <select name="rating" class="form-select form-select-sm" required>
                        <option value="5">5 - 매우 만족</option>
                        <option value="4">4 - 만족</option>
                        <option value="3" selected>3 - 보통</option>
                        <option value="2">2 - 불만족</option>
                        <option value="1">1 - 매우 불만족</option>
                    </select>
                </div>

                <div class="mb-2">
                    <label class="form-label form-label-sm">리뷰 내용</label>
                    <textarea name="review_text"
                              class="form-control form-control-sm"
                              rows="3"
                              placeholder="이벤트에 대한 의견을 남겨주세요."></textarea>
                </div>

                <button type="submit" class="btn btn-primary btn-sm">
                    리뷰 저장
                </button>
            </form>

            <form action="<%= request.getContextPath() %>/events/review/delete"
                  method="post"
                  onsubmit="return confirm('내 리뷰를 삭제하시겠습니까?');">
                <input type="hidden" name="event_id" value="<%= ev.getEventId() %>">
                <input type="hidden" name="from" value="detail">
                <button type="submit" class="btn btn-outline-danger btn-sm">
                    내 리뷰 삭제
                </button>
            </form>
            <p class="text-muted small mt-2">
                * 같은 이벤트에 여러 번 제출하면 기존 리뷰를 수정하는 방식으로 처리합니다.
            </p>
            <% } %>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
