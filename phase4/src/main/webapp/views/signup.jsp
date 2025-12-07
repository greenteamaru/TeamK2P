<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<%
    String ctx   = request.getContextPath();
    String email = request.getParameter("email");
    String name  = request.getParameter("name");
    String error = (String) request.getAttribute("error");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입 - Campus Club Event Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">

<%@ include file="common/header.jspf" %>

<!-- 로그인 화면처럼 가운데 카드 정렬 -->
<div class="container d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 80px);">
    <div class="col-md-5 col-lg-4">
        <div class="card shadow-sm border-0">
            <div class="card-body p-4">

                <h3 class="mb-3 text-center">회원가입</h3>

                <% if (error != null) { %>
                    <div class="alert alert-danger py-2 small">
                        <%= error %>
                    </div>
                <% } %>

                <form method="post" action="<%= ctx %>/signup">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">이메일</label>
                        <input type="email"
                               name="email"
                               class="form-control form-control-sm"
                               value="<%= email != null ? email : "" %>"
                               required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">비밀번호</label>
                        <input type="password"
                               name="password"
                               class="form-control form-control-sm"
                               required>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold">이름</label>
                        <input type="text"
                               name="name"
                               class="form-control form-control-sm"
                               value="<%= name != null ? name : "" %>"
                               required>
                    </div>

                    <button type="submit"
                            class="btn btn-primary w-100 mb-2">
                        회원가입
                    </button>

                    <div class="text-center small text-muted">
                        이미 계정이 있으신가요?
                        <a href="<%= ctx %>/login">로그인</a>
                    </div>
                </form>

            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
