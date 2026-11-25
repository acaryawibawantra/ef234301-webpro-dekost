<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<body class="admin-body">
<div class="admin-layout">
    <%@ include file="/WEB-INF/views/layout/resident-sidebar.jspf" %>

    <div class="admin-main">
        <%@ include file="/WEB-INF/views/layout/topbar.jspf" %>

        <%@ include file="/WEB-INF/views/admin/fragments/toast.jspf" %>

        <main class="admin-content">
            <c:choose>
                <c:when test="${profileMissing}">
                    <div class="empty-state">
                        <h3 class="empty-state__title">Akun belum terhubung ke data penghuni</h3>
                        <p>Hubungi pengelola kos untuk menautkan akun Anda dengan data hunian.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="toolbar">
                        <div class="toolbar__group">
                            <a class="chip ${empty status ? 'is-active' : ''}" href="${ctx}/penghuni/keluhan">Semua</a>
                            <a class="chip ${status eq 'BARU' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/keluhan?status=BARU">Baru</a>
                            <a class="chip ${status eq 'DIPROSES' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/keluhan?status=DIPROSES">Diproses</a>
                            <a class="chip ${status eq 'SELESAI' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/keluhan?status=SELESAI">Selesai</a>
                        </div>
                    </div>

                    <section class="table-card">
                        <header class="table-card__header">
                            <div>
                                <h2 class="table-card__title">Daftar keluhan</h2>
                                <p class="table-card__subtitle"><c:out value="${totalKeluhan}"/> keluhan</p>
                            </div>
                        </header>

                        <c:choose>
                            <c:when test="${empty keluhanList}">
                                <div class="empty-state">
                                    <h3 class="empty-state__title">
                                        <c:choose>
                                            <c:when test="${not empty status}">Tidak ada keluhan dengan status ini</c:when>
                                            <c:otherwise>Belum ada keluhan</c:otherwise>
                                        </c:choose>
                                    </h3>
                                    <p>
                                        <c:choose>
                                            <c:when test="${not empty status}">
                                                Reset filter untuk melihat seluruh keluhan Anda.
                                            </c:when>
                                            <c:otherwise>
                                                Sampaikan keluhan terkait fasilitas atau kebersihan kamar Anda.
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
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="item" items="${keluhanList}">
                                            <tr>
                                                <td>
                                                    <div class="table__person">
                                                        <span class="table__person-text">
                                                            <span class="table__person-name"><c:out value="${item.judul}"/></span>
                                                            <span class="table__person-sub"><c:out value="${item.deskripsi}"/></span>
                                                        </span>
                                                    </div>
                                                    <c:if test="${not empty item.tanggapan}">
                                                        <p class="table__person-sub">
                                                            Tanggapan<c:if test="${not empty item.penanganNama}">
                                                                (<c:out value="${item.penanganNama}"/>)</c:if>:
                                                            <c:out value="${item.tanggapan}"/>
                                                        </p>
                                                    </c:if>
                                                </td>
                                                <td class="table__cell--muted">
                                                    <c:choose>
                                                        <c:when test="${item.kategori eq 'FASILITAS'}">Fasilitas</c:when>
                                                        <c:when test="${item.kategori eq 'KEBERSIHAN'}">Kebersihan</c:when>
                                                        <c:when test="${item.kategori eq 'KEBISINGAN'}">Kebisingan</c:when>
                                                        <c:when test="${item.kategori eq 'LAINNYA'}">Lainnya</c:when>
                                                        <c:otherwise><c:out value="${item.kategori}"/></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${item.prioritas eq 'TINGGI'}">
                                                            <span class="badge badge--danger"><span class="badge__dot"></span>Tinggi</span>
                                                        </c:when>
                                                        <c:when test="${item.prioritas eq 'SEDANG'}">
                                                            <span class="badge badge--warning"><span class="badge__dot"></span>Sedang</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge--neutral"><span class="badge__dot"></span>Rendah</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${item.status eq 'SELESAI'}">
                                                            <span class="badge badge--success"><span class="badge__dot"></span>Selesai</span>
                                                        </c:when>
                                                        <c:when test="${item.status eq 'DIPROSES'}">
                                                            <span class="badge badge--warning"><span class="badge__dot"></span>Diproses</span>
                                                        </c:when>
                                                        <c:when test="${item.status eq 'BARU'}">
                                                            <span class="badge badge--danger"><span class="badge__dot"></span>Baru</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge--neutral"><span class="badge__dot"></span><c:out value="${item.status}"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="table__cell--muted">
                                                    <c:out value="${empty complaintDates[item.id] ? '-' : complaintDates[item.id]}"/>
                                                    <c:if test="${item.status eq 'SELESAI' and not empty complaintResolvedDates[item.id]}">
                                                        <br>Selesai: <c:out value="${complaintResolvedDates[item.id]}"/>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:otherwise>
            </c:choose>
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<div class="modal ${not empty errors ? 'is-open' : ''}"
     id="keluhan-create-modal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="keluhan-create-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="keluhan-create-title">Buat keluhan</h2>
                <p class="modal__subtitle">Sampaikan kendala kamar atau fasilitas kepada pengelola.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/penghuni/keluhan/create" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="status" value="${fn:escapeXml(status)}">

            <div class="modal__body">
                <div class="field">
                    <label class="field__label" for="create-judul">Judul keluhan</label>
                    <input class="field__input"
                           id="create-judul"
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
                    <label class="field__label" for="create-deskripsi">Deskripsi</label>
                    <textarea class="field__textarea"
                              id="create-deskripsi"
                              name="deskripsi"
                              rows="4"
                              <c:if test="${not empty errors.deskripsi}">aria-invalid="true"</c:if>
                              required><c:out value="${form.deskripsi}"/></textarea>
                    <c:if test="${not empty errors.deskripsi}">
                        <p class="field__error"><c:out value="${errors.deskripsi}"/></p>
                    </c:if>
                </div>

                <div class="form-grid">
                    <div class="field">
                        <label class="field__label" for="create-kategori">Kategori</label>
                        <select class="field__select"
                                id="create-kategori"
                                name="kategori"
                                <c:if test="${not empty errors.kategori}">aria-invalid="true"</c:if>>
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
                        <label class="field__label" for="create-prioritas">Prioritas</label>
                        <select class="field__select"
                                id="create-prioritas"
                                name="prioritas"
                                <c:if test="${not empty errors.prioritas}">aria-invalid="true"</c:if>>
                            <c:forEach var="prioritas" items="${priorities}">
                                <option value="${prioritas}" ${form.prioritas eq prioritas ? 'selected' : ''}>
                                    <c:choose>
                                        <c:when test="${prioritas eq 'TINGGI'}">Tinggi</c:when>
                                        <c:when test="${prioritas eq 'SEDANG'}">Sedang</c:when>
                                        <c:otherwise>Rendah</c:otherwise>
                                    </c:choose>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty errors.prioritas}">
                            <p class="field__error"><c:out value="${errors.prioritas}"/></p>
                        </c:if>
                    </div>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Kirim keluhan</button>
            </footer>
        </form>
    </div>
</div>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
