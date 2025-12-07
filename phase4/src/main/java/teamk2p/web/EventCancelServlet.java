package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.UserDao.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/events/cancel")
public class EventCancelServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();

        // 1) 로그인 체크
        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        // 2) event_id 파라미터 파싱
        String eventIdParam = req.getParameter("event_id");
        if (eventIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event_id가 필요합니다.");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event_id 형식이 잘못되었습니다.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            EventDao dao = new EventDao();

            String result = dao.cancelRegistration(conn, loginUser.getUserId(), eventId);
            conn.commit();

            // 내 신청 목록 페이지로 리다이렉트 + 결과 코드 전달
            resp.sendRedirect(ctx + "/my/events?cancelResult=" + result);

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}

            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (Exception ignore) {}
        }
    }
}
