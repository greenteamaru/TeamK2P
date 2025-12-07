package teamk2p.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;
import teamk2p.db.UserDao.LoginUser;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 이미 로그인 상태면 /events로 보내버리자
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/events");
            return;
        }

        RequestDispatcher rd =
                req.getRequestDispatcher("/views/login.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            req.setAttribute("error", "이메일과 비밀번호를 모두 입력하세요.");
            doGet(req, resp);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDao dao = new UserDao();
            LoginUser user = dao.login(conn, email.trim(), password);

            conn.commit(); // SELECT지만 Phase3 스타일대로 commit

            if (user == null) {
                req.setAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
                doGet(req, resp);
                return;
            }

            // 로그인 성공 → 세션에 로그인 정보 저장
            HttpSession session = req.getSession(true);
            session.setAttribute("loginUser", user);

            // 로그인 후 기본 페이지
            resp.sendRedirect(req.getContextPath() + "/events");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "로그인 처리 중 오류: " + e.getMessage());
            doGet(req, resp);
        }
    }
}
