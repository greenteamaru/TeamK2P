<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.EventDao.EventDetail" %>

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

    int remaining = ev.getCapacity() - ev.getCurrentRegistrations();

    // 2) 신청 결과 코드 (등록 서블릿에서 세팅)
    String regResult = (String) request.getAttribute("regResult");
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
            <p class="mb-3">
                <strong>상태:</strong> <%= ev.getStatus() %>
            </p>

            <hr/>

            <p class="mb-3">
                <strong>설명:</strong><br/>
                <%= ev.getDescription() %>
            </p>

            <%-- 5) 참가 신청 폼 (원하면 나중에 활성화) --%>
            <form action="<%= request.getContextPath() %>/events/register"
                  method="post"
                  class="mt-3 border-top pt-3">

                <input type="hidden" name="event_id" value="<%= ev.getEventId() %>">

                <div class="mb-2">
                    <label class="form-label form-label-sm">사용자 ID</label>
                    <input type="number" name="user_id"
                           class="form-control form-control-sm"
                           placeholder="예: 100" required>
                    <div class="form-text">
                        테스트용으로 users 테이블에 있는 user_id 입력.
                    </div>
                </div>

                <button type="submit" class="btn btn-success btn-sm">
                    참가 신청
                </button>
            </form>

        </div>
    </div>

</div>
</body>
</html>
