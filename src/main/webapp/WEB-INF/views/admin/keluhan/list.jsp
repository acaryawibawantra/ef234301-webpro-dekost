<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="admin-body">
<div class="admin-layout">
    <%@ include file="/WEB-INF/views/layout/sidebar.jspf" %>

    <div class="admin-main">
        <%@ include file="/WEB-INF/views/layout/topbar.jspf" %>

        <%@ include file="/WEB-INF/views/admin/fragments/toast.jspf" %>

        <main class="admin-content">
            <c:if test="${not empty errors.general and empty form.id}">
                <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
            </c:if>

            <div class="toolbar">
                <div class="toolbar__group">
                    <c:if test="${not empty q}">
                        <c:url var="clearSearchUrl" value="/admin/keluhan">
                            <c:if test="${not empty status}">
                                <c:param name="status" value="${status}"/>
                            </c:if>
                        </c:url>
                        <a class="chip" href="${fn:escapeXml(clearSearchUrl)}">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </c:if>

                    <c:url var="allUrl" value="/admin/keluhan">
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${empty status ? 'is-active' : ''}" href="${fn:escapeXml(allUrl)}">Semua</a>

                    <c:url var="baruUrl" value="/admin/keluhan">
                        <c:param name="status" value="BARU"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'BARU' ? 'is-active' : ''}" href="${fn:escapeXml(baruUrl)}">Baru</a>

                    <c:url var="diprosesUrl" value="/admin/keluhan">
                        <c:param name="status" value="DIPROSES"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'DIPROSES' ? 'is-active' : ''}" href="${fn:escapeXml(diprosesUrl)}">Diproses</a>

                    <c:url var="selesaiUrl" value="/admin/keluhan">
                        <c:param name="status" value="SELESAI"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'SELESAI' ? 'is-active' : ''}" href="${fn:escapeXml(selesaiUrl)}">Selesai</a>
                </div>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar keluhan</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> keluhan
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty keluhanList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">Tidak ada keluhan yang cocok</c:when>
                                    <c:otherwise>Belum ada keluhan</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">
                                        Ubah kata kunci atau reset filter untuk melihat semua keluhan.
                                    </c:when>
                                    <c:otherwise>
                                        Catat keluhan penghuni untuk mulai memantau penanganannya.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th>Keluhan</th>
                                    <th>Kategori</th>
                                    <th>Prioritas</th>
                                    <th>Status</th>
                                    <th>Dibuat</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${keluhanList}">
                                    <c:url var="editUrl" value="/admin/keluhan">
                                        <c:param name="edit" value="${item.id}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty status}">
                                            <c:param name="status" value="${status}"/>
                                        </c:if>
                                        <c:if test="${pagination.page gt 1}">
                                            <c:param name="page" value="${pagination.page}"/>
                                        </c:if>
                                    </c:url>
                                    <tr>
                                        <td>
                                            <div class="table__person">
                                                <span class="table__person-text">
                                                    <span class="table__person-name"><c:out value="${item.judul}"/></span>
                                                    <span class="table__person-sub">
                                                        <c:out value="${empty item.penghuniNama ? 'Penghuni tidak diketahui' : item.penghuniNama}"/>
                                                        <c:if test="${not empty item.nomorKamar}">
                                                            &middot; <c:out value="${item.nomorKamar}"/>
                                                        </c:if>
                                                    </span>
                                                </span>
                                            </div>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:choose>
                                                <c:when test="${item.kategori eq 'FASILITAS'}">Fasilitas</c:when>
                                                <c:when test="${item.kategori eq 'KEBERSIHAN'}">Kebersihan</c:when>
                                                <c:when test="${item.kategori eq 'KEBISINGAN'}">Kebisingan</c:when>
                                                <c:otherwise>Lainnya</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.prioritas eq 'TINGGI'}">
                                                    <span class="badge badge--danger">Tinggi</span>
                                                </c:when>
                                                <c:when test="${item.prioritas eq 'SEDANG'}">
                                                    <span class="badge badge--warning">Sedang</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral">Rendah</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.status eq 'SELESAI'}">
                                                    <span class="badge badge--success">Selesai</span>
                                                </c:when>
                                                <c:when test="${item.status eq 'DIPROSES'}">
                                                    <span class="badge badge--warning">Diproses</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--accent">Baru</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:out value="${fn:replace(item.createdAt, 'T', ' ')}"/>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <a class="table__action"
                                                   href="${fn:escapeXml(editUrl)}"
                                                   aria-label="Ubah keluhan ${fn:escapeXml(item.judul)}"
                                                   title="Ubah">
                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                         stroke="currentColor" stroke-width="1.75"
                                                         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                        <path d="M12 20h9"/>
                                                        <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                                                    </svg>
                                                </a>
                                                <button class="table__action"
                                                        type="button"
                                                        aria-label="Hapus keluhan ${fn:escapeXml(item.judul)}"
                                                        title="Hapus"
                                                        data-confirm-open="confirm-delete-modal"
                                                        data-delete-url="${ctx}/admin/keluhan/delete"
                                                        data-delete-id="${item.id}"
                                                        data-delete-title="Hapus keluhan"
                                                        data-delete-message="Hapus keluhan &ldquo;${fn:escapeXml(item.judul)}&rdquo;?">
                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                         stroke="currentColor" stroke-width="1.75"
                                                         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                        <path d="M3 6h18"/>
                                                        <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                                                        <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
                                                    </svg>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <footer class="table-card__footer">
                    <p>
                        <c:choose>
                            <c:when test="${pagination.totalItems eq 0}">
                                Menampilkan 0 dari 0 keluhan
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> keluhan
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/keluhan">
                                        <c:param name="page" value="${pagination.page - 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty status}">
                                            <c:param name="status" value="${status}"/>
                                        </c:if>
                                    </c:url>
                                    <a class="pager__link" href="${fn:escapeXml(prevUrl)}" aria-label="Halaman sebelumnya">&lsaquo;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__link pager__link--disabled" aria-disabled="true">&lsaquo;</span>
                                </c:otherwise>
                            </c:choose>

                            <c:forEach var="pageNum" begin="1" end="${pagination.totalPages}">
                                <c:url var="pageUrl" value="/admin/keluhan">
                                    <c:param name="page" value="${pageNum}"/>
                                    <c:if test="${not empty q}">
                                        <c:param name="q" value="${q}"/>
                                    </c:if>
                                    <c:if test="${not empty status}">
                                        <c:param name="status" value="${status}"/>
                                    </c:if>
                                </c:url>
                                <a class="pager__link ${pageNum eq pagination.page ? 'pager__link--active' : ''}"
                                   href="${fn:escapeXml(pageUrl)}"><c:out value="${pageNum}"/></a>
                            </c:forEach>

                            <c:choose>
                                <c:when test="${pagination.hasNext}">
                                    <c:url var="nextUrl" value="/admin/keluhan">
                                        <c:param name="page" value="${pagination.page + 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty status}">
                                            <c:param name="status" value="${status}"/>
                                        </c:if>
                                    </c:url>
                                    <a class="pager__link" href="${fn:escapeXml(nextUrl)}" aria-label="Halaman berikutnya">&rsaquo;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__link pager__link--disabled" aria-disabled="true">&rsaquo;</span>
                                </c:otherwise>
                            </c:choose>
                        </nav>
                    </c:if>
                </footer>
            </section>
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<div class="modal ${empty form.id and not empty errors ? 'is-open' : ''}"
     id="keluhan-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="keluhan-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="keluhan-create-title">Catat keluhan</h2>
                <p class="modal__subtitle">Catat keluhan baru dari penghuni untuk ditindaklanjuti.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/keluhan/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="q" value="${fn:escapeXml(q)}">
            <input type="hidden" name="status" value="${fn:escapeXml(status)}">
            <input type="hidden" name="page" value="${pagination.page}">

            <div class="modal__body">
                <c:if test="${empty form.id and not empty errors.general}">
                    <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                </c:if>

                <div class="field">
                    <label class="field__label" for="create-penghuni">Penghuni</label>
                    <select class="field__select"
                            id="create-penghuni"
                            name="penghuniId"
                            <c:if test="${empty form.id and not empty errors.penghuniId}">aria-invalid="true"</c:if>
                            required>
                        <option value="">Pilih penghuni</option>
                        <c:forEach var="opsi" items="${penghuniOptions}">
                            <option value="${opsi.id}" ${empty form.id and form.penghuniId eq opsi.id ? 'selected' : ''}>
                                <c:out value="${opsi.namaLengkap}"/><c:if test="${opsi.status ne 'AKTIF'}"> (nonaktif)</c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty form.id and not empty errors.penghuniId}">
                        <p class="field__error"><c:out value="${errors.penghuniId}"/></p>
                    </c:if>
                </div>

                <div class="field">
                    <label class="field__label" for="create-judul">Judul</label>
                    <input class="field__input"
                           id="create-judul"
                           name="judul"
                           type="text"
                           maxlength="150"
                           placeholder="Contoh: AC kamar tidak dingin"
                           value="${empty form.id ? fn:escapeXml(form.judul) : ''}"
                           <c:if test="${empty form.id and not empty errors.judul}">aria-invalid="true"</c:if>
                           required>
                    <c:if test="${empty form.id and not empty errors.judul}">
                        <p class="field__error"><c:out value="${errors.judul}"/></p>
                    </c:if>
                </div>

                <div class="field">
                    <label class="field__label" for="create-deskripsi">Deskripsi</label>
                    <textarea class="field__textarea"
                              id="create-deskripsi"
                              name="deskripsi"
                              placeholder="Jelaskan detail keluhan yang dialami penghuni"
                              <c:if test="${empty form.id and not empty errors.deskripsi}">aria-invalid="true"</c:if>
                              required><c:if test="${empty form.id}"><c:out value="${form.deskripsi}"/></c:if></textarea>
                    <c:if test="${empty form.id and not empty errors.deskripsi}">
                        <p class="field__error"><c:out value="${errors.deskripsi}"/></p>
                    </c:if>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-kategori">Kategori</label>
                        <select class="field__select" id="create-kategori" name="kategori">
                            <c:forEach var="kategori" items="${categories}">
                                <option value="${kategori}" ${empty form.id and form.kategori eq kategori ? 'selected' : ''}>
                                    <c:choose>
                                        <c:when test="${kategori eq 'FASILITAS'}">Fasilitas</c:when>
                                        <c:when test="${kategori eq 'KEBERSIHAN'}">Kebersihan</c:when>
                                        <c:when test="${kategori eq 'KEBISINGAN'}">Kebisingan</c:when>
                                        <c:otherwise>Lainnya</c:otherwise>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty form.id and not empty errors.kategori}">
                            <p class="field__error"><c:out value="${errors.kategori}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-prioritas">Prioritas</label>
                        <select class="field__select" id="create-prioritas" name="prioritas">
                            <c:forEach var="prioritas" items="${priorities}">
                                <option value="${prioritas}" ${empty form.id and (form.prioritas eq prioritas or (empty form.prioritas and prioritas eq 'SEDANG')) ? 'selected' : ''}>
                                    <c:choose>
                                        <c:when test="${prioritas eq 'RENDAH'}">Rendah</c:when>
                                        <c:when test="${prioritas eq 'TINGGI'}">Tinggi</c:when>
                                        <c:otherwise>Sedang</c:otherwise>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty form.id and not empty errors.prioritas}">
                            <p class="field__error"><c:out value="${errors.prioritas}"/></p>
                        </c:if>
                    </div>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan keluhan</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="keluhan-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="keluhan-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="keluhan-edit-title">Ubah keluhan</h2>
                    <p class="modal__subtitle">Perbarui data, status, dan tanggapan keluhan.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/keluhan/update" novalidate>
                <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
                <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                <input type="hidden" name="q" value="${fn:escapeXml(q)}">
                <input type="hidden" name="status" value="${fn:escapeXml(status)}">
                <input type="hidden" name="page" value="${pagination.page}">

                <div class="modal__body">
                    <c:if test="${not empty errors.general}">
                        <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                    </c:if>

                    <div class="field">
                        <label class="field__label" for="edit-penghuni">Penghuni</label>
                        <select class="field__select"
                                id="edit-penghuni"
                                name="penghuniId"
                                <c:if test="${not empty errors.penghuniId}">aria-invalid="true"</c:if>
                                required>
                            <option value="">Pilih penghuni</option>
                            <c:forEach var="opsi" items="${penghuniOptions}">
                                <option value="${opsi.id}" ${form.penghuniId eq opsi.id ? 'selected' : ''}>
                                    <c:out value="${opsi.namaLengkap}"/><c:if test="${opsi.status ne 'AKTIF'}"> (nonaktif)</c:if>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty errors.penghuniId}">
                            <p class="field__error"><c:out value="${errors.penghuniId}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-judul">Judul</label>
                        <input class="field__input"
                               id="edit-judul"
                               name="judul"
                               type="text"
                               maxlength="150"
                               value="${fn:escapeXml(form.judul)}"
                               <c:if test="${not empty errors.judul}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${not empty errors.judul}">
                            <p class="field__error"><c:out value="${errors.judul}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-deskripsi">Deskripsi</label>
                        <textarea class="field__textarea"
                                  id="edit-deskripsi"
                                  name="deskripsi"
                                  <c:if test="${not empty errors.deskripsi}">aria-invalid="true"</c:if>
                                  required><c:out value="${form.deskripsi}"/></textarea>
                        <c:if test="${not empty errors.deskripsi}">
                            <p class="field__error"><c:out value="${errors.deskripsi}"/></p>
                        </c:if>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-kategori">Kategori</label>
                            <select class="field__select" id="edit-kategori" name="kategori">
                                <c:forEach var="kategori" items="${categories}">
                                    <option value="${kategori}" ${form.kategori eq kategori ? 'selected' : ''}>
                                        <c:choose>
                                            <c:when test="${kategori eq 'FASILITAS'}">Fasilitas</c:when>
                                            <c:when test="${kategori eq 'KEBERSIHAN'}">Kebersihan</c:when>
                                            <c:when test="${kategori eq 'KEBISINGAN'}">Kebisingan</c:when>
                                            <c:otherwise>Lainnya</c:otherwise>
                                        </c:choose>
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.kategori}">
                                <p class="field__error"><c:out value="${errors.kategori}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-prioritas">Prioritas</label>
                            <select class="field__select" id="edit-prioritas" name="prioritas">
                                <c:forEach var="prioritas" items="${priorities}">
                                    <option value="${prioritas}" ${form.prioritas eq prioritas ? 'selected' : ''}>
                                        <c:choose>
                                            <c:when test="${prioritas eq 'RENDAH'}">Rendah</c:when>
                                            <c:when test="${prioritas eq 'TINGGI'}">Tinggi</c:when>
                                            <c:otherwise>Sedang</c:otherwise>
                                        </c:choose>
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.prioritas}">
                                <p class="field__error"><c:out value="${errors.prioritas}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-status">Status</label>
                        <select class="field__select"
                                id="edit-status"
                                name="status"
                                <c:if test="${not empty errors.status}">aria-invalid="true"</c:if>
                                required>
                            <c:forEach var="statusKeluhan" items="${statuses}">
                                <option value="${statusKeluhan}" ${form.status eq statusKeluhan ? 'selected' : ''}>
                                    <c:choose>
                                        <c:when test="${statusKeluhan eq 'SELESAI'}">Selesai</c:when>
                                        <c:when test="${statusKeluhan eq 'DIPROSES'}">Diproses</c:when>
                                        <c:otherwise>Baru</c:otherwise>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                        <p class="field__hint">Keluhan yang sudah selesai tidak dapat dikembalikan ke status Baru.</p>
                        <c:if test="${not empty errors.status}">
                            <p class="field__error"><c:out value="${errors.status}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-tanggapan">Tanggapan</label>
                        <textarea class="field__textarea"
                                  id="edit-tanggapan"
                                  name="tanggapan"
                                  placeholder="Catatan penanganan keluhan (opsional)"><c:out value="${form.tanggapan}"/></textarea>
                        <c:if test="${not empty errors.tanggapan}">
                            <p class="field__error"><c:out value="${errors.tanggapan}"/></p>
                        </c:if>
                    </div>
                </div>

                <footer class="modal__footer">
                    <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                    <button class="button button--primary" type="submit">Simpan perubahan</button>
                </footer>
            </form>
        </div>
    </div>
</c:if>

<%@ include file="/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf" %>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
