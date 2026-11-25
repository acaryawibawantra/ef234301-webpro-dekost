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

            <c:if test="${not empty q}">
                <div class="toolbar">
                    <div class="toolbar__group">
                        <a class="chip is-active" href="${ctx}/admin/tipe-kamar">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </div>
                </div>
            </c:if>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar tipe kamar</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> tipe kamar
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty tipeKamarList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q}">Tidak ada tipe kamar yang cocok</c:when>
                                    <c:otherwise>Belum ada tipe kamar</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q}">
                                        Coba kata kunci lain atau reset pencarian untuk melihat semua tipe kamar.
                                    </c:when>
                                    <c:otherwise>
                                        Tambahkan tipe kamar pertama untuk menentukan tarif dan fasilitas dasar.
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
                                    <th>Tipe</th>
                                    <th>Harga / bulan</th>
                                    <th>Luas</th>
                                    <th>Fasilitas</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${tipeKamarList}">
                                    <c:url var="editUrl" value="/admin/tipe-kamar">
                                        <c:param name="edit" value="${item.id}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${pagination.page gt 1}">
                                            <c:param name="page" value="${pagination.page}"/>
                                        </c:if>
                                    </c:url>
                                    <tr>
                                        <td><c:out value="${item.nama}"/></td>
                                        <td><c:out value="${empty hargaFormatted[item.id] ? 'Rp0' : hargaFormatted[item.id]}"/></td>
                                        <td class="table__cell--muted"><c:out value="${item.luasM2}"/> m&sup2;</td>
                                        <td class="table__cell--muted">
                                            <c:out value="${empty item.fasilitas ? '-' : item.fasilitas}"/>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <a class="table__action"
                                                   href="${fn:escapeXml(editUrl)}"
                                                   aria-label="Ubah ${fn:escapeXml(item.nama)}"
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
                                                        aria-label="Hapus ${fn:escapeXml(item.nama)}"
                                                        title="Hapus"
                                                        data-confirm-open="confirm-delete-modal"
                                                        data-delete-url="${ctx}/admin/tipe-kamar/delete"
                                                        data-delete-id="${item.id}"
                                                        data-delete-title="Hapus tipe kamar"
                                                        data-delete-message="Hapus tipe kamar ${fn:escapeXml(item.nama)}? Tipe yang masih dipakai kamar tidak dapat dihapus.">
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
                                Menampilkan 0 dari 0 tipe kamar
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> tipe kamar
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/tipe-kamar">
                                        <c:param name="page" value="${pagination.page - 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                    </c:url>
                                    <a class="pager__link" href="${fn:escapeXml(prevUrl)}" aria-label="Halaman sebelumnya">&lsaquo;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__link pager__link--disabled" aria-disabled="true">&lsaquo;</span>
                                </c:otherwise>
                            </c:choose>

                            <c:forEach var="pageNum" begin="1" end="${pagination.totalPages}">
                                <c:url var="pageUrl" value="/admin/tipe-kamar">
                                    <c:param name="page" value="${pageNum}"/>
                                    <c:if test="${not empty q}">
                                        <c:param name="q" value="${q}"/>
                                    </c:if>
                                </c:url>
                                <a class="pager__link ${pageNum eq pagination.page ? 'pager__link--active' : ''}"
                                   href="${fn:escapeXml(pageUrl)}"><c:out value="${pageNum}"/></a>
                            </c:forEach>

                            <c:choose>
                                <c:when test="${pagination.hasNext}">
                                    <c:url var="nextUrl" value="/admin/tipe-kamar">
                                        <c:param name="page" value="${pagination.page + 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
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
     id="tipe-kamar-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="tipe-kamar-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="tipe-kamar-create-title">Tambah tipe kamar</h2>
                <p class="modal__subtitle">Tentukan nama, tarif bulanan, luas, dan fasilitas tipe kamar.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/tipe-kamar/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="q" value="${fn:escapeXml(q)}">
            <input type="hidden" name="page" value="${pagination.page}">

            <div class="modal__body">
                <c:if test="${empty form.id and not empty errors.general}">
                    <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                </c:if>

                <div class="field">
                    <label class="field__label" for="create-nama">Nama tipe kamar</label>
                    <input class="field__input"
                           id="create-nama"
                           name="nama"
                           type="text"
                           maxlength="50"
                           placeholder="Contoh: Deluxe"
                           value="${empty form.id ? fn:escapeXml(form.nama) : ''}"
                           <c:if test="${empty form.id and not empty errors.nama}">aria-invalid="true"</c:if>
                           required>
                    <c:if test="${empty form.id and not empty errors.nama}">
                        <p class="field__error"><c:out value="${errors.nama}"/></p>
                    </c:if>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-harga">Harga bulanan (Rp)</label>
                        <input class="field__input"
                               id="create-harga"
                               name="hargaBulanan"
                               type="number"
                               min="0"
                               step="0.01"
                               placeholder="Contoh: 1800000"
                               value="${empty form.id ? fn:escapeXml(form.hargaBulanan) : ''}"
                               <c:if test="${empty form.id and not empty errors.hargaBulanan}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.hargaBulanan}">
                            <p class="field__error"><c:out value="${errors.hargaBulanan}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-luas">Luas kamar (m&sup2;)</label>
                        <input class="field__input"
                               id="create-luas"
                               name="luasM2"
                               type="number"
                               min="1"
                               step="1"
                               placeholder="Contoh: 12"
                               value="${empty form.id ? fn:escapeXml(form.luasM2) : ''}"
                               <c:if test="${empty form.id and not empty errors.luasM2}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.luasM2}">
                            <p class="field__error"><c:out value="${errors.luasM2}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label class="field__label" for="create-fasilitas">Fasilitas</label>
                    <textarea class="field__textarea"
                              id="create-fasilitas"
                              name="fasilitas"
                              placeholder="Contoh: Kasur, AC, kamar mandi dalam, WiFi"><c:if test="${empty form.id}"><c:out value="${form.fasilitas}"/></c:if></textarea>
                    <c:if test="${empty form.id and not empty errors.fasilitas}">
                        <p class="field__error"><c:out value="${errors.fasilitas}"/></p>
                    </c:if>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan tipe kamar</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="tipe-kamar-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="tipe-kamar-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="tipe-kamar-edit-title">Ubah tipe kamar</h2>
                    <p class="modal__subtitle">Perbarui informasi tarif, luas, dan fasilitas tipe kamar.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/tipe-kamar/update" novalidate>
                <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
                <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                <input type="hidden" name="q" value="${fn:escapeXml(q)}">
                <input type="hidden" name="page" value="${pagination.page}">

                <div class="modal__body">
                    <c:if test="${not empty errors.general}">
                        <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                    </c:if>

                    <div class="field">
                        <label class="field__label" for="edit-nama">Nama tipe kamar</label>
                        <input class="field__input"
                               id="edit-nama"
                               name="nama"
                               type="text"
                               maxlength="50"
                               value="${fn:escapeXml(form.nama)}"
                               <c:if test="${not empty errors.nama}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${not empty errors.nama}">
                            <p class="field__error"><c:out value="${errors.nama}"/></p>
                        </c:if>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-harga">Harga bulanan (Rp)</label>
                            <input class="field__input"
                                   id="edit-harga"
                                   name="hargaBulanan"
                                   type="number"
                                   min="0"
                                   step="0.01"
                                   value="${fn:escapeXml(form.hargaBulanan)}"
                                   <c:if test="${not empty errors.hargaBulanan}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.hargaBulanan}">
                                <p class="field__error"><c:out value="${errors.hargaBulanan}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-luas">Luas kamar (m&sup2;)</label>
                            <input class="field__input"
                                   id="edit-luas"
                                   name="luasM2"
                                   type="number"
                                   min="1"
                                   step="1"
                                   value="${fn:escapeXml(form.luasM2)}"
                                   <c:if test="${not empty errors.luasM2}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.luasM2}">
                                <p class="field__error"><c:out value="${errors.luasM2}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-fasilitas">Fasilitas</label>
                        <textarea class="field__textarea"
                                  id="edit-fasilitas"
                                  name="fasilitas"><c:out value="${form.fasilitas}"/></textarea>
                        <c:if test="${not empty errors.fasilitas}">
                            <p class="field__error"><c:out value="${errors.fasilitas}"/></p>
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
