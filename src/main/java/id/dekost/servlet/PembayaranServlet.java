package id.dekost.servlet;

import id.dekost.dao.PembayaranDao;
import id.dekost.model.Pembayaran;
import id.dekost.service.PembayaranService;
import id.dekost.service.PembayaranService.MutationResult;
import id.dekost.util.Flash;
import id.dekost.util.Pagination;
import id.dekost.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@WebServlet(name = "PembayaranServlet", urlPatterns = "/admin/pembayaran/*")
public class PembayaranServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/pembayaran/list.jsp";
    private static final String LIST_PATH = "/admin/pembayaran";
    private static final Set<String> PAYMENT_STATUSES = Set.of("BELUM_BAYAR", "LUNAS", "TERLAMBAT");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d{1,10}(\\.\\d{1,2})?$");

    private final PembayaranDao pembayaranDao = new PembayaranDao();
    private final PembayaranService pembayaranService = new PembayaranService(pembayaranDao);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isBlank() && !"/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String status = request.getParameter("status");
        Map<String, String> errors = new LinkedHashMap<>();
        if (status != null && !status.isBlank() && !PAYMENT_STATUSES.contains(status)) {
            errors.put("status", "Filter status tidak valid.");
            status = "";
        }

        Pembayaran form = new Pembayaran();
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID pembayaran tidak valid.");
            } else {
                try {
                    Optional<Pembayaran> existing = pembayaranDao.findById(id);
                    if (existing.isPresent()) {
                        form = existing.get();
                    } else {
                        errors.put("general", "Pembayaran tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat pembayaran.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), status, form, errors);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar pembayaran.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if ("/create".equals(pathInfo)) {
                create(request, response);
            } else if ("/update".equals(pathInfo)) {
                update(request, response);
            } else if ("/pay".equals(pathInfo)) {
                markPaid(request, response);
            } else if ("/delete".equals(pathInfo)) {
                delete(request, response);
            } else if ("/generate".equals(pathInfo)) {
                generate(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Gagal memproses pembayaran.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        MutationResult result = pembayaranService.create(validation.form());
        if (result == MutationResult.SAVED) {
            Flash.success(request, "Tagihan berhasil ditambahkan.");
            redirectToList(request, response);
            return;
        }
        addMutationError(result, validation.errors());
        renderList(request, response, request.getParameter("q"),
                request.getParameter("page"), request.getParameter("status"),
                validation.form(), validation.errors());
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Map<String, String> errors = Map.of("general", "ID pembayaran tidak valid.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validateForm(request, null).form(), errors);
            return;
        }

        FormValidation validation = validateForm(request, id);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        MutationResult result = pembayaranService.update(validation.form());
        if (result == MutationResult.SAVED) {
            Flash.success(request, "Tagihan berhasil diperbarui.");
            redirectToList(request, response);
            return;
        }
        if (result == MutationResult.NOT_FOUND || result == MutationResult.ALREADY_PAID
                || result == MutationResult.CONFLICT) {
            Flash.error(request, mutationMessage(result));
            redirectToList(request, response);
            return;
        }

        addMutationError(result, validation.errors());
        renderList(request, response, request.getParameter("q"),
                request.getParameter("page"), request.getParameter("status"),
                validation.form(), validation.errors());
    }

    private void markPaid(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        LocalDate tanggalBayar = parseDate(request.getParameter("tanggalBayar"));
        String metode = request.getParameter("metode");
        if (id == null || tanggalBayar == null || metode == null || metode.isBlank()) {
            Flash.error(request, "ID, tanggal pembayaran, dan metode pembayaran wajib diisi dengan benar.");
            redirectToList(request, response);
            return;
        }

        MutationResult result = pembayaranService.markPaid(id, tanggalBayar, metode.trim());
        if (result == MutationResult.PAID) {
            Flash.success(request, "Tagihan berhasil ditandai lunas.");
        } else {
            Flash.error(request, mutationMessage(result));
        }
        redirectToList(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID pembayaran tidak valid.");
        } else {
            MutationResult result = pembayaranService.delete(id);
            if (result == MutationResult.DELETED) {
                Flash.success(request, "Tagihan berhasil dihapus.");
            } else {
                Flash.error(request, mutationMessage(result));
            }
        }
        redirectToList(request, response);
    }

    private void generate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int generated = pembayaranService.generateForCurrentMonth();
        Flash.success(request, generated == 0
                ? "Tidak ada tagihan baru; penghuni aktif sudah memiliki tagihan bulan ini."
                : generated + " tagihan bulan ini berhasil dibuat.");
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id) throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();

        Integer penghuniId = parsePositiveInteger(request.getParameter("penghuniId"));
        if (penghuniId == null) {
            errors.put("penghuniId", "Penghuni wajib dipilih.");
        }

        LocalDate periode = parseDate(request.getParameter("periode"));
        if (periode == null || periode.getDayOfMonth() != 1) {
            errors.put("periode", "Periode wajib menggunakan tanggal pertama pada bulan tagihan.");
        }

        String rawJumlah = request.getParameter("jumlah");
        BigDecimal jumlah = null;
        if ((rawJumlah == null || rawJumlah.isBlank()) && id == null && penghuniId != null) {
            jumlah = pembayaranDao.findCurrentRent(penghuniId).orElse(null);
        }
        if (jumlah == null
                && (rawJumlah == null || !AMOUNT_PATTERN.matcher(rawJumlah.trim()).matches())) {
            errors.put("jumlah", "Jumlah wajib berupa angka non-negatif dengan maksimal 10 digit dan 2 desimal.");
        } else if (jumlah == null) {
            jumlah = new BigDecimal(rawJumlah.trim());
        }

        LocalDate jatuhTempo = parseDate(request.getParameter("jatuhTempo"));
        if (jatuhTempo == null) {
            errors.put("jatuhTempo", "Tanggal jatuh tempo wajib diisi dengan format yang valid.");
        } else if (periode != null && jatuhTempo.isBefore(periode)) {
            errors.put("jatuhTempo", "Jatuh tempo tidak boleh sebelum periode tagihan.");
        }

        String buktiUrl = optionalText(request.getParameter("buktiUrl"));
        if (buktiUrl != null && buktiUrl.length() > 255) {
            errors.put("buktiUrl", "URL bukti pembayaran maksimal 255 karakter.");
        }

        Pembayaran form = new Pembayaran(
                id, penghuniId, periode, jumlah, jatuhTempo, null, null, "BELUM_BAYAR", buktiUrl);
        return new FormValidation(form, errors);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, String status, Pembayaran form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        if (!normalizedStatus.isEmpty() && !PAYMENT_STATUSES.contains(normalizedStatus)) {
            normalizedStatus = "";
        }

        long totalItems = pembayaranDao.count(normalizedQuery, normalizedStatus);
        Pagination pagination = Pagination.of(requestedPage, totalItems);
        request.setAttribute("pembayaranList", pembayaranDao.findPage(
                normalizedQuery, normalizedStatus, pagination.getPageSize(), pagination.getOffset()));
        request.setAttribute("penghuniOptions", pembayaranDao.findOccupantOptions());
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("status", normalizedStatus);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("paymentMethods", Set.of("TUNAI", "TRANSFER", "QRIS"));
        request.setAttribute("pageTitle", "Pembayaran");
        request.setAttribute("pageSubtitle", totalItems + " tagihan");
        request.setAttribute("pageSearchAction", request.getContextPath() + LIST_PATH);
        request.setAttribute("pageSearchPlaceholder", "Cari tagihan");
        request.setAttribute("pageActionLabel", "Tambah tagihan");
        request.setAttribute("pageActionModal", "pembayaran-create-modal");
        request.setAttribute("activeNav", "pembayaran");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static void addMutationError(MutationResult result, Map<String, String> errors) {
        switch (result) {
            case DUPLICATE_PERIOD -> errors.put("periode", "Tagihan untuk penghuni dan periode tersebut sudah ada.");
            case OCCUPANT_NOT_FOUND -> errors.put("penghuniId", "Penghuni tidak ditemukan.");
            case INVALID_PAYMENT_DETAILS -> errors.put("general", "Tanggal dan metode pembayaran tidak valid.");
            case INVALID_DATA -> errors.put("general", "Data tagihan tidak valid.");
            default -> errors.put("general", mutationMessage(result));
        }
    }

    private static String mutationMessage(MutationResult result) {
        return switch (result) {
            case DUPLICATE_PERIOD -> "Tagihan untuk penghuni dan periode tersebut sudah ada.";
            case OCCUPANT_NOT_FOUND -> "Penghuni tidak ditemukan.";
            case NOT_FOUND -> "Tagihan tidak ditemukan.";
            case ALREADY_PAID -> "Tagihan yang sudah lunas tidak dapat diubah atau dibayar kembali.";
            case INVALID_PAYMENT_DETAILS -> "Tanggal dan metode pembayaran tidak valid.";
            case INVALID_DATA -> "Data tagihan tidak valid.";
            case CONFLICT -> "Tagihan berubah bersamaan. Muat ulang halaman lalu coba lagi.";
            case SAVED, PAID, DELETED -> "Operasi pembayaran berhasil.";
        };
    }

    private static Integer parsePositiveInteger(String value) {
        if (!Validator.isPositiveInteger(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private record FormValidation(Pembayaran form, Map<String, String> errors) {
    }
}
