<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="landing">
    <header class="site-header">
        <a class="site-header__brand" href="${pageContext.request.contextPath}/">
            <span class="site-header__logo">
                <img src="${pageContext.request.contextPath}/assets/img/logo-mark.svg"
                     alt="" width="22" height="22">
            </span>
            <span class="site-header__name">De&rsquo; Kost</span>
        </a>

        <nav class="site-header__nav" aria-label="Navigasi utama">
            <a class="site-header__link site-header__link--active" href="#tentang">Tentang</a>
            <a class="site-header__link" href="#tipe-kamar">Tipe kamar</a>
            <a class="site-header__link" href="#fasilitas">Fasilitas</a>
            <a class="site-header__link" href="#lokasi">Lokasi</a>
            <a class="site-header__link" href="#kontak">Kontak</a>
        </nav>

        <a class="site-header__cta" href="#kontak">Booking kamar</a>
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
                <a class="hero__cta" href="#tipe-kamar">Lihat kamar kosong</a>
            </div>

            <%-- Angka masih statis; akan dihubungkan ke data kamar pada tahap berikutnya. --%>
            <p class="hero__occupancy">24 kamar &middot; 3 masih kosong</p>

            <nav class="hero__social" aria-label="Tautan sosial De' Kost">
                <a href="#" rel="noopener">Instagram</a>
                <a href="#" rel="noopener">WhatsApp</a>
                <a href="#" rel="noopener">Maps</a>
            </nav>
        </section>

        <section class="about" id="tentang" aria-label="Tentang De' Kost">
            <div class="about__aside">
                <span class="section-pill">Tentang De&rsquo; Kost</span>
            </div>
            <p class="about__text">
                Di De&rsquo; Kost kamu tidak sekadar menyewa kamar, kamu pulang. Sejak 2021 kami
                menjadi rumah bagi mahasiswa dan pekerja muda dengan kamar bersih, lingkungan
                aman, dan pengelolaan yang tertata.
            </p>
        </section>

        <section class="highlights" id="fasilitas" aria-labelledby="highlights-title">
            <h2 class="visually-hidden" id="highlights-title">Fasilitas De&rsquo; Kost</h2>

            <article class="highlight-card highlight-card--dark">
                <span class="highlight-card__icon">
                    <img src="${pageContext.request.contextPath}/assets/img/icon-shield.svg"
                         alt="" width="24" height="24">
                </span>
                <p class="highlight-card__text">
                    Akses 24 jam dengan CCTV dan kunci pribadi, listrik token terpisah, serta
                    WiFi cepat di setiap lantai. Nyaman untuk istirahat dan belajar.
                </p>
                <div class="highlight-card__badges">
                    <span class="badge badge--accent"><span class="badge__dot"></span>CCTV 24 jam</span>
                    <span class="badge badge--neutral"><span class="badge__dot"></span>WiFi</span>
                </div>
            </article>

            <article class="highlight-card highlight-card--photo">
                <p class="highlight-card__label">Lorong terang dan sirkulasi udara baik</p>
            </article>

            <article class="highlight-card highlight-card--stats">
                <p class="highlight-card__stat">24</p>
                <p class="highlight-card__stat-label">Kamar siap huni</p>
                <p class="highlight-card__stat-note">
                    Tiga tipe kamar untuk setiap kebutuhan dan budget.
                </p>

                <div class="highlight-card__spacer"></div>

                <div class="room-row">
                    <span class="room-row__name">Standar</span>
                    <img class="room-row__dots"
                         src="${pageContext.request.contextPath}/assets/img/dots.svg"
                         alt="" width="164" height="10">
                    <span class="room-row__count">12</span>
                </div>
                <div class="room-row">
                    <span class="room-row__name">Deluxe</span>
                    <img class="room-row__dots"
                         src="${pageContext.request.contextPath}/assets/img/dots.svg"
                         alt="" width="164" height="10">
                    <span class="room-row__count">8</span>
                </div>
                <div class="room-row">
                    <span class="room-row__name">Premium</span>
                    <img class="room-row__dots"
                         src="${pageContext.request.contextPath}/assets/img/dots.svg"
                         alt="" width="164" height="10">
                    <span class="room-row__count">4</span>
                </div>
            </article>
        </section>
    </main>
</div>
</body>
</html>
