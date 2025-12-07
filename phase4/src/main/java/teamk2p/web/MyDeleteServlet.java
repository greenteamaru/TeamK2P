package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;
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
import java.sql.SQLException;

@WebServlet("/me/delete")
public class MyDeleteServlet extends HttpServlet {

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

        // 에러 메시지(비밀번호 틀림 등) 전달용
        req.setAttribute("error", req.getParameter("error"));

        RequestDispatcher rd =
                req.getRequestDispatcher("/views/my_delete.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String password = req.getParameter("password");
        if (password == null || password.isBlank()) {
            // 비밀번호 안 넣었으면 다시 폼으로
            req.setAttribute("error", "비밀번호를 입력해 주세요.");
            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/me_delete.jsp");
            rd.forward(req, resp);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDao userDao = new UserDao();

            // 비밀번호 확인: 기존 login() 재사용
            UserDao.LoginUser checked =
                    userDao.login(conn, loginUser.getEmail(), password);

            if (checked == null || checked.getUserId() != loginUser.getUserId()) {
                req.setAttribute("error", "비밀번호가 일치하지 않습니다.");
                RequestDispatcher rd =
                        req.getRequestDispatcher("/views/me_delete.jsp");
                rd.forward(req, resp);
                return;
            }

            int deleted = userDao.deleteUser(conn, loginUser.getUserId());
            conn.commit();

            // 세션 제거
            session.invalidate();

            // 간단히 홈으로 리다이렉트 + 메시지
            resp.sendRedirect(req.getContextPath() + "/?deleted=1");

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
