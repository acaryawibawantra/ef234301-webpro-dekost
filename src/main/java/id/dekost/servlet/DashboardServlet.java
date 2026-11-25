package id.dekost.servlet;

import id.dekost.dao.DashboardDao;
import id.dekost.model.Keluhan;
import id.dekost.model.Pembayaran;
import id.dekost.model.User;
import id.dekost.util.Flash;
import id.dekost.util.RupiahFormatter;
import id.dekost.util.TanggalFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "DashboardServlet", urlPatterns = "/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/dashboard.jsp";
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter TODAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", ID_LOCALE);
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("MMMM", ID_LOCALE);
    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000L);

    private final DashboardDao dashboardDao = new DashboardDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Long> roomOccupancy = dashboardDao.getRoomOccupancy();
            List<DashboardDao.FloorOccupancy> floorOccupancy = dashboardDao.getOccupancyByFloor();
            BigDecimal monthlyRevenue = dashboardDao.getMonthlyRevenue();
            DashboardDao.CurrentMonthBillingSummary currentMonthBilling =
                    dashboardDao.getCurrentMonthBillingSummary();
            DashboardDao.BillingSummary overdueBilling = dashboardDao.getOverdueBillingSummary();
            DashboardDao.OpenComplaintSummary openComplaints = dashboardDao.getOpenComplaintSummary();
            List<Pembayaran> duePayments = dashboardDao.findUpcomingPayments();
            List<Keluhan> recentComplaints = dashboardDao.findRecentComplaints();

            long availableRooms = valueOrZero(roomOccupancy.get("TERSEDIA"));
            long occupiedRooms = valueOrZero(roomOccupancy.get("TERISI"));
            long maintenanceRooms = valueOrZero(roomOccupancy.get("PERBAIKAN"));
            long totalRooms = availableRooms + occupiedRooms + maintenanceRooms;

            Map<Integer, String> paymentAmounts = new LinkedHashMap<>();
            Map<Integer, String> paymentDueDates = new LinkedHashMap<>();
            Map<Integer, String> paymentInitials = new LinkedHashMap<>();
            for (Pembayaran payment : duePayments) {
                if (payment.getId() == null) {
                    continue;
                }
                paymentAmounts.put(payment.getId(), formatRupiah(payment.getJumlah()));
                paymentDueDates.put(payment.getId(), formatDate(payment.getJatuhTempo()));
                paymentInitials.put(payment.getId(), extractInitials(payment.getPenghuniNama()));
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

            ZonedDateTime nowJakarta = ZonedDateTime.now(JAKARTA_ZONE);

            request.setAttribute("roomOccupancy", roomOccupancy);
            request.setAttribute("floorOccupancy", floorOccupancy);
            request.setAttribute("monthlyRevenue", monthlyRevenue);
            request.setAttribute("currentMonthBilling", currentMonthBilling);
            request.setAttribute("overdueBilling", overdueBilling);
            request.setAttribute("openComplaints", openComplaints);
            request.setAttribute("duePayments", duePayments);
            request.setAttribute("recentComplaints", recentComplaints);

            request.setAttribute("totalRooms", totalRooms);
            request.setAttribute("occupiedRooms", occupiedRooms);
            request.setAttribute("availableRooms", availableRooms);
            request.setAttribute("maintenanceRooms", maintenanceRooms);
            request.setAttribute("currentMonthName", MONTH_FORMAT.format(nowJakarta));
            request.setAttribute("monthlyRevenueFormatted", formatRupiah(monthlyRevenue));
            request.setAttribute("monthlyRevenueCompact", formatCompactRupiah(monthlyRevenue));
            request.setAttribute("overdueTotalFormatted",
                    formatRupiah(overdueBilling == null ? null : overdueBilling.totalJumlah()));
            request.setAttribute("overdueTotalCompact",
                    formatCompactRupiah(overdueBilling == null ? null : overdueBilling.totalJumlah()));
            request.setAttribute("paymentAmounts", paymentAmounts);
            request.setAttribute("paymentDueDates", paymentDueDates);
            request.setAttribute("paymentInitials", paymentInitials);
            request.setAttribute("complaintDates", complaintDates);

            request.setAttribute("pageTitle", buildGreeting(request, nowJakarta.toLocalTime()));
            request.setAttribute("pageSubtitle",
                    TODAY_FORMAT.format(nowJakarta) + " \u00b7 Ringkasan hunian bulan ini");
            request.setAttribute("pageSearchAction", request.getContextPath() + "/admin/penghuni");
            request.setAttribute("pageSearchPlaceholder", "Cari penghuni atau kamar");
            request.setAttribute("pageActionLabel", "Tagihan baru");
            request.setAttribute("pageActionHref", request.getContextPath() + "/admin/pembayaran");
            request.setAttribute("activeNav", "dashboard");
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat data dashboard.", e);
        }

        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private static String buildGreeting(HttpServletRequest request, LocalTime time) {
        int hour = time.getHour();
        String partOfDay;
        if (hour >= 4 && hour < 11) {
            partOfDay = "pagi";
        } else if (hour >= 11 && hour < 15) {
            partOfDay = "siang";
        } else if (hour >= 15 && hour < 18) {
            partOfDay = "sore";
        } else {
            partOfDay = "malam";
        }

        String displayName = "Pengelola";
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE) instanceof User user) {
            String nama = user.getNama() == null ? "" : user.getNama().trim();
            if (!nama.isEmpty() && !"Pengelola Kos".equalsIgnoreCase(nama)) {
                displayName = nama;
            }
        }
        return "Selamat " + partOfDay + ", " + displayName;
    }

    private static String formatRupiah(BigDecimal amount) {
        return amount == null ? RupiahFormatter.format(BigDecimal.ZERO) : RupiahFormatter.format(amount);
    }

    private static String formatCompactRupiah(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return RupiahFormatter.format(BigDecimal.ZERO);
        }
        if (amount.compareTo(ONE_MILLION) < 0) {
            return RupiahFormatter.format(amount);
        }
        BigDecimal millions = amount.divide(ONE_MILLION, 1, RoundingMode.HALF_UP);
        String formatted = String.format(ID_LOCALE, "%.1f", millions);
        if (formatted.endsWith(",0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return "Rp " + formatted + " jt";
    }

    private static String extractInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            String token = parts[0].toUpperCase(ID_LOCALE);
            return token.length() >= 2 ? token.substring(0, 2) : token;
        }
        String first = parts[0].substring(0, 1).toUpperCase(ID_LOCALE);
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase(ID_LOCALE);
        return first + last;
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : TanggalFormatter.format(date);
    }
}
