package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String name     = req.getParameter("name");

        // 간단한 유효성 검사
        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || name == null || name.isBlank()) {

            req.setAttribute("error", "이메일, 비밀번호, 이름은 필수 입력 항목입니다.");
            req.getRequestDispatcher("/views/signup.jsp").forward(req, resp);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDao userDao = new UserDao();

            try {
                userDao.createUser(conn,
                        email.trim(),
                        password.trim(),
                        name.trim());

                conn.commit();

                // 회원가입 성공 → 로그인 페이지로
                resp.sendRedirect(req.getContextPath() + "/login?signup=ok");

            } catch (SQLException e) {
                conn.rollback();

                String msg;
                if (e.getErrorCode() == 1) {  // ORA-00001
                    msg = "이미 사용 중인 이메일입니다.";
                } else {
                    msg = "회원가입 처리 중 오류가 발생했습니다: " + e.getMessage();
                }

                req.setAttribute("error", msg);
                req.getRequestDispatcher("/views/signup.jsp").forward(req, resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
