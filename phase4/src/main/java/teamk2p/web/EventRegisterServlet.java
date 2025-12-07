package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.EventDetail;
import teamk2p.db.UserDao.LoginUser;      // ✅ 로그인 유저 DTO

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;  // ✅ 세션
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/events/register")
public class EventRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // ✅ 1) 로그인 여부 확인 + userId 얻기
        HttpSession session = req.getSession(false);
        LoginUser loginUser =
                (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int userId = loginUser.getUserId();   // ← 여기서 user_id 확보

        // ✅ 2) event_id 는 폼에서 넘어온 값 사용
        String eventIdParam = req.getParameter("event_id");
        if (eventIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event_id가 필요합니다.");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID 형식이 잘못되었습니다.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            EventDao dao = new EventDao();

            // 3) 신청 시도 (userId 는 세션에서 온 값)
            String result = dao.registerForEvent(conn, userId, eventId);

            // 4) 최신 상세 정보 다시 조회
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
