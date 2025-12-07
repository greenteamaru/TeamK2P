package teamk2p.web;

import teamk2p.db.DBUtil;
import teamk2p.db.ClubDao;
import teamk2p.db.ClubDao.MyClubItem;
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

@WebServlet("/clubs/leave")
public class ClubLeaveServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // 1) 로그인 체크
        HttpSession session = req.getSession(false);
        LoginUser loginUser = (session == null)
                ? null
                : (LoginUser) session.getAttribute("loginUser");

        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String clubIdParam = req.getParameter("club_id");
        if (clubIdParam == null || clubIdParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "club_id 파라미터가 필요합니다.");
            return;
        }

        int clubId;
        try {
            clubId = Integer.parseInt(clubIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "club_id 형식이 잘못되었습니다.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            ClubDao dao = new ClubDao();

            // 2) 탈퇴 처리
            String result = dao.leaveClub(conn, loginUser.getUserId(), clubId);

            // 3) 최신 내 클럽 목록 다시 조회
            List<MyClubItem> clubs = dao.listMyClubs(conn, loginUser.getUserId());
            conn.commit();

            req.setAttribute("clubs", clubs);
            req.setAttribute("leaveResult", result);

            req.getRequestDispatcher("/views/my_clubs.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
