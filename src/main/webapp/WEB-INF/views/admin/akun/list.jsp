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
                        <c:url var="clearSearchUrl" value="/admin/akun">
                            <c:if test="${not empty role}">
                                <c:param name="role" value="${role}"/>
                            </c:if>
                        </c:url>
                        <a class="chip" href="${fn:escapeXml(clearSearchUrl)}">
                            Pencarian: &ldquo;<c:out value="${q}"/>&rdquo; &times;
                        </a>
                    </c:if>

                    <c:url var="allUrl" value="/admin/akun">
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${empty role ? 'is-active' : ''}" href="${fn:escapeXml(allUrl)}">Semua</a>

                    <c:url var="adminUrl" value="/admin/akun">
                        <c:param name="role" value="ADMIN"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${role eq 'ADMIN' ? 'is-active' : ''}" href="${fn:escapeXml(adminUrl)}">Admin</a>

                    <c:url var="penghuniUrl" value="/admin/akun">
                        <c:param name="role" value="PENGHUNI"/>
                        <c:if test="${not empty q}">
                            <c:param name="q" value="${q}"/>
                        </c:if>
                    </c:url>
                    <a class="chip ${role eq 'PENGHUNI' ? 'is-active' : ''}" href="${fn:escapeXml(penghuniUrl)}">Penghuni</a>
                </div>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar akun</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${pagination.totalItems}"/> akun pengguna
                        </p>
                    </div>
                </header>

                <c:choose>
                    <c:when test="${empty accountList}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">
                                <c:choose>
                                    <c:when test="${not empty q or not empty role}">Tidak ada akun yang cocok</c:when>
                                    <c:otherwise>Belum ada akun</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty q or not empty role}">
                                        Ubah kata kunci atau reset filter untuk melihat semua akun.
                                    </c:when>
                                    <c:otherwise>
                                        Tambahkan akun agar pengelola atau penghuni dapat masuk ke aplikasi.
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
                                    <th>Akun</th>
                                    <th>Role</th>
                                    <th>Tertaut ke penghuni</th>
                                    <th>Dibuat</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="item" items="${accountList}">
                                    <c:url var="editUrl" value="/admin/akun">
                                        <c:param name="edit" value="${item.id}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty role}">
                                            <c:param name="role" value="${role}"/>
                                        </c:if>
                                        <c:if test="${pagination.page gt 1}">
                                            <c:param name="page" value="${pagination.page}"/>
                                        </c:if>
                                    </c:url>
                                    <tr>
                                        <td>
                                            <div class="table__person">
                                                <span class="avatar">
                                                    <c:out value="${fn:toUpperCase(fn:substring(item.nama, 0, 1))}"/>
                                                </span>
                                                <span class="table__person-text">
                                                    <span class="table__person-name"><c:out value="${item.nama}"/></span>
                                                    <span class="table__person-sub"><c:out value="${item.email}"/></span>
                                                </span>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${item.role eq 'ADMIN'}">
                                                    <span class="badge badge--accent">Admin</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--success">Penghuni</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:choose>
                                                <c:when test="${not empty item.penghuniNama}">
                                                    <c:out value="${item.penghuniNama}"/>
                                                    <c:if test="${not empty item.nomorKamar}">
                                                        &middot; Kamar <c:out value="${item.nomorKamar}"/>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted">
                                            <c:out value="${empty accountCreatedDates[item.id] ? '-' : accountCreatedDates[item.id]}"/>
                                        </td>
                                        <td class="table__cell--actions">
                                            <div class="table__actions">
                                                <a class="table__action"
                                                   href="${fn:escapeXml(editUrl)}"
                                                   aria-label="Ubah akun ${fn:escapeXml(item.nama)}"
                                                   title="Ubah">
                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                         stroke="currentColor" stroke-width="1.75"
                                                         stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                        <path d="M12 20h9"/>
                                                        <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4Z"/>
                                                    </svg>
                                                </a>
                                                <c:if test="${item.id ne currentUserId}">
                                                    <button class="table__action"
                                                            type="button"
                                                            aria-label="Hapus akun ${fn:escapeXml(item.nama)}"
                                                            title="Hapus"
                                                            data-confirm-open="confirm-delete-modal"
                                                            data-delete-url="${ctx}/admin/akun/delete"
                                                            data-delete-id="${item.id}"
                                                            data-delete-title="Hapus akun"
                                                            data-delete-message="Hapus akun ${fn:escapeXml(item.email)}? Penghuni yang tertaut akan kehilangan akses login.">
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
                                Menampilkan 0 dari 0 akun
                            </c:when>
                            <c:otherwise>
                                Menampilkan <c:out value="${pagination.startItem}"/>&ndash;<c:out value="${pagination.endItem}"/>
                                dari <c:out value="${pagination.totalItems}"/> akun
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <c:if test="${pagination.totalPages gt 1}">
                        <nav class="pager" aria-label="Pagination">
                            <c:choose>
                                <c:when test="${pagination.hasPrevious}">
                                    <c:url var="prevUrl" value="/admin/akun">
                                        <c:param name="page" value="${pagination.page - 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty role}">
                                            <c:param name="role" value="${role}"/>
                                        </c:if>
                                    </c:url>
                                    <a class="pager__link" href="${fn:escapeXml(prevUrl)}" aria-label="Halaman sebelumnya">&lsaquo;</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__link pager__link--disabled" aria-disabled="true">&lsaquo;</span>
                                </c:otherwise>
                            </c:choose>

                            <c:forEach var="pageNum" begin="1" end="${pagination.totalPages}">
                                <c:url var="pageUrl" value="/admin/akun">
                                    <c:param name="page" value="${pageNum}"/>
                                    <c:if test="${not empty q}">
                                        <c:param name="q" value="${q}"/>
                                    </c:if>
                                    <c:if test="${not empty role}">
                                        <c:param name="role" value="${role}"/>
                                    </c:if>
                                </c:url>
                                <a class="pager__link ${pageNum eq pagination.page ? 'pager__link--active' : ''}"
                                   href="${fn:escapeXml(pageUrl)}"><c:out value="${pageNum}"/></a>
                            </c:forEach>

                            <c:choose>
                                <c:when test="${pagination.hasNext}">
                                    <c:url var="nextUrl" value="/admin/akun">
                                        <c:param name="page" value="${pagination.page + 1}"/>
                                        <c:if test="${not empty q}">
                                            <c:param name="q" value="${q}"/>
                                        </c:if>
                                        <c:if test="${not empty role}">
                                            <c:param name="role" value="${role}"/>
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
     id="akun-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="akun-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="akun-create-title">Tambah akun</h2>
                <p class="modal__subtitle">Buat akun admin atau penghuni, lalu tautkan ke data hunian.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/admin/akun/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="q" value="${fn:escapeXml(q)}">
            <input type="hidden" name="returnRole" value="${fn:escapeXml(role)}">
            <input type="hidden" name="page" value="${pagination.page}">

            <div class="modal__body">
                <c:if test="${empty form.id and not empty errors.general}">
                    <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                </c:if>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-nama">Nama akun</label>
                        <input class="field__input"
                               id="create-nama"
                               name="nama"
                               type="text"
                               maxlength="100"
                               placeholder="Contoh: Budi Santoso"
                               value="${empty form.id ? fn:escapeXml(form.nama) : ''}"
                               <c:if test="${empty form.id and not empty errors.nama}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.nama}">
                            <p class="field__error"><c:out value="${errors.nama}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-email">Email</label>
                        <input class="field__input"
                               id="create-email"
                               name="email"
                               type="email"
                               maxlength="120"
                               placeholder="nama@email.com"
                               value="${empty form.id ? fn:escapeXml(form.email) : ''}"
                               <c:if test="${empty form.id and not empty errors.email}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.email}">
                            <p class="field__error"><c:out value="${errors.email}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-role">Role</label>
                        <select class="field__select" id="create-role" name="role">
                            <option value="PENGHUNI" ${empty form.id and (empty form.role or form.role eq 'PENGHUNI') ? 'selected' : ''}>Penghuni</option>
                            <option value="ADMIN" ${empty form.id and form.role eq 'ADMIN' ? 'selected' : ''}>Admin</option>
                        </select>
                        <c:if test="${empty form.id and not empty errors.role}">
                            <p class="field__error"><c:out value="${errors.role}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="create-password">Password</label>
                        <input class="field__input"
                               id="create-password"
                               name="password"
                               type="password"
                               minlength="8"
                               maxlength="72"
                               placeholder="Minimal 8 karakter"
                               autocomplete="new-password"
                               <c:if test="${empty form.id and not empty errors.password}">aria-invalid="true"</c:if>
                               required>
                        <c:if test="${empty form.id and not empty errors.password}">
                            <p class="field__error"><c:out value="${errors.password}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label class="field__label" for="create-penghuni">Tautkan ke penghuni (opsional)</label>
                    <select class="field__select" id="create-penghuni" name="penghuniId"
                            <c:if test="${empty form.id and not empty errors.penghuniId}">aria-invalid="true"</c:if>>
                        <option value="">Tidak ditautkan</option>
                        <c:forEach var="option" items="${penghuniOptions}">
                            <option value="${option.id}" ${empty form.id and selectedPenghuniId eq option.id ? 'selected' : ''}>
                                <c:out value="${option.namaLengkap}"/>
                                <c:if test="${not empty option.nomorKamar}">
                                    &middot; Kamar <c:out value="${option.nomorKamar}"/>
                                </c:if>
                            </option>
                        </c:forEach>
                    </select>
                    <p class="field__hint">
                        Hanya untuk role penghuni. Akun yang tertaut dapat mengakses portal penghuni.
                    </p>
                    <c:if test="${empty form.id and not empty errors.penghuniId}">
                        <p class="field__error"><c:out value="${errors.penghuniId}"/></p>
                    </c:if>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Simpan akun</button>
            </footer>
        </form>
    </div>
</div>

<c:if test="${not empty form.id}">
    <div class="modal is-open"
         id="akun-edit-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="akun-edit-title">
        <div class="modal__dialog">
            <header class="modal__header">
                <div>
                    <h2 class="modal__title" id="akun-edit-title">Ubah akun</h2>
                    <p class="modal__subtitle">Perbarui identitas, role, atau password akun.</p>
                </div>
                <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
            </header>

            <form method="post" action="${ctx}/admin/akun/update" novalidate>
                <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
                <input type="hidden" name="id" value="${fn:escapeXml(form.id)}">
                <input type="hidden" name="q" value="${fn:escapeXml(q)}">
                <input type="hidden" name="returnRole" value="${fn:escapeXml(role)}">
                <input type="hidden" name="page" value="${pagination.page}">

                <div class="modal__body">
                    <c:if test="${not empty errors.general}">
                        <p class="alert alert--error" role="alert"><c:out value="${errors.general}"/></p>
                    </c:if>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-nama">Nama akun</label>
                            <input class="field__input"
                                   id="edit-nama"
                                   name="nama"
                                   type="text"
                                   maxlength="100"
                                   value="${fn:escapeXml(form.nama)}"
                                   <c:if test="${not empty errors.nama}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.nama}">
                                <p class="field__error"><c:out value="${errors.nama}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-email">Email</label>
                            <input class="field__input"
                                   id="edit-email"
                                   name="email"
                                   type="email"
                                   maxlength="120"
                                   value="${fn:escapeXml(form.email)}"
                                   <c:if test="${not empty errors.email}">aria-invalid="true"</c:if>
                                   required>
                            <c:if test="${not empty errors.email}">
                                <p class="field__error"><c:out value="${errors.email}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-grid">
                        <div class="field">
                            <label class="field__label" for="edit-role">Role</label>
                            <select class="field__select" id="edit-role" name="role">
                                <option value="PENGHUNI" ${form.role eq 'PENGHUNI' ? 'selected' : ''}>Penghuni</option>
                                <option value="ADMIN" ${form.role eq 'ADMIN' ? 'selected' : ''}>Admin</option>
                            </select>
                            <c:if test="${not empty errors.role}">
                                <p class="field__error"><c:out value="${errors.role}"/></p>
                            </c:if>
                        </div>

                        <div class="field">
                            <label class="field__label" for="edit-password">Password baru (opsional)</label>
                            <input class="field__input"
                                   id="edit-password"
                                   name="password"
                                   type="password"
                                   minlength="8"
                                   maxlength="72"
                                   placeholder="Kosongkan jika tidak diubah"
                                   autocomplete="new-password"
                                   <c:if test="${not empty errors.password}">aria-invalid="true"</c:if>>
                            <c:if test="${not empty errors.password}">
                                <p class="field__error"><c:out value="${errors.password}"/></p>
                            </c:if>
                        </div>
                    </div>

                    <c:if test="${not empty linkedPenghuniNama}">
                        <p class="field__hint">
                            Tertaut ke penghuni <strong><c:out value="${linkedPenghuniNama}"/></strong>
                            <c:if test="${not empty linkedPenghuniKamar}">
                                (kamar <c:out value="${linkedPenghuniKamar}"/>)
                            </c:if>.
                        </p>
                    </c:if>
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
