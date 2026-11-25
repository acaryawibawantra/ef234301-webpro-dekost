<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
            <div class="toolbar">
                <div class="toolbar__group"></div>
                <button class="button button--primary" type="button" data-modal-open="penghuni-modal">
                    Tambah penghuni
                </button>
            </div>

            <section class="table-card">
                <header class="table-card__header">
                    <div>
                        <h2 class="table-card__title">Daftar <c:out value="${pageTitle}"/></h2>
                        <p class="table-card__subtitle">Kerangka halaman siap diisi pada tahap berikutnya.</p>
                    </div>
                </header>
                <div class="empty-state">
                    <h3 class="empty-state__title">Halaman penghuni siap diisi</h3>
                    <p>Tabel daftar penghuni, pencarian, dan formulir check-in/check-out akan dihubungkan di Sprint 2.</p>
                </div>
            </section>
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<div class="modal" id="penghuni-modal" role="dialog" aria-modal="true" aria-labelledby="penghuni-modal-title">
    <div class="modal__dialog">
        <header class="modal__header">
            <h2 class="modal__title" id="penghuni-modal-title">Tambah penghuni</h2>
            <button class="modal__close" type="button" aria-label="Tutup" data-modal-close>&times;</button>
        </header>
        <div class="modal__body">
            <p class="field__hint">Formulir penghuni akan diisi pada tahap implementasi CRUD penghuni.</p>
        </div>
        <footer class="modal__footer">
            <button class="button button--secondary" type="button" data-modal-close>Batal</button>
        </footer>
    </div>
</div>

<%@ include file="/WEB-INF/views/admin/fragments/confirm-delete-modal.jspf" %>

<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
