package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.MyReviewItem;
import teamk2p.db.UserDao.LoginUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/me/reviews")
public class MyReviewsServlet extends HttpServlet {

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

        String deleteResult = req.getParameter("deleteResult");

        try (Connection conn = DBUtil.getConnection()) {
            EventDao dao = new EventDao();
            List<MyReviewItem> reviews =
                    dao.listMyReviews(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("reviews", reviews);
            req.setAttribute("deleteResult", deleteResult);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/my_reviews.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
