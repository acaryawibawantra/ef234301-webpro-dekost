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

```bash
docker compose up --build
```

- Aplikasi: <http://localhost:8080> (WAR dipasang sebagai `ROOT`)
- Health check: <http://localhost:8080/health> → `OK` + status database
- PostgreSQL: `localhost:5432`, database/user `dekost` (kredensial development lokal)

Saat pertama kali dibuat, container Postgres menjalankan `db/schema.sql`.
`db/seed.sql` **tidak** dijalankan otomatis; file tersebut sudah berisi hash BCrypt
admin dan data contoh, lalu jalankan:

```bash
docker compose exec -T postgres psql -U dekost -d dekost < db/seed.sql
```

Hentikan dengan `docker compose down` (tambahkan `-v` untuk menghapus volume database).

## Menjalankan tanpa Docker

1. Buat database dan jalankan skema:
   ```bash
   createdb dekost
   psql -d dekost -f db/schema.sql
   ```
2. Set environment variable (contoh ada di `.env.example`):
   `DB_URL`, `DB_USER`, `DB_PASSWORD`, `WA_NUMBER`.
   Default lokal: `jdbc:postgresql://localhost:5432/dekost`, user/password `dekost`.
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
  dao/        akses database (UserDao dan DAO entitas lain)
  service/    logika bisnis/transaksi (AuthService dan service entitas lain)
  servlet/    LandingServlet, AuthServlet (login/logout), HealthServlet
  filter/     EncodingFilter, AuthFilter, CsrfFilter
  util/       Flash, Validator, Pagination, RupiahFormatter, TanggalFormatter
src/main/webapp/
  WEB-INF/web.xml
  WEB-INF/views/layout/   head.jspf, sidebar.jspf, topbar.jspf, footer.jspf
  WEB-INF/views/public/   landing.jsp, login.jsp
  WEB-INF/views/admin/    (kosong, menunggu halaman CRUD)
  assets/css/             tokens.css, base.css, components.css, pages.css
  assets/js/              app.js
  assets/img/             hero, footer, lorong, foto kamar, ikon
db/       schema.sql (DDL), seed.sql (data contoh)
docs/     CONTRACT.md, DECISIONS.md, SPRINT.md, UI-KIT.md, ADMIN-SPEC.md
```

## Pembagian kerja FE / BE

- **FE (frontend):** seluruh `src/main/webapp/**` — CSS, JS, gambar, dan JSP.
- **BE (backend):** `src/main/java/**` (kecuali servlet mock/`HealthServlet`), seluruh `db/**`,
  serta konfigurasi build dan deployment (`pom.xml`, `Dockerfile`, `docker-compose.yml`).

Kontrak request attribute dan parameter form antar FE/BE dicatat di `docs/CONTRACT.md`.
Pembagian task dan sprint ada di `docs/SPRINT.md`.

## Catatan

- Konfigurasi sensitif dibaca dari environment variable; password seed hanya disimpan sebagai hash BCrypt.
- Tidak memakai Spring, Hibernate, Bootstrap, atau Tailwind. Semua CSS ditulis sendiri
  memakai design token di `assets/css/tokens.css`.
