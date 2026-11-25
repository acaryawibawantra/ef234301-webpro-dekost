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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
            request.setAttribute("monthlyRevenueCompact", RupiahFormatter.formatCompact(monthlyRevenue));
            request.setAttribute("overdueTotalFormatted",
                    formatRupiah(overdueBilling == null ? null : overdueBilling.totalJumlah()));
            request.setAttribute("overdueTotalCompact",
                    RupiahFormatter.formatCompact(overdueBilling == null ? null : overdueBilling.totalJumlah()));
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
        String rawName = null;
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE) instanceof User user) {
            rawName = user.getNama();
        }
        return buildGreetingForHour(rawName, time.getHour());
    }

    static String buildGreetingForHour(String rawName, int hour) {
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
        String nama = rawName == null ? "" : rawName.trim();
        if (!nama.isEmpty() && !"Pengelola Kos".equalsIgnoreCase(nama)) {
            displayName = nama;
        }
        return "Selamat " + partOfDay + ", " + displayName;
    }

    private static String formatRupiah(BigDecimal amount) {
        return amount == null ? RupiahFormatter.format(BigDecimal.ZERO) : RupiahFormatter.format(amount);
    }

    static String extractInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        String[] rawTokens = name.split("[\\s\\p{Z}]+");
        List<String> cleanTokens = new ArrayList<>();
        for (String token : rawTokens) {
            String stripped = stripNonAlphanumeric(token);
            if (!stripped.isEmpty()) {
                cleanTokens.add(stripped);
            }
        }
        if (cleanTokens.isEmpty()) {
            return "?";
        }
        if (cleanTokens.size() == 1) {
            String single = cleanTokens.get(0).toUpperCase(ID_LOCALE);
            int count = single.codePointCount(0, single.length());
            int end = single.offsetByCodePoints(0, Math.min(2, count));
            return single.substring(0, end);
        }
        String first = firstCodePoint(cleanTokens.get(0)).toUpperCase(ID_LOCALE);
        String last = firstCodePoint(cleanTokens.get(cleanTokens.size() - 1)).toUpperCase(ID_LOCALE);
        return first + last;
    }

    private static String stripNonAlphanumeric(String token) {
        int start = 0;
        int len = token.length();
        while (start < len) {
            int cp = token.codePointAt(start);
            if (Character.isLetterOrDigit(cp)) {
                break;
            }
            start += Character.charCount(cp);
        }
        int end = len;
        while (end > start) {
            int cp = token.codePointBefore(end);
            if (Character.isLetterOrDigit(cp)) {
                break;
            }
            end -= Character.charCount(cp);
        }
        return token.substring(start, end);
    }

    private static String firstCodePoint(String text) {
        return text.substring(0, text.offsetByCodePoints(0, 1));
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : TanggalFormatter.format(date);
    }
}
