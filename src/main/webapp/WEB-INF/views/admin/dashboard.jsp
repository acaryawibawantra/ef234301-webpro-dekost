<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
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
                        <c:out value="${empty availableRooms ? 0 : availableRooms}"/> tersedia &middot;
                        <c:out value="${empty maintenanceRooms ? 0 : maintenanceRooms}"/> perbaikan
                    </p>
                </article>

                <article class="stat-card">
                    <p class="stat-card__label">Pendapatan bulan ini</p>
                    <p class="stat-card__value">
                        <c:out value="${empty monthlyRevenueFormatted ? 'Rp0' : monthlyRevenueFormatted}"/>
                    </p>
                    <p class="stat-card__hint">Tagihan lunas bulan berjalan</p>
                </article>

                <article class="stat-card stat-card--dark">
                    <p class="stat-card__label">Tagihan menunggak</p>
                    <p class="stat-card__value">
                        <c:out value="${empty overdueBilling ? 0 : overdueBilling.jumlahTagihan}"/>
                    </p>
                    <p class="stat-card__hint">
                        Total <c:out value="${empty overdueTotalFormatted ? 'Rp0' : overdueTotalFormatted}"/>
                    </p>
                </article>

                <article class="stat-card">
                    <p class="stat-card__label">Keluhan terbuka</p>
                    <p class="stat-card__value">
                        <c:out value="${empty openComplaints ? 0 : openComplaints.jumlahTerbuka}"/>
                    </p>
                    <p class="stat-card__hint">
                        <c:out value="${empty openComplaints ? 0 : openComplaints.prioritasTinggi}"/> prioritas tinggi
                    </p>
                </article>
            </section>

            <section class="admin-grid admin-grid--content" aria-label="Detail operasional">
                <section class="table-card">
                    <header class="table-card__header">
                        <div>
                            <h2 class="table-card__title">Tagihan jatuh tempo</h2>
                            <p class="table-card__subtitle">Maksimal 5 tagihan belum lunas terdekat</p>
                        </div>
                        <a class="button button--ghost button--sm" href="${ctx}/admin/pembayaran">Lihat semua</a>
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
                                        <th>Nominal</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="payment" items="${duePayments}">
                                        <tr>
                                            <td><c:out value="${payment.penghuniNama}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty payment.nomorKamar}">
                                                        <c:out value="${payment.nomorKamar}"/>
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><c:out value="${paymentDueDates[payment.id]}"/></td>
                                            <td><c:out value="${paymentAmounts[payment.id]}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${payment.status eq 'TERLAMBAT'}">
                                                        <span class="badge badge--danger"><span class="badge__dot"></span>Terlambat</span>
                                                    </c:when>
                                                    <c:when test="${payment.status eq 'LUNAS'}">
                                                        <span class="badge badge--success"><span class="badge__dot"></span>Lunas</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge--warning"><span class="badge__dot"></span>Belum bayar</span>
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
                    <section class="card card--outline card--compact">
                        <header class="panel-head">
                            <h2 class="panel-head__title">Okupansi per lantai</h2>
                            <a class="panel-head__link" href="${ctx}/admin/kamar">Kelola</a>
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
                                                <span>Lantai <c:out value="${floor.lantai}"/></span>
                                                <span class="occupancy-item__count">
                                                    <c:out value="${floor.kamarTerisi}"/> / <c:out value="${floor.totalKamar}"/>
                                                </span>
                                            </div>
                                            <progress class="occupancy-progress"
                                                      value="${floor.kamarTerisi}"
                                                      max="${floor.totalKamar gt 0 ? floor.totalKamar : 1}"
                                                      aria-label="Okupansi Lantai ${floor.lantai}"></progress>
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
                                            <div class="complaint-item__top">
                                                <p class="complaint-item__title"><c:out value="${complaint.judul}"/></p>
                                                <c:choose>
                                                    <c:when test="${complaint.status eq 'SELESAI'}">
                                                        <span class="badge badge--success"><span class="badge__dot"></span>Selesai</span>
                                                    </c:when>
                                                    <c:when test="${complaint.status eq 'DIPROSES'}">
                                                        <span class="badge badge--warning"><span class="badge__dot"></span>Diproses</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge--danger"><span class="badge__dot"></span>Baru</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="complaint-item__badges">
                                                <c:choose>
                                                    <c:when test="${complaint.prioritas eq 'TINGGI'}">
                                                        <span class="badge badge--danger"><span class="badge__dot"></span>Tinggi</span>
                                                    </c:when>
                                                    <c:when test="${complaint.prioritas eq 'SEDANG'}">
                                                        <span class="badge badge--warning"><span class="badge__dot"></span>Sedang</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge--neutral"><span class="badge__dot"></span>Rendah</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <p class="complaint-item__meta">
                                                <c:out value="${complaint.penghuniNama}"/> &middot;
                                                <c:out value="${complaintDates[complaint.id]}"/>
                                            </p>
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
