package id.dekost.servlet;

import id.dekost.dao.DashboardDao;
import id.dekost.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/dashboard.jsp";

    private final DashboardDao dashboardDao = new DashboardDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roomOccupancy", dashboardDao.getRoomOccupancy());
            request.setAttribute("floorOccupancy", dashboardDao.getOccupancyByFloor());
            request.setAttribute("monthlyRevenue", dashboardDao.getMonthlyRevenue());
            request.setAttribute("overdueBilling", dashboardDao.getOverdueBillingSummary());
            request.setAttribute("openComplaints", dashboardDao.getOpenComplaintSummary());
            request.setAttribute("duePayments", dashboardDao.findUpcomingPayments());
            request.setAttribute("recentComplaints", dashboardDao.findRecentComplaints());
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat data dashboard.", e);
        }

        request.setAttribute("pageTitle", "Dashboard");
        request.setAttribute("activeNav", "dashboard");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
