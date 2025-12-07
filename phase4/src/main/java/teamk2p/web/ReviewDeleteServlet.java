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

@WebServlet("/events/review/delete")
public class ReviewDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String eventIdStr = req.getParameter("event_id");
        String from       = req.getParameter("from"); // "detail" or "myReviews"

        if (eventIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "event_id 파라미터가 필요합니다.");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(eventIdStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "event_id 형식이 올바르지 않습니다.");
            return;
        }

        String result;
        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            result = dao.deleteMyReview(conn, loginUser.getUserId(), eventId);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
            return;
        }

        if ("myReviews".equalsIgnoreCase(from)) {
            resp.sendRedirect(
                req.getContextPath()
                        + "/me/reviews?deleteResult=" + result
            );
        } else {
            resp.sendRedirect(
                req.getContextPath()
                        + "/events/detail?event_id=" + eventId
                        + "&reviewDelete=" + result
            );
        }
    }
}
