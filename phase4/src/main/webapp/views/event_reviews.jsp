<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.EventDetail" %>
<%@ page import="teamk2p.db.EventDao.EventReviewItem" %>

<%
    EventDetail ev = (EventDetail) request.getAttribute("detail");
    List<EventReviewItem> reviews =
        (List<EventReviewItem>) request.getAttribute("reviews");

    if (ev == null) {
%>
<html><body><p>이벤트 정보를 불러올 수 없습니다.</p></body></html>
<%
        return;
    }
%>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= ev.getTitle() %> - 리뷰 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">
    <a href="<%= request.getContextPath() %>/events/detail?event_id=<%= ev.getEventId() %>"
       class="btn btn-link mb-3">&larr; 이벤트 상세로</a>

    <h2 class="mb-1"><%= ev.getTitle() %></h2>
    <p class="text-muted mb-3">
        <strong>클럽:</strong> <%= ev.getClubName() %> /
        <strong>장소:</strong> <%= ev.getVenueName() %>
    </p>

    <h5 class="mb-3">전체 리뷰</h5>

    <%
        if (reviews == null || reviews.isEmpty()) {
    %>
    <p class="text-muted">등록된 리뷰가 없습니다.</p>
    <%
        } else {
    %>
    <div class="list-group">
        <% for (EventReviewItem r : reviews) { %>
        <div class="list-group-item">
            <div class="d-flex justify-content-between">
                <div>
                    <strong><%= r.getUserName() %></strong>
                    <span class="text-muted small"> (user_id: <%= r.getUserId() %>)</span>
                </div>
                <div>
                    <span class="badge bg-primary">
                        <%= r.getRating() %> / 5
                    </span>
                </div>
            </div>
            <p class="mb-1 mt-2"><%= r.getReviewText() %></p>
            <p class="text-muted small mb-0">
                작성일: <%= r.getCreatedAt() %> /
                상태: <%= r.getStatus() %>
            </p>
        </div>
        <% } %>
    </div>
    <% } %>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
