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

        // 2) event_id 파라미터
        String eventIdParam = req.getParameter("event_id");
        if (eventIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event_id 파라미터가 필요합니다.");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event_id 형식이 올바르지 않습니다.");
            return;
        }

        String resultCode = "ERROR";

        try (Connection conn = DBUtil.getConnection()) {

            EventDao dao = new EventDao();
            resultCode = dao.cancelRegistration(conn, loginUser.getUserId(), eventId);

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            // resultCode = "ERROR" 유지
        }

        // 결과 코드와 함께 내 이벤트 페이지로 리다이렉트
        resp.sendRedirect(ctx + "/me/events?cancelResult=" + resultCode);
    }
}
