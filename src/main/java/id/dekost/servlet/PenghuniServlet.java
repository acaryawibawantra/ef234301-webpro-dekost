package id.dekost.servlet;

import id.dekost.dao.KamarDao;
import id.dekost.dao.PenghuniDao;
import id.dekost.model.Kamar;
import id.dekost.model.Penghuni;
import id.dekost.service.PenghuniService;
import id.dekost.util.Flash;
import id.dekost.util.Pagination;
import id.dekost.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "PenghuniServlet", urlPatterns = "/admin/penghuni/*")
public class PenghuniServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/penghuni/list.jsp";
    private static final String LIST_PATH = "/admin/penghuni";
    private static final Set<String> OCCUPANT_STATUSES = Set.of("AKTIF", "NONAKTIF");

    private final PenghuniDao penghuniDao = new PenghuniDao();
    private final KamarDao kamarDao = new KamarDao();
    private final PenghuniService penghuniService = new PenghuniService(penghuniDao);

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
        if (status != null && !status.isBlank() && !OCCUPANT_STATUSES.contains(status)) {
            errors.put("status", "Filter status tidak valid.");
            status = "";
        }

        Penghuni form = new Penghuni();
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID penghuni tidak valid.");
            } else {
                try {
                    Optional<Penghuni> existing = penghuniDao.findById(id);
                    if (existing.isPresent()) {
                        form = existing.get();
                    } else {
                        errors.put("general", "Penghuni tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat penghuni.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), status, form, errors);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar penghuni.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if ("/create".equals(pathInfo)) {
                checkIn(request, response);
            } else if ("/update".equals(pathInfo)) {
                updateAndMove(request, response);
            } else if ("/checkout".equals(pathInfo)) {
                checkOut(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Gagal memproses data penghuni.", e);
        }
    }

    private void checkIn(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        PenghuniService.CheckInResult result = penghuniService.checkIn(validation.form());
        if (result == PenghuniService.CheckInResult.CHECKED_IN) {
            Flash.success(request, "Penghuni berhasil check-in.");
            redirectToList(request, response);
            return;
        }

        if (result == PenghuniService.CheckInResult.ROOM_UNAVAILABLE
                || result == PenghuniService.CheckInResult.ROOM_NOT_FOUND) {
            validation.errors().put("kamarId", "Kamar tidak tersedia atau sedang diperbaiki.");
        } else {
            validation.errors().put("general", "Data penghuni tidak valid.");
        }
        renderList(request, response, request.getParameter("q"),
                request.getParameter("page"), request.getParameter("status"),
                validation.form(), validation.errors());
    }

    private void updateAndMove(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Map<String, String> errors = Map.of("general", "ID penghuni tidak valid.");
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

        PenghuniService.MoveResult result = penghuniService.updateAndMove(
                validation.form(), id, validation.form().getKamarId());
        switch (result) {
            case MOVED -> Flash.success(request, "Data penghuni berhasil diperbarui dan kamar berhasil dipindahkan.");
            case UPDATED -> Flash.success(request, "Data penghuni berhasil diperbarui.");
            case OCCUPANT_NOT_FOUND -> Flash.error(request, "Penghuni tidak ditemukan.");
            case NOT_ACTIVE -> Flash.error(request, "Hanya penghuni aktif yang dapat diperbarui atau dipindahkan.");
            case NO_ACTIVE_ROOM -> Flash.error(request, "Penghuni tidak memiliki kamar aktif.");
            case SAME_ROOM -> Flash.error(request, "Pilih kamar yang berbeda untuk memindahkan penghuni.");
            case ROOM_NOT_FOUND -> validation.errors().put("kamarId", "Kamar tujuan tidak ditemukan.");
            case ROOM_UNAVAILABLE -> validation.errors().put(
                    "kamarId", "Kamar tujuan tidak tersedia atau sedang diperbaiki.");
            case ASSIGNMENT_CHANGED -> Flash.error(
                    request, "Penempatan penghuni berubah bersamaan. Muat ulang halaman lalu coba lagi.");
            case INVALID_DATA -> validation.errors().put("general", "Data penghuni tidak valid.");
        }

        if (result == PenghuniService.MoveResult.OCCUPANT_NOT_FOUND
                || result == PenghuniService.MoveResult.NOT_ACTIVE
                || result == PenghuniService.MoveResult.NO_ACTIVE_ROOM
                || result == PenghuniService.MoveResult.ASSIGNMENT_CHANGED) {
            redirectToList(request, response);
            return;
        }
        if (result == PenghuniService.MoveResult.MOVED
                || result == PenghuniService.MoveResult.UPDATED
                || result == PenghuniService.MoveResult.SAME_ROOM) {
            redirectToList(request, response);
            return;
        }

        renderList(request, response, request.getParameter("q"),
                request.getParameter("page"), request.getParameter("status"),
                validation.form(), validation.errors());
    }

    private void checkOut(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID penghuni tidak valid.");
            redirectToList(request, response);
            return;
        }

        String rawTanggalKeluar = request.getParameter("tanggalKeluar");
        LocalDate tanggalKeluar = parseDate(rawTanggalKeluar);
        if (rawTanggalKeluar == null || rawTanggalKeluar.isBlank()) {
            tanggalKeluar = LocalDate.now();
        } else if (tanggalKeluar == null) {
            Flash.error(request, "Tanggal keluar tidak valid.");
            redirectToList(request, response);
            return;
        }

        PenghuniService.CheckOutResult result = penghuniService.checkOut(id, tanggalKeluar);
        switch (result) {
            case CHECKED_OUT -> Flash.success(request, "Penghuni berhasil check-out dan kamar kembali tersedia.");
            case INVALID_DATE -> Flash.error(request, "Tanggal keluar tidak boleh sebelum tanggal masuk.");
            case OCCUPANT_NOT_FOUND -> Flash.error(request, "Penghuni tidak ditemukan.");
            case NOT_ACTIVE -> Flash.error(request, "Penghuni sudah tidak aktif.");
            case NO_ACTIVE_ROOM -> Flash.error(request, "Penghuni tidak memiliki kamar aktif.");
            case ASSIGNMENT_CHANGED -> Flash.error(
                    request, "Penempatan penghuni berubah bersamaan. Muat ulang halaman lalu coba lagi.");
        }
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id) throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();

        String nama = request.getParameter("namaLengkap");
        nama = nama == null ? "" : nama.trim();
        if (nama.isEmpty()) {
            errors.put("namaLengkap", "Nama penghuni wajib diisi.");
        } else if (nama.length() > 100) {
            errors.put("namaLengkap", "Nama penghuni maksimal 100 karakter.");
        }

        String noHp = request.getParameter("noHp");
        noHp = noHp == null ? "" : noHp.trim();
        if (!Validator.isValidPhoneNumber(noHp)) {
            errors.put("noHp", "Nomor HP harus terdiri dari 9 sampai 15 digit.");
        }

        String pekerjaanInstansi = optionalText(request.getParameter("pekerjaanInstansi"));
        if (pekerjaanInstansi != null && pekerjaanInstansi.length() > 100) {
            errors.put("pekerjaanInstansi", "Pekerjaan/instansi maksimal 100 karakter.");
        }

        String kontakDarurat = optionalText(request.getParameter("kontakDarurat"));
        if (kontakDarurat != null && kontakDarurat.length() > 100) {
            errors.put("kontakDarurat", "Kontak darurat maksimal 100 karakter.");
        }

        LocalDate tanggalMasuk = parseDate(request.getParameter("tanggalMasuk"));
        if (tanggalMasuk == null) {
            errors.put("tanggalMasuk", "Tanggal masuk wajib diisi dengan format tanggal yang valid.");
        }

        Integer kamarId = parsePositiveInteger(request.getParameter("kamarId"));
        if (kamarId == null) {
            errors.put("kamarId", "Kamar wajib dipilih.");
        }

        Penghuni form = new Penghuni(
                id,
                null,
                kamarId,
                null,
                nama,
                noHp,
                pekerjaanInstansi,
                kontakDarurat,
                tanggalMasuk,
                null,
                "AKTIF"
        );
        return new FormValidation(form, errors);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, String status, Penghuni form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        if (!normalizedStatus.isEmpty() && !OCCUPANT_STATUSES.contains(normalizedStatus)) {
            normalizedStatus = "";
        }

        long totalItems = penghuniDao.count(normalizedQuery, normalizedStatus);
        Pagination pagination = Pagination.of(requestedPage, totalItems);
        ArrayList<Kamar> kamarOptions = new ArrayList<>(kamarDao.findAvailableRooms());
        if (form.getKamarId() != null
                && kamarOptions.stream().noneMatch(kamar -> form.getKamarId().equals(kamar.getId()))) {
            kamarDao.findById(form.getKamarId()).ifPresent(kamarOptions::add);
        }

        request.setAttribute("penghuniList", penghuniDao.findPage(
                normalizedQuery, normalizedStatus, pagination.getPageSize(), pagination.getOffset()));
        request.setAttribute("kamarOptions", kamarOptions);
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("status", normalizedStatus);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("pageTitle", "Penghuni");
        request.setAttribute("activeNav", "penghuni");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static Integer parsePositiveInteger(String value) {
        if (!Validator.isPositiveInteger(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
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

    private record FormValidation(Penghuni form, Map<String, String> errors) {
    }
}
