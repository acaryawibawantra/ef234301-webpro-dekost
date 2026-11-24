# PRD: De' Kost - Aplikasi Manajemen Kos (JSP)

Dokumen ini adalah sumber kebenaran untuk coding agent. Baca seluruhnya sebelum menulis kode.
Konteks: Quiz 2 EF234301 Web Programming (ITS). Wajib JSP (Jakarta Server Pages), minimal 8 fitur CRUD, berbasis database, di-deploy ke hosting gratis, source di GitHub publik.

---

## 0. Aturan kerja untuk agent

1. Kerjakan per fase (bagian 11). Selesaikan dan jalankan satu fase sebelum lanjut. Commit kecil dan sering.
2. Skema database ada di `db/schema.sql`. Jangan ubah kolom atau nama tabel tanpa menulis alasannya di `docs/CHANGELOG-DB.md`.
3. UI harus mengikuti desain Figma (bagian 8). Ambil detail dari file Figma, jangan menebak. Jangan pakai gradient, jangan pakai library UI yang mengubah tampilan (tidak perlu Bootstrap/Tailwind). Pakai CSS sendiri dengan design token dari bagian 8.2.
4. Semua query memakai `PreparedStatement`. Password disimpan sebagai hash BCrypt. Semua output ke HTML di-escape (`<c:out>` atau `fn:escapeXml`).
5. Tidak ada logika bisnis di JSP. JSP hanya menampilkan data dari request attribute. Logika ada di Servlet dan DAO/Service.
6. Jangan tambah fitur di luar PRD. Kalau ada keputusan yang belum jelas, pilih opsi paling sederhana dan catat di `docs/DECISIONS.md`.
7. Setelah tiap fase: aplikasi harus bisa di-build (`mvn clean package`) dan dijalankan lokal tanpa error.

---

## 1. Ringkasan produk

De' Kost adalah web untuk pengelola kos: mengelola kamar, penghuni, pembayaran sewa bulanan, dan keluhan penghuni, dengan dashboard ringkasan. Ada juga halaman publik (landing page) untuk calon penghuni melihat tipe kamar.

Aturan utama domain: **satu kamar hanya untuk satu penghuni aktif**.

### Pengguna
| Peran | Kebutuhan |
|---|---|
| Pengelola (`ADMIN`) | Mengelola semua data, melihat dashboard. **Prioritas utama (P0).** |
| Pengunjung publik | Melihat landing page dan tipe kamar. |
| Penghuni (`PENGHUNI`) | Login, lihat tagihan sendiri, ajukan keluhan. **Opsional (P2).** |

### Di luar cakupan
Pembayaran online (payment gateway), notifikasi email/WhatsApp, upload file ke storage eksternal, multi-cabang kos, aplikasi mobile.

---

## 2. Tech stack (wajib)

- Java 17, Maven, packaging `war`.
- Tomcat 10.1+ (Jakarta EE 10). Namespace **`jakarta.*`**, bukan `javax.*`.
- JSP + JSTL (`jakarta.tags.core`, `jakarta.tags.fmt`, `jakarta.tags.functions`) + Servlet 6.
- Database: PostgreSQL. Akses lewat JDBC murni + HikariCP (connection pool).
- Hash password: BCrypt (library `at.favre.lib:bcrypt`).
- Tanpa framework besar (Spring/Hibernate tidak dipakai).
- Frontend: HTML + CSS sendiri (design token) + sedikit JavaScript vanilla untuk modal dan konfirmasi hapus. Font Urbanist (Google Fonts).

Dependency utama (cek dan pin versi terbaru yang stabil):
`jakarta.servlet:jakarta.servlet-api` (provided), `org.glassfish.web:jakarta.servlet.jsp.jstl`, `org.postgresql:postgresql`, `com.zaxxer:HikariCP`, `at.favre.lib:bcrypt`.

---

## 3. Arsitektur

Pola MVC sederhana.

```
src/main/java/id/dekost/
  config/        Database.java (HikariCP, baca env var)
  model/         User, TipeKamar, Kamar, Penghuni, Pembayaran, Keluhan
  dao/           UserDao, TipeKamarDao, KamarDao, PenghuniDao, PembayaranDao, KeluhanDao, DashboardDao
  service/       PenghuniService (transaksi check-in/out), PembayaranService, AuthService
  servlet/       AuthServlet, DashboardServlet, KamarServlet, PenghuniServlet, PembayaranServlet,
                 KeluhanServlet, TipeKamarServlet, LandingServlet
  filter/        AuthFilter (cek login + role), EncodingFilter (UTF-8), CsrfFilter
  util/          Flash (pesan sukses/gagal), Validator, Pagination, Rupiah/Tanggal formatter
src/main/webapp/
  WEB-INF/views/
    layout/      head.jspf, sidebar.jspf, topbar.jspf, footer.jspf
    public/      landing.jsp, login.jsp
    admin/       dashboard.jsp, kamar/list.jsp, penghuni/list.jsp, pembayaran/list.jsp,
                 keluhan/list.jsp, tipe-kamar/list.jsp  (form berupa modal di halaman yang sama)
  assets/css/    tokens.css, base.css, components.css, pages.css
  assets/js/     app.js
  assets/img/    hero.jpg, lorong.jpg, kamar-standar.jpg, kamar-deluxe.jpg, kamar-premium.jpg, login.jpg
db/              schema.sql, seed.sql
docs/            CHANGELOG-DB.md, DECISIONS.md
Dockerfile, docker-compose.yml, README.md
```

Aturan:
- Semua JSP di `WEB-INF/views` (tidak bisa diakses langsung). Servlet melakukan `forward`.
- Pola Post-Redirect-Get: setelah POST sukses, `redirect` ke halaman list dengan flash message.
- Satu transaksi database untuk operasi yang mengubah lebih dari satu tabel (check-in, check-out, pindah kamar).

---

## 4. Model data

Sumber kebenaran: `db/schema.sql` (PostgreSQL). ERD ada di FigJam (lihat bagian 8.1). Ringkasan:

| Tabel | Kolom penting |
|---|---|
| `users` | id, nama, email (unik), password_hash, role (`ADMIN`/`PENGHUNI`), created_at |
| `tipe_kamar` | id, nama (unik), harga_bulanan, luas_m2, fasilitas |
| `kamar` | id, nomor_kamar (unik), lantai, tipe_kamar_id, status (`TERSEDIA`/`TERISI`/`PERBAIKAN`), catatan |
| `penghuni` | id, user_id (unik, boleh null), kamar_id (**unik**, boleh null), nama_lengkap, no_hp, pekerjaan_instansi, kontak_darurat, tanggal_masuk, tanggal_keluar, status (`AKTIF`/`NONAKTIF`) |
| `pembayaran` | id, penghuni_id, periode (tanggal 1 bulan tagihan), jumlah, jatuh_tempo, tanggal_bayar, metode (`TUNAI`/`TRANSFER`/`QRIS`), status (`BELUM_BAYAR`/`LUNAS`/`TERLAMBAT`), bukti_url. Unik (penghuni_id, periode) |
| `keluhan` | id, penghuni_id, ditangani_oleh (users.id, boleh null), judul, deskripsi, kategori (`FASILITAS`/`KEBERSIHAN`/`KEBISINGAN`/`LAINNYA`), prioritas (`RENDAH`/`SEDANG`/`TINGGI`), status (`BARU`/`DIPROSES`/`SELESAI`), tanggapan, created_at, resolved_at |

Relasi: tipe_kamar 1-N kamar; kamar 1-0..1 penghuni; users 0..1-0..1 penghuni; penghuni 1-N pembayaran; penghuni 1-N keluhan; users 0..1-N keluhan (penangan).

---

## 5. Aturan bisnis

### Kamar
- Nomor kamar unik. Lantai bilangan bulat positif.
- Kamar tidak boleh dihapus jika masih dihuni penghuni aktif. Tampilkan pesan error yang jelas.
- Status `TERISI` dan `TERSEDIA` diatur otomatis oleh check-in/check-out. Admin hanya mengubah manual ke/dari `PERBAIKAN`. Kamar `PERBAIKAN` tidak bisa dipilih untuk penghuni baru.

### Penghuni (satu kamar satu penghuni)
- **Tambah penghuni aktif + kamar:** kamar harus `TERSEDIA`. Dalam satu transaksi: insert penghuni, set kamar `TERISI`.
- **Pindah kamar:** transaksi: kamar lama jadi `TERSEDIA`, kamar baru jadi `TERISI`, update `kamar_id`.
- **Set `NONAKTIF` (check-out):** transaksi: isi `tanggal_keluar`, set `kamar_id = NULL`, kamar jadi `TERSEDIA`. Riwayat pembayaran dan keluhan tetap tersimpan.
- Hapus penghuni diizinkan hanya jika tidak punya pembayaran berstatus `LUNAS`; kalau ada, sarankan set `NONAKTIF`.
- Validasi: nama wajib, no HP 9-15 digit, tanggal masuk wajib, tanggal keluar tidak boleh sebelum tanggal masuk.

### Pembayaran
- Satu tagihan per penghuni per bulan (constraint unik). Tangani error duplikat dengan pesan ramah.
- `jumlah` default terisi dari `harga_bulanan` tipe kamar penghuni, boleh diubah.
- Status ditampilkan dengan aturan: jika `LUNAS` maka Lunas; jika bukan `LUNAS` dan `jatuh_tempo < hari ini` maka **Terlambat**; selain itu Belum bayar. Hitung lewat SQL `CASE` agar konsisten di list dan dashboard.
- Saat ditandai lunas: wajib isi `tanggal_bayar` dan `metode`.
- Fitur bantu: tombol "Generate tagihan bulan ini" untuk semua penghuni aktif yang belum punya tagihan periode tersebut (jatuh tempo tanggal 5 bulan itu). Prioritas P1.

### Keluhan
- Dibuat oleh admin (atas nama penghuni) atau penghuni (P2). Default status `BARU`.
- Saat status berubah ke `DIPROSES`, isi `ditangani_oleh` dengan admin yang login. Saat `SELESAI`, isi `resolved_at`.
- Keluhan `SELESAI` tidak bisa kembali ke `BARU`.

### Auth
- Login dengan email + password. Session timeout 30 menit. `AuthFilter` melindungi `/admin/*`. Role `ADMIN` bebas akses; `PENGHUNI` hanya `/portal/*` (P2).
- Rate limit sederhana: setelah 5 gagal berturut-turut dalam sesi, tampilkan jeda. Prioritas P2.
- Seed minimal satu admin: `admin@kos.test` (password diset lewat seed, hash BCrypt dibuat aplikasi).

---

## 6. Fitur dan CRUD

Hitungan CRUD: 6 entitas x 4 operasi, jauh di atas syarat minimal 8.

| ID | Fitur | Create | Read | Update | Delete | Prioritas |
|---|---|---|---|---|---|---|
| F1 | Autentikasi | - | login/logout | - | - | P0 |
| F2 | Dashboard | - | statistik | - | - | P0 |
| F3 | Kamar | tambah | daftar + filter + cari + detail | ubah | hapus | P0 |
| F4 | Penghuni | tambah | daftar + filter + cari + detail | ubah, pindah kamar, check-out | hapus | P0 |
| F5 | Pembayaran | tagihan baru | daftar + filter + cari | ubah, tandai lunas | hapus | P0 |
| F6 | Keluhan | catat | daftar + filter + cari | ubah status, tanggapan | hapus | P0 |
| F7 | Tipe kamar | tambah | daftar | ubah | hapus (ditolak jika dipakai kamar) | P1 |
| F8 | Landing page publik | - | tipe kamar + ketersediaan dari DB | - | - | P0 |
| F9 | Akun penghuni (opsional saat tambah penghuni) | buat akun | - | reset password | - | P2 |
| F10 | Portal penghuni | ajukan keluhan | tagihan sendiri | - | - | P2 |

Semua halaman list: pencarian (`?q=`), filter status (`?status=`), pagination 10 baris (`?page=`). Form tambah/ubah tampil sebagai **modal** di halaman list. Aksi hapus memakai dialog konfirmasi.

---

## 7. Routing

| Method | Path | Fungsi |
|---|---|---|
| GET | `/` | Landing page |
| GET, POST | `/login` | Form dan proses login |
| GET | `/logout` | Logout |
| GET | `/admin/dashboard` | Dashboard |
| GET | `/admin/kamar` | List (query: q, status, page, edit=ID untuk buka modal ubah) |
| POST | `/admin/kamar/create`, `/update`, `/delete` | Aksi kamar |
| GET | `/admin/penghuni` | List |
| POST | `/admin/penghuni/create`, `/update`, `/checkout`, `/delete` | Aksi penghuni |
| GET | `/admin/pembayaran` | List |
| POST | `/admin/pembayaran/create`, `/update`, `/pay`, `/delete`, `/generate` | Aksi pembayaran |
| GET | `/admin/keluhan` | List |
| POST | `/admin/keluhan/create`, `/update`, `/delete` | Aksi keluhan |
| GET | `/admin/tipe-kamar` | List |
| POST | `/admin/tipe-kamar/create`, `/update`, `/delete` | Aksi tipe kamar |

Semua POST menyertakan token CSRF (hidden input, divalidasi `CsrfFilter`).

### Query dashboard (`DashboardDao`)
- Okupansi: `SELECT status, COUNT(*) FROM kamar GROUP BY status`.
- Okupansi per lantai: jumlah kamar dan jumlah `TERISI` per `lantai`.
- Pendapatan bulan ini: `SUM(jumlah)` pembayaran `LUNAS` dengan `periode` bulan berjalan.
- Tagihan menunggak: pembayaran bukan `LUNAS` dengan `jatuh_tempo < CURRENT_DATE` (jumlah baris dan total rupiah).
- Keluhan terbuka: `COUNT(*)` status bukan `SELESAI`, plus jumlah berprioritas `TINGGI`.
- Tagihan jatuh tempo: 5 tagihan belum lunas dengan jatuh tempo terdekat/terlama.
- Keluhan terbaru: 3 teratas berdasarkan `created_at`.

---

## 8. Desain UI (Figma)

### 8.1 Lokasi
- File Figma desain: `https://www.figma.com/design/AU54oSb33HxrVZQXYZlqYX` (fileKey `AU54oSb33HxrVZQXYZlqYX`)
- ERD ada di file FigJam terpisah bernama "ERD Manajemen Kos" (akun yang sama).
- Jika agent punya Figma MCP, gunakan `get_design_context` dan `get_screenshot` dengan nodeId di bawah untuk detail layout. Jika tidak, gunakan spesifikasi token dan struktur di PRD ini.

| Layar | Halaman Figma | nodeId |
|---|---|---|
| Design System (token, komponen) | Design System | `4:2` |
| Landing page | Public | `5:2` |
| Login | Public | `6:19` |
| Dashboard | Admin | `7:2` |
| Kamar | Admin | `8:82` |
| Penghuni | Admin | `8:377` |
| Pembayaran | Admin | `8:672` |
| Keluhan | Admin | `8:929` |
| Modal tambah kamar | Admin | `8:1174` |
| Modal tambah penghuni | Admin | `8:1210` |
| Modal tagihan | Admin | `8:1255` |
| Modal tanggapi keluhan | Admin | `8:1300` |

Komponen: Button `4:144`, Badge `4:162`, Input `4:173`, Stat Card `4:184`, Sidebar `4:447`.

### 8.2 Design token (wajib dipakai sebagai CSS variables di `tokens.css`)

Warna (semuanya **solid, tanpa gradient**):
```
--bg:#FFFFFF  --surface:#F6F4F1  --surface-2:#EFEBE6  --border:#E4DED7
--ink:#14202B  --ink-2:#3B4754  --muted:#6B7480
--accent:#B5502F  --accent-dark:#8F3D22  --accent-soft:#F7E9E3  --on-accent:#FFFFFF
--success:#2F7D5B  --success-soft:#E4F2EB
--warning:#A86A0F  --warning-soft:#FBF0D9
--danger:#B3372F   --danger-soft:#FAE5E2
```
Radius: `--radius-sm:8px  --radius-md:16px  --radius-lg:24px  --radius-pill:999px`.
Spasi: 4, 8, 12, 16, 24, 32, 48, 64, 96 px.

Tipografi: **Urbanist**. Skala: Display 64/Light, H1 44/Light, H2 32/Regular, H3 22/Medium, Body Large 18, Body 15, Body Small 13, Label 14/SemiBold, Caption 12/Medium uppercase letter-spacing 2%, Stat 40/Light. Line height dan letter spacing mengikuti text style di Figma.

### 8.3 Pola komponen
- **Button:** pill. Varian Primary (isi `--ink`, teks putih), Accent (isi `--accent`), Secondary (outline 1.5px `--ink`). Padding 12px 24px.
- **Badge status:** pill dengan titik 6px, latar `*-soft`, teks warna penuh. Pemetaan: Terisi=Accent, Tersedia=Success, Perbaikan=Warning; Lunas=Success, Belum bayar=Warning, Terlambat=Danger; Baru=Danger, Diproses=Warning, Selesai=Success; Aktif=Success, Nonaktif=Neutral (`--surface-2` + `--ink-2`); Prioritas Tinggi=Danger, Sedang=Warning, Rendah=Neutral.
- **Input:** tinggi 48px, border 1.5px `--border`, radius 16px, fokus border `--accent` 2px.
- **Kartu:** radius 24px. Varian terang (`--surface`), gelap (`--ink`, teks putih), dan outline (`--border`).
- **Tabel:** kartu outline radius 24px, header baris `--surface` dengan caption uppercase, tinggi baris 68px, garis bawah `--border`, kolom aksi rata kanan dengan tombol ikon bulat 36px (ubah, hapus). Footer: teks "Menampilkan x-y dari z" + tombol pager.
- **Sidebar admin:** lebar 264px, latar `--surface`, item aktif latar `--accent-soft` + teks `--accent-dark`. Item: Dashboard, Kamar, Penghuni, Pembayaran, Keluhan (tambah Tipe kamar di bawah Kamar jika F7 dikerjakan). Kartu user + tombol Keluar di bawah.
- **Modal:** lebar 560px, kartu radius 24px, padding 32px, header judul H2 + tombol tutup bulat, grid field 1 atau 2 kolom, aksi di kanan: Batal (Secondary), Simpan (Primary).
- **Ikon:** SVG stroke 1.75px (gaya Lucide). Simpan sebagai SVG inline atau sprite.

### 8.4 Aset gambar
Foto belum final. Gunakan file di `assets/img/` dengan nama di bagian 3. Selama foto belum ada, pakai blok warna `--surface-2` sebagai placeholder. Landing hero memakai overlay solid `--ink` opasitas 40% di atas foto agar teks putih terbaca.

### 8.5 Perilaku halaman
- **Landing:** tipe kamar dan jumlah tersedia dibaca dari database (`tipe_kamar` + hitung kamar `TERSEDIA` per tipe). Badge "x tersedia" hijau; jika sisa 1 gunakan kuning "1 tersisa"; jika 0 tampilkan "Penuh" (netral) dan tombol dinonaktifkan. Tombol "Pesan kamar" mengarah ke tautan WhatsApp (nomor dari konfigurasi).
- **Login:** error login tampil di atas form. Tombol Masuk lebar penuh.
- **Dashboard:** 4 kartu statistik (Kamar terisi, Pendapatan bulan ini, Tagihan menunggak [kartu gelap], Keluhan terbuka), tabel tagihan jatuh tempo, panel okupansi per lantai (progress bar), daftar keluhan terbaru.
- **List page:** chip filter (aktif berlatar `--ink`), kotak cari, tombol tambah di kanan atas. Setelah aksi sukses tampil flash message (toast sederhana) di atas konten.

---

## 9. Non-fungsional

- Desktop-first (lebar 1440). Minimal tidak rusak di 1024px; responsif mobile adalah bonus.
- Semua teks UI Bahasa Indonesia. Format uang `Rp1.200.000`; tanggal `5 Nov 2026`.
- Encoding UTF-8 di seluruh aplikasi.
- Pesan error validasi tampil di bawah field terkait.
- Tidak ada secret di repo. Konfigurasi lewat environment variable: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `WA_NUMBER`.

---

## 10. Deployment dan repo

- Repo GitHub **publik**, berisi seluruh source, `db/schema.sql`, `db/seed.sql`, `README.md` (cara menjalankan lokal + cara deploy + akun demo).
- `docker-compose.yml` untuk lokal: service `postgres` (otomatis menjalankan `db/schema.sql`) dan `app` (Tomcat 10.1 JDK 17 dengan WAR sebagai ROOT).
- `Dockerfile`: multi-stage (build Maven, lalu jalankan di `tomcat:10.1-jdk17`, WAR disalin ke `webapps/ROOT.war`).
- Hosting gratis untuk aplikasi (kandidat: Render atau Railway via Docker; cek syarat gratis terbaru) dan database PostgreSQL gratis (Neon, Supabase, atau Aiven). Jalankan `schema.sql` dan `seed.sql` di database hosting.
- Setelah deploy: uji login admin dan satu alur lengkap (tambah kamar, tambah penghuni, buat tagihan, tandai lunas, catat keluhan).

---

## 11. Rencana fase

| Fase | Isi | Selesai jika |
|---|---|---|
| 1. Fondasi | Struktur Maven, `Database.java` + Hikari, filter encoding/auth/CSRF, `tokens.css` + `base.css` + `components.css`, layout JSP (sidebar, topbar), login/logout dengan BCrypt | Bisa login ke halaman dashboard kosong, `schema.sql` + seed berjalan |
| 2. CRUD inti | Kamar, Penghuni (dengan aturan check-in/out), Pembayaran, Keluhan: DAO, Servlet, list + modal form, validasi, flash message, search/filter/pagination | Semua aksi 4 entitas bekerja dan aturan bisnis bagian 5 terpenuhi |
| 3. Dashboard dan landing | `DashboardDao`, halaman dashboard, landing page dari data DB, Tipe kamar (F7) | Angka dashboard sesuai data; landing menampilkan ketersediaan nyata |
| 4. Polish dan deploy | Pencocokan visual dengan Figma, state kosong dan error, Dockerfile, deploy, README | Aplikasi online dan alur lengkap lulus uji |
| 5. Opsional | Generate tagihan massal, portal penghuni (F9, F10) | Hanya jika waktu cukup |

### Pembagian kerja tim (3 orang: 1 BE, 2 FE)
- **BE:** database, model, DAO, service, servlet, validasi, transaksi, auth, deploy.
- **FE-1:** token CSS, komponen, layout, login, dashboard, landing page.
- **FE-2:** halaman list dan modal (kamar, penghuni, pembayaran, keluhan, tipe kamar), JavaScript modal dan konfirmasi hapus.
- Kontrak antara FE dan BE: nama request attribute dan parameter form didokumentasikan di `docs/CONTRACT.md` pada akhir Fase 1 (misalnya `kamarList`, `page`, `totalPages`, `flash`).

---

## 12. Kriteria penerimaan

1. Login admin berhasil; akses `/admin/*` tanpa login diarahkan ke `/login`.
2. CRUD kamar, penghuni, pembayaran, keluhan bekerja penuh dari UI, dengan validasi dan pesan error.
3. Dua penghuni tidak bisa menempati kamar yang sama; check-out mengembalikan kamar menjadi tersedia.
4. Tidak bisa membuat dua tagihan untuk penghuni dan periode yang sama.
5. Status "Terlambat" muncul otomatis untuk tagihan lewat jatuh tempo.
6. Dashboard menampilkan angka yang cocok dengan isi database.
7. Tampilan mengikuti Figma: token warna dan font benar, tidak ada gradient.
8. Tidak ada SQL yang dibangun dengan penggabungan string; tidak ada password plaintext.
9. Aplikasi berjalan online di hosting gratis; repo GitHub publik dengan README yang bisa diikuti.
10. `mvn clean package` sukses tanpa error.

---

## 13. Di luar repo (dikerjakan manusia, bukan agent)

Laporan `Report.PDF` (model data konseptual dan fisik, daftar fitur, screenshot, penjelasan per fitur, pembagian kerja dan persentase kontribusi, analisis/evaluasi, kesimpulan), `Declaration.PDF` (pledge integritas akademik bertanda tangan semua anggota), ZIP bernama `EF234301_WebPro_Q2_StudentID1_Name1_StudentID2_Name2.ZIP`, dan email ke dosen sebelum **25 November 2026 pukul 23.59 WIB**. Agent boleh membantu menyiapkan bahan screenshot dan draf penjelasan fitur setelah aplikasi berjalan.
