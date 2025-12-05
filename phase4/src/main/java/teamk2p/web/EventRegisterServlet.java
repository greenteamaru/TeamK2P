package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.EventDetail;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/events/register")
public class EventRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String userIdParam  = req.getParameter("user_id");
        String eventIdParam = req.getParameter("event_id");

        if (userIdParam == null || eventIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "user_id와 event_id가 필요합니다.");
            return;
        }

        int userId;
        int eventId;
        try {
            userId  = Integer.parseInt(userIdParam);
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID 형식이 잘못되었습니다.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            EventDao dao = new EventDao();

            // 1) 신청 시도
            String result = dao.registerForEvent(conn, userId, eventId);

            // 2) 최신 상세 정보 다시 조회 (같은 트랜잭션 안에서)
            EventDetail detail = dao.getEventDetail(conn, eventId);

            conn.commit();

            req.setAttribute("regResult", result);
            req.setAttribute("detail",    detail);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/event_detail.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignore) {}
            }
        }
    }
}
