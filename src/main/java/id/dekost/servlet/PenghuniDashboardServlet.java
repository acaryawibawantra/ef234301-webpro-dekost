package id.dekost.servlet;

import id.dekost.dao.PortalDao;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "PenghuniDashboardServlet", urlPatterns = "/penghuni/dashboard")
public class PenghuniDashboardServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/penghuni/dashboard.jsp";
    private static final int PREVIEW_LIMIT = 4;
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter TODAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", ID_LOCALE);
    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("MMMM uuuu", ID_LOCALE);

    private final PortalDao portalDao = new PortalDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Sesi penghuni tidak valid. Silakan login kembali.");
            return;
        }

        try {
            Optional<PortalDao.ResidentProfile> profileOptional =
                    portalDao.findProfileByUserId(userId);
            if (profileOptional.isEmpty()) {
                renderMissingProfile(request, response);
                return;
            }

            PortalDao.ResidentProfile profile = profileOptional.get();
            int penghuniId = profile.getPenghuniId();
            PortalDao.ResidentSummary summary = portalDao.loadSummary(penghuniId);
            List<Pembayaran> payments = portalDao.findPayments(penghuniId);
            List<Keluhan> complaints = portalDao.findComplaints(penghuniId);

            Map<Integer, String> paymentPeriods = new LinkedHashMap<>();
            Map<Integer, String> paymentAmounts = new LinkedHashMap<>();
            Map<Integer, String> paymentDueDates = new LinkedHashMap<>();
            for (Pembayaran payment : payments) {
                if (payment.getId() == null) {
                    continue;
                }
                paymentPeriods.put(payment.getId(), formatPeriod(payment.getPeriode()));
                paymentAmounts.put(payment.getId(), formatRupiah(payment.getJumlah()));
                paymentDueDates.put(payment.getId(), formatDate(payment.getJatuhTempo()));
            }

            Map<Integer, String> complaintDates = new LinkedHashMap<>();
            for (Keluhan complaint : complaints) {
                if (complaint.getId() == null) {
                    continue;
                }
                LocalDate createdDate = complaint.getCreatedAt() == null
                        ? null
                        : complaint.getCreatedAt().toLocalDate();
                complaintDates.put(complaint.getId(), formatDate(createdDate));
            }

            ZonedDateTime nowJakarta = ZonedDateTime.now(JAKARTA_ZONE);

            request.setAttribute("profile", profile);
            request.setAttribute("summary", summary);
            request.setAttribute("recentPayments", head(payments, PREVIEW_LIMIT));
            request.setAttribute("recentComplaints", head(complaints, PREVIEW_LIMIT));
            request.setAttribute("paymentPeriods", paymentPeriods);
            request.setAttribute("paymentAmounts", paymentAmounts);
            request.setAttribute("paymentDueDates", paymentDueDates);
            request.setAttribute("complaintDates", complaintDates);
            request.setAttribute("belumBayarTotalFormatted", formatRupiah(summary.getBelumBayarTotal()));
            request.setAttribute("hargaBulananFormatted", formatRupiah(profile.getHargaBulanan()));
            request.setAttribute("tanggalMasukFormatted", formatDate(profile.getTanggalMasuk()));
            request.setAttribute("todayFormatted", TODAY_FORMAT.format(nowJakarta));
            request.setAttribute("greeting", buildGreeting(request, nowJakarta));
            request.setAttribute("pageTitle", buildGreeting(request, nowJakarta));
            request.setAttribute("pageSubtitle", "Ringkasan hunian dan tagihan Anda");
            request.setAttribute("pageActionLabel", "Buat keluhan");
            request.setAttribute("pageActionHref",
                    request.getContextPath() + "/penghuni/keluhan");
            request.setAttribute("activeNav", "dashboard");
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat data penghuni.", e);
        }

        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void renderMissingProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("profileMissing", Boolean.TRUE);
        request.setAttribute("pageTitle", "Dashboard Penghuni");
        request.setAttribute("pageSubtitle", "Akun Anda belum terhubung ke data penghuni");
        request.setAttribute("activeNav", "dashboard");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static <T> List<T> head(List<T> items, int limit) {
        return items.size() > limit ? items.subList(0, limit) : items;
    }

    private static String buildGreeting(HttpServletRequest request, ZonedDateTime nowJakarta) {
        String rawName = null;
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE) instanceof User user) {
            rawName = user.getNama();
        }
        String nama = rawName == null ? "" : rawName.trim();
        String displayName = nama.isEmpty() ? "Penghuni" : nama;

        int hour = nowJakarta.getHour();
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
        return "Selamat " + partOfDay + ", " + displayName;
    }

    private static String formatRupiah(BigDecimal amount) {
        return RupiahFormatter.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : TanggalFormatter.format(date);
    }

    private static String formatPeriod(LocalDate period) {
        return period == null ? "-" : PERIOD_FORMAT.format(period);
    }

    private static Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null
                ? null
                : session.getAttribute(AuthServlet.USER_SESSION_ATTRIBUTE);
        if (sessionUser instanceof User user && user.getId() != null) {
            return user.getId();
        }
        return null;
    }
}
