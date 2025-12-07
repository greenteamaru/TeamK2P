<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="teamk2p.db.UserDao.LoginUser" %>

<%
    String error = (String) request.getAttribute("error");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>회원 탈퇴 - Campus Club Event Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">
    <div class="row justify-content-center">
        <div class="col-md-6">

            <div class="card shadow-sm">
                <div class="card-body">
                    <h3 class="card-title mb-3">회원 탈퇴</h3>

                    <p class="text-muted small mb-3">
                        계정 (<strong><%= loginUser != null ? loginUser.getEmail() : "" %></strong>) 을(를) 영구적으로 삭제합니다.<br>
                        관련된 이벤트 신청 / 리뷰 / 멤버십 정보 등이 함께 삭제되거나
                        더 이상 복구되지 않을 수 있습니다.
                    </p>

                    <% if (error != null) { %>
                        <div class="alert alert-danger py-2">
                            <%= error %>
                        </div>
                    <% } %>

                    <form method="post" action="<%= request.getContextPath() %>/me/delete">
                        <div class="mb-3">
                            <label class="form-label">비밀번호 확인</label>
                            <input type="password" name="password"
                                   class="form-control" required>
                            <div class="form-text">
                                본인 확인을 위해 현재 비밀번호를 한 번 더 입력해 주세요.
                            </div>
                        </div>

                        <div class="d-flex justify-content-between">
                            <a href="<%= request.getContextPath() %>/me/profile"
                               class="btn btn-outline-secondary">
                                취소
                            </a>
                            <button type="submit" class="btn btn-danger">
                                정말 탈퇴하겠습니다
                            </button>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
