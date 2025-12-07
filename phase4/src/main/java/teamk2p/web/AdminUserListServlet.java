package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;
import teamk2p.db.UserDao.AdminUserItem;

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

@WebServlet("/admin/users")
public class AdminUserListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // loginUser 타입 이름은 너 프로젝트에 맞춰 수정.
        teamk2p.db.UserDao.LoginUser loginUser =
                (teamk2p.db.UserDao.LoginUser) session.getAttribute("loginUser");

        // 관리자만 접근
        if (loginUser == null || !loginUser.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 전용 페이지입니다.");
            return;
        }

        String emailKeyword = req.getParameter("emailKeyword");
        if (emailKeyword != null) {
            emailKeyword = emailKeyword.trim();
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDao dao = new UserDao();
            List<AdminUserItem> users;

            if (emailKeyword == null || emailKeyword.isEmpty()) {
                users = dao.listAllUsers(conn);          // Q7
            } else {
                users = dao.searchUsersByEmail(conn, emailKeyword); // Q8
            }
            conn.commit();

            req.setAttribute("users", users);
            req.setAttribute("emailKeyword", emailKeyword);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/admin_users.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
