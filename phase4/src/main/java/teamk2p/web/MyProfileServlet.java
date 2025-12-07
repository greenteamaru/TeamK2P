package teamk2p.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;
import teamk2p.db.UserDao.LoginUser;
import teamk2p.db.UserDao.UserProfile;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/me/profile")
public class MyProfileServlet extends HttpServlet {

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
            UserDao dao = new UserDao();
            UserProfile profile = dao.getProfile(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("profile", profile);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/my_profile.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        LoginUser loginUser =
            (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String name        = req.getParameter("name");
        String nickname    = req.getParameter("nickname");
        String nationality = req.getParameter("nationality");
        String language    = req.getParameter("language");

        try (Connection conn = DBUtil.getConnection()) {
            UserDao dao = new UserDao();
            UserProfile p = dao.getProfile(conn, loginUser.getUserId());

            if (p != null) {
                p.setName(name);
                p.setNickname(nickname);
                p.setNationality(nationality);
                p.setLanguage(language);

                dao.updateProfile(conn, p);
                conn.commit();
            }

            // 수정 후 다시 GET으로 리다이렉트 (PRG 패턴)
            resp.sendRedirect(req.getContextPath() + "/me/profile?saved=1");

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
