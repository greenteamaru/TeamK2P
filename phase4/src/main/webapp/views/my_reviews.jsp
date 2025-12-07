<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.MyReviewItem" %>

<%
    List<MyReviewItem> reviews =
        (List<MyReviewItem>) request.getAttribute("reviews");
    String deleteResult = (String) request.getAttribute("deleteResult");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 리뷰 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">
    <h2 class="mb-3">내 리뷰</h2>

    <% if (deleteResult != null) { %>
    <div class="alert alert-warning">
        <% if ("DELETED".equals(deleteResult)) { %>
            리뷰가 삭제되었습니다.
        <% } else if ("NO_REVIEW".equals(deleteResult)) { %>
            삭제할 리뷰가 없습니다.
        <% } else { %>
            리뷰 삭제 처리 중 오류가 발생했습니다.
        <% } %>
    </div>
    <% } %>

    <%
        if (reviews == null || reviews.isEmpty()) {
    %>
    <p class="text-muted">작성한 리뷰가 없습니다.</p>
    <%
        } else {
    %>
    <div class="table-responsive">
        <table class="table table-sm align-middle">
            <thead class="table-light">
            <tr>
                <th>이벤트</th>
                <th>평점</th>
                <th>상태</th>
                <th>작성일</th>
                <th>내용</th>
                <th>관리</th>
            </tr>
            </thead>
            <tbody>
            <% for (MyReviewItem r : reviews) { %>
            <tr>
                <td>
                    <a href="<%= request.getContextPath() %>/events/detail?event_id=<%= r.getEventId() %>">
                        <%= r.getEventTitle() %>
                    </a>
                </td>
                <td><%= r.getRating() %> / 5</td>
                <td><%= r.getStatus() %></td>
                <td><%= r.getCreatedAt() %></td>
                <td><%= r.getReviewText() %></td>
                <td>
                    <form action="<%= request.getContextPath() %>/events/review/delete"
                          method="post"
                          onsubmit="return confirm('이 리뷰를 삭제하시겠습니까?');">
                        <input type="hidden" name="event_id" value="<%= r.getEventId() %>">
                        <input type="hidden" name="from" value="myReviews">
                        <button type="submit" class="btn btn-sm btn-outline-danger">
                            삭제
                        </button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
