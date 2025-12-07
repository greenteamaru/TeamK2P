<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="teamk2p.db.UserDao.AdminUserItem" %>

<%@ include file="common/header.jspf" %>

<%
    // 검색어 값 미리 받아두기
    String emailKeyword = (String) request.getAttribute("emailKeyword");
    if (emailKeyword == null) {
        emailKeyword = "";
    }

    List<AdminUserItem> users =
        (List<AdminUserItem>) request.getAttribute("users");
%>

<h2>회원 관리 (관리자 전용)</h2>

<form method="get" action="<%= request.getContextPath() %>/admin/users" style="margin-bottom: 16px;">
    <label>이메일 검색:
        <input type="text" name="emailKeyword"
               value="<%= emailKeyword %>">
    </label>
    <button type="submit">검색</button>
    <a href="<%= request.getContextPath() %>/admin/users">전체 보기</a>
</form>

<table border="1" cellpadding="4" cellspacing="0">
    <thead>
    <tr>
        <th>ID</th>
        <th>이름</th>
        <th>이메일</th>
        <th>권한</th>
    </tr>
    </thead>
    <tbody>
    <%
        if (users == null || users.isEmpty()) {
    %>
        <tr>
            <td colspan="4" style="text-align:center;">조회된 회원이 없습니다.</td>
        </tr>
    <%
        } else {
            for (AdminUserItem u : users) {
    %>
        <tr>
            <td><%= u.getUserId() %></td>
            <td><%= u.getName() %></td>
            <td><%= u.getEmail() %></td>
            <td><%= u.isAdmin() ? "관리자" : "일반" %></td>
        </tr>
    <%
            }
        }
    %>
    </tbody>
</table>
