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

            <p class="hero__occupancy">24 kamar &middot; 3 masih kosong</p>

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
                <p class="highlight-card__stat">24</p>
                <p class="highlight-card__stat-label">Kamar siap huni</p>
                <p class="highlight-card__stat-note">
                    Tiga tipe kamar untuk setiap kebutuhan dan budget.
                </p>

                <div class="highlight-card__spacer"></div>

                <div class="room-row">
                    <span class="room-row__name">Standar</span>
                    <span class="room-row__count">12 kamar</span>
                </div>
                <div class="room-row">
                    <span class="room-row__name">Deluxe</span>
                    <span class="room-row__count">8 kamar</span>
                </div>
                <div class="room-row">
                    <span class="room-row__name">Premium</span>
                    <span class="room-row__count">4 kamar</span>
                </div>
            </article>
        </section>

        <section class="numbers" aria-label="De' Kost dalam angka">
            <p class="numbers__title">Beberapa fakta tentang kami dalam angka</p>

            <div class="numbers__row">
                <div class="stat">
                    <p class="stat__value">24</p>
                    <p class="stat__label">Kamar tersedia</p>
                </div>
                <div class="stat">
                    <p class="stat__value">96%</p>
                    <p class="stat__label">Tingkat hunian</p>
                </div>
                <div class="stat">
                    <p class="stat__value">24 jam</p>
                    <p class="stat__label">Akses dan keamanan</p>
                </div>
                <div class="stat">
                    <p class="stat__value">Rp1,2 jt</p>
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

            <div class="room-grid">
                <article class="room-card">
                    <div class="room-card__photo room-card__photo--standar"
                         role="img" aria-label="Kamar tipe Standar"></div>
                    <div class="room-card__head">
                        <h3 class="room-card__name">Standar</h3>
                        <span class="badge badge--success">3 tersedia</span>
                    </div>
                    <p class="room-card__price">Rp1.200.000 <span>/ bulan</span></p>
                    <p class="room-card__facilities">9 m&sup2; &middot; Kipas angin &middot; Kamar mandi luar &middot; WiFi</p>
                    <a class="button button--secondary room-card__cta" href="#kontak">Pesan kamar</a>
                </article>

                <article class="room-card">
                    <div class="room-card__photo room-card__photo--deluxe"
                         role="img" aria-label="Kamar tipe Deluxe"></div>
                    <div class="room-card__head">
                        <h3 class="room-card__name">Deluxe</h3>
                        <span class="badge badge--success">2 tersedia</span>
                    </div>
                    <p class="room-card__price">Rp1.800.000 <span>/ bulan</span></p>
                    <p class="room-card__facilities">12 m&sup2; &middot; AC &middot; Kamar mandi dalam &middot; WiFi</p>
                    <a class="button button--secondary room-card__cta" href="#kontak">Pesan kamar</a>
                </article>

                <article class="room-card">
                    <div class="room-card__photo room-card__photo--premium"
                         role="img" aria-label="Kamar tipe Premium"></div>
                    <div class="room-card__head">
                        <h3 class="room-card__name">Premium</h3>
                        <span class="badge badge--warning">1 tersisa</span>
                    </div>
                    <p class="room-card__price">Rp2.400.000 <span>/ bulan</span></p>
                    <p class="room-card__facilities">16 m&sup2; &middot; AC &middot; Kamar mandi dalam &middot; Meja kerja</p>
                    <a class="button button--secondary room-card__cta" href="#kontak">Pesan kamar</a>
                </article>
            </div>
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
