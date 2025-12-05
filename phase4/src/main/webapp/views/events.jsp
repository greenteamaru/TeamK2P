<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*, teamk2p.db.EventDao.EventListItem" %>
<%
    List<EventListItem> events =
        (List<EventListItem>) request.getAttribute("events");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upcoming Events</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4">
    <h1 class="mb-4">향후 7일 내 동아리 이벤트</h1>

    <%
        if (events == null || events.isEmpty()) {
    %>
        <p class="text-muted">향후 7일 이내 예정된 이벤트가 없습니다.</p>
    <%
        } else {
    %>
        <div class="row g-3">
        <%
            for (EventListItem e : events) {
        %>
            <div class="col-md-6 col-lg-4">
                <div class="card shadow-sm h-100">
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title"><%= e.getTitle() %></h5>
                        <p class="card-subtitle text-muted small mb-2">
                            <%= e.getClubName() %> · <%= e.getVenueName() %>
                        </p>
                        <p class="small text-secondary mb-3">
                            시작: <%= e.getStartTime() %>
                        </p>
                        <div class="mt-auto">
                            <a href="<%= request.getContextPath() %>/events/detail?id=<%= e.getEventId() %>"
							   class="btn btn-sm btn-primary w-100">
							    상세 보기
							</a>
                        </div>
                    </div>
                </div>
            </div>
        <%
            }
        %>
        </div>
    <%
        }
    %>
</div>
</body>
</html>
