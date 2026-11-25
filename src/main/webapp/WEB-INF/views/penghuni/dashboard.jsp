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
                    <section class="admin-grid admin-grid--stats" aria-label="Ringkasan hunian">
                        <article class="stat-card">
                            <p class="stat-card__label">Tagihan belum dibayar</p>
                            <p class="stat-card__value">
                                <c:out value="${summary.belumBayarCount}"/>
                            </p>
                            <p class="stat-card__hint">
                                Total <c:out value="${belumBayarTotalFormatted}"/>
                            </p>
                        </article>

                        <article class="stat-card">
                            <p class="stat-card__label">Terlambat</p>
                            <p class="stat-card__value">
                                <c:out value="${summary.terlambatCount}"/>
                            </p>
                            <p class="stat-card__hint">Segera selesaikan pembayaran</p>
                        </article>

                        <article class="stat-card">
                            <p class="stat-card__label">Tagihan lunas</p>
                            <p class="stat-card__value">
                                <c:out value="${summary.lunasCount}"/>
                            </p>
                            <p class="stat-card__hint">Terima kasih atas ketertiban Anda</p>
                        </article>

                        <article class="stat-card stat-card--dark">
                            <p class="stat-card__label">Keluhan terbuka</p>
                            <p class="stat-card__value">
                                <c:out value="${summary.keluhanTerbukaCount}"/>
                            </p>
                            <p class="stat-card__hint">
                                <c:out value="${summary.keluhanTotalCount}"/> keluhan total
                            </p>
                        </article>
                    </section>

                    <section class="admin-grid admin-grid--content" aria-label="Detail hunian">
                        <section class="table-card">
                            <header class="table-card__header">
                                <h2 class="table-card__title">Tagihan terbaru</h2>
                                <a class="panel-head__link" href="${ctx}/penghuni/tagihan">Lihat semua</a>
                            </header>

                            <c:choose>
                                <c:when test="${empty recentPayments}">
                                    <div class="empty-state">
                                        <h3 class="empty-state__title">Belum ada tagihan</h3>
                                        <p>Tagihan sewa Anda akan tampil di sini setelah diterbitkan pengelola.</p>
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
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach var="payment" items="${recentPayments}">
                                                <tr>
                                                    <td>
                                                        <c:out value="${empty paymentPeriods[payment.id] ? '-' : paymentPeriods[payment.id]}"/>
                                                    </td>
                                                    <td>
                                                        <c:out value="${empty paymentAmounts[payment.id] ? 'Rp0' : paymentAmounts[payment.id]}"/>
                                                    </td>
                                                    <td class="table__cell--muted">
                                                        <c:out value="${empty paymentDueDates[payment.id] ? '-' : paymentDueDates[payment.id]}"/>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${payment.status eq 'LUNAS'}">
                                                                <span class="badge badge--success"><span class="badge__dot"></span>Lunas</span>
                                                            </c:when>
                                                            <c:when test="${payment.status eq 'TERLAMBAT'}">
                                                                <span class="badge badge--danger"><span class="badge__dot"></span>Terlambat</span>
                                                            </c:when>
                                                            <c:when test="${payment.status eq 'BELUM_BAYAR'}">
                                                                <span class="badge badge--warning"><span class="badge__dot"></span>Belum bayar</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge--neutral"><span class="badge__dot"></span><c:out value="${payment.status}"/></span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <div class="admin-stack">
                            <section class="card card--light card--compact">
                                <header class="panel-head">
                                    <h2 class="panel-head__title">Kamar Anda</h2>
                                </header>

                                <ul class="detail-list">
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Nomor kamar</span>
                                        <span class="detail-list__value">
                                            <c:out value="${empty profile.nomorKamar ? '-' : profile.nomorKamar}"/>
                                        </span>
                                    </li>
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Tipe kamar</span>
                                        <span class="detail-list__value">
                                            <c:out value="${empty profile.tipeKamarNama ? '-' : profile.tipeKamarNama}"/>
                                        </span>
                                    </li>
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Lantai</span>
                                        <span class="detail-list__value">
                                            <c:out value="${empty profile.lantai ? '-' : profile.lantai}"/>
                                        </span>
                                    </li>
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Sewa bulanan</span>
                                        <span class="detail-list__value"><c:out value="${hargaBulananFormatted}"/></span>
                                    </li>
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Tanggal masuk</span>
                                        <span class="detail-list__value"><c:out value="${tanggalMasukFormatted}"/></span>
                                    </li>
                                    <li class="detail-list__item">
                                        <span class="detail-list__label">Status</span>
                                        <span class="detail-list__value">
                                            <c:choose>
                                                <c:when test="${profile.status eq 'AKTIF'}">
                                                    <span class="badge badge--success"><span class="badge__dot"></span>Aktif</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral"><span class="badge__dot"></span><c:out value="${profile.status}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </li>
                                </ul>
                            </section>

                            <section class="card card--outline card--compact">
                                <header class="panel-head">
                                    <h2 class="panel-head__title">Keluhan terbaru</h2>
                                    <a class="panel-head__link" href="${ctx}/penghuni/keluhan">Lihat semua</a>
                                </header>

                                <c:choose>
                                    <c:when test="${empty recentComplaints}">
                                        <div class="empty-state">
                                            <h3 class="empty-state__title">Belum ada keluhan</h3>
                                            <p>Keluhan yang Anda kirim akan tampil di sini.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <ul class="complaint-list">
                                            <c:forEach var="complaint" items="${recentComplaints}">
                                                <li class="complaint-item">
                                                    <div class="complaint-item__text">
                                                        <p class="complaint-item__title"><c:out value="${complaint.judul}"/></p>
                                                        <p class="complaint-item__meta">
                                                            <c:out value="${empty complaintDates[complaint.id] ? '-' : complaintDates[complaint.id]}"/>
                                                        </p>
                                                    </div>

                                                    <c:choose>
                                                        <c:when test="${complaint.status eq 'SELESAI'}">
                                                            <span class="badge badge--success"><span class="badge__dot"></span>Selesai</span>
                                                        </c:when>
                                                        <c:when test="${complaint.status eq 'DIPROSES'}">
                                                            <span class="badge badge--warning"><span class="badge__dot"></span>Diproses</span>
                                                        </c:when>
                                                        <c:when test="${complaint.status eq 'BARU'}">
                                                            <span class="badge badge--danger"><span class="badge__dot"></span>Baru</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge--neutral"><span class="badge__dot"></span><c:out value="${complaint.status}"/></span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </c:otherwise>
                                </c:choose>
                            </section>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
