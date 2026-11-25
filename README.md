# De' Kost

Aplikasi manajemen kos (JSP + Servlet + PostgreSQL) untuk tugas EF234301 Web Programming.
Landing page publik dan halaman login sudah tersedia. Fondasi backend menyediakan pool
koneksi PostgreSQL, filter UTF-8, model JavaBean untuk entitas utama, serta utilitas flash
message, validasi, pagination, dan format Rupiah/tanggal. Halaman `/health` memeriksa
konektivitas database.

## Prasyarat

- JDK 17
- Maven 3.9+
- Docker + Docker Compose (untuk cara termudah)
- PostgreSQL 16 (jika menjalankan tanpa Docker)
- Tomcat 10.1 (Jakarta EE 10, namespace `jakarta.*`)

## Menjalankan lokal dengan Docker

Salin `.env.example` menjadi `.env`, lalu isi `POSTGRES_PASSWORD` dan
`ADMIN_PASSWORD` dengan password acak yang kuat (8-72 karakter, maksimal 72 byte
UTF-8). Compose memakai file `.env` secara otomatis:

```bash
cp .env.example .env
docker compose up --build
```

- Aplikasi: <http://localhost:8080> (WAR dipasang sebagai `ROOT`)
- Health check: <http://localhost:8080/health> → `OK` + status database
- Database/user default `dekost`, hanya tersedia di jaringan internal Compose.
  Untuk mengakses PostgreSQL lokal:
  `docker compose exec postgres psql -U dekost -d dekost`.

Pada volume PostgreSQL baru, container otomatis menjalankan `db/schema.sql` dan
`db/seed.sql`. Seed membuat akun admin dari `ADMIN_EMAIL` dan `ADMIN_PASSWORD`;
password disimpan sebagai hash BCrypt. Email login dinormalisasi menjadi huruf
kecil. Seed juga menyediakan akun penghuni dan data contoh. Skrip init hanya
berjalan saat volume database dibuat pertama kali; deploy ulang mempertahankan
data dan password akun yang ada.
Untuk menghapus dan membuat ulang database lokal:

```bash
docker compose down -v
docker compose up --build
```

Hentikan tanpa menghapus data dengan `docker compose down`.

### Deploy di DomCloud

Pada Setup → Deploy, aktifkan Docker dan gunakan konfigurasi deployment:

```yaml
features:
  - docker
services: docker-compose.yml
```

Docker di DomCloud memerlukan plan Kit/Pro. Buat `.env` secara aman di root
aplikasi pada host (jangan commit), lalu isi variabel dari `.env.example`;
khususnya set password acak kuat untuk `POSTGRES_PASSWORD` dan `ADMIN_PASSWORD`.
Pastikan `.env` tersedia saat deployment menjalankan Compose.

Service `app` menerima trafik publik melalui port container `8080`; frontend JSP
dan backend Servlet disajikan oleh service yang sama. Service `postgres` hanya
diakses lewat jaringan internal Compose dan tidak mempublikasikan port database.
Ini juga menghindari binding host IP yang tidak didukung Docker rootless DomCloud.
`DB_URL` di dalam container harus memakai hostname service database, misalnya
`jdbc:postgresql://postgres:5432/dekost`, bukan `localhost`.

Gunakan volume persisten untuk `/var/lib/postgresql/data` pada service `postgres`.
`db/schema.sql` dan `db/seed.sql` hanya diinisialisasi pada volume kosong; jangan
menghapus volume production saat redeploy. Endpoint `/health` mengembalikan status
503 bila database tidak dapat diakses. Lihat [panduan Docker DomCloud](https://domcloud.co/docs/deployment/docker)
untuk detail routing port dan Compose.

## Menjalankan tanpa Docker

1. Buat database dan jalankan skema:
   ```bash
   createdb dekost
   psql -d dekost -f db/schema.sql
   ```
2. Set `ADMIN_EMAIL` dan `ADMIN_PASSWORD` pada environment proses `psql`, lalu
   jalankan seed agar akun admin dibuat dengan hash BCrypt:
   ```bash
   psql -d dekost -f db/seed.sql
   ```
   Set juga `DB_URL`, `DB_USER`, `DB_PASSWORD`, dan `WA_NUMBER` untuk proses
   Tomcat (contoh ada di `.env.example`).
   Untuk proses Tomcat langsung, gunakan `DB_URL=jdbc:postgresql://localhost:5432/dekost`.
3. Build:
   ```bash
   mvn clean package
   ```
4. Salin `target/ROOT.war` ke `webapps/` Tomcat 10.1, lalu jalankan Tomcat.
5. Buka <http://localhost:8080/health>.

## Struktur folder

```
src/main/java/id/dekost/
  config/     Database.java (HikariCP, env var)
  model/      User, TipeKamar, Kamar, Penghuni, Pembayaran, Keluhan
  dao/        akses database (UserDao, KamarDao, PenghuniDao, PembayaranDao,
              KeluhanDao, DashboardDao, dan DAO entitas lain)
  service/    logika bisnis/transaksi (AuthService, PenghuniService, PembayaranService, dan service lain)
  servlet/    LandingServlet, AuthServlet, KamarServlet, PenghuniServlet,
              PembayaranServlet, KeluhanServlet, TipeKamarServlet, HealthServlet
  filter/     EncodingFilter, AuthFilter, CsrfFilter
  util/       Flash, Validator, Pagination, RupiahFormatter, TanggalFormatter
src/main/webapp/
  WEB-INF/web.xml
  WEB-INF/views/layout/   head.jspf, sidebar.jspf, topbar.jspf, footer.jspf
  WEB-INF/views/public/   landing.jsp, login.jsp
  WEB-INF/views/admin/    dashboard, kamar, tipe-kamar, penghuni, pembayaran, keluhan
  assets/css/             tokens.css, base.css, components.css, pages.css, theme.css
  assets/js/              app.js
  assets/img/             hero, footer, lorong, foto kamar, logo
db/       schema.sql (DDL), seed.sql (data contoh)
```

## Fitur CRUD

| # | Fitur | Create | Read | Update | Delete |
|---|-------|--------|------|--------|--------|
| 1 | Tipe kamar | Ya | Ya | Ya | Ya |
| 2 | Kamar | Ya | Ya | Ya | Ya |
| 3 | Penghuni | Check-in | Daftar/detail | Ubah data & pindah kamar | Hapus (setelah check-out) |
| 4 | Pembayaran | Tagihan baru + generate bulanan | Daftar tagihan | Ubah tagihan + tandai lunas | Ya |
| 5 | Keluhan (admin) | Ya | Ya | Ya | Ya |
| 6 | Akun pengguna | Ya | Ya | Nama, email, role, password | Ya |
| 7 | Portal penghuni: keluhan | Ya | Ya | - | - |
| 8 | Portal penghuni: tagihan | - | Ya | Unggah bukti pembayaran | - |

Dashboard admin dan dashboard penghuni memakai data ketersediaan kamar yang sama
(`KamarDao.findAvailabilityByType`), begitu juga landing page publik.

## Pembagian kerja FE / BE

- **FE (frontend):** seluruh `src/main/webapp/**` — CSS, JS, gambar, dan JSP.
- **BE (backend):** `src/main/java/**` (kecuali servlet mock/`HealthServlet`), seluruh `db/**`,
  serta konfigurasi build dan deployment (`pom.xml`, `Dockerfile`, `docker-compose.yml`).

## Catatan

- Konfigurasi sensitif dibaca dari environment variable; password seed hanya disimpan sebagai hash BCrypt.
- Tidak memakai Spring, Hibernate, Bootstrap, atau Tailwind. Semua CSS ditulis sendiri
  memakai design token di `assets/css/tokens.css`.
