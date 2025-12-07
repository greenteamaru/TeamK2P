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
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/my/events")
public class MyEventsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ctx = req.getContextPath();

        // 1) 로그인 사용자 확인
        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(ctx + "/login");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            EventDao dao = new EventDao();

            List<MyEventItem> list = dao.listMyEvents(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("events", list);

            // 취소 결과 코드가 있으면 JSP로 넘김
            String cancelResult = req.getParameter("cancelResult");
            if (cancelResult != null && !cancelResult.isEmpty()) {
                req.setAttribute("cancelResult", cancelResult);
            }

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/my_events.jsp");
            rd.forward(req, resp);

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
