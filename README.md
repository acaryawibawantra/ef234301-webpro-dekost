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

<<<<<<< HEAD
Salin `.env.example` menjadi `.env`, lalu isi `ADMIN_PASSWORD` dengan password
kuat (8-72 karakter dan maksimal 72 byte UTF-8):
=======
Salin `.env.example` menjadi `.env`, lalu isi `POSTGRES_PASSWORD` dan
`ADMIN_PASSWORD` dengan password acak yang kuat (8-72 karakter, maksimal 72 byte
UTF-8). Compose memakai file `.env` secara otomatis:
>>>>>>> d36df0c998ad56112dc7b75bc21d657dcb0409a6

```bash
cp .env.example .env
docker compose up --build
```

- Aplikasi: <http://localhost:8080> (WAR dipasang sebagai `ROOT`)
- Health check: <http://localhost:8080/health> → `OK` + status database
- Database/user default `dekost`, hanya tersedia di jaringan internal Compose.
  Untuk mengakses PostgreSQL lokal:
  `docker compose exec postgres psql -U dekost -d dekost`.

<<<<<<< HEAD
Saat aplikasi pertama kali terhubung, aplikasi membuat schema, akun admin
berdasarkan `ADMIN_EMAIL`/`ADMIN_PASSWORD`, dan data contoh dari `db/seed.sql`
jika database masih kosong. Password admin disimpan sebagai hash BCrypt. Schema
aman dijalankan ulang dan redeploy tidak menghapus data atau mereset password.
=======
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
>>>>>>> d36df0c998ad56112dc7b75bc21d657dcb0409a6

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

1. Buat database:
   ```bash
   createdb dekost
   ```
<<<<<<< HEAD
2. Set environment variable (contoh ada di `.env.example`):
   `DB_URL`, `DB_USER`, `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, dan
   `WA_NUMBER`. Schema dan data contoh akan dibuat saat aplikasi pertama kali
   terhubung ke database kosong. Default jika variabel DB tidak diatur:
   `jdbc:postgresql://localhost:5432/dekost`, user/password `dekost`; sesuaikan
   placeholder di `.env.example` jika kredensial database berbeda.
=======
2. Set `ADMIN_EMAIL` dan `ADMIN_PASSWORD` pada environment proses `psql`, lalu
   jalankan seed agar akun admin dibuat dengan hash BCrypt:
   ```bash
   psql -d dekost -f db/seed.sql
   ```
   Set juga `DB_URL`, `DB_USER`, `DB_PASSWORD`, dan `WA_NUMBER` untuk proses
   Tomcat (contoh ada di `.env.example`).
   Untuk proses Tomcat langsung, gunakan `DB_URL=jdbc:postgresql://localhost:5432/dekost`.
>>>>>>> d36df0c998ad56112dc7b75bc21d657dcb0409a6
3. Build:
   ```bash
   mvn clean package
   ```
4. Salin `target/ROOT.war` ke `webapps/` Tomcat 10.1, lalu jalankan Tomcat.
5. Buka <http://localhost:8080/health>.

## Deploy di Railway

Deploy repo sebagai Docker service, tambahkan PostgreSQL service, lalu atur
variabel di service aplikasi. Ganti `Postgres` dengan nama service database di
project Railway:

```text
DB_URL=${{Postgres.DATABASE_URL}}
DB_USER=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
ADMIN_EMAIL=admin@kos.test
ADMIN_PASSWORD=<password kuat 8-72 karakter>
```

Railway menyediakan `PORT`; entrypoint Docker mengatur port HTTP Tomcat dari
variabel tersebut (default lokal `8080`). Atur health check Railway ke
`/health`. Saat startup pertama, schema dan data contoh dibuat otomatis jika
database kosong. Pada database yang sudah berisi data, schema dijalankan tanpa
perintah `DROP`, akun admin dibuat hanya jika belum ada, dan seed contoh tidak
dijalankan ulang. Pastikan variabel admin tersedia saat bootstrap database
kosong. Untuk perubahan struktur pada database yang sudah ada, tambahkan
migrasi SQL `ALTER TABLE` yang eksplisit; `CREATE TABLE IF NOT EXISTS` bukan
migrasi perubahan kolom.

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
