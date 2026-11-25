<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="landing">
    <header class="site-header">
        <a class="site-header__brand" href="${pageContext.request.contextPath}/">
            <span class="site-header__name">De&rsquo; Kost</span>
        </a>

        <nav class="site-header__nav" aria-label="Navigasi utama">
            <a class="site-header__link site-header__link--active" href="#tentang">Tentang</a>
            <a class="site-header__link" href="#tipe-kamar">Tipe kamar</a>
            <a class="site-header__link" href="#fasilitas">Fasilitas</a>
            <a class="site-header__link" href="#lokasi">Lokasi</a>
            <a class="site-header__link" href="#kontak">Kontak</a>
        </nav>

        <a class="button button--primary" href="${pageContext.request.contextPath}/login">Masuk</a>
    </header>

    <main class="landing__main">
        <section class="hero" aria-labelledby="hero-title">
            <div class="hero__photo" role="img" aria-label="Eksterior kos De' Kost"></div>
            <div class="hero__overlay" aria-hidden="true"></div>

            <div class="hero__content">
                <h1 class="hero__title" id="hero-title">Hunian kos yang tenang, tertata, dan terawat.</h1>
                <p class="hero__subtitle">
                    Kamar bersih, lingkungan aman, dan pengelolaan transparan.
                    Cek kamar yang tersedia dan pesan langsung dari satu halaman.
                </p>
                <a class="button button--accent" href="#tipe-kamar">Lihat kamar kosong</a>
            </div>

            <p class="hero__occupancy">
                <c:out value="${empty totalRooms ? 0 : totalRooms}"/> kamar &middot;
                <c:out value="${empty availableRooms ? 0 : availableRooms}"/> masih kosong
            </p>

            <nav class="hero__social" aria-label="Tautan sosial De' Kost">
                <a href="#" rel="noopener">Instagram</a>
                <a href="#" rel="noopener">WhatsApp</a>
                <a href="#" rel="noopener">Maps</a>
            </nav>
        </section>

        <section class="about" id="tentang">
            <p class="about__text">
                Kos untuk mahasiswa dan pekerja muda. Kamar bersih, aman, dan dikelola dengan tertata sejak 2021.
            </p>
        </section>

        <section class="highlights" id="fasilitas" aria-labelledby="highlights-title">
            <h2 class="visually-hidden" id="highlights-title">Fasilitas De&rsquo; Kost</h2>

            <article class="highlight-card highlight-card--dark">
                <p class="highlight-card__text">
                    Akses 24 jam dengan CCTV dan kunci pribadi, listrik token terpisah, serta
                    WiFi cepat di setiap lantai. Nyaman untuk istirahat dan belajar.
                </p>
                <div class="highlight-card__badges">
                    <span class="badge badge--accent">CCTV 24 jam</span>
                    <span class="badge badge--neutral">WiFi</span>
                </div>
            </article>

            <article class="highlight-card highlight-card--photo">
                <p class="highlight-card__label">Lorong terang dan sirkulasi udara baik</p>
            </article>

            <article class="highlight-card highlight-card--stats">
                <p class="highlight-card__stat"><c:out value="${empty totalRooms ? 0 : totalRooms}"/></p>
                <p class="highlight-card__stat-label">Kamar siap huni</p>
                <p class="highlight-card__stat-note">
                    <c:out value="${empty typeAvailability ? 0 : fn:length(typeAvailability)}"/>
                    tipe kamar untuk setiap kebutuhan dan budget.
                </p>

                <div class="highlight-card__spacer"></div>

                <c:forEach var="type" items="${typeAvailability}">
                    <div class="room-row">
                        <span class="room-row__name"><c:out value="${type.nama}"/></span>
                        <span class="room-row__count"><c:out value="${type.totalKamar}"/> kamar</span>
                    </div>
                </c:forEach>
            </article>
        </section>

        <section class="numbers" aria-label="De' Kost dalam angka">
            <p class="numbers__title">Beberapa fakta tentang kami dalam angka</p>

            <div class="numbers__row">
                <div class="stat">
                    <p class="stat__value"><c:out value="${empty availableRooms ? 0 : availableRooms}"/></p>
                    <p class="stat__label">Kamar tersedia</p>
                </div>
                <div class="stat">
                    <p class="stat__value"><c:out value="${empty occupancyPercent ? 0 : occupancyPercent}"/>%</p>
                    <p class="stat__label">Tingkat hunian</p>
                </div>
                <div class="stat">
                    <p class="stat__value">24 jam</p>
                    <p class="stat__label">Akses dan keamanan</p>
                </div>
                <div class="stat">
                    <p class="stat__value"><c:out value="${empty startingPriceCompact ? 'Rp0' : startingPriceCompact}"/></p>
                    <p class="stat__label">Mulai dari per bulan</p>
                </div>
            </div>
        </section>

        <section class="rooms" id="tipe-kamar" aria-labelledby="rooms-title">
            <div class="section-head">
                <div class="section-head__aside">
                    <span class="section-pill">Tipe kamar</span>
                </div>
                <div class="section-head__body">
                    <h2 class="section-head__title" id="rooms-title">
                        Pilih kamar sesuai kebutuhan, dari yang sederhana sampai lengkap dengan
                        AC dan kamar mandi dalam.
                    </h2>
                    <a class="button button--secondary" href="#tipe-kamar">Lihat semua kamar</a>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty typeAvailability}">
                    <div class="empty-state">
                        <h3 class="empty-state__title">Belum ada tipe kamar</h3>
                        <p>Tipe kamar dan ketersediaannya akan tampil di sini setelah pengelola menambahkannya.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="room-grid">
                        <c:forEach var="type" items="${typeAvailability}">
                            <c:set var="typeName" value="${fn:toLowerCase(type.nama)}"/>
                            <article class="room-card">
                                <c:choose>
                                    <c:when test="${fn:contains(typeName, 'premium')}">
                                        <div class="room-card__photo room-card__photo--premium"
                                             role="img" aria-label="Kamar tipe ${fn:escapeXml(type.nama)}"></div>
                                    </c:when>
                                    <c:when test="${fn:contains(typeName, 'deluxe')}">
                                        <div class="room-card__photo room-card__photo--deluxe"
                                             role="img" aria-label="Kamar tipe ${fn:escapeXml(type.nama)}"></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="room-card__photo room-card__photo--standar"
                                             role="img" aria-label="Kamar tipe ${fn:escapeXml(type.nama)}"></div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="room-card__head">
                                    <h3 class="room-card__name"><c:out value="${type.nama}"/></h3>
                                    <c:choose>
                                        <c:when test="${type.kamarTersedia gt 0}">
                                            <span class="badge badge--success">
                                                <c:out value="${type.kamarTersedia}"/> tersedia
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge--neutral">Penuh</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <p class="room-card__price">
                                    <c:out value="${empty typeAvailabilityPrices[type.tipeKamarId] ? 'Rp0' : typeAvailabilityPrices[type.tipeKamarId]}"/>
                                    <span>/ bulan</span>
                                </p>
                                <p class="room-card__facilities">
                                    <c:out value="${type.luasM2}"/> m&sup2;
                                    <c:if test="${not empty type.fasilitas}">
                                        &middot; <c:out value="${type.fasilitas}"/>
                                    </c:if>
                                </p>
                                <a class="button button--secondary room-card__cta" href="#kontak">
                                    <c:choose>
                                        <c:when test="${type.kamarTersedia gt 0}">Pesan kamar</c:when>
                                        <c:otherwise>Hubungi pengelola</c:otherwise>
                                    </c:choose>
                                </a>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="closing" id="kontak" aria-labelledby="closing-title">
            <div class="closing__cta">
                <div class="closing__intro">
                    <p class="closing__eyebrow">Kontak</p>
                    <h2 class="closing__title" id="closing-title">Siap pindah minggu ini?</h2>
                    <p class="closing__text">
                        Hubungi pengelola untuk survei kamar. Kami balas di hari yang sama.
                    </p>
                    <a class="button button--accent"
                       href="https://wa.me/6281234567890" rel="noopener">Hubungi pengelola</a>
                </div>

                <dl class="closing__details">
                    <div class="closing__detail">
                        <dt>Alamat</dt>
                        <dd>Jl. Contoh Raya No. 12, Surabaya</dd>
                    </div>
                    <div class="closing__detail">
                        <dt>WhatsApp</dt>
                        <dd>0812-3456-7890</dd>
                    </div>
                    <div class="closing__detail">
                        <dt>Jam respon</dt>
                        <dd>Setiap hari, 08.00 &ndash; 21.00 WIB</dd>
                    </div>
                </dl>
            </div>

            <div class="closing__photo">
                <footer class="site-footer">
                    <p>&copy; 2026 De&rsquo; Kost. Semua hak dilindungi.</p>
                    <a href="${pageContext.request.contextPath}/login">Masuk sebagai pengelola</a>
                </footer>
            </div>
        </section>
    </main>
</div>
</body>
</html>
