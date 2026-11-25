package id.dekost.servlet;

import id.dekost.dao.TipeKamarDao;
import id.dekost.model.TipeKamar;
import id.dekost.util.Flash;
import id.dekost.util.Pagination;
import id.dekost.util.RupiahFormatter;
import id.dekost.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet(name = "TipeKamarServlet", urlPatterns = "/admin/tipe-kamar/*")
public class TipeKamarServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/admin/tipe-kamar/list.jsp";
    private static final String LIST_PATH = "/admin/tipe-kamar";
    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d{1,10}(?:\\.\\d{1,2})?$");

    private final TipeKamarDao tipeKamarDao = new TipeKamarDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.isBlank() && !"/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, String> errors = new LinkedHashMap<>();
        TipeKamar form = new TipeKamar();
        String editId = request.getParameter("edit");
        if (editId != null && !editId.isBlank()) {
            Integer id = parsePositiveInteger(editId);
            if (id == null) {
                errors.put("general", "ID tipe kamar tidak valid.");
            } else {
                try {
                    Optional<TipeKamar> existing = tipeKamarDao.findById(id);
                    if (existing.isPresent()) {
                        form = existing.get();
                    } else {
                        errors.put("general", "Tipe kamar tidak ditemukan.");
                    }
                } catch (SQLException e) {
                    throw new ServletException("Gagal memuat tipe kamar.", e);
                }
            }
        }

        try {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), form, errors);
        } catch (SQLException e) {
            throw new ServletException("Gagal memuat daftar tipe kamar.", e);
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
            throw new ServletException("Gagal memproses data tipe kamar.", e);
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        FormValidation validation = validateForm(request, null);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        if (tipeKamarDao.existsByNama(validation.form().getNama(), null)) {
            validation.errors().put("nama", "Nama tipe kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        try {
            tipeKamarDao.create(validation.form());
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("nama", "Nama tipe kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        Flash.success(request, "Tipe kamar berhasil ditambahkan.");
        redirectToList(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Map<String, String> errors = Map.of("general", "ID tipe kamar tidak valid.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validateForm(request, null).form(), errors);
            return;
        }

        if (tipeKamarDao.findById(id).isEmpty()) {
            Flash.error(request, "Tipe kamar tidak ditemukan.");
            redirectToList(request, response);
            return;
        }

        FormValidation validation = validateForm(request, id);
        if (!validation.errors().isEmpty()) {
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        if (tipeKamarDao.existsByNama(validation.form().getNama(), id)) {
            validation.errors().put("nama", "Nama tipe kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        try {
            if (!tipeKamarDao.update(validation.form())) {
                Flash.error(request, "Tipe kamar tidak ditemukan.");
                redirectToList(request, response);
                return;
            }
        } catch (SQLException e) {
            if (!"23505".equals(e.getSQLState())) {
                throw e;
            }
            validation.errors().put("nama", "Nama tipe kamar sudah digunakan.");
            renderList(request, response, request.getParameter("q"),
                    request.getParameter("page"), validation.form(), validation.errors());
            return;
        }

        Flash.success(request, "Tipe kamar berhasil diperbarui.");
        redirectToList(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Integer id = parsePositiveInteger(request.getParameter("id"));
        if (id == null) {
            Flash.error(request, "ID tipe kamar tidak valid.");
            redirectToList(request, response);
            return;
        }

        TipeKamarDao.DeleteResult result = tipeKamarDao.deleteById(id);
        switch (result) {
            case DELETED -> Flash.success(request, "Tipe kamar berhasil dihapus.");
            case IN_USE -> Flash.error(request, "Tipe kamar tidak dapat dihapus karena masih digunakan kamar.");
            case NOT_FOUND -> Flash.error(request, "Tipe kamar tidak ditemukan.");
        }
        redirectToList(request, response);
    }

    private FormValidation validateForm(HttpServletRequest request, Integer id) {
        Map<String, String> errors = new LinkedHashMap<>();

        String nama = request.getParameter("nama");
        nama = nama == null ? "" : nama.trim();
        if (nama.isEmpty()) {
            errors.put("nama", "Nama tipe kamar wajib diisi.");
        } else if (nama.length() > 50) {
            errors.put("nama", "Nama tipe kamar maksimal 50 karakter.");
        }

        String rawHarga = request.getParameter("hargaBulanan");
        BigDecimal harga = null;
        if (rawHarga == null || rawHarga.isBlank()) {
            errors.put("hargaBulanan", "Harga bulanan wajib diisi.");
        } else if (!PRICE_PATTERN.matcher(rawHarga.trim()).matches()) {
            errors.put("hargaBulanan",
                    "Harga harus 0 atau lebih, maksimal 10 digit dan 2 angka desimal.");
        } else {
            harga = new BigDecimal(rawHarga.trim());
            if (!Validator.isNonNegativeAmount(harga)) {
                errors.put("hargaBulanan", "Harga bulanan tidak boleh negatif.");
            }
        }

        String rawLuas = request.getParameter("luasM2");
        Integer luas = null;
        if (!Validator.isPositiveInteger(rawLuas)) {
            errors.put("luasM2", "Luas kamar harus bilangan bulat lebih besar dari 0.");
        } else {
            luas = Integer.parseInt(rawLuas.trim());
        }

        String fasilitas = request.getParameter("fasilitas");
        if (fasilitas != null && fasilitas.isBlank()) {
            fasilitas = null;
        }

        TipeKamar form = new TipeKamar(id, nama, harga, luas, fasilitas);
        return new FormValidation(form, errors);
    }

    private void renderList(HttpServletRequest request, HttpServletResponse response,
                            String query, String requestedPage, TipeKamar form,
                            Map<String, String> errors)
            throws SQLException, ServletException, IOException {
        String normalizedQuery = query == null ? "" : query.trim();
        long totalItems = tipeKamarDao.count(normalizedQuery);
        Pagination pagination = Pagination.of(requestedPage, totalItems);
        List<TipeKamar> tipeKamarList = tipeKamarDao.findPage(
                normalizedQuery, pagination.getPageSize(), pagination.getOffset());

        request.setAttribute("tipeKamarList", tipeKamarList);
        request.setAttribute("hargaFormatted", buildHargaFormattedMap(tipeKamarList));
        request.setAttribute("pagination", pagination);
        request.setAttribute("q", normalizedQuery);
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("pageTitle", "Tipe Kamar");
        request.setAttribute("pageSubtitle", totalItems + " tipe kamar");
        request.setAttribute("pageSearchAction", request.getContextPath() + LIST_PATH);
        request.setAttribute("pageSearchPlaceholder", "Cari tipe kamar");
        request.setAttribute("pageActionLabel", "Tambah tipe kamar");
        request.setAttribute("pageActionModal", "tipe-kamar-create-modal");
        request.setAttribute("activeNav", "tipe-kamar");
        Flash.consume(request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    static Map<Integer, String> buildHargaFormattedMap(List<TipeKamar> items) {
        Map<Integer, String> formatted = new LinkedHashMap<>();
        if (items == null) {
            return formatted;
        }
        for (TipeKamar item : items) {
            if (item == null || item.getId() == null) {
                continue;
            }
            BigDecimal harga = item.getHargaBulanan();
            formatted.put(item.getId(),
                    harga == null ? RupiahFormatter.format(BigDecimal.ZERO) : RupiahFormatter.format(harga));
        }
        return formatted;
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

    private record FormValidation(TipeKamar form, Map<String, String> errors) {
    }
}
