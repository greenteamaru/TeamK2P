package teamk2p.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import teamk2p.db.DBUtil;
import teamk2p.db.ClubDao;
import teamk2p.db.ClubDao.ClubSummary;
import teamk2p.db.ClubDao.MyClubItem;
import teamk2p.db.UserDao.LoginUser;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/clubs/join")
public class ClubJoinServlet extends HttpServlet {

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

        String clubIdParam = req.getParameter("club_id");
        if (clubIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "club_id가 필요합니다.");
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

            // 가입 시도
            String joinResult = dao.joinClub(conn, loginUser.getUserId(), clubId);

            // 최신 목록 다시 조회해서 화면 갱신
            List<ClubSummary> clubs = dao.listAllClubs(conn);
            List<MyClubItem> myClubs = dao.listMyClubs(conn, loginUser.getUserId());
            Map<Integer, MyClubItem> myClubMap = new HashMap<>();
            for (MyClubItem m : myClubs) {
                myClubMap.put(m.getClubId(), m);
            }

            conn.commit();

            req.setAttribute("joinResult", joinResult);
            req.setAttribute("clubs", clubs);
            req.setAttribute("myClubMap", myClubMap);

            RequestDispatcher rd =
                    req.getRequestDispatcher("/views/clubs.jsp");
            rd.forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
