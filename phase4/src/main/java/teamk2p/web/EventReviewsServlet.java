package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.EventDetail;
import teamk2p.db.EventDao.EventReviewItem;
import teamk2p.db.UserDao.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/events/reviews")
public class EventReviewsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
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

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            EventDetail detail = dao.getEventDetail(conn, eventId);
            if (detail == null) {
                conn.rollback();
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "해당 이벤트를 찾을 수 없습니다.");
                return;
            }

            List<EventReviewItem> reviews =
                    dao.listEventReviews(conn, eventId);

            conn.commit();

            req.setAttribute("detail", detail);
            req.setAttribute("reviews", reviews);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/event_reviews.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
