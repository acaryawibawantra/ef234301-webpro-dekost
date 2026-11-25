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
            <section class="card card--outline">
                <div class="empty-state">
                    <h2 class="empty-state__title">Dashboard siap diisi</h2>
                    <p>Ringkasan hunian kamar, pendapatan bulan ini, tagihan jatuh tempo, dan keluhan terbaru akan ditampilkan di sini.</p>
                </div>
            </section>
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/app.js" defer></script>
</body>
</html>
