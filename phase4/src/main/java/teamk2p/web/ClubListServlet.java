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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/clubs")
public class ClubListServlet extends HttpServlet {

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

	    String sort = req.getParameter("sort");  // name / members_desc / members_asc

	    try (Connection conn = DBUtil.getConnection()) {
	        ClubDao dao = new ClubDao();

	        List<ClubSummary> clubs = dao.listActiveClubs(conn, sort);
	        List<MyClubItem> myClubs = dao.listMyClubs(conn, loginUser.getUserId());

	        Map<Integer, MyClubItem> myClubMap = new HashMap<>();
	        for (MyClubItem m : myClubs) {
	            myClubMap.put(m.getClubId(), m);
	        }

	        conn.commit();

	        req.setAttribute("clubs", clubs);
	        req.setAttribute("myClubMap", myClubMap);
	        req.setAttribute("sort", sort == null ? "name" : sort);

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
