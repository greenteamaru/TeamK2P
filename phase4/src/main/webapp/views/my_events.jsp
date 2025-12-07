<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="teamk2p.db.EventDao.MyEventItem" %>

<%
    String ctx = request.getContextPath();
    List<MyEventItem> events = (List<MyEventItem>) request.getAttribute("events");
    String cancelResult = (String) request.getAttribute("cancelResult");
%>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 이벤트 신청 내역</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>
<body class="bg-light">
<%@ include file="common/header.jspf" %>

<div class="container py-4">

    <h2 class="mb-3">내 이벤트 신청 내역</h2>

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
            취소할 수 있는 신청 기록이 없습니다.
        <%
            } else {
        %>
            신청 취소 처리 중 오류가 발생했습니다.
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
                <th>이벤트</th>
                <th>일시</th>
                <th>장소</th>
                <th>상태</th>
                <th>참석 여부</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <%
                for (MyEventItem e : events) {

                    boolean cancellable =
                        ("PENDING".equalsIgnoreCase(e.getStatus()) ||
                         ("CONFIRMED".equalsIgnoreCase(e.getStatus())) ||
                         ("WAITLISTED".equalsIgnoreCase(e.getStatus())))
                        && "N".equalsIgnoreCase(e.getAttended());
            %>
                <tr>
                    <td>
                        <a href="<%= ctx %>/events/detail?event_id=<%= e.getEventId() %>">
                            <%= e.getTitle() %>
                        </a>
                    </td>
                    <td>
                        <%= e.getStartTime() %>
                    </td>
                    <td><%= e.getVenueName() %></td>
                    <td><%= e.getStatus() %></td>
                    <td><%= e.getAttended() %></td>
                    <td>
                        <% if (cancellable) { %>
                        <form action="<%= ctx %>/events/cancel"
                              method="post"
                              class="d-inline">
                            <input type="hidden" name="event_id"
                                   value="<%= e.getEventId() %>">
                            <button type="submit"
                                    class="btn btn-sm btn-outline-danger">
                                신청 취소
                            </button>
                        </form>
                        <% } %>
                    </td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>

    <%
        } // end if events
    %>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
