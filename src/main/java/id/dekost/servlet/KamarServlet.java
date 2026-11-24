package id.dekost.servlet;

import id.dekost.dao.KamarDao;
import id.dekost.dao.TipeKamarDao;
import id.dekost.model.Kamar;
import id.dekost.util.Flash;
import id.dekost.util.Pagination;
import id.dekost.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@WebServlet(name = "KamarServlet", urlPatterns = "/admin/kamar/*")
public class KamarServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/kamar/list.jsp";
    private static final String LIST_PATH = "/admin/kamar";
    private static final Set<String> ROOM_STATUSES = Set.of("TERSEDIA", "TERISI", "PERBAIKAN");
    private static final Set<String> MANUAL_STATUSES = Set.of("TERSEDIA", "PERBAIKAN");

    private final KamarDao kamarDao = new KamarDao();
    private final TipeKamarDao tipeKamarDao = new TipeKamarDao();

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
        if (status != null && !status.isBlank() && !ROOM_STATUSES.contains(status)) {
            errors.put("status", "Filter status tidak valid.");
            status = "";
        }

        Kamar form = new Kamar();
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID kamar tidak valid.");
            } else {
                try {
                    Optional<Kamar> existing = kamarDao.findById(id);
                    if (existing.isPresent()) {
                        form = existing.get();
                    } else {
                        errors.put("general", "Kamar tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat kamar.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), status, form, errors);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar kamar.", e);
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
            } else if ("/delete".equals(pathInfo)) {
                delete(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Gagal memproses data kamar.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        if (kamarDao.existsByNomorKamar(validation.form().getNomorKamar(), null)) {
            validation.errors().put("nomorKamar", "Nomor kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        try {
            kamarDao.create(validation.form(), validation.requestedStatus());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                validation.errors().put("nomorKamar", "Nomor kamar sudah digunakan.");
            } else if ("23503".equals(e.getSQLState())) {
                validation.errors().put("tipeKamarId", "Tipe kamar tidak ditemukan.");
            } else {
                throw e;
            }
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        Flash.success(request, "Kamar berhasil ditambahkan.");
        redirectToList(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Map<String, String> errors = Map.of("general", "ID kamar tidak valid.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validateForm(request, null, null).form(), errors);
            return;
        }

        Optional<Kamar> existingResult = kamarDao.findById(id);
        if (existingResult.isEmpty()) {
            Flash.error(request, "Kamar tidak ditemukan.");
            redirectToList(request, response);
            return;
        }

        Kamar existing = existingResult.get();
        FormValidation validation = validateForm(request, id, existing.getStatus());
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        if (kamarDao.existsByNomorKamar(validation.form().getNomorKamar(), id)) {
            validation.errors().put("nomorKamar", "Nomor kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        try {
            KamarDao.UpdateResult result = kamarDao.update(
                    validation.form(), validation.requestedStatus());
            if (result != KamarDao.UpdateResult.UPDATED) {
                handleUpdateResult(result, request, response, validation);
                return;
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                validation.errors().put("nomorKamar", "Nomor kamar sudah digunakan.");
            } else if ("23503".equals(e.getSQLState())) {
                validation.errors().put("tipeKamarId", "Tipe kamar tidak ditemukan.");
            } else {
                throw e;
            }
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), request.getParameter("status"),
                    validation.form(), validation.errors());
            return;
        }

        Flash.success(request, "Kamar berhasil diperbarui.");
        redirectToList(request, response);
    }

    private void handleUpdateResult(KamarDao.UpdateResult result, HttpServletRequest request,
                                    HttpServletResponse response, FormValidation validation)
            throws SQLException, ServletException, IOException {
        switch (result) {
            case NOT_FOUND -> Flash.error(request, "Kamar tidak ditemukan.");
            case INVALID_STATUS -> validation.errors().put(
                    "status", "Status hanya dapat diubah ke atau dari PERBAIKAN.");
            case STATUS_MANAGED_BY_OCCUPANT -> validation.errors().put(
                    "status", "Status TERISI dikelola oleh transaksi penghuni.");
            case ACTIVE_OCCUPANT -> validation.errors().put(
                    "status", "Status tidak dapat diubah karena kamar memiliki penghuni aktif.");
            case UPDATED -> {
                return;
            }
        }

        if (result == KamarDao.UpdateResult.NOT_FOUND) {
            redirectToList(request, response);
            return;
        }
        renderList(request, response, request.getParameter("q"),
                request.getParameter("page"), request.getParameter("status"),
                validation.form(), validation.errors());
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID kamar tidak valid.");
            redirectToList(request, response);
            return;
        }

        KamarDao.DeleteResult result = kamarDao.deleteById(id);
        switch (result) {
            case DELETED -> Flash.success(request, "Kamar berhasil dihapus.");
            case ACTIVE_OCCUPANT -> Flash.error(request, "Kamar tidak dapat dihapus karena masih dihuni penghuni aktif.");
            case NOT_FOUND -> Flash.error(request, "Kamar tidak ditemukan.");
        }
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id, String existingStatus)
            throws SQLException {
        Map<String, String> errors = new LinkedHashMap<>();

        String nomorKamar = request.getParameter("nomorKamar");
        nomorKamar = nomorKamar == null ? "" : nomorKamar.trim();
        if (nomorKamar.isEmpty()) {
            errors.put("nomorKamar", "Nomor kamar wajib diisi.");
        } else if (nomorKamar.length() > 10) {
            errors.put("nomorKamar", "Nomor kamar maksimal 10 karakter.");
        }

        String rawLantai = request.getParameter("lantai");
        Integer lantai = null;
        if (!Validator.isPositiveInteger(rawLantai)) {
            errors.put("lantai", "Lantai harus bilangan bulat lebih besar dari 0.");
        } else {
            lantai = Integer.parseInt(rawLantai.trim());
        }

        String rawTipeKamarId = request.getParameter("tipeKamarId");
        Integer tipeKamarId = parsePositiveInteger(rawTipeKamarId);
        if (tipeKamarId == null) {
            errors.put("tipeKamarId", "Tipe kamar wajib dipilih.");
        } else if (!kamarDao.existsTipeKamar(tipeKamarId)) {
            errors.put("tipeKamarId", "Tipe kamar tidak ditemukan.");
        }

        String requestedStatus = request.getParameter("status");
        if (id == null) {
            if (requestedStatus == null || requestedStatus.isBlank()) {
                requestedStatus = "TERSEDIA";
            }
            if (!MANUAL_STATUSES.contains(requestedStatus)) {
                errors.put("status", "Kamar baru hanya dapat berstatus TERSEDIA atau PERBAIKAN.");
            }
        } else if (requestedStatus != null && !requestedStatus.isBlank()) {
            if ("TERISI".equals(existingStatus) && "TERISI".equals(requestedStatus)) {
                requestedStatus = null;
            } else if (!MANUAL_STATUSES.contains(requestedStatus)) {
                errors.put("status", "Status TERISI dikelola oleh transaksi penghuni.");
            }
        } else if (requestedStatus != null && requestedStatus.isBlank()) {
            requestedStatus = null;
        }

        String catatan = request.getParameter("catatan");
        if (catatan != null && catatan.isBlank()) {
            catatan = null;
        }

        String formStatus = requestedStatus == null ? existingStatus : requestedStatus;
        Kamar form = new Kamar(id, nomorKamar, lantai, tipeKamarId, formStatus, catatan);
        return new FormValidation(form, errors, requestedStatus);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, String status, Kamar form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        String normalizedStatus = status == null ? "" : status.trim();
        if (!normalizedStatus.isEmpty() && !ROOM_STATUSES.contains(normalizedStatus)) {
            normalizedStatus = "";
        }
        long totalItems = kamarDao.count(normalizedQuery, normalizedStatus);
        Pagination pagination = Pagination.of(requestedPage, totalItems);

        request.setAttribute("kamarList", kamarDao.findPage(
                normalizedQuery, normalizedStatus, pagination.getPageSize(), pagination.getOffset()));
        request.setAttribute("tipeKamarOptions", tipeKamarDao.findAll());
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("status", normalizedStatus);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("pageTitle", "Kamar");
        request.setAttribute("activeNav", "kamar");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static Integer parsePositiveInteger(String value) {
        if (!Validator.isPositiveInteger(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private static void redirectToList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + LIST_PATH);
    }

    private record FormValidation(Kamar form, Map<String, String> errors, String requestedStatus) {
    }
}
