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
                        <c:url var="clearSearchUrl" value="/admin/kamar">
                            <c:if test="${not empty status}">
                                <c:param name="status" value="${status}"/>
                            </c:if>
                        </c:url>
                        <a class="chip" href="${fn:escapeXml(clearSearchUrl)}">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </c:if>

                    <c:url var="allUrl" value="/admin/kamar">
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${empty status ? 'is-active' : ''}" href="${fn:escapeXml(allUrl)}">Semua</a>

                    <c:url var="tersediaUrl" value="/admin/kamar">
                        <c:param name="status" value="TERSEDIA"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'TERSEDIA' ? 'is-active' : ''}" href="${fn:escapeXml(tersediaUrl)}">Tersedia</a>

                    <c:url var="terisiUrl" value="/admin/kamar">
                        <c:param name="status" value="TERISI"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'TERISI' ? 'is-active' : ''}" href="${fn:escapeXml(terisiUrl)}">Terisi</a>

                    <c:url var="perbaikanUrl" value="/admin/kamar">
                        <c:param name="status" value="PERBAIKAN"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'PERBAIKAN' ? 'is-active' : ''}" href="${fn:escapeXml(perbaikanUrl)}">Perbaikan</a>
                </div>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar kamar</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> kamar
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty kamarList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">Tidak ada kamar yang cocok</c:when>
                                    <c:otherwise>Belum ada kamar</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">
                                        Ubah kata kunci atau reset filter untuk melihat semua kamar.
                                    </c:when>
                                    <c:otherwise>
                                        Tambahkan kamar pertama lalu tetapkan tipe kamarnya.
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
                                    <th>Nomor</th>
                                    <th>Lantai</th>
                                    <th>Tipe</th>
                                    <th>Status</th>
                                    <th>Catatan</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${kamarList}">
                                    <c:url var="editUrl" value="/admin/kamar">
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
                                        <td><c:out value="${item.nomorKamar}"/></td>
                                        <td class="table__cell--muted"><c:out value="${item.lantai}"/></td>
                                        <td><c:out value="${item.tipeKamarNama}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.status eq 'TERSEDIA'}">
                                                    <span class="badge badge--success">Tersedia</span>
                                                </c:when>
                                                <c:when test="${item.status eq 'TERISI'}">
                                                    <span class="badge badge--accent">Terisi</span>
                                                </c:when>
                                                <c:when test="${item.status eq 'PERBAIKAN'}">
                                                    <span class="badge badge--warning">Perbaikan</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral"><c:out value="${item.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:out value="${empty item.catatan ? '-' : item.catatan}"/>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <a class="table__action"
                                                   href="${fn:escapeXml(editUrl)}"
                                                   aria-label="Ubah kamar ${fn:escapeXml(item.nomorKamar)}"
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
                                                        aria-label="Hapus kamar ${fn:escapeXml(item.nomorKamar)}"
                                                        title="Hapus"
                                                        data-confirm-open="confirm-delete-modal"
                                                        data-delete-url="${ctx}/admin/kamar/delete"
                                                        data-delete-id="${item.id}"
                                                        data-delete-title="Hapus kamar"
                                                        data-delete-message="Hapus kamar ${fn:escapeXml(item.nomorKamar)}? Kamar yang masih dihuni penghuni aktif tidak dapat dihapus.">
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
                                Menampilkan 0 dari 0 kamar
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> kamar
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/kamar">
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
                                <c:url var="pageUrl" value="/admin/kamar">
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
                                    <c:url var="nextUrl" value="/admin/kamar">
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
     id="kamar-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="kamar-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="kamar-create-title">Tambah kamar</h2>
                <p class="modal__subtitle">Tentukan nomor, lantai, tipe, dan status awal kamar.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/kamar/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="q" value="${fn:escapeXml(q)}">
            <input type="hidden" name="status" value="${fn:escapeXml(status)}">
            <input type="hidden" name="page" value="${pagination.page}">

            <div class="modal__body">
                <c:if test="${empty form.id and not empty errors.general}">
                    <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                </c:if>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-nomor">Nomor kamar</label>
                        <input class="field__input"
                               id="create-nomor"
                               name="nomorKamar"
                               type="text"
                               maxlength="10"
                               placeholder="Contoh: A-101"
                               value="${empty form.id ? fn:escapeXml(form.nomorKamar) : ''}"
                               <c:if test="${empty form.id and not empty errors.nomorKamar}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.nomorKamar}">
                            <p class="field__error"><c:out value="${errors.nomorKamar}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-lantai">Lantai</label>
                        <input class="field__input"
                               id="create-lantai"
                               name="lantai"
                               type="number"
                               min="1"
                               step="1"
                               placeholder="Contoh: 1"
                               value="${empty form.id ? fn:escapeXml(form.lantai) : ''}"
                               <c:if test="${empty form.id and not empty errors.lantai}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.lantai}">
                            <p class="field__error"><c:out value="${errors.lantai}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-tipe">Tipe kamar</label>
                        <select class="field__select"
                                id="create-tipe"
                                name="tipeKamarId"
                                <c:if test="${empty form.id and not empty errors.tipeKamarId}">aria-invalid="true"</c:if>
                                required>
                            <option value="">Pilih tipe kamar</option>
                            <c:forEach var="tipe" items="${tipeKamarOptions}">
                                <option value="${tipe.id}" ${empty form.id and form.tipeKamarId eq tipe.id ? 'selected' : ''}>
                                    <c:out value="${tipe.nama}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty form.id and not empty errors.tipeKamarId}">
                            <p class="field__error"><c:out value="${errors.tipeKamarId}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-status">Status awal</label>
                        <select class="field__select" id="create-status" name="status">
                            <option value="TERSEDIA" ${empty form.id or form.status eq 'TERSEDIA' ? 'selected' : ''}>Tersedia</option>
                            <option value="PERBAIKAN" ${empty form.id and form.status eq 'PERBAIKAN' ? 'selected' : ''}>Perbaikan</option>
                        </select>
                        <p class="field__hint">Status Terisi otomatis ditetapkan saat penghuni check-in.</p>
                        <c:if test="${empty form.id and not empty errors.status}">
                            <p class="field__error"><c:out value="${errors.status}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label class="field__label" for="create-catatan">Catatan</label>
                    <textarea class="field__textarea"
                              id="create-catatan"
                              name="catatan"
                              placeholder="Contoh: dekat balkon, perlu perbaikan AC"><c:if test="${empty form.id}"><c:out value="${form.catatan}"/></c:if></textarea>
                    <c:if test="${empty form.id and not empty errors.catatan}">
                        <p class="field__error"><c:out value="${errors.catatan}"/></p>
                    </c:if>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan kamar</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="kamar-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="kamar-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="kamar-edit-title">Ubah kamar</h2>
                    <p class="modal__subtitle">Perbarui nomor, lantai, tipe, dan status kamar.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/kamar/update" novalidate>
                <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
                <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                <input type="hidden" name="q" value="${fn:escapeXml(q)}">
                <input type="hidden" name="status" value="${fn:escapeXml(status)}">
                <input type="hidden" name="page" value="${pagination.page}">

                <div class="modal__body">
                    <c:if test="${not empty errors.general}">
                        <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                    </c:if>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-nomor">Nomor kamar</label>
                            <input class="field__input"
                                   id="edit-nomor"
                                   name="nomorKamar"
                                   type="text"
                                   maxlength="10"
                                   value="${fn:escapeXml(form.nomorKamar)}"
                                   <c:if test="${not empty errors.nomorKamar}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.nomorKamar}">
                                <p class="field__error"><c:out value="${errors.nomorKamar}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-lantai">Lantai</label>
                            <input class="field__input"
                                   id="edit-lantai"
                                   name="lantai"
                                   type="number"
                                   min="1"
                                   step="1"
                                   value="${fn:escapeXml(form.lantai)}"
                                   <c:if test="${not empty errors.lantai}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.lantai}">
                                <p class="field__error"><c:out value="${errors.lantai}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-tipe">Tipe kamar</label>
                            <select class="field__select"
                                    id="edit-tipe"
                                    name="tipeKamarId"
                                    <c:if test="${not empty errors.tipeKamarId}">aria-invalid="true"</c:if>
                                    required>
                                <option value="">Pilih tipe kamar</option>
                                <c:forEach var="tipe" items="${tipeKamarOptions}">
                                    <option value="${tipe.id}" ${form.tipeKamarId eq tipe.id ? 'selected' : ''}>
                                        <c:out value="${tipe.nama}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <c:if test="${not empty errors.tipeKamarId}">
                                <p class="field__error"><c:out value="${errors.tipeKamarId}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-status">Status</label>
                            <c:choose>
                                <c:when test="${form.status eq 'TERISI'}">
                                    <input class="field__input" type="text" value="Terisi" disabled>
                                    <p class="field__hint">Status Terisi dikelola melalui transaksi penghuni.</p>
                                </c:when>
                                <c:otherwise>
                                    <select class="field__select" id="edit-status" name="status">
                                        <option value="TERSEDIA" ${form.status eq 'TERSEDIA' ? 'selected' : ''}>Tersedia</option>
                                        <option value="PERBAIKAN" ${form.status eq 'PERBAIKAN' ? 'selected' : ''}>Perbaikan</option>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty errors.status}">
                                <p class="field__error"><c:out value="${errors.status}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-catatan">Catatan</label>
                        <textarea class="field__textarea"
                                  id="edit-catatan"
                                  name="catatan"><c:out value="${form.catatan}"/></textarea>
                        <c:if test="${not empty errors.catatan}">
                            <p class="field__error"><c:out value="${errors.catatan}"/></p>
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
