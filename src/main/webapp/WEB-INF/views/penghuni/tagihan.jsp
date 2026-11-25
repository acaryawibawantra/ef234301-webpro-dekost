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
                            <a class="chip ${empty status ? 'is-active' : ''}" href="${ctx}/penghuni/tagihan">Semua</a>
                            <a class="chip ${status eq 'BELUM_BAYAR' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/tagihan?status=BELUM_BAYAR">Belum bayar</a>
                            <a class="chip ${status eq 'TERLAMBAT' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/tagihan?status=TERLAMBAT">Terlambat</a>
                            <a class="chip ${status eq 'LUNAS' ? 'is-active' : ''}"
                               href="${ctx}/penghuni/tagihan?status=LUNAS">Lunas</a>
                        </div>
                    </div>

                    <section class="table-card">
                        <header class="table-card__header">
                            <div>
                                <h2 class="table-card__title">Tagihan sewa</h2>
                                <p class="table-card__subtitle">
                                    <c:out value="${totalTagihan}"/> tagihan &middot; belum dibayar
                                    <c:out value="${belumBayarTotalFormatted}"/>
                                </p>
                            </div>
                        </header>

                        <c:choose>
                            <c:when test="${empty pembayaranList}">
                                <div class="empty-state">
                                    <h3 class="empty-state__title">
                                        <c:choose>
                                            <c:when test="${not empty status}">Tidak ada tagihan dengan status ini</c:when>
                                            <c:otherwise>Belum ada tagihan</c:otherwise>
                                        </c:choose>
                                    </h3>
                                    <p>
                                        <c:choose>
                                            <c:when test="${not empty status}">
                                                Reset filter untuk melihat seluruh tagihan Anda.
                                            </c:when>
                                            <c:otherwise>
                                                Tagihan sewa Anda akan tampil di sini setelah diterbitkan pengelola.
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
                                            <tr>
                                                <td>
                                                    <c:out value="${empty paymentPeriods[item.id] ? '-' : paymentPeriods[item.id]}"/>
                                                </td>
                                                <td>
                                                    <c:out value="${empty paymentAmounts[item.id] ? 'Rp0' : paymentAmounts[item.id]}"/>
                                                </td>
                                                <td class="table__cell--muted">
                                                    <c:out value="${empty paymentDueDates[item.id] ? '-' : paymentDueDates[item.id]}"/>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${item.status eq 'LUNAS'}">
                                                            <span class="badge badge--success"><span class="badge__dot"></span>Lunas</span>
                                                        </c:when>
                                                        <c:when test="${item.status eq 'TERLAMBAT'}">
                                                            <span class="badge badge--danger"><span class="badge__dot"></span>Terlambat</span>
                                                        </c:when>
                                                        <c:when test="${item.status eq 'BELUM_BAYAR'}">
                                                            <span class="badge badge--warning"><span class="badge__dot"></span>Belum bayar</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge--neutral"><span class="badge__dot"></span><c:out value="${item.status}"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="table__cell--muted">
                                                    <c:choose>
                                                        <c:when test="${item.status eq 'LUNAS'}">
                                                            <c:out value="${empty item.metode ? '-' : item.metode}"/>
                                                            <c:if test="${not empty item.tanggalBayar}">
                                                                &middot; <c:out value="${paymentPayDates[item.id]}"/>
                                                            </c:if>
                                                        </c:when>
                                                        <c:when test="${not empty item.buktiUrl}">
                                                            Bukti terkirim
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="table__cell--actions">
                                                    <c:if test="${item.status ne 'LUNAS'}">
                                                        <div class="table__actions">
                                                            <button class="button button--secondary button--sm"
                                                                    type="button"
                                                                    aria-label="Kirim bukti pembayaran periode ${fn:escapeXml(paymentPeriods[item.id])}"
                                                                    data-modal-open="pembayaran-bukti-modal"
                                                                    data-modal-action="${ctx}/penghuni/tagihan/bukti"
                                                                    data-modal-field-id="${item.id}"
                                                                    data-modal-field-buktiUrl="${fn:escapeXml(item.buktiUrl)}">
                                                                <c:choose>
                                                                    <c:when test="${not empty item.buktiUrl}">Ubah bukti</c:when>
                                                                    <c:otherwise>Kirim bukti</c:otherwise>
                                                                </c:choose>
                                                            </button>
                                                        </div>
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

<div class="modal" id="pembayaran-bukti-modal" role="dialog" aria-modal="true" aria-labelledby="pembayaran-bukti-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <div>
                <h2 class="modal__title" id="pembayaran-bukti-title">Kirim bukti pembayaran</h2>
                <p class="modal__subtitle">Tautkan URL bukti transfer agar pengelola dapat memverifikasi.</p>
            </div>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>

        <form method="post" action="${ctx}/penghuni/tagihan/bukti" novalidate>
            <input type="hidden" name="_csrf" value="${fn:escapeXml(csrfToken)}">
            <input type="hidden" name="id" value="">

            <div class="modal__body">
                <div class="field">
                    <label class="field__label" for="bukti-url">URL bukti pembayaran</label>
                    <input class="field__input"
                           id="bukti-url"
                           name="buktiUrl"
                           type="text"
                           maxlength="255"
                           placeholder="https://contoh.test/bukti.jpg"
                           required>
                    <p class="field__hint">Contoh: tautan Google Drive atau gambar transfer.</p>
                </div>
            </div>

            <footer class="modal__footer">
                <button class="button button--secondary" type="button" data-modal-close>Batal</button>
                <button class="button button--primary" type="submit">Kirim bukti</button>
            </footer>
        </form>
    </div>
</div>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
