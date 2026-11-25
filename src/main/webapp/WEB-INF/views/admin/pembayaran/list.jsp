<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<fmt:setLocale value="id_ID"/>
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
                        <c:url var="clearSearchUrl" value="/admin/pembayaran">
                            <c:if test="${not empty status}">
                                <c:param name="status" value="${status}"/>
                            </c:if>
                        </c:url>
                        <a class="chip" href="${fn:escapeXml(clearSearchUrl)}">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </c:if>

                    <c:url var="allUrl" value="/admin/pembayaran">
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${empty status ? 'is-active' : ''}" href="${fn:escapeXml(allUrl)}">Semua</a>

                    <c:url var="belumUrl" value="/admin/pembayaran">
                        <c:param name="status" value="BELUM_BAYAR"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'BELUM_BAYAR' ? 'is-active' : ''}" href="${fn:escapeXml(belumUrl)}">Belum bayar</a>

                    <c:url var="terlambatUrl" value="/admin/pembayaran">
                        <c:param name="status" value="TERLAMBAT"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'TERLAMBAT' ? 'is-active' : ''}" href="${fn:escapeXml(terlambatUrl)}">Terlambat</a>

                    <c:url var="lunasUrl" value="/admin/pembayaran">
                        <c:param name="status" value="LUNAS"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${status eq 'LUNAS' ? 'is-active' : ''}" href="${fn:escapeXml(lunasUrl)}">Lunas</a>
                </div>

                <div class="toolbar__group">
                    <form method="post" action="${ctx}/admin/pembayaran/generate">
                        <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
                        <button class="button button--secondary button--sm" type="submit">Buat tagihan bulan ini</button>
                    </form>
                </div>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar tagihan</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> tagihan
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty pembayaranList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">Tidak ada tagihan yang cocok</c:when>
                                    <c:otherwise>Belum ada tagihan</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q or not empty status}">
                                        Ubah kata kunci atau reset filter untuk melihat semua tagihan.
                                    </c:when>
                                    <c:otherwise>
                                        Buat tagihan bulan ini untuk seluruh penghuni aktif atau tambahkan manual.
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
                                    <th>Periode</th>
                                    <th>Jumlah</th>
                                    <th>Jatuh tempo</th>
                                    <th>Status</th>
                                    <th>Pembayaran</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${pembayaranList}">
                                    <c:url var="editUrl" value="/admin/pembayaran">
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
                                                    <span class="table__person-name"><c:out value="${item.penghuniNama}"/></span>
                                                    <span class="table__person-sub">
                                                        <c:out value="${empty item.nomorKamar ? 'Tanpa kamar' : item.nomorKamar}"/>
                                                    </span>
                                                </span>
                                            </div>
                                        </td>
                                        <td><c:out value="${item.periode}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty item.jumlah}">Rp0</c:when>
                                                <c:otherwise>
                                                    Rp<fmt:formatNumber value="${item.jumlah}" type="number" minFractionDigits="0" maxFractionDigits="0"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted"><c:out value="${item.jatuhTempo}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.status eq 'LUNAS'}">
                                                    <span class="badge badge--success">Lunas</span>
                                                </c:when>
                                                <c:when test="${item.status eq 'TERLAMBAT'}">
                                                    <span class="badge badge--danger">Terlambat</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--warning">Belum bayar</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:choose>
                                                <c:when test="${item.status eq 'LUNAS'}">
                                                    <c:out value="${empty item.metode ? '-' : item.metode}"/>
                                                    <c:if test="${not empty item.tanggalBayar}">
                                                        &middot; <c:out value="${item.tanggalBayar}"/>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <c:if test="${item.status ne 'LUNAS'}">
                                                    <button class="table__action"
                                                            type="button"
                                                            aria-label="Tandai lunas tagihan ${fn:escapeXml(item.penghuniNama)}"
                                                            title="Tandai lunas"
                                                            data-modal-open="pembayaran-pay-modal"
                                                            data-modal-action="${ctx}/admin/pembayaran/pay"
                                                            data-modal-field-id="${item.id}"
                                                            data-modal-field-tanggalBayar=""
                                                            data-modal-field-metode="TUNAI">
                                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                             stroke="currentColor" stroke-width="1.75"
                                                             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <path d="M20 6 9 17l-5-5"/>
                                                        </svg>
                                                    </button>
                                                    <a class="table__action"
                                                       href="${fn:escapeXml(editUrl)}"
                                                       aria-label="Ubah tagihan ${fn:escapeXml(item.penghuniNama)}"
                                                       title="Ubah">
                                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                             stroke="currentColor" stroke-width="1.75"
                                                             stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                            <path d="M12 20h9"/>
                                                            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                                                        </svg>
                                                    </a>
                                                </c:if>
                                                <button class="table__action"
                                                        type="button"
                                                        aria-label="Hapus tagihan ${fn:escapeXml(item.penghuniNama)}"
                                                        title="Hapus"
                                                        data-confirm-open="confirm-delete-modal"
                                                        data-delete-url="${ctx}/admin/pembayaran/delete"
                                                        data-delete-id="${item.id}"
                                                        data-delete-title="Hapus tagihan"
                                                        data-delete-message="Hapus tagihan ${fn:escapeXml(item.penghuniNama)} periode ${fn:escapeXml(item.periode)}?">
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
                                Menampilkan 0 dari 0 tagihan
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> tagihan
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/pembayaran">
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
                                <c:url var="pageUrl" value="/admin/pembayaran">
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
                                    <c:url var="nextUrl" value="/admin/pembayaran">
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
     id="pembayaran-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="pembayaran-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="pembayaran-create-title">Tambah tagihan</h2>
                <p class="modal__subtitle">Buat tagihan sewa untuk penghuni pada periode tertentu.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/pembayaran/create" novalidate>
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
                                <c:out value="${opsi.nama}"/><c:if test="${not empty opsi.nomorKamar}"> &middot; <c:out value="${opsi.nomorKamar}"/></c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty form.id and not empty errors.penghuniId}">
                        <p class="field__error"><c:out value="${errors.penghuniId}"/></p>
                    </c:if>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-periode">Periode</label>
                        <input class="field__input"
                               id="create-periode"
                               name="periode"
                               type="date"
                               value="${empty form.id ? fn:escapeXml(form.periode) : ''}"
                               <c:if test="${empty form.id and not empty errors.periode}">aria-invalid="true"</c:if>
                               required>
                        <p class="field__hint">Gunakan tanggal 1 sebagai awal periode tagihan.</p>
                        <c:if test="${empty form.id and not empty errors.periode}">
                            <p class="field__error"><c:out value="${errors.periode}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-jatuh-tempo">Jatuh tempo</label>
                        <input class="field__input"
                               id="create-jatuh-tempo"
                               name="jatuhTempo"
                               type="date"
                               value="${empty form.id ? fn:escapeXml(form.jatuhTempo) : ''}"
                               <c:if test="${empty form.id and not empty errors.jatuhTempo}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.jatuhTempo}">
                            <p class="field__error"><c:out value="${errors.jatuhTempo}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label class="field__label" for="create-jumlah">Jumlah (Rp)</label>
                    <input class="field__input"
                           id="create-jumlah"
                           name="jumlah"
                           type="number"
                           min="0"
                           step="0.01"
                           placeholder="Kosongkan untuk memakai tarif kamar saat ini"
                           value="${empty form.id ? fn:escapeXml(form.jumlah) : ''}"
                           <c:if test="${empty form.id and not empty errors.jumlah}">aria-invalid="true"</c:if>>
                    <c:if test="${empty form.id and not empty errors.jumlah}">
                        <p class="field__error"><c:out value="${errors.jumlah}"/></p>
                    </c:if>
                </div>

                <div class="field">
                    <label class="field__label" for="create-bukti">URL bukti pembayaran</label>
                    <input class="field__input"
                           id="create-bukti"
                           name="buktiUrl"
                           type="text"
                           maxlength="255"
                           placeholder="Opsional, contoh: https://contoh.test/bukti.jpg"
                           value="${empty form.id ? fn:escapeXml(form.buktiUrl) : ''}"
                           <c:if test="${empty form.id and not empty errors.buktiUrl}">aria-invalid="true"</c:if>>
                    <c:if test="${empty form.id and not empty errors.buktiUrl}">
                        <p class="field__error"><c:out value="${errors.buktiUrl}"/></p>
                    </c:if>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan tagihan</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="pembayaran-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="pembayaran-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="pembayaran-edit-title">Ubah tagihan</h2>
                    <p class="modal__subtitle">Perbarui data tagihan selama belum berstatus lunas.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/pembayaran/update" novalidate>
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
                                    <c:out value="${opsi.nama}"/><c:if test="${not empty opsi.nomorKamar}"> &middot; <c:out value="${opsi.nomorKamar}"/></c:if>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty errors.penghuniId}">
                            <p class="field__error"><c:out value="${errors.penghuniId}"/></p>
                        </c:if>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-periode">Periode</label>
                            <input class="field__input"
                                   id="edit-periode"
                                   name="periode"
                                   type="date"
                                   value="${fn:escapeXml(form.periode)}"
                                   <c:if test="${not empty errors.periode}">aria-invalid="true"</c:if>
                                   required>
                            <p class="field__hint">Gunakan tanggal 1 sebagai awal periode tagihan.</p>
                            <c:if test="${not empty errors.periode}">
                                <p class="field__error"><c:out value="${errors.periode}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-jatuh-tempo">Jatuh tempo</label>
                            <input class="field__input"
                                   id="edit-jatuh-tempo"
                                   name="jatuhTempo"
                                   type="date"
                                   value="${fn:escapeXml(form.jatuhTempo)}"
                                   <c:if test="${not empty errors.jatuhTempo}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.jatuhTempo}">
                                <p class="field__error"><c:out value="${errors.jatuhTempo}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-jumlah">Jumlah (Rp)</label>
                        <input class="field__input"
                               id="edit-jumlah"
                               name="jumlah"
                               type="number"
                               min="0"
                               step="0.01"
                               value="${fn:escapeXml(form.jumlah)}"
                               <c:if test="${not empty errors.jumlah}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${not empty errors.jumlah}">
                            <p class="field__error"><c:out value="${errors.jumlah}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="edit-bukti">URL bukti pembayaran</label>
                        <input class="field__input"
                               id="edit-bukti"
                               name="buktiUrl"
                               type="text"
                               maxlength="255"
                               value="${fn:escapeXml(form.buktiUrl)}"
                               <c:if test="${not empty errors.buktiUrl}">aria-invalid="true"</c:if>>
                        <c:if test="${not empty errors.buktiUrl}">
                            <p class="field__error"><c:out value="${errors.buktiUrl}"/></p>
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

<div class="modal" id="pembayaran-pay-modal" role="dialog" aria-modal="true" aria-labelledby="pembayaran-pay-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="pembayaran-pay-title">Tandai lunas</h2>
                <p class="modal__subtitle">Catat tanggal dan metode pembayaran tagihan ini.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/pembayaran/pay">
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="id" value="">

            <div class="modal__body">
                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="pay-tanggal">Tanggal bayar</label>
                        <input class="field__input"
                               id="pay-tanggal"
                               name="tanggalBayar"
                               type="date"
                               required>
                    </div>

                    <div class="field">
                        <label class="field__label" for="pay-metode">Metode</label>
                        <select class="field__select" id="pay-metode" name="metode" required>
                            <c:forEach var="metode" items="${paymentMethods}">
                                <option value="${metode}"><c:out value="${metode}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Tandai lunas</button>
            </footer>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf" %>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
