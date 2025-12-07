package teamk2p.web;

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

import teamk2p.db.DBUtil;
import teamk2p.db.UserDao;
import teamk2p.db.UserDao.LoginUser;

@WebServlet("/me/password")
public class ChangePasswordServlet extends HttpServlet {

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

        RequestDispatcher rd =
                req.getRequestDispatcher("/views/change_password.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        LoginUser loginUser =
                (session == null) ? null : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String currentPw = req.getParameter("current_password");
        String newPw     = req.getParameter("new_password");
        String confirmPw = req.getParameter("confirm_password");

        // 1) 단순 검증
        if (currentPw == null) currentPw = "";
        if (newPw == null)     newPw = "";
        if (confirmPw == null) confirmPw = "";

        if (newPw.isBlank() || confirmPw.isBlank()) {
            req.setAttribute("error", "새 비밀번호와 확인 비밀번호를 모두 입력해주세요.");
            forward(req, resp);
            return;
        }

        if (!newPw.equals(confirmPw)) {
            req.setAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            forward(req, resp);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            UserDao dao = new UserDao();

            // 2) 현재 비밀번호 확인
            boolean ok = dao.checkPassword(conn, loginUser.getUserId(), currentPw);
            if (!ok) {
                conn.rollback();
                req.setAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
                forward(req, resp);
                return;
            }

            // 3) 비밀번호 변경
            dao.changePassword(conn, loginUser.getUserId(), newPw);
            conn.commit();

            req.setAttribute("success", "비밀번호가 변경되었습니다.");
            forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            forward(req, resp);
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher rd =
                req.getRequestDispatcher("/views/change_password.jsp");
        rd.forward(req, resp);
    }
}
