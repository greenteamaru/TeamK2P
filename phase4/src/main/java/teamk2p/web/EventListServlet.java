package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.EventListItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/events")
public class EventListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try (Connection conn = DBUtil.getConnection()) {

            EventDao dao = new EventDao();
            List<EventListItem> events = dao.listUpcomingEvents(conn);
            conn.commit(); // SELECT지만 깔끔하게 커밋

            req.setAttribute("events", events);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/events.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
