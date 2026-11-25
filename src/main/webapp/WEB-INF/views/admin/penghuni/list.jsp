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
                        <c:url var="clearSearchUrl" value="/admin/penghuni">
                            <c:if test="${not empty status}">
                                <c:param name="status" value="${status}"/>
                            </c:if>
                        </c:url>
                        <a class="chip" href="${fn:escapeXml(clearSearchUrl)}">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </c:if>

                    <c:url var="allUrl" value="/admin/penghuni">
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${empty status ? 'is-active' : ''}" href="${fn:escapeXml(allUrl)}">Semua</a>

                    <c:url var="aktifUrl" value="/admin/penghuni">
                        <c:param name="status" value="AKTIF"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'AKTIF' ? 'is-active' : ''}" href="${fn:escapeXml(aktifUrl)}">Aktif</a>

                    <c:url var="nonaktifUrl" value="/admin/penghuni">
                        <c:param name="status" value="NONAKTIF"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'NONAKTIF' ? 'is-active' : ''}" href="${fn:escapeXml(nonaktifUrl)}">Nonaktif</a>
                </div>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar penghuni</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> penghuni
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty penghuniList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">Tidak ada penghuni yang cocok</c:when>
                                    <c:otherwise>Belum ada penghuni</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">
                                        Ubah kata kunci atau reset filter untuk melihat semua penghuni.
                                    </c:when>
                                    <c:otherwise>
                                        Catat penghuni pertama dengan menempatkannya pada kamar yang tersedia.
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
                                    <th>Penghuni</th>
                                    <th>Kamar</th>
                                    <th>Pekerjaan / instansi</th>
                                    <th>Masuk</th>
                                    <th>Status</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${penghuniList}">
                                    <c:url var="editUrl" value="/admin/penghuni">
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
                                                <span class="avatar">
                                                    <c:out value="${fn:toUpperCase(fn:substring(item.namaLengkap, 0, 1))}"/>
                                                </span>
                                                <span class="table__person-text">
                                                    <span class="table__person-name"><c:out value="${item.namaLengkap}"/></span>
                                                    <span class="table__person-sub"><c:out value="${item.noHp}"/></span>
                                                </span>
                                            </div>
                                        </td>
                                        <td>
                                            <c:out value="${empty item.nomorKamar ? '-' : item.nomorKamar}"/>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:out value="${empty item.pekerjaanInstansi ? '-' : item.pekerjaanInstansi}"/>
                                        </td>
                                        <td class="table__cell--muted"><c:out value="${item.tanggalMasuk}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.status eq 'AKTIF'}">
                                                    <span class="badge badge--success">Aktif</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral">Nonaktif</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <c:if test="${item.status eq 'AKTIF'}">
                                                    <button class="table__action"
                                                            type="button"
                                                            aria-label="Check-out ${fn:escapeXml(item.namaLengkap)}"
                                                            title="Check-out"
                                                            data-modal-open="penghuni-checkout-modal"
                                                            data-modal-action="${ctx}/admin/penghuni/checkout"
                                                            data-modal-field-id="${item.id}"
                                                            data-modal-field-tanggalKeluar="">
                                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                             stroke="currentColor" stroke-width="1.75"
                                                             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                                                            <path d="m16 17 5-5-5-5"/>
                                                            <path d="M21 12H9"/>
                                                        </svg>
                                                    </button>
                                                </c:if>
                                                <a class="table__action"
                                                   href="${fn:escapeXml(editUrl)}"
                                                   aria-label="Ubah ${fn:escapeXml(item.namaLengkap)}"
                                                   title="Ubah">
                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                         stroke="currentColor" stroke-width="1.75"
                                                         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                        <path d="M12 20h9"/>
                                                        <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                                                    </svg>
                                                </a>
                                                <c:if test="${item.status ne 'AKTIF'}">
                                                    <button class="table__action"
                                                            type="button"
                                                            aria-label="Hapus penghuni ${fn:escapeXml(item.namaLengkap)}"
                                                            title="Hapus"
                                                            data-confirm-open="confirm-delete-modal"
                                                            data-delete-url="${ctx}/admin/penghuni/delete"
                                                            data-delete-id="${item.id}"
                                                            data-delete-title="Hapus data penghuni"
                                                            data-delete-message="Hapus data ${fn:escapeXml(item.namaLengkap)}? Riwayat tagihan dan keluhan penghuni ini ikut terhapus.">
                                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                             stroke="currentColor" stroke-width="1.75"
                                                             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <path d="M3 6h18"/>
                                                            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                                                            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
                                                        </svg>
                                                    </button>
                                                </c:if>
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
                                Menampilkan 0 dari 0 penghuni
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> penghuni
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/penghuni">
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
                                <c:url var="pageUrl" value="/admin/penghuni">
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
                                    <c:url var="nextUrl" value="/admin/penghuni">
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
     id="penghuni-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="penghuni-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="penghuni-create-title">Tambah penghuni</h2>
                <p class="modal__subtitle">Catat data penghuni dan tempatkan pada kamar yang tersedia.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/penghuni/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="q" value="${fn:escapeXml(q)}">
            <input type="hidden" name="status" value="${fn:escapeXml(status)}">
            <input type="hidden" name="page" value="${pagination.page}">

            <div class="modal__body">
                <c:if test="${empty form.id and not empty errors.general}">
                    <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                </c:if>

                <div class="field">
                    <label class="field__label" for="create-nama">Nama lengkap</label>
                    <input class="field__input"
                           id="create-nama"
                           name="namaLengkap"
                           type="text"
                           maxlength="100"
                           placeholder="Contoh: Budi Santoso"
                           value="${empty form.id ? fn:escapeXml(form.namaLengkap) : ''}"
                           <c:if test="${empty form.id and not empty errors.namaLengkap}">aria-invalid="true"</c:if>
                           required>
                    <c:if test="${empty form.id and not empty errors.namaLengkap}">
                        <p class="field__error"><c:out value="${errors.namaLengkap}"/></p>
                    </c:if>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-hp">Nomor HP</label>
                        <input class="field__input"
                               id="create-hp"
                               name="noHp"
                               type="tel"
                               maxlength="15"
                               placeholder="081234567890"
                               value="${empty form.id ? fn:escapeXml(form.noHp) : ''}"
                               <c:if test="${empty form.id and not empty errors.noHp}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.noHp}">
                            <p class="field__error"><c:out value="${errors.noHp}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-masuk">Tanggal masuk</label>
                        <input class="field__input"
                               id="create-masuk"
                               name="tanggalMasuk"
                               type="date"
                               value="${empty form.id ? fn:escapeXml(form.tanggalMasuk) : ''}"
                               <c:if test="${empty form.id and not empty errors.tanggalMasuk}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.tanggalMasuk}">
                            <p class="field__error"><c:out value="${errors.tanggalMasuk}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-pekerjaan">Pekerjaan / instansi</label>
                        <input class="field__input"
                               id="create-pekerjaan"
                               name="pekerjaanInstansi"
                               type="text"
                               maxlength="100"
                               placeholder="Contoh: Mahasiswa Universitas Brawijaya"
                               value="${empty form.id ? fn:escapeXml(form.pekerjaanInstansi) : ''}"
                               <c:if test="${empty form.id and not empty errors.pekerjaanInstansi}">aria-invalid="true"</c:if>>
                        <c:if test="${empty form.id and not empty errors.pekerjaanInstansi}">
                            <p class="field__error"><c:out value="${errors.pekerjaanInstansi}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-kontak">Kontak darurat</label>
                        <input class="field__input"
                               id="create-kontak"
                               name="kontakDarurat"
                               type="text"
                               maxlength="100"
                               placeholder="Contoh: 081298765432 (orang tua)"
                               value="${empty form.id ? fn:escapeXml(form.kontakDarurat) : ''}"
                               <c:if test="${empty form.id and not empty errors.kontakDarurat}">aria-invalid="true"</c:if>>
                        <c:if test="${empty form.id and not empty errors.kontakDarurat}">
                            <p class="field__error"><c:out value="${errors.kontakDarurat}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label class="field__label" for="create-kamar">Kamar</label>
                    <select class="field__select"
                            id="create-kamar"
                            name="kamarId"
                            <c:if test="${empty form.id and not empty errors.kamarId}">aria-invalid="true"</c:if>
                            required>
                        <option value="">Pilih kamar tersedia</option>
                        <c:forEach var="kamar" items="${kamarOptions}">
                            <option value="${kamar.id}" ${empty form.id and form.kamarId eq kamar.id ? 'selected' : ''}>
                                <c:out value="${kamar.nomorKamar}"/> &middot; <c:out value="${kamar.tipeKamarNama}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty form.id and not empty errors.kamarId}">
                        <p class="field__error"><c:out value="${errors.kamarId}"/></p>
                    </c:if>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan penghuni</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="penghuni-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="penghuni-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="penghuni-edit-title">Ubah penghuni</h2>
                    <p class="modal__subtitle">Perbarui data penghuni atau pindahkan ke kamar lain.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/penghuni/update" novalidate>
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
                        <label class="field__label" for="edit-nama">Nama lengkap</label>
                        <input class="field__input"
                               id="edit-nama"
                               name="namaLengkap"
                               type="text"
                               maxlength="100"
                               value="${fn:escapeXml(form.namaLengkap)}"
                               <c:if test="${not empty errors.namaLengkap}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${not empty errors.namaLengkap}">
                            <p class="field__error"><c:out value="${errors.namaLengkap}"/></p>
                        </c:if>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-hp">Nomor HP</label>
                            <input class="field__input"
                                   id="edit-hp"
                                   name="noHp"
                                   type="tel"
                                   maxlength="15"
                                   value="${fn:escapeXml(form.noHp)}"
                                   <c:if test="${not empty errors.noHp}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.noHp}">
                                <p class="field__error"><c:out value="${errors.noHp}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-masuk">Tanggal masuk</label>
                            <input class="field__input"
                                   id="edit-masuk"
                                   name="tanggalMasuk"
                                   type="date"
                                   value="${fn:escapeXml(form.tanggalMasuk)}"
                                   <c:if test="${not empty errors.tanggalMasuk}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.tanggalMasuk}">
                                <p class="field__error"><c:out value="${errors.tanggalMasuk}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-pekerjaan">Pekerjaan / instansi</label>
                            <input class="field__input"
                                   id="edit-pekerjaan"
                                   name="pekerjaanInstansi"
                                   type="text"
                                   maxlength="100"
                                   value="${fn:escapeXml(form.pekerjaanInstansi)}"
                                   <c:if test="${not empty errors.pekerjaanInstansi}">aria-invalid="true"</c:if>>
                            <c:if test="${not empty errors.pekerjaanInstansi}">
                                <p class="field__error"><c:out value="${errors.pekerjaanInstansi}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-kontak">Kontak darurat</label>
                            <input class="field__input"
                                   id="edit-kontak"
                                   name="kontakDarurat"
                                   type="text"
                                   maxlength="100"
                                   value="${fn:escapeXml(form.kontakDarurat)}"
                                   <c:if test="${not empty errors.kontakDarurat}">aria-invalid="true"</c:if>>
                            <c:if test="${not empty errors.kontakDarurat}">
                                <p class="field__error"><c:out value="${errors.kontakDarurat}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-kamar">Kamar</label>
                        <select class="field__select"
                                id="edit-kamar"
                                name="kamarId"
                                <c:if test="${not empty errors.kamarId}">aria-invalid="true"</c:if>
                                required>
                            <option value="">Pilih kamar</option>
                            <c:forEach var="kamar" items="${kamarOptions}">
                                <option value="${kamar.id}" ${form.kamarId eq kamar.id ? 'selected' : ''}>
                                    <c:out value="${kamar.nomorKamar}"/> &middot; <c:out value="${kamar.tipeKamarNama}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <p class="field__hint">Pilih kamar berbeda untuk memindahkan penghuni.</p>
                        <c:if test="${not empty errors.kamarId}">
                            <p class="field__error"><c:out value="${errors.kamarId}"/></p>
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

<div class="modal" id="penghuni-checkout-modal" role="dialog" aria-modal="true" aria-labelledby="penghuni-checkout-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="penghuni-checkout-title">Check-out penghuni</h2>
                <p class="modal__subtitle">Penghuni akan ditandai nonaktif dan kamarnya kembali tersedia.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/penghuni/checkout">
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="id" value="">

            <div class="modal__body">
                <div class="field">
                    <label class="field__label" for="checkout-tanggal">Tanggal keluar</label>
                    <input class="field__input"
                           id="checkout-tanggal"
                           name="tanggalKeluar"
                           type="date"
                           required>
                    <p class="field__hint">Kosongkan untuk memakai tanggal hari ini.</p>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Proses check-out</button>
            </footer>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf" %>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
