package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.MyEventItem;
import teamk2p.db.UserDao.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/me/events")
public class MyEventsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) 로그인 확인
        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            // 로그인 안 되어 있으면 로그인 페이지로
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            List<MyEventItem> list =
                    dao.listMyEvents(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("events", list);

            // 취소 결과가 있으면 (cancel 서블릿에서 넘겨줄 예정)
            String cancelResult = (String) req.getAttribute("cancelResult");
            if (cancelResult != null) {
                req.setAttribute("cancelResult", cancelResult);
            }

            req.getRequestDispatcher("/views/my_events.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
