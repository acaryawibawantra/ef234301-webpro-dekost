# Rencana Sprint — De' Kost

Dokumen kerja tim untuk Quiz 2 EF234301 Web Programming.
Acuan fitur dan aturan bisnis: `PRD-quiz2.md`. Kontrak data FE–BE: `docs/CONTRACT.md`.

## 1. Tim dan kepemilikan folder

| Orang | Peran | Fokus utama | Folder yang boleh diubah |
|---|---|---|---|
| **Acarya** (`@acaryawibawantra`) | UI/UX + FE Public | Design system, landing page, login, layout shell, QA visual | `src/main/webapp/assets/**`, `WEB-INF/views/public/**`, `WEB-INF/views/layout/**`, `docs/` desain |
| **Atma** (`@Daemon19`) | FE Admin | Dashboard, 5 halaman list + modal CRUD, JS interaksi | `WEB-INF/views/admin/**`, `assets/js/**`, halaman admin |
| **Anang** (`@ArdhiCode`) | BE + DB + Deploy | Model, DAO, service, servlet, auth, validasi, transaksi, Docker/deploy | `src/main/java/**`, `db/**`, `Dockerfile`, `docker-compose.yml` |

Aturan kolaborasi:

- Acarya menyediakan `components.css`/`pages.css` + spesifikasi visual admin; **Atma yang merakit JSP admin**. Acarya tidak menulis JSP admin.
- Atma memakai class dan token yang sudah ada; perubahan token/komponen dibahas dengan Acarya.
- Anang menulis semua akses database dan logika bisnis. JSP hanya menampilkan request attribute.
- Setiap perubahan melewati 1 reviewer (bukan penulisnya) sebelum merge ke `main`.
- Branch: `feat/<nama>/<singkat>`, `fix/<nama>/<singkat>`, `docs/<singkat>`.
- Commit kecil, bahasa Indonesia/Inggris konsisten, tidak mencampur pekerjaan lain.

## 2. Definition of Done (DoD)

Sebuah task dianggap selesai jika:

- [ ] `mvn clean package` sukses dan aplikasi jalan tanpa error di log.
- [ ] Alur utama bisa didemokan dari UI (bukan hanya lewat database).
- [ ] Tidak ada `String` SQL yang digabung; semua query `PreparedStatement`.
- [ ] Semua output ke HTML di-escape (`<c:out>`/`fn:escapeXml`).
- [ ] Validasi server-side tampil di bawah field terkait.
- [ ] Tampilan memakai token `tokens.css`, tanpa gradient, tidak rusak di lebar 1024px.
- [ ] Tidak ada secret/plaintext password di repo.
- [ ] Sudah di-review minimal 1 anggota lain.

## 3. Ringkasan sprint

| Sprint | Tema | Goal yang bisa didemokan |
|---|---|---|
| **Sprint 0** | Fondasi & design system | Build jalan, `/health` OK, shell layout + komponen dasar siap dipakai |
| **Sprint 1** | Auth & public page | Login/logout + proteksi `/admin/*`, landing page dan login tampil |
| **Sprint 2** | CRUD 1 | CRUD Tipe kamar, Kamar, Penghuni; aturan 1 kamar 1 penghuni dan check-out |
| **Sprint 3** | CRUD 2 + dashboard | CRUD Pembayaran, Keluhan; dashboard terisi angka nyata; landing baca DB |
| **Sprint 4** | Hardening & deploy | Semua alur lulus uji, aman, online, dokumentasi + screenshot |

Durasi normal: 1 sprint = 1–2 hari kerja. Jika mengejar deadline, pakai jadwal kilat di bagian 10.

## 4. Beban kerja

Masing-masing memegang **9 task** (27 task total). Pembagian per sprint:

| Sprint | Acarya | Atma | Anang |
|---|---|---|---|
| 0 | 2 | 1 | 1 |
| 1 | 2 | 1 | 2 |
| 2 | 2 | 3 | 3 |
| 3 | 2 | 3 | 2 |
| 4 | 1 | 1 | 1 |
| **Total** | **9** | **9** | **9** |

Catatan: task BE umumnya lebih banyak baris kode, sedangkan task UIUX/FE lebih banyak iterasi desain. Karena itu Anang tidak diberi task dokumentasi/QA; Acarya dan Atma mengambil porsi QA, screenshot, dan dokumentasi.

## 5. Detail sprint

### Sprint 0 — Fondasi & Design System

**Goal:** semua orang punya fondasi yang sama; tidak ada pekerjaan FE yang menunggu class CSS.

| PIC | Task | Output / acceptance |
|---|---|---|
| Acarya | **A1. Finalisasi design system** — rapikan `tokens.css`, `base.css`, buat `components.css` (button, badge, input, card, table, modal, sidebar) | Semua komponen punya state default/hover/focus/disabled; tidak ada gradient |
| Acarya | **A2. Layout shell + spesifikasi visual admin** — `head.jspf`, `sidebar.jspf`, `topbar.jspf`, `footer.jspf`; dokumen spesifikasi halaman admin | Shell bisa di-include Atma; spec menampilkan susunan dashboard + 5 list page + modal |
| Atma | **T1. Skeleton admin + JS dasar** — 6 halaman JSP kosong, `app.js` untuk modal, konfirmasi hapus, toast flash | Halaman bisa dibuka via forward servlet dummy/URL internal; modal buka-tutup tanpa error console |
| Anang | **N1. Finalisasi fondasi BE** — `schema.sql`/`seed.sql`, `Database`, `EncodingFilter`, model, util (Flash, Validator, Pagination, formatter Rupiah/tanggal) | `mvn clean package` sukses, `/health` OK + DB terhubung, util punya unit test manual |

**Demo Sprint 0:** `/health` OK, shell layout tampil, komponen CSS terdokumentasi.

### Sprint 1 — Auth & Public Page

**Goal:** pengelola bisa login dan masuk ke area admin; publik bisa melihat landing page.

| PIC | Task | Output / acceptance |
|---|---|---|
| Acarya | **A3. Landing page** — hero, daftar tipe kamar, badge ketersediaan, CTA WhatsApp, footer | Responsif 1024px; teks putih di atas overlay solid `--ink` 40%; CTA memakai `WA_NUMBER` |
| Acarya | **A4. Login page** — form email/password, state error, tombol lebar penuh | Error tampil di atas form; fokus input `--accent`; tanpa gradient |
| Atma | **T2. Integrasi layout admin + dashboard statis** — sidebar aktif, topbar user, 4 stat card, panel okupansi, tabel tagihan, daftar keluhan | Semua elemen dari Figma tampil dengan data dummy; tidak ada logika di JSP |
| Anang | **N2. User DAO + AuthService** — cari user by email, verifikasi BCrypt, seed hash admin | Login `admin@kos.test` berhasil dengan hash BCrypt asli (bukan placeholder) |
| Anang | **N3. AuthServlet + AuthFilter + CsrfFilter** — login/logout, session 30 menit, proteksi `/admin/*`, token CSRF semua POST | Akses `/admin/*` tanpa login redirect `/login`; POST tanpa token ditolak |

**Demo Sprint 1:** login admin → dashboard; logout; akses langsung `/admin/kamar` dialihkan ke login.

### Sprint 2 — CRUD 1: Tipe Kamar, Kamar, Penghuni

**Goal:** tiga entitas inti bisa dikelola penuh dari UI, termasuk aturan satu kamar satu penghuni.

| PIC | Task | Output / acceptance |
|---|---|---|
| Acarya | **A5. Style halaman admin + review visual** — tabel, badge status, modal, empty state, flash toast; review hasil Atma | Semua halaman memakai komponen yang sama; badge sesuai mapping PRD 8.3 |
| Acarya | **A6. Landing ambil data DB** — jumlah kamar tersedia per tipe, state "x tersedia"/"1 tersisa"/"Penuh" + tombol nonaktif | Angka berubah saat data kamar berubah; tidak ada hardcode |
| Atma | **T3. Halaman Tipe kamar** — list + modal tambah/ubah + konfirmasi hapus | CRUD tipe kamar dari UI; error "tipe dipakai kamar" tampil jelas |
| Atma | **T4. Halaman Kamar** — list, cari, filter status, modal CRUD, detail | Nomor unik, lantai > 0, status hanya bisa diubah ke/dari `PERBAIKAN` secara manual |
| Atma | **T5. Halaman Penghuni** — list, cari/filter, modal tambah/ubah, pindah kamar, check-out | Kamar `PERBAIKAN` tidak muncul di pilihan; aksi check-out minta konfirmasi |
| Anang | **N4. TipeKamar DAO/Servlet CRUD + validasi** | Delete ditolak jika dipakai kamar; semua input tervalidasi |
| Anang | **N5. Kamar DAO/Servlet CRUD + aturan hapus** | Kamar berpenghuni aktif tidak bisa dihapus; status otomatis dari transaksi penghuni |
| Anang | **N6. Penghuni DAO + PenghuniService** — transaksi check-in, check-out, pindah kamar | Satu kamar tidak bisa dipakai 2 penghuni; check-out mengosongkan `kamar_id` dan set kamar `TERSEDIA` |

**Demo Sprint 2:** tambah tipe kamar → tambah kamar → tambah penghuni → pindah kamar → check-out; kamar kembali tersedia.

### Sprint 3 — CRUD 2 + Dashboard

**Goal:** pembayaran dan keluhan bekerja, dashboard menampilkan angka yang cocok dengan database.

| PIC | Task | Output / acceptance |
|---|---|---|
| Acarya | **A7. Visual dashboard** — 4 stat card (termasuk kartu gelap), progress okupansi per lantai, panel tagihan/keluhan | Angka besar 40/Light, layout rapi di 1024px, status badge konsisten |
| Acarya | **A8. Polish public page** — responsif, aksesibilitas (label, kontras, fokus), aset gambar placeholder | Landing + login lulus cek keyboard dan kontras dasar |
| Atma | **T6. Halaman Pembayaran** — list/filter/cari, modal tagihan, tandai lunas (tanggal + metode), tombol generate | Status Lunas/Belum bayar/Terlambat tampil benar; form lunas wajib lengkap |
| Atma | **T7. Halaman Keluhan** — list/filter, modal catat, modal tanggapi/ubah status | Status `SELESAI` tidak bisa kembali ke `BARU`; field tanggapan tampil |
| Atma | **T8. Wiring dashboard** — ganti data dummy dengan request attribute dari `DashboardDao` | Semua angka dashboard sama dengan hasil query DB |
| Anang | **N7. Pembayaran DAO/Service** — CRUD, status via SQL `CASE`, generate tagihan bulan ini | Tidak bisa duplikat (penghuni, periode); generate hanya untuk yang belum punya tagihan |
| Anang | **N8. Keluhan DAO/Servlet + Dashboard DAO** — CRUD keluhan, aturan `ditangani_oleh`/`resolved_at`, semua query dashboard | Angka dashboard cocok dengan data; keluhan selesai tidak bisa balik ke baru |

**Demo Sprint 3:** buat tagihan → tandai lunas → dashboard pendapatan naik; catat keluhan → tanggapi → selesai.

### Sprint 4 — Hardening, Deploy, Dokumentasi

**Goal:** aplikasi siap dinilai dan online.

| PIC | Task | Output / acceptance |
|---|---|---|
| Acarya | **A9. QA visual + screenshot + dokumentasi desain** | Screenshot semua halaman untuk laporan; catatan desain di `docs/`; tidak ada gradient |
| Atma | **T9. Polish admin + uji alur end-to-end** | Empty/error state rapi, bug JS selesai, checklist alur CRUD lulus |
| Anang | **N9. Keamanan + deploy** | Audit CSRF/PreparedStatement/escape, isi hash seed, deploy Docker + PostgreSQL hosting, smoke test online |

**Demo Sprint 4:** buka URL hosting, login, jalankan satu alur lengkap (kamar → penghuni → tagihan → lunas → keluhan).

## 6. Matriks 8 fitur dan CRUD

Syarat tugas: minimal **8 fitur CRUD**. Yang dipakai adalah 8 fitur (F1–F8); lima entitas di antaranya punya CRUD penuh (20 operasi).

| ID | Fitur | Create | Read | Update | Delete | PIC utama | Sprint |
|---|---|---|---|---|---|---|---|
| F1 | Autentikasi | login | sesi/login | logout | - | Anang + Acarya | 1 |
| F2 | Dashboard | - | statistik | - | - | Anang + Acarya + Atma | 1, 3 |
| F3 | Kamar | tambah | list/filter/cari/detail | ubah | hapus | Anang + Atma | 2 |
| F4 | Penghuni | tambah | list/filter/cari/detail | ubah, pindah, check-out | hapus | Anang + Atma | 2 |
| F5 | Pembayaran | tagihan | list/filter/cari | ubah, lunas | hapus | Anang + Atma | 3 |
| F6 | Keluhan | catat | list/filter/cari | status/tanggapan | hapus | Anang + Atma | 3 |
| F7 | Tipe kamar | tambah | list | ubah | hapus | Anang + Atma | 2 |
| F8 | Landing page | - | tipe + ketersediaan | - | - | Acarya + Anang | 1, 2 |

**Fitur tambahan (wajib/P1):**

- Pencarian `?q=`, filter `?status=`, pagination 10 baris di semua list.
- Flash message (toast) setelah POST + pola Post-Redirect-Get.
- CSRF token di semua form POST.
- Generate tagihan bulan ini untuk penghuni aktif.
- Transaksi check-in/check-out/pindah kamar.
- Dashboard: okupansi, pendapatan bulan ini, tunggakan, keluhan terbuka, jatuh tempo, keluhan terbaru.
- CTA WhatsApp di landing dengan nomor dari env `WA_NUMBER`.
- Responsif minimal tidak rusak di 1024px, format Rupiah/tanggal Indonesia.

**Opsional (P2, hanya jika Sprint 0–4 selesai):** akun penghuni (F9), portal penghuni (F10), rate limit login.

## 7. Deliverable UI/UX (Acarya)

### 7.1 Design system

- `tokens.css` — warna, radius, spacing (sudah ada; jangan diubah tanpa review).
- `base.css` — reset + Urbanist (sudah ada).
- `components.css` — button (primary/accent/secondary), badge status, input, card (terang/gelap/outline), table, modal, sidebar, toast.
- `pages.css` — gaya khusus landing, login, dashboard, list page.
- Spesifikasi visual admin (Figma + catatan): dashboard, kamar, penghuni, pembayaran, keluhan, tipe kamar, modal.

### 7.2 Public page (Acarya)

- Landing: hero + overlay, daftar tipe kamar, badge ketersediaan, CTA WhatsApp, footer.
- Login: form, error di atas form, tombol masuk lebar penuh.

### 7.3 Admin page (desain oleh Acarya, implementasi JSP oleh Atma)

- Dashboard: 4 stat card, tabel tagihan jatuh tempo, panel okupansi per lantai, daftar keluhan terbaru.
- List page: header + tombol tambah, chip filter, search, tabel, footer pagination.
- Modal: tambah/ubah kamar, penghuni, tagihan, keluhan, tipe kamar, tanggapan keluhan.
- Handoff: Acarya memberi class + contoh markup; Atma memasang ke JSP dan menghubungkan request attribute.

## 8. Dependency dan urutan handoff

1. **Kontrak dulu:** Anang menulis nama request attribute + parameter form di `docs/CONTRACT.md` sebelum Atma wiring.
2. **Komponen dulu:** Acarya menyelesaikan komponen yang dipakai sprint berjalan sebelum Atma mulai halaman.
3. **BE siap sebelum wiring:** Anang merge DAO/servlet + contoh data, lalu Atma menghubungkan JSP.
4. **Review silang:** Acarya review tampilan admin; Atma review alur UI public; Anang review query/validasi.
5. **Blocker:** jika BE terlambat, Atma memakai data dummy dan menandai `TODO(contract)`; jangan menulis query di JSP.

## 9. GitHub Projects

Struktur board yang dipakai:

- **Project:** [`De' Kost — Sprint Board`](https://github.com/users/acaryawibawantra/projects/4) (linked ke repo).
- **Milestone:** `Sprint 0` s.d. `Sprint 4`.
- **Label:** `owner: acarya`, `owner: atma`, `owner: anang`, `area: uiux`, `area: fe-public`, `area: fe-admin`, `area: be`, `priority: p0`, `priority: p1`, `priority: p2`.
- **Status:** Todo → In Progress → Review → Done.
- **Field tambahan:** `Sprint` (single select) dan `Owner` (single select).
- **Format judul issue:** `[S0][BE] Finalisasi fondasi backend`.

Setiap issue memuat: konteks, output, acceptance criteria, file utama, dan PIC. Board ini adalah sumber status harian; dokumen ini sumber pembagian kerja.

Semua 27 issue sudah di-assign ke akun GitHub masing-masing: `@acaryawibawantra` (Acarya), `@Daemon19` (Atma), `@ArdhiCode` (Anang) — 9 issue per orang.

## 10. Jadwal kilat (jika mengejar deadline 25 Nov 2026 23:59)

| Waktu | Sprint | Fokus |
|---|---|---|
| 24 Nov 22.00–24.00 | Sprint 0 | Fondasi, komponen, shell |
| 25 Nov 00.00–05.00 | Sprint 1 | Auth, landing, login |
| 25 Nov 05.00–11.00 | Sprint 2 | CRUD tipe kamar, kamar, penghuni |
| 25 Nov 11.00–17.00 | Sprint 3 | Pembayaran, keluhan, dashboard |
| 25 Nov 17.00–23.00 | Sprint 4 | Polish, deploy, screenshot, laporan |

Aturan kilat: hanya kerjakan P0; P2 (akun/portal penghuni) dilewati; merge ke `main` setiap sprint selesai agar deploy selalu bisa dilakukan.

## 11. Risiko dan mitigasi

| Risiko | Mitigasi |
|---|---|
| BE menjadi bottleneck | Kontrak attribute lebih dulu, data dummy di FE, Anang merge per entitas bukan sekaligus |
| Tampilan tiap halaman berbeda | Satu `components.css`, review visual Acarya tiap sprint |
| Konflik merge | Kepemilikan folder jelas; file shared (`components.css`, `app.js`) diumumkan sebelum diubah |
| Deadline | Jadwal kilat, P2 dipotong, deploy dilakukan lebih awal di Sprint 4 |
| Hash password placeholder | Anang membuat hash BCrypt nyata di Sprint 1 dan mengisi `seed.sql` |
| Data dashboard tidak cocok | Semua angka dashboard lewat `DashboardDao`, dites dengan data seed |
