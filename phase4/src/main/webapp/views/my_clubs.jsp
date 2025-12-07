<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.ClubDao.MyClubItem" %>

<%
    String ctx = request.getContextPath();
    List<MyClubItem> clubs =
        (List<MyClubItem>) request.getAttribute("clubs");
    String leaveResult =
        (String) request.getAttribute("leaveResult");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>내 클럽 - Campus Club Event Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <h2 class="mb-3">내 클럽</h2>

    <%-- 탈퇴 결과 메시지 --%>
    <%
        if (leaveResult != null) {
    %>
    <div class="alert alert-info">
        <%
            if ("LEFT".equals(leaveResult)) {
        %>
            클럽 탈퇴가 완료되었습니다.
        <%
            } else if ("NO_ACTIVE".equals(leaveResult)) {
        %>
            탈퇴할 수 있는 활성 멤버십이 없습니다.
        <%
            } else {
        %>
            클럽 탈퇴 처리 중 오류가 발생했습니다. (코드: <%= leaveResult %>)
        <%
            }
        %>
    </div>
    <%
        }
    %>

    <%
        if (clubs == null || clubs.isEmpty()) {
    %>
        <p class="text-muted">가입한 클럽이 없습니다.</p>
    <%
        } else {
    %>
    <div class="table-responsive">
        <table class="table table-sm align-middle">
            <thead class="table-light">
            <tr>
                <th>클럽 이름</th>
                <th>유형</th>
                <th>내 역할</th>
                <th>상태</th>
                <th style="width: 200px;">액션</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MyClubItem c : clubs) {
            %>
            <tr>
                <td><%= c.getName() %></td>
                <td><%= c.getClubType() %></td>
                <td><%= c.getRole() %></td>
                <td><%= c.getStatus() %></td>
                <td>
                    <div class="d-flex gap-2">
                        <%-- 클럽 상세보기: 매핑에 맞게 수정해서 사용 (/clubs/detail?club_id=...) --%>
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/clubs/detail?club_id=<%= c.getClubId() %>">
                            상세보기
                        </a>

                        <% if ("ACTIVE".equalsIgnoreCase(c.getStatus())) { %>
                        <form method="post"
                              action="<%= ctx %>/clubs/leave"
                              onsubmit="return confirm('이 클럽을 탈퇴하시겠습니까?');">
                            <input type="hidden" name="club_id"
                                   value="<%= c.getClubId() %>">
                            <button type="submit"
                                    class="btn btn-sm btn-outline-danger">
                                탈퇴
                            </button>
                        </form>
                        <% } %>
                    </div>
                </td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
    <%
        }
    %>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
