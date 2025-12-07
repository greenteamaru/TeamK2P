<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("error");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Team K²P 로그인</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<div class="container d-flex justify-content-center align-items-center" style="min-height: 100vh;">
    <div class="card shadow-sm" style="min-width: 380px;">
        <div class="card-body">
            <h3 class="card-title mb-3 text-center">Team K²P 이벤트 사이트</h3>
            <p class="text-muted small text-center mb-4">
                Phase 3와 동일하게 <strong>이메일 + 비밀번호</strong>로 로그인합니다.
            </p>

            <% if (error != null) { %>
            <div class="alert alert-danger py-2">
                <%= error %>
            </div>
            <% } %>

            <form method="post" action="<%= request.getContextPath() %>/login">
                <div class="mb-3">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email"
                           id="email"
                           name="email"
                           class="form-control"
                           placeholder="user@example.com">
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label">비밀번호</label>
                    <input type="password"
                           id="password"
                           name="password"
                           class="form-control"
                           placeholder="Phase3에서 사용한 기본 비밀번호">
                </div>

                <button type="submit" class="btn btn-primary w-100">
                    로그인
                </button>
            </form>
        </div>
    </div>
</div>
</body>
</html>
