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

@WebServlet("/events/detail")
public class EventDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "event id is required");
            return;
        }

        int eventId;
        try {
            eventId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid event id");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            EventDetail detail = dao.getEventDetail(conn, eventId);
            conn.commit();

            if (detail == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "event not found");
                return;
            }

            req.setAttribute("detail", detail);
            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/event_detail.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
