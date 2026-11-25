package id.dekost.servlet;

import id.dekost.dao.PortalDao;
import id.dekost.model.Pembayaran;
import id.dekost.model.User;
import id.dekost.util.Flash;
import id.dekost.util.RupiahFormatter;
import id.dekost.util.TanggalFormatter;
import id.dekost.util.Validator;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "PenghuniTagihanServlet", urlPatterns = "/penghuni/tagihan/*")
public class PenghuniTagihanServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/penghuni/tagihan.jsp";
    private static final String LIST_PATH = "/penghuni/tagihan";
    private static final Set<String> FILTER_STATUSES = Set.of("BELUM_BAYAR", "TERLAMBAT", "LUNAS");
    private static final Locale ID_LOCALE = Locale.forLanguageTag("id-ID");
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

            String status = request.getParameter("status");
            if (status == null || !FILTER_STATUSES.contains(status)) {
                status = "";
            }

            List<Pembayaran> payments = portalDao.findPayments(profile.getPenghuniId());
            List<Pembayaran> visible = new ArrayList<>();
            for (Pembayaran payment : payments) {
                if (status.isEmpty() || status.equals(payment.getStatus())) {
                    visible.add(payment);
                }
            }

            Map<Integer, String> paymentPeriods = new LinkedHashMap<>();
            Map<Integer, String> paymentAmounts = new LinkedHashMap<>();
            Map<Integer, String> paymentDueDates = new LinkedHashMap<>();
            Map<Integer, String> paymentPayDates = new LinkedHashMap<>();
            for (Pembayaran payment : visible) {
                if (payment.getId() == null) {
                    continue;
                }
                paymentPeriods.put(payment.getId(), formatPeriod(payment.getPeriode()));
                paymentAmounts.put(payment.getId(), formatRupiah(payment.getJumlah()));
                paymentDueDates.put(payment.getId(), formatDate(payment.getJatuhTempo()));
                paymentPayDates.put(payment.getId(), formatDate(payment.getTanggalBayar()));
            }

            PortalDao.ResidentSummary summary = portalDao.loadSummary(profile.getPenghuniId());

            request.setAttribute("profile", profile);
            request.setAttribute("summary", summary);
            request.setAttribute("pembayaranList", visible);
            request.setAttribute("totalTagihan", payments.size());
            request.setAttribute("paymentPeriods", paymentPeriods);
            request.setAttribute("paymentAmounts", paymentAmounts);
            request.setAttribute("paymentDueDates", paymentDueDates);
            request.setAttribute("paymentPayDates", paymentPayDates);
            request.setAttribute("belumBayarTotalFormatted", formatRupiah(summary.getBelumBayarTotal()));
            request.setAttribute("status", status);
            request.setAttribute("pageTitle", "Tagihan Saya");
            request.setAttribute("pageSubtitle", payments.size() + " tagihan sewa");
            request.setAttribute("activeNav", "tagihan");
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat tagihan penghuni.", e);
        }

        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (!"/bukti".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

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
                Flash.error(request, "Akun Anda belum terhubung ke data penghuni.");
                response.sendRedirect(request.getContextPath() + LIST_PATH);
                return;
            }
            submitProof(request, profileOptional.get().getPenghuniId());
        } catch (SQLException e) {
            throw new ServletException("Gagal mengirim bukti pembayaran.", e);
        }

        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private void submitProof(HttpServletRequest request, int penghuniId) throws SQLException {
        Integer pembayaranId = parsePositiveInteger(request.getParameter("id"));
        String buktiUrl = request.getParameter("buktiUrl");
        buktiUrl = buktiUrl == null ? "" : buktiUrl.trim();

        if (pembayaranId == null) {
            Flash.error(request, "Tagihan tidak valid.");
        } else if (buktiUrl.isEmpty()) {
            Flash.error(request, "URL bukti pembayaran wajib diisi.");
        } else if (buktiUrl.length() > 255) {
            Flash.error(request, "URL bukti pembayaran maksimal 255 karakter.");
        } else if (portalDao.submitPaymentProof(penghuniId, pembayaranId, buktiUrl)) {
            Flash.success(request, "Bukti pembayaran berhasil dikirim.");
        } else {
            Flash.error(request, "Tagihan tidak ditemukan atau sudah lunas.");
        }
    }

    private void renderMissingProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("profileMissing", Boolean.TRUE);
        request.setAttribute("pageTitle", "Tagihan Saya");
        request.setAttribute("pageSubtitle", "Akun Anda belum terhubung ke data penghuni");
        request.setAttribute("activeNav", "tagihan");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static Integer parsePositiveInteger(String value) {
        if (!Validator.isPositiveInteger(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
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
