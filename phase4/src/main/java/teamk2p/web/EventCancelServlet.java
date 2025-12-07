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

        // 1) 로그인 확인
        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String eventIdParam = req.getParameter("event_id");
        if (eventIdParam == null || eventIdParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "event_id 파라미터가 필요합니다.");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "event_id 형식이 잘못되었습니다.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();

            // 2) 취소 수행
            String result =
                    dao.cancelRegistration(conn, loginUser.getUserId(), eventId);

            // 3) 내 이벤트 목록 다시 조회
            var list = dao.listMyEvents(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("events", list);
            req.setAttribute("cancelResult", result);

            // 같은 화면(my_events.jsp)으로 forward
            req.getRequestDispatcher("/views/my_events.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
