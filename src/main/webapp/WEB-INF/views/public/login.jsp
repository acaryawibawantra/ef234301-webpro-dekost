<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<main class="login">
    <section class="login__media" aria-label="Eksterior De' Kost">
        <div class="login__caption">
            <p class="login__caption-brand">De&rsquo; Kost</p>
            <p class="login__caption-meta">24 kamar &middot; Akses 24 jam &middot; CCTV</p>
        </div>
    </section>

    <section class="login__panel">
        <div class="login__form-wrap">
            <a class="login__brand" href="${pageContext.request.contextPath}/">
                <span class="login__logo">
                    <img src="${pageContext.request.contextPath}/assets/img/logo-mark.svg"
                         alt="" width="22" height="22">
                </span>
                <span class="login__brand-name">De&rsquo; Kost</span>
            </a>

            <div class="login__heading">
                <h1 class="login__title">Masuk ke panel pengelola</h1>
                <p class="login__subtitle">
                    Kelola kamar, penghuni, pembayaran, dan keluhan dalam satu tempat.
                </p>
            </div>

            <form class="login__form" method="post" action="${pageContext.request.contextPath}/login">
                <c:if test="${not empty error}">
                    <p class="login__error" role="alert"><c:out value="${error}"/></p>
                </c:if>

                <div class="field">
                    <label class="field__label" for="email">Email</label>
                    <input class="field__input" type="email" id="email" name="email"
                           placeholder="nama@email.com" autocomplete="username" required>
                </div>

                <div class="field">
                    <label class="field__label" for="password">Password</label>
                    <input class="field__input" type="password" id="password" name="password"
                           placeholder="Masukkan password" autocomplete="current-password" required>
                </div>

                <div class="login__options">
                    <label class="checkbox">
                        <input type="checkbox" name="remember" value="true">
                        <span>Ingat saya</span>
                    </label>
                    <a class="login__forgot" href="#">Lupa password?</a>
                </div>

                <button class="button button--primary button--block" type="submit">Masuk</button>
            </form>

            <p class="login__note">Penghuni masuk dengan akun yang dibuat pengelola.</p>
        </div>
    </section>
</main>
</body>
</html>
