<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();  // /TeamK2P_phase4 같은 컨텍스트 경로
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Campus Club Event Portal - Home</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">
<%@ include file="views/common/header.jspf" %>

<div class="container py-5">

    <!-- 히어로 영역 -->
    <div class="row align-items-center mb-5">
        <div class="col-md-7">
            <h1 class="display-5 fw-bold mb-3">
                Campus Club Event Portal
            </h1>
            <p class="lead text-muted mb-4">
                교내 동아리 이벤트를 한 곳에서 조회하고, 상세 정보를 확인하고,
                웹에서 바로 참가 신청까지 할 수 있는 서비스입니다.
                (Phase 2/3에서 설계·구현한 Oracle DB를 백엔드로 활용)
            </p>

            <!-- 메인 액션 버튼 -->
            <div class="d-flex flex-wrap gap-2">
                <a href="<%= ctx %>/clubs" class="btn btn-primary btn-lg">
                    클럽 찾기
                </a>
                <a href="<%= ctx %>/events" class="btn btn-primary btn-lg">
				    향후 7일 이벤트 보러가기
				</a>
				<a href="<%= ctx %>/events?all=true" class="btn btn-outline-secondary btn-lg">
				    전체 이벤트 보기
				</a>
            </div>
        </div>

        <div class="col-md-5 mt-4 mt-md-0">
            <div class="card shadow-sm border-0">
                <div class="card-body">
                    <h5 class="card-title mb-3">Phase 4 구현 개요</h5>
                    <ul class="list-unstyled small mb-0">
                        <li class="mb-1">✔ Phase 3 JDBC Application의 핵심 기능을 웹으로 제공</li>
                        <li class="mb-1">✔ Oracle 기반 동아리·이벤트 DB 연동</li>
                        <li class="mb-1">✔ 이벤트 조회 / 상세 조회 / 참가 신청</li>
                        <li class="mb-1">✔ 클럽 목록 / 내 클럽 / 클럽 가입 기능</li>
                        <li class="mb-1">✔ GitHub Repository & README / Task 문서 연계</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <!-- 기능 카드 3개 -->
    <div class="row g-4">
        <!-- 카드 1: 이벤트 -->
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">이벤트 둘러보기</h5>
                    <p class="card-text small text-muted">
                        향후 7일 내에 열리는 동아리 이벤트를 카드 형태로 확인하고,
                        클럽·장소·시간 정보를 한 눈에 볼 수 있습니다.
                    </p>
                    <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-primary">
                        이벤트 목록 이동
                    </a>
                </div>
            </div>
        </div>

        <!-- 카드 2: 상세 + 신청 -->
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">상세 정보 & 신청</h5>
                    <p class="card-text small text-muted">
                        각 이벤트 페이지에서 정원, 남은 자리, 참가비, 상세 설명을 확인하고
                        로그인한 사용자 계정으로 온라인 참가 신청을 진행할 수 있습니다.
                    </p>
                    <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-secondary">
                        아무 이벤트 선택 후 테스트
                    </a>
                </div>
            </div>
        </div>

        <!-- 카드 3: 클럽 찾기 -->
        <div class="col-md-4">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">클럽 찾기 & 가입</h5>
                    <p class="card-text small text-muted">
                        교내에 개설된 클럽 목록을 조회하고,
                        아직 가입하지 않은 클럽은 &quot;가입하기&quot; 버튼으로
                        바로 memberships 테이블에 등록되도록 구현했습니다.
                    </p>
                    <a href="<%= ctx %>/clubs" class="btn btn-sm btn-outline-success">
                        클럽 목록 이동
                    </a>
                </div>
            </div>
        </div>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
