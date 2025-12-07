package teamk2p.web;

import teamk2p.db.ClubDao;
import teamk2p.db.ClubDao.ClubDetail;
import teamk2p.db.DBUtil;
import teamk2p.db.UserDao.LoginUser;
import teamk2p.db.EventDao;
import teamk2p.db.EventDao.ClubEventItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/clubs/detail")
public class ClubDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) 로그인 체크
        HttpSession session = req.getSession(false);
        LoginUser loginUser =
                (session == null) ? null : (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) club_id 파라미터 파싱
        String clubIdParam = req.getParameter("club_id");
        if (clubIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "club_id 파라미터가 필요합니다.");
            return;
        }

        int clubId;
        try {
            clubId = Integer.parseInt(clubIdParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "club_id 형식이 올바르지 않습니다.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {

            ClubDao dao = new ClubDao();
            ClubDetail detail = dao.getClubDetail(conn, clubId, loginUser.getUserId());

            if (detail == null) {
                conn.commit();
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "해당 클럽을 찾을 수 없습니다.");
                return;
            }

            // 클럽의 이벤트 목록 조회
            EventDao eventDao = new EventDao();
            List<ClubEventItem> upcomingEvents =
                    eventDao.listClubUpcomingEvents(conn, clubId);
            List<ClubEventItem> pastEvents =
                    eventDao.listClubPastEvents(conn, clubId);

            conn.commit();

            // JSP에서 사용할 속성들
            req.setAttribute("detail", detail);
            req.setAttribute("upcomingEvents", upcomingEvents);
            req.setAttribute("pastEvents", pastEvents);

            // join/leave 결과 표시용 (있으면)
            req.setAttribute("joinResult", req.getParameter("joinResult"));
            req.setAttribute("leaveResult", req.getParameter("leaveResult"));

            req.getRequestDispatcher("/views/club_detail.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Error: " + e.getMessage());
        }
    }
}
