<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.ClubDao.ClubDetail" %>

<%
    String ctx = request.getContextPath();
    ClubDetail club = (ClubDetail) request.getAttribute("club");

    if (club == null) {
%>
<html>
<head><meta charset="UTF-8"><title>클럽 상세</title></head>
<body>
<p>클럽 정보를 불러올 수 없습니다.</p>
</body>
</html>
<%
        return;
    }

    String joinResult  = (String) request.getAttribute("joinResult");
    String leaveResult = (String) request.getAttribute("leaveResult");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title><%= club.getName() %> - 클럽 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <a href="<%= ctx %>/clubs" class="btn btn-link mb-3">&larr; 클럽 목록으로</a>

    <!-- 알림 영역 (추후 join/leave 결과 표시용) -->
    <%
        if (joinResult != null || leaveResult != null) {
    %>
    <div class="alert alert-info">
        <%-- 나중에 join/leave 서블릿 붙이면 여기에서 메시지 분기 --%>
        <%
            if ("JOINED".equals(joinResult)) {
        %>
            클럽에 가입되었습니다.
        <%
            } else if ("REJOIN".equals(joinResult)) {
        %>
            이전 기록을 기반으로 클럽에 재가입되었습니다.
        <%
            } else if ("ALREADY".equals(joinResult)) {
        %>
            이미 이 클럽의 ACTIVE 멤버입니다.
        <%
            }

            if ("LEFT".equals(leaveResult)) {
        %>
            클럽에서 탈퇴 처리되었습니다.
        <%
            } else if ("NO_ACTIVE".equals(leaveResult)) {
        %>
            현재 ACTIVE 상태의 멤버가 아니라 탈퇴할 수 없습니다.
        <%
            }
        %>
    </div>
    <%
        }
    %>

    <div class="card shadow-sm">
        <div class="card-body">
            <h2 class="card-title mb-2"><%= club.getName() %></h2>
            <p class="text-muted mb-2">
                <strong>유형:</strong> <%= club.getClubType() %> /
                <strong>카테고리:</strong> <%= club.getCategoryName() %>
            </p>
            <p class="text-muted mb-2">
                <strong>상태:</strong> <%= club.getStatus() %> /
                <strong>개설일:</strong>
                <%= club.getCreatedAt() != null ? club.getCreatedAt() : "N/A" %>
            </p>
            <p class="text-muted mb-3">
                <strong>멤버 수:</strong>
                <%= club.getActiveMembers() %>명 (현재 ACTIVE) /
                <%= club.getTotalMembers() %>명 (누적 가입)
            </p>

            <hr/>

            <h5 class="mb-2">클럽 소개</h5>
            <p class="mb-4" style="white-space: pre-line;">
                <%= club.getDescription() != null ? club.getDescription() : "등록된 소개가 없습니다." %>
            </p>

            <hr/>

            <h5 class="mb-2">내 가입 상태</h5>
            <%
                String myStatus = club.getMyStatus();
                if (myStatus == null) {
            %>
                <p class="text-muted mb-1">
                    이 클럽에 가입한 이력이 없습니다.
                </p>
            <%
                } else {
            %>
                <p class="mb-1">
                    <strong>상태:</strong> <%= myStatus %>
                    <% if (club.getMyRole() != null) { %>
                        (<%= club.getMyRole() %>)
                    <% } %>
                </p>
                <p class="text-muted mb-1">
                    <strong>가입일:</strong>
                    <%= club.getMyJoinedAt() != null ? club.getMyJoinedAt() : "-" %> /
                    <strong>탈퇴일:</strong>
                    <%= club.getMyLeftAt() != null ? club.getMyLeftAt() : "-" %>
                </p>
            <%
                }
            %>

            <%-- 
                여기 아래에 나중에 "가입 신청 / 탈퇴" 버튼을 붙이면 됨.
                예:
                - myStatus == null 또는 'LEFT' → 가입 신청 버튼
                - myStatus == 'ACTIVE' → 탈퇴 버튼
                지금은 상세 정보 조회만 구현.
            --%>
        </div>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
