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
            <section class="admin-grid admin-grid--stats" aria-label="Ringkasan statistik">
                <article class="stat-card">
                    <p class="stat-card__label">Kamar terisi</p>
                    <p class="stat-card__value">
                        <c:out value="${empty occupiedRooms ? 0 : occupiedRooms}"/> / <c:out value="${empty totalRooms ? 0 : totalRooms}"/>
                    </p>
                    <p class="stat-card__hint">
                        <c:out value="${empty availableRooms ? 0 : availableRooms}"/> kamar masih tersedia
                    </p>
                </article>

                <article class="stat-card">
                    <p class="stat-card__label">
                        Pendapatan <c:out value="${empty currentMonthName ? 'bulan ini' : currentMonthName}"/>
                    </p>
                    <p class="stat-card__value">
                        <c:out value="${empty monthlyRevenueCompact ? 'Rp0' : monthlyRevenueCompact}"/>
                    </p>
                    <p class="stat-card__hint">
                        <c:out value="${empty currentMonthBilling ? 0 : currentMonthBilling.lunasCount}"/> dari
                        <c:out value="${empty currentMonthBilling ? 0 : currentMonthBilling.totalCount}"/> tagihan sudah lunas
                    </p>
                </article>

                <article class="stat-card stat-card--dark">
                    <p class="stat-card__label">Tagihan menunggak</p>
                    <p class="stat-card__value">
                        <c:out value="${empty overdueBilling ? 0 : overdueBilling.jumlahTagihan}"/>
                    </p>
                    <p class="stat-card__hint">
                        Total <c:out value="${empty overdueTotalCompact ? 'Rp0' : overdueTotalCompact}"/> belum masuk
                    </p>
                </article>

                <article class="stat-card">
                    <p class="stat-card__label">Keluhan terbuka</p>
                    <p class="stat-card__value">
                        <c:out value="${empty openComplaints ? 0 : openComplaints.jumlahTerbuka}"/>
                    </p>
                    <p class="stat-card__hint">
                        <c:out value="${empty openComplaints ? 0 : openComplaints.prioritasTinggi}"/> berprioritas tinggi
                    </p>
                </article>
            </section>

            <section class="card card--light card--compact" aria-label="Aksi cepat">
                <header class="panel-head">
                    <h2 class="panel-head__title">Aksi cepat</h2>
                </header>

                <div class="toolbar__group">
                    <a class="button button--primary" href="${ctx}/admin/penghuni#penghuni-create-modal">Tambah penghuni</a>
                    <a class="button button--secondary" href="${ctx}/admin/kamar#kamar-create-modal">Tambah kamar</a>
                    <a class="button button--secondary" href="${ctx}/admin/tipe-kamar#tipe-kamar-create-modal">Tambah tipe kamar</a>
                    <a class="button button--secondary" href="${ctx}/admin/pembayaran#pembayaran-create-modal">Catat pembayaran</a>
                    <a class="button button--secondary" href="${ctx}/admin/keluhan#keluhan-create-modal">Catat keluhan</a>
                    <a class="button button--secondary" href="${ctx}/admin/akun#akun-create-modal">Tambah akun</a>
                </div>
            </section>

            <section class="table-card" aria-label="Ketersediaan kamar">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Ketersediaan kamar</h2>
                        <p class="table-card__subtitle">
                            <c:out value="${empty availableRooms ? 0 : availableRooms}"/> dari
                            <c:out value="${empty totalRooms ? 0 : totalRooms}"/> kamar siap dihuni saat ini
                        </p>
                    </div>
                    <a class="panel-head__link" href="${ctx}/admin/kamar?status=TERSEDIA">Kelola kamar</a>
                </header>

                <c:choose>
                    <c:when test="${empty typeAvailability}">
                        <div class="empty-state">
                            <h3 class="empty-state__title">Belum ada tipe kamar</h3>
                            <p>
                                Tambahkan tipe kamar lebih dulu, lalu daftarkan kamar agar ketersediaan
                                hunian dapat dipantau dari sini.
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th>Tipe kamar</th>
                                    <th>Harga / bulan</th>
                                    <th>Tersedia</th>
                                    <th>Total kamar</th>
                                    <th>Okupansi</th>
                                    <th class="table__cell--actions">Aksi</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="type" items="${typeAvailability}">
                                    <c:url var="filteredRoomsUrl" value="/admin/kamar">
                                        <c:param name="status" value="TERSEDIA"/>
                                        <c:param name="q" value="${type.nama}"/>
                                    </c:url>
                                    <tr>
                                        <td><c:out value="${type.nama}"/></td>
                                        <td>
                                            <c:out value="${empty typeAvailabilityPrices[type.tipeKamarId] ? 'Rp0' : typeAvailabilityPrices[type.tipeKamarId]}"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${type.kamarTersedia gt 0}">
                                                    <span class="badge badge--success">
                                                        <span class="badge__dot"></span><c:out value="${type.kamarTersedia}"/> tersedia
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral">
                                                        <span class="badge__dot"></span>Tidak tersedia
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="table__cell--muted"><c:out value="${type.totalKamar}"/> kamar</td>
                                        <td>
                                            <div class="occupancy-item">
                                                <div class="occupancy-item__meta">
                                                    <span class="occupancy-item__count">
                                                        <c:out value="${type.kamarTerisi}"/> / <c:out value="${type.totalKamar}"/> terisi
                                                    </span>
                                                </div>
                                                <progress class="occupancy-progress"
                                                          value="${type.kamarTerisi}"
                                                          max="${type.totalKamar gt 0 ? type.totalKamar : 1}"
                                                          aria-label="Okupansi ${fn:escapeXml(type.nama)}"></progress>
                                            </div>
                                        </td>
                                        <td class="table__cell--actions">
                                            <a class="table__action"
                                               href="${fn:escapeXml(filteredRoomsUrl)}"
                                               aria-label="Lihat kamar tersedia tipe ${fn:escapeXml(type.nama)}"
                                               title="Lihat kamar">
                                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                                                     stroke="currentColor" stroke-width="1.75"
                                                     stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                                    <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"/>
                                                    <circle cx="12" cy="12" r="3"/>
                                                </svg>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="admin-grid admin-grid--content" aria-label="Detail operasional">
                <section class="table-card">
                    <header class="table-card__header">
                        <h2 class="table-card__title">Tagihan jatuh tempo</h2>
                        <a class="panel-head__link" href="${ctx}/admin/pembayaran">Lihat semua</a>
                    </header>

                    <c:choose>
                        <c:when test="${empty duePayments}">
                            <div class="empty-state">
                                <h3 class="empty-state__title">Tidak ada tagihan jatuh tempo</h3>
                                <p>Semua tagihan penghuni saat ini sudah lunas atau belum ada tagihan aktif.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th>Penghuni</th>
                                        <th>Kamar</th>
                                        <th>Jatuh tempo</th>
                                        <th>Jumlah</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="payment" items="${duePayments}">
                                        <tr>
                                            <td>
                                                <div class="table__person">
                                                    <span class="avatar" aria-hidden="true">
                                                        <c:out value="${empty paymentInitials[payment.id] ? '?' : paymentInitials[payment.id]}"/>
                                                    </span>
                                                    <div class="table__person-text">
                                                        <span class="table__person-name">
                                                            <c:out value="${payment.penghuniNama}"/>
                                                        </span>
                                                        <span class="table__person-sub">
                                                            <c:out value="${empty payment.penghuniNoHp ? '-' : payment.penghuniNoHp}"/>
                                                        </span>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="table__cell--muted">
                                                <c:out value="${empty payment.nomorKamar ? '-' : payment.nomorKamar}"/>
                                            </td>
                                            <td class="table__cell--muted">
                                                <c:out value="${empty paymentDueDates[payment.id] ? '-' : paymentDueDates[payment.id]}"/>
                                            </td>
                                            <td>
                                                <c:out value="${empty paymentAmounts[payment.id] ? 'Rp0' : paymentAmounts[payment.id]}"/>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${payment.status eq 'TERLAMBAT'}">
                                                        <span class="badge badge--danger"><span class="badge__dot"></span>Terlambat</span>
                                                    </c:when>
                                                    <c:when test="${payment.status eq 'LUNAS'}">
                                                        <span class="badge badge--success"><span class="badge__dot"></span>Lunas</span>
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
                            <h2 class="panel-head__title">Okupansi per lantai</h2>
                        </header>

                        <c:choose>
                            <c:when test="${empty floorOccupancy}">
                                <div class="empty-state">
                                    <h3 class="empty-state__title">Belum ada data kamar</h3>
                                    <p>Tambahkan kamar terlebih dahulu untuk memantau okupansi per lantai.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="occupancy-list">
                                    <c:forEach var="floor" items="${floorOccupancy}">
                                        <div class="occupancy-item">
                                            <div class="occupancy-item__meta">
                                                <span class="occupancy-item__label">
                                                    Lantai <c:out value="${floor.lantai}"/>
                                                </span>
                                                <span class="occupancy-item__count">
                                                    <c:out value="${floor.kamarTerisi}"/> / <c:out value="${floor.totalKamar}"/> terisi
                                                </span>
                                            </div>
                                            <progress class="occupancy-progress"
                                                      value="${floor.kamarTerisi}"
                                                      max="${floor.totalKamar gt 0 ? floor.totalKamar : 1}"
                                                      aria-label="Okupansi Lantai ${fn:escapeXml(floor.lantai)}"></progress>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="card card--outline card--compact">
                        <header class="panel-head">
                            <h2 class="panel-head__title">Keluhan terbaru</h2>
                            <a class="panel-head__link" href="${ctx}/admin/keluhan">Lihat semua</a>
                        </header>

                        <c:choose>
                            <c:when test="${empty recentComplaints}">
                                <div class="empty-state">
                                    <h3 class="empty-state__title">Belum ada keluhan</h3>
                                    <p>Keluhan penghuni yang masuk akan tampil di sini.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <ul class="complaint-list">
                                    <c:forEach var="complaint" items="${recentComplaints}">
                                        <li class="complaint-item">
                                            <div class="complaint-item__text">
                                                <p class="complaint-item__title"><c:out value="${complaint.judul}"/></p>
                                                <p class="complaint-item__meta">
                                                    <c:if test="${not empty complaint.nomorKamar}">
                                                        Kamar <c:out value="${complaint.nomorKamar}"/> &middot;
                                                    </c:if>
                                                    <c:out value="${complaint.penghuniNama}"/>
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
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<script src="${ctx}/assets/js/app.js" defer></script>
</body>
</html>
