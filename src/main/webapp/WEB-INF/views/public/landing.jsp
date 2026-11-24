<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<main class="landing">
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
</main>
</body>
</html>
