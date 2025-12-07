<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="teamk2p.db.ClubDao.ClubSummary" %>
<%@ page import="teamk2p.db.ClubDao.MyClubItem" %>

<%
    String ctx = request.getContextPath();

    List<ClubSummary> clubs =
        (List<ClubSummary>) request.getAttribute("clubs");
    Map<Integer, MyClubItem> myClubMap =
        (Map<Integer, MyClubItem>) request.getAttribute("myClubMap");

    String sort = (String) request.getAttribute("sort");
    if (sort == null) sort = "name";
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>클럽 목록 - Team K²P</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h2 class="mb-1">활성 클럽 목록</h2>
            <p class="text-muted mb-0">
                현재 <code>ACTIVE</code> 상태인 클럽만 표시합니다. (비활성/폐쇄 클럽 제외)
            </p>
        </div>

        <!-- 정렬 드롭다운 -->
        <form method="get" class="d-flex align-items-center">
            <label class="me-2 small text-muted">정렬:</label>
            <select name="sort" class="form-select form-select-sm"
                    onchange="this.form.submit()">
                <option value="name" <%= "name".equals(sort) ? "selected" : "" %>>
                    이름 순
                </option>
                <option value="members_desc" <%= "members_desc".equals(sort) ? "selected" : "" %>>
                    멤버 많은 순
                </option>
                <option value="members_asc" <%= "members_asc".equals(sort) ? "selected" : "" %>>
                    멤버 적은 순
                </option>
                <option value="latest" <%= "latest".equals(sort) ? "selected" : "" %>>
                    최신순
                </option>
            </select>
        </form>
    </div>

    <%
        if (clubs == null || clubs.isEmpty()) {
    %>
        <div class="alert alert-info">
            표시할 클럽이 없습니다.
        </div>
    <%
        } else {
    %>
        <table class="table table-hover align-middle bg-white">
            <thead class="table-light">
            <tr>
                <th>이름</th>
                <th>유형</th>
                <th>카테고리</th>
                <th class="text-end">멤버 수 (현재 / 누적)</th>
                <th style="width: 180px;">액션</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (ClubSummary c : clubs) {
                    MyClubItem mine = (myClubMap == null)
                            ? null : myClubMap.get(c.getClubId());
                    boolean isMember =
                            (mine != null && "ACTIVE".equalsIgnoreCase(mine.getStatus()));
            %>
                <tr>
                    <td><%= c.getName() %></td>
                    <td><%= c.getClubType() %></td>
                    <td><%= c.getCategoryName() %></td>
                    <td class="text-end">
                        <%-- 현재 ACTIVE 인원 / 지금까지 누적 가입자 수 (정원이 아님) --%>
                        <%= c.getMemberCount() %> / <%= c.getTotalMembers() %>
                    </td>
                    <td>
                        <a href="<%= ctx %>/clubs/detail?club_id=<%= c.getClubId() %>"
                           class="btn btn-sm btn-outline-primary">
                            상세보기
                        </a>
                        <% if (isMember) { %>
                            <span class="badge bg-success ms-1">내 클럽</span>
                        <% } %>
                    </td>
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
