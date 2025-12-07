<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.MyEventItem" %>

<%
    String ctx = request.getContextPath();
    List<MyEventItem> events =
        (List<MyEventItem>) request.getAttribute("events");
    String cancelResult =
        (String) request.getAttribute("cancelResult");
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>내 이벤트 - Campus Club Event Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <h2 class="mb-3">내 이벤트</h2>

    <%-- 취소 결과 메시지 --%>
    <%
        if (cancelResult != null) {
    %>
    <div class="alert alert-info">
        <%
            if ("CANCELLED".equals(cancelResult)) {
        %>
            신청이 정상적으로 취소되었습니다.
        <%
            } else if ("NO_ACTIVE".equals(cancelResult)) {
        %>
            취소할 수 있는 활성 신청이 없습니다.
        <%
            } else {
        %>
            신청 취소 처리 중 오류가 발생했습니다. (코드: <%= cancelResult %>)
        <%
            }
        %>
    </div>
    <%
        }
    %>

    <%
        if (events == null || events.isEmpty()) {
    %>
        <p class="text-muted">신청한 이벤트가 없습니다.</p>
    <%
        } else {
    %>
    <div class="table-responsive">
        <table class="table table-sm align-middle">
            <thead class="table-light">
            <tr>
                <th>제목</th>
                <th>일시</th>
                <th>장소</th>
                <th>신청 상태</th>
                <th>참석 여부</th>
                <th style="width: 160px;">액션</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MyEventItem e : events) {
            %>
            <tr>
                <td><%= e.getTitle() %></td>
                <td><%= e.getStartTime() %></td>
                <td><%= e.getVenueName() %></td>
                <td><%= e.getStatus() %></td>
                <td><%= e.getAttended() %></td>
                <td>
                    <div class="d-flex gap-2">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/events/detail?event_id=<%= e.getEventId() %>">
                            상세보기
                        </a>

                        <form method="post"
                              action="<%= ctx %>/events/cancel"
                              onsubmit="return confirm('이 신청을 취소하시겠습니까?');">
                            <input type="hidden" name="event_id"
                                   value="<%= e.getEventId() %>">
                            <button type="submit"
                                    class="btn btn-sm btn-outline-danger">
                                취소
                            </button>
                        </form>
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
