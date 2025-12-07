<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.UserDao.UserProfile" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 프로필 - Team K²P</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">

<%@ include file="common/header.jspf" %>

<div class="container" style="max-width: 720px;">
    <h2 class="mb-4">내 프로필</h2>

    <%
        UserProfile p = (UserProfile) request.getAttribute("profile");
        String saved = request.getParameter("saved");
        if ("1".equals(saved)) {
    %>
        <div class="alert alert-success">
            프로필이 저장되었습니다.
        </div>
    <%
        }
        if (p == null) {
    %>
        <div class="alert alert-danger">
            프로필 정보를 불러올 수 없습니다.
        </div>
    <%
        } else {
    %>
    <form method="post" action="<%= request.getContextPath() %>/me/profile">
        <div class="mb-3">
            <label class="form-label">이메일</label>
            <input type="text" class="form-control" value="<%= p.getEmail() %>" disabled>
        </div>

        <div class="mb-3">
            <label class="form-label">이름</label>
            <input type="text" class="form-control" name="name" value="<%= p.getName() %>">
        </div>

        <div class="mb-3">
            <label class="form-label">닉네임</label>
            <input type="text" class="form-control" name="nickname" value="<%= p.getNickname() %>">
        </div>

        <div class="mb-3">
            <label class="form-label">국적</label>
            <input type="text" class="form-control" name="nationality" value="<%= p.getNationality() %>">
        </div>

        <div class="mb-3">
            <label class="form-label">선호 언어</label>
            <input type="text" class="form-control" name="language" value="<%= p.getLanguage() %>">
        </div>

        <div class="mb-3">
            <label class="form-label">가입일</label>
            <input type="text" class="form-control" value="<%= p.getCreatedAt() %>" disabled>
        </div>

        <button type="submit" class="btn btn-primary">
            프로필 저장
        </button>
    </form>
    <%
        }
    %>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
