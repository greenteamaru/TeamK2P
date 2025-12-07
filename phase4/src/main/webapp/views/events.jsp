<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.EventListItem" %>

<%
    String ctx = request.getContextPath();
    List<EventListItem> events = (List<EventListItem>) request.getAttribute("events");
    String keyword = (String) request.getAttribute("keyword");
    Boolean showAllObj = (Boolean) request.getAttribute("showAll");
    boolean showAll = (showAllObj != null && showAllObj.booleanValue());
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>이벤트 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <!-- 제목 + 7일/전체 토글 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="mb-0">
            <%
                if (showAll) {
            %>
                전체 이벤트
            <%
                } else {
            %>
                향후 7일 이벤트
            <%
                }
            %>
        </h2>

        <div class="btn-group">
            <a href="<%= ctx %>/events"
               class="btn btn-sm <%= showAll ? "btn-outline-primary" : "btn-primary" %>">
                향후 7일
            </a>
            <a href="<%= ctx %>/events?all=true"
               class="btn btn-sm <%= showAll ? "btn-primary" : "btn-outline-primary" %>">
                전체 이벤트
            </a>
        </div>
    </div>

    <!-- 검색 폼 -->
    <form class="row g-2 align-items-center mb-4" method="get" action="<%= ctx %>/events">
        <div class="col-auto">
            <label class="col-form-label">검색</label>
        </div>
        <div class="col-auto">
            <input type="text"
                   name="q"
                   class="form-control form-control-sm"
                   placeholder="이벤트명 / 클럽 / 장소"
                   value="<%= (keyword != null ? keyword : "") %>">
        </div>

        <%-- showAll 유지용 hidden --%>
        <%
            if (showAll) {
        %>
        <input type="hidden" name="all" value="true">
        <%
            }
        %>

        <div class="col-auto">
            <button type="submit" class="btn btn-sm btn-primary">
                검색
            </button>
        </div>
        <div class="col-auto">
            <a href="<%= ctx %>/events" class="btn btn-sm btn-outline-secondary">
                초기화
            </a>
        </div>
    </form>

    <%-- 안내 문구 --%>
    <p class="text-muted small mb-3">
        <%
            if (showAll) {
        %>
            전체 기간의 이벤트를 표시합니다.
        <%
            } else {
        %>
            오늘 기준으로 향후 7일 동안 열리는 이벤트만 표시합니다.
        <%
            }
        %>
        <%
            if (keyword != null && !keyword.isBlank()) {
        %>
            / 검색어: "<%= keyword %>"
        <%
            }
        %>
    </p>

    <%-- 이벤트 카드 목록 --%>
    <%
        if (events == null || events.isEmpty()) {
    %>
        <p class="text-muted">표시할 이벤트가 없습니다.</p>
    <%
        } else {
    %>
        <div class="row g-3 mt-3">
            <%
                for (EventListItem e : events) {
            %>
            <div class="col-md-4">
                <!-- 카드 전체를 링크로 감싸기 -->
                <a href="<%= ctx %>/events/detail?event_id=<%= e.getEventId() %>"
                   class="text-decoration-none text-reset">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title mb-1"><%= e.getTitle() %></h5>
                            <p class="card-subtitle mb-1 text-muted small">
                                <%= e.getClubName() %> @ <%= e.getVenueName() %>
                            </p>
                            <p class="card-text small mb-1">
                                시작 시간: <%= e.getStartTime() %>
                            </p>
                            <p class="card-text small mb-0">
                                정원: <%= e.getCurrentRegistrations() %> / <%= e.getCapacity() %>
                            </p>
                        </div>
                    </div>
                </a>
            </div>
            <%
                } // for
            %>
        </div>
    <%
        } // else
    %>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
