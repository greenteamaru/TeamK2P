package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.EventListItem;
import teamk2p.db.UserDao.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/events")
public class EventListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

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

        // 2) 파라미터 처리
        String allParam = req.getParameter("all");
        boolean showAll = "true".equalsIgnoreCase(allParam);

        String keyword = req.getParameter("q");
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            List<EventListItem> events;

            if (keyword == null && !showAll) {
                // 검색어 없고, 전체도 아니면: 기본 "향후 7일"
                events = dao.listUpcomingEvents(conn);
            } else {
                // 그 외에는 통합 검색 메서드 사용
                events = dao.searchEvents(conn, keyword, showAll);
            }

            conn.commit();

            req.setAttribute("events", events);
            req.setAttribute("keyword", keyword);
            req.setAttribute("showAll", showAll);

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
