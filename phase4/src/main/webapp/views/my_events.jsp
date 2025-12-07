<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.MyEventItem" %>

<%
    String ctx = request.getContextPath();
    List<MyEventItem> myEvents =
        (List<MyEventItem>) request.getAttribute("myEvents");
    String cancelResult = (String) request.getAttribute("cancelResult");

    java.util.Date now = new java.util.Date();
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>내 이벤트</title>
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
            취소할 수 있는 신청 상태가 아닙니다.
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
        if (myEvents == null || myEvents.isEmpty()) {
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
                <th>시작 시간</th>
                <th>장소</th>
                <th>신청 상태</th>
                <th>참석 여부</th>
                <th>신청 취소</th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MyEventItem e : myEvents) {

                    boolean isPast = false;
                    if (e.getStartTime() != null) {
                        isPast = e.getStartTime().before(now);
                    }

                    String s = (e.getStatus() == null) ? "" : e.getStatus().toUpperCase();
                    boolean cancellable =
                        !isPast &&
                        ("PENDING".equals(s) ||
                         "CONFIRMED".equals(s) ||
                         "WAITLISTED".equals(s));
            %>
            <tr>
                <td>
                    <a href="<%= ctx %>/events/detail?event_id=<%= e.getEventId() %>">
                        <%= e.getTitle() %>
                    </a>
                </td>
                <td><%= e.getStartTime() %></td>
                <td><%= e.getVenueName() %></td>
                <td><%= e.getStatus() %></td>
                <td>
                    <%
                        if ("Y".equalsIgnoreCase(e.getAttended())) {
                    %>
                        참석
                    <%
                        } else {
                    %>
                        -
                    <%
                        }
                    %>
                </td>
                <td>
                    <form action="<%= ctx %>/events/cancel" method="post" class="d-inline">
                        <input type="hidden" name="event_id" value="<%= e.getEventId() %>">
                        <button type="submit"
                                class="btn btn-sm btn-outline-danger"
                                <%= cancellable ? "" : "disabled" %>>
                            취소
                        </button>
                    </form>
                    <%
                        if (isPast) {
                    %>
                        <span class="small text-muted ms-1">종료됨</span>
                    <%
                        } else if (!cancellable) {
                    %>
                        <span class="small text-muted ms-1">취소 완료</span>
                    <%
                        }
                    %>
                </td>
            </tr>
            <%
                } // for
            %>
            </tbody>
        </table>
    </div>
    <%
        } // else
    %>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
