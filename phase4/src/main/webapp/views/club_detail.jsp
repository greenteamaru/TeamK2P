<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.ClubDao.ClubDetail" %>
<%@ page import="teamk2p.db.EventDao.ClubEventItem" %>

<%
    ClubDetail c = (ClubDetail) request.getAttribute("detail");
    if (c == null) {
%>
<html><body><p>클럽 정보를 불러올 수 없습니다.</p></body></html>
<%
        return;
    }

    List<ClubEventItem> upcoming =
        (List<ClubEventItem>) request.getAttribute("upcomingEvents");
    List<ClubEventItem> past =
        (List<ClubEventItem>) request.getAttribute("pastEvents");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title><%= c.getName() %> - 클럽 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <a href="<%= request.getContextPath() %>/clubs"
       class="btn btn-link mb-3">&larr; 클럽 목록으로</a>

    <!-- 클럽 기본 정보 카드 -->
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <h2 class="card-title mb-3"><%= c.getName() %></h2>

            <p class="text-muted mb-1">
                <strong>카테고리:</strong> <%= c.getCategoryName() %>
                /
                <strong>유형:</strong> <%= c.getClubType() %>
            </p>
            <p class="text-muted mb-2">
                <strong>상태:</strong> <%= c.getStatus() %>
                /
                <strong>개설일:</strong> <%= c.getCreatedAt() %>
            </p>

            <p class="mb-2">
                <strong>멤버 수:</strong>
                현재 <%= c.getActiveMembers() %>명
                (누적 <%= c.getTotalMembers() %>명)
            </p>

            <p class="mb-2">
                <strong>클럽 평점:</strong>
                <%
                    Double avg = c.getAvgRating();
                    int rc = c.getReviewCount();
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
            <p class="mb-0">
                <strong>소개:</strong><br/>
                <%= c.getDescription() %>
            </p>
        </div>
    </div>

    <!-- (선택) 내 멤버십 정보 -->
    <%
        if (c.getMyStatus() != null) {
    %>
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <h5 class="card-title mb-2">내 멤버십 정보</h5>
            <p class="mb-1">
                <strong>역할:</strong> <%= c.getMyRole() %>
                /
                <strong>상태:</strong> <%= c.getMyStatus() %>
            </p>
            <p class="mb-0">
                <strong>가입일:</strong> <%= c.getMyJoinedAt() %>
                <% if (c.getMyLeftAt() != null) { %>
                    / <strong>탈퇴일:</strong> <%= c.getMyLeftAt() %>
                <% } %>
            </p>
        </div>
    </div>
    <%
        }
    %>

    <!-- 향후 이벤트 -->
    <h4 class="mb-2">앞으로 열릴 이벤트</h4>
    <%
        if (upcoming == null || upcoming.isEmpty()) {
    %>
        <p class="text-muted">예정된 이벤트가 없습니다.</p>
    <%
        } else {
    %>
    <div class="table-responsive mb-4">
        <table class="table table-sm align-middle">
            <thead class="table-light">
            <tr>
                <th>제목</th>
                <th>시작 시간</th>
                <th>상태</th>
                <th>평점</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (ClubEventItem e : upcoming) {
            %>
            <tr>
                <td>
                    <a href="<%= request.getContextPath() %>/events/detail?event_id=<%= e.getEventId() %>">
                        <%= e.getTitle() %>
                    </a>
                </td>
                <td><%= e.getStartTime() %></td>
                <td><%= e.getStatus() %></td>
                <td>
                    <%
                        Double ea = e.getAvgRating();
                        int erc = e.getReviewCount();
                        if (ea == null || erc == 0) {
                    %>
                        -
                    <%
                        } else {
                    %>
                        <%= String.format("%.1f", ea) %> / 5.0
                        (<%= erc %>개)
                    <%
                        }
                    %>
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

    <!-- 지난 이벤트 -->
    <h4 class="mb-2">완료된 이벤트</h4>
    <%
        if (past == null || past.isEmpty()) {
    %>
        <p class="text-muted">완료된 이벤트가 없습니다.</p>
    <%
        } else {
    %>
    <div class="table-responsive mb-4">
        <table class="table table-sm align-middle">
            <thead class="table-light">
            <tr>
                <th>제목</th>
                <th>시작 시간</th>
                <th>상태</th>
                <th>평점</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (ClubEventItem e : past) {
            %>
            <tr>
                <td>
                    <a href="<%= request.getContextPath() %>/events/detail?event_id=<%= e.getEventId() %>">
                        <%= e.getTitle() %>
                    </a>
                </td>
                <td><%= e.getStartTime() %></td>
                <td><%= e.getStatus() %></td>
                <td>
                    <%
                        Double ea = e.getAvgRating();
                        int erc = e.getReviewCount();
                        if (ea == null || erc == 0) {
                    %>
                        -
                    <%
                        } else {
                    %>
                        <%= String.format("%.1f", ea) %> / 5.0
                        (<%= erc %>개)
                    <%
                        }
                    %>
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
