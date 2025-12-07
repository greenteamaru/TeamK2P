package teamk2p.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import teamk2p.db.DBUtil;
import teamk2p.db.ClubDao;
import teamk2p.db.ClubDao.MyClubItem;
import teamk2p.db.UserDao.LoginUser;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/me/clubs")
public class MyClubsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        LoginUser loginUser =
            (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            ClubDao dao = new ClubDao();
            List<MyClubItem> list = dao.listMyClubs(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("myClubs", list);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/my_clubs.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
