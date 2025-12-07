<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="teamk2p.db.ClubDao.MyClubItem" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 클럽 - Team K²P</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">

<%@ include file="common/header.jspf" %>

<div class="container">
    <h2 class="mb-4">내 클럽</h2>

    <%
        List<MyClubItem> list =
            (List<MyClubItem>) request.getAttribute("myClubs");
        if (list == null || list.isEmpty()) {
    %>
        <div class="alert alert-info">
            가입된 클럽이 없습니다.
        </div>
    <%
        } else {
    %>
        <table class="table table-hover align-middle bg-white">
            <thead class="table-light">
            <tr>
                <th>클럽 이름</th>
                <th>유형</th>
                <th>내 역할</th>
                <th>상태</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MyClubItem c : list) {
            %>
                <tr>
                    <td><%= c.getName() %></td>
                    <td><%= c.getClubType() %></td>
                    <td><%= c.getRole() %></td>
                    <td><%= c.getStatus() %></td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    <%
        }
    %>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
