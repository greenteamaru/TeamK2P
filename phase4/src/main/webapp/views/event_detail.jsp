<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.EventDao.EventDetail" %>
<%@ page import="teamk2p.db.UserDao.LoginUser" %>

<%
    // 1) 컨트롤러가 넘겨준 이벤트 상세 객체
    EventDetail ev = (EventDetail) request.getAttribute("detail");
    if (ev == null) {
%>
<html>
<body>
<p>이벤트 정보를 불러올 수 없습니다.</p>
</body>
</html>
<%
        return;
    }

    // 남은 자리
    int remaining = ev.getCapacity() - ev.getCurrentRegistrations();

    // 신청 결과 코드 (등록 서블릿에서 세팅)
    String regResult = (String) request.getAttribute("regResult");

    // 로그인 사용자 (세션)
    LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

    // 오늘 기준으로 이벤트가 지났는지 계산
    java.util.Date now = new java.util.Date();
    boolean isPast;
    if (ev.getEndTime() != null) {
        isPast = ev.getEndTime().before(now);
    } else {
        isPast = ev.getStartTime().before(now);
    }

    // 신청 가능 여부 = 정원 남아 있고 + 아직 안 끝난 이벤트
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
<div class="container py-4">

    <a href="<%= request.getContextPath() %>/events"
       class="btn btn-link mb-3">&larr; 목록으로</a>

    <%-- 3) 신청 결과 메시지 --%>
    <%
        if (regResult != null) {
    %>
    <div class="alert alert-info">
        <%
            if ("OK".equals(regResult)) {
        %>
            신청이 완료되었습니다.
        <%
            } else if ("FULL".equals(regResult)) {
        %>
            정원이 이미 모두 찼습니다. (신청 불가)
        <%
            } else if ("DUP".equals(regResult)) {
        %>
            이미 이 이벤트에 신청한 사용자입니다.
        <%
            } else if ("NO_EVENT".equals(regResult)) {
        %>
            해당 이벤트를 찾을 수 없습니다.
        <%
            } else {
        %>
            신청 처리 중 오류가 발생했습니다.
        <%
            }
        %>
    </div>
    <%
        }
    %>

    <%-- 4) 이벤트 정보 카드 --%>
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
                <strong>참가비:</strong>
                <%= ev.getFee() %>
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
                <%
                    }
                %>
            </p>

            <hr/>

            <p class="mb-3">
                <strong>설명:</strong><br/>
                <%= ev.getDescription() %>
            </p>

            <%-- 5) 참가 신청 폼  --%>
            <form action="<%= request.getContextPath() %>/events/register"
                  method="post"
                  class="mt-3 border-top pt-3">

                <%-- 이제는 event_id만 넘기면 됨. user_id는 세션에서 가져감 --%>
                <input type="hidden" name="event_id" value="<%= ev.getEventId() %>">

                <%-- 현재 로그인 사용자 정보 표시만 해주기 --%>
                <%
                    if (loginUser != null) {
                %>
                    <p class="mb-2 small text-muted">
                        현재 로그인: <strong><%= loginUser.getName() %></strong>
                        (&lt;<%= loginUser.getEmail() %>&gt;)
                    </p>
                <%
                    }
                %>

                <button type="submit"
                        class="btn btn-success btn-sm"
                        <%= canApply ? "" : "disabled" %>>
                    참가 신청
                </button>

                <%-- 안내 메시지 --%>
                <%
                    if (isPast) {
                %>
                    <p class="text-warning small mt-2">
                        이미 종료된 이벤트입니다. 새로운 신청은 불가합니다.
                    </p>
                <%
                    } else if (remaining <= 0) {
                %>
                    <p class="text-danger small mt-2">
                        정원이 가득 찼습니다. 더 이상 신청할 수 없습니다.
                    </p>
                <%
                    }
                %>
            </form>
        </div>
    </div>

</div>
</body>
</html>
