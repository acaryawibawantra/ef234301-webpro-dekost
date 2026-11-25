package id.dekost.servlet;

import id.dekost.dao.DashboardDao;
import id.dekost.model.Keluhan;
import id.dekost.model.Pembayaran;
import id.dekost.util.Flash;
import id.dekost.util.RupiahFormatter;
import id.dekost.util.TanggalFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = "/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/dashboard.jsp";

    private final DashboardDao dashboardDao = new DashboardDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Long> roomOccupancy = dashboardDao.getRoomOccupancy();
            List<DashboardDao.FloorOccupancy> floorOccupancy = dashboardDao.getOccupancyByFloor();
            BigDecimal monthlyRevenue = dashboardDao.getMonthlyRevenue();
            DashboardDao.BillingSummary overdueBilling = dashboardDao.getOverdueBillingSummary();
            DashboardDao.OpenComplaintSummary openComplaints = dashboardDao.getOpenComplaintSummary();
            List<Pembayaran> duePayments = dashboardDao.findUpcomingPayments();
            List<Keluhan> recentComplaints = dashboardDao.findRecentComplaints();

            long availableRooms = roomOccupancy.getOrDefault("TERSEDIA", 0L);
            long occupiedRooms = roomOccupancy.getOrDefault("TERISI", 0L);
            long maintenanceRooms = roomOccupancy.getOrDefault("PERBAIKAN", 0L);
            long totalRooms = availableRooms + occupiedRooms + maintenanceRooms;

            Map<Integer, String> paymentAmounts = new LinkedHashMap<>();
            Map<Integer, String> paymentDueDates = new LinkedHashMap<>();
            for (Pembayaran payment : duePayments) {
                if (payment.getId() == null) {
                    continue;
                }
                paymentAmounts.put(payment.getId(), formatRupiah(payment.getJumlah()));
                paymentDueDates.put(payment.getId(), formatDate(payment.getJatuhTempo()));
            }

            Map<Integer, String> complaintDates = new LinkedHashMap<>();
            for (Keluhan complaint : recentComplaints) {
                if (complaint.getId() == null) {
                    continue;
                }
                LocalDate createdDate = complaint.getCreatedAt() == null
                        ? null
                        : complaint.getCreatedAt().toLocalDate();
                complaintDates.put(complaint.getId(), formatDate(createdDate));
            }

            request.setAttribute("roomOccupancy", roomOccupancy);
            request.setAttribute("floorOccupancy", floorOccupancy);
            request.setAttribute("monthlyRevenue", monthlyRevenue);
            request.setAttribute("overdueBilling", overdueBilling);
            request.setAttribute("openComplaints", openComplaints);
            request.setAttribute("duePayments", duePayments);
            request.setAttribute("recentComplaints", recentComplaints);

            request.setAttribute("totalRooms", totalRooms);
            request.setAttribute("occupiedRooms", occupiedRooms);
            request.setAttribute("availableRooms", availableRooms);
            request.setAttribute("maintenanceRooms", maintenanceRooms);
            request.setAttribute("monthlyRevenueFormatted", formatRupiah(monthlyRevenue));
            request.setAttribute("overdueTotalFormatted",
                    formatRupiah(overdueBilling == null ? null : overdueBilling.totalJumlah()));
            request.setAttribute("paymentAmounts", paymentAmounts);
            request.setAttribute("paymentDueDates", paymentDueDates);
            request.setAttribute("complaintDates", complaintDates);

            request.setAttribute("pageTitle", "Dashboard");
            request.setAttribute("pageSubtitle", totalRooms + " kamar \u00b7 " + availableRooms + " tersedia");
            request.setAttribute("pageActionLabel", "Kelola kamar");
            request.setAttribute("pageActionHref", request.getContextPath() + "/admin/kamar");
            request.setAttribute("activeNav", "dashboard");
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat data dashboard.", e);
        }

        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String formatRupiah(BigDecimal amount) {
        return amount == null ? RupiahFormatter.format(BigDecimal.ZERO) : RupiahFormatter.format(amount);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : TanggalFormatter.format(date);
    }
}
