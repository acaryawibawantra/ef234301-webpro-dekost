# Spesifikasi Shell Admin — De' Kost

Acuan visual: Figma “Admin” (Dashboard `7:2`, Sidebar `4:447`).
Komponen dasar: `docs/UI-KIT.md`.

## 1. Struktur halaman

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="id">
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body class="admin-body">
<div class="admin-layout">
    <%@ include file="/WEB-INF/views/layout/sidebar.jspf" %>

    <div class="admin-main">
        <%@ include file="/WEB-INF/views/layout/topbar.jspf" %>

        <main class="admin-content">
            <!-- isi halaman: stat card, table-card, modal, dll. -->
        </main>

        <%@ include file="/WEB-INF/views/layout/footer.jspf" %>
    </div>
</div>
</body>
</html>
```

`head.jspf` sudah memuat tokens, base, components, dan pages. Jangan menambah
`<link>` CSS lain di halaman admin.

## 2. Request attribute

Diset oleh servlet sebelum forward, bukan dihitung di JSP.

| Attribute | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `pageTitle` | String | ya | Judul topbar, mis. “Kamar” |
| `pageSubtitle` | String | tidak | Subjudul/ringkasan, mis. “24 kamar · 3 tersedia” |
| `pageActionLabel` | String | tidak | Label tombol kanan topbar, mis. “Tambah kamar” |
| `pageActionHref` | String | tidak | URL tombol kanan topbar |
| `activeNav` | String | ya | `dashboard`, `kamar`, `tipe-kamar`, `penghuni`, `pembayaran`, `keluhan` |

Session:

| Key | Isi |
|---|---|
| `user` | objek User dengan properti `nama` dan `email` (dipakai kartu user sidebar) |

Jika `sessionScope.user` kosong, sidebar menampilkan fallback “Pengelola Kos /
admin@kos.test” agar shell tetap bisa direview.

## 3. Ukuran dan layout

- Sidebar: lebar **264px**, latar `--surface`, padding 24px 20px, sticky setinggi viewport.
- Item nav: padding 12px 16px, radius 16px, gap 12px, ikon 20px.
  Aktif: latar `--accent-soft`, teks `--accent-dark`. Nonaktif: `--ink-2`.
- User card: putih, border 1px `--border`, radius 16px, avatar 40px.
- Main: padding **40px 40px 48px**, gap antar-blok **32px**.
- Topbar: judul H1 44/Light, subjudul 15/Regular `--muted`, aksi di kanan.
- Footer admin: garis atas `--border`, teks 13px `--muted`.

Responsif:

- ≤1100px: padding main 32px 24px.
- ≤900px: sidebar menjadi bar horizontal di atas, nav wrap; topbar menumpuk;
  modal dan form grid menjadi satu kolom.

## 4. Halaman

### Dashboard

- 4 `stat-card` dalam grid 4 kolom (kartu “Tagihan menunggak” memakai `stat-card--dark`).
- Baris konten: tabel tagihan jatuh tempo (kiri, `1fr`) + kolom kanan 400px
  berisi okupansi per lantai dan keluhan terbaru.
- Grid: `display:grid; grid-template-columns: minmax(0,1fr) 400px; gap:24px;`
  (buat class halaman di `pages.css` bila perlu).

### List page (kamar, penghuni, pembayaran, keluhan, tipe kamar)

Urutan blok:

1. `topbar` (dari fragment).
2. Toolbar: chip filter status, kotak cari (`field__input`), tombol tambah.
3. `table-card`: header + `table-wrap` + footer pagination.
4. `modal` form tambah/ubah + dialog konfirmasi hapus.

Aturan:

- Chip aktif memakai latar `--ink`, teks putih.
- Baris tabel tinggi 68px, header 44px latar `--surface`, aksi rata kanan.
- Pagination 10 baris; teks “Menampilkan x–y dari z”.
- Setelah POST sukses: redirect ke list + flash toast.
- Empty state memakai `.empty-state`.

### Modal

- Lebar **560px**, radius 24px, padding 32px.
- Header: judul H2 32px + tombol tutup bulat 36px.
- Field memakai `.field`; grid dua kolom memakai `.form-grid`.
- Footer: Batal (`button--secondary`) + Simpan (`button--primary`), rata kanan.
- Buka/tutup dengan menambah/menghapus `.is-open` pada `.modal` (lihat `assets/js/app.js`).

## 5. Ikon

Ikon sidebar sudah inline SVG stroke 1.75px (gaya Lucide) di `sidebar.jspf`.
Untuk aksi tabel, gunakan SVG inline 18–20px dengan `stroke="currentColor"`.
Jangan memakai library ikon eksternal.

## 6. Checklist halaman admin

- [ ] Include `head`, `sidebar`, `topbar`, `footer` sesuai urutan.
- [ ] `pageTitle` dan `activeNav` selalu diset servlet.
- [ ] Semua tombol memakai `.button` + varian.
- [ ] Semua status memakai `.badge` sesuai mapping PRD 8.3.
- [ ] Tabel memakai `.table-card`, bukan tabel polos.
- [ ] Modal memakai `.modal`; tidak ada form di halaman terpisah.
- [ ] Tidak ada query/logika bisnis di JSP.
- [ ] Tidak ada gradient di halaman admin.
