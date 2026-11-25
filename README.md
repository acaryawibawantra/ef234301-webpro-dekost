# De' Kost

Aplikasi manajemen kos berbasis **JSP + Servlet + PostgreSQL** untuk Quiz 2 Web Programming.
Terdapat dua area: **panel pengelola (admin)** dan **portal penghuni**, dengan autentikasi
dan pembatasan akses berdasarkan role.

- **Website:** <https://ef234301-webpro-dekost-production.up.railway.app/>
- **Repository:** <https://github.com/acaryawibawantra/ef234301-webpro-dekost.git>
- **Laporan:** <https://docs.google.com/document/d/1UODh9L5-0Wbx6RTMFke0-THfMk2axb8G_VXs9zLvBQg/edit>
- **Desain Figma:** <https://www.figma.com/design/AU54oSb33HxrVZQXYZlqYX>
- **Desain Database:** <https://app.eraser.io/workspace/Rx65wqGoGNBuqyXTkt7d?origin=share>

## Akun Demo (untuk Asdos)

| Role | Email | Password |
|------|-------|----------|
| Admin / Pengelola | `admin@dekost.com` | `admin1234` |
| Penghuni | `penghuni@gmail.com` | `penghuni1234` |

Akun admin dibuat otomatis dari `ADMIN_EMAIL` dan `ADMIN_PASSWORD` saat database masih kosong.
Nilai default untuk demo sudah disiapkan di `.env.example`. Akun penghuni dibuat oleh
`db/seed.sql`. Setelah login, admin diarahkan ke dashboard admin dan penghuni ke portal penghuni.

## Fitur

| Fitur | Deskripsi |
|-------|-----------|
| Authentication | Login, logout, dan role-based access control (ADMIN / PENGHUNI). |
| Room Management | CRUD kamar: nomor, lantai, tipe, status (Tersedia/Terisi/Perbaikan), catatan. |
| Room Type Management | CRUD tipe kamar: harga bulanan, luas, fasilitas. |
| Tenant Management | CRUD penghuni, check-in, pindah kamar, check-out, dan hapus data penghuni nonaktif. |
| Payment Management | CRUD tagihan, pembuatan tagihan bulanan massal, dan konfirmasi pembayaran lunas. |
| Complaint Management | CRUD keluhan dari sisi admin, termasuk tanggapan, prioritas, dan status. |
| Account Management | CRUD akun admin/penghuni, reset password, dan tautkan akun ke data penghuni. |
| Tenant Payment | Penghuni melihat tagihan dan mengirim bukti pembayaran. |
| Tenant Complaint | Penghuni membuat dan memantau keluhan. |
| Admin Dashboard | Ringkasan okupansi, pendapatan bulan ini, tunggakan, keluhan terbuka, dan ketersediaan kamar per tipe. |

### Cakupan CRUD (minimal 8 fitur)

1. Tipe kamar — Create, Read, Update, Delete
2. Kamar — Create, Read, Update, Delete
3. Penghuni — Create, Read, Update, Delete (hapus setelah check-out)
4. Pembayaran — Create, Read, Update, Delete
5. Keluhan admin — Create, Read, Update, Delete
6. Akun pengguna — Create, Read, Update, Delete
7. Portal penghuni: keluhan — Create, Read
8. Portal penghuni: tagihan — Read, Update bukti pembayaran

## Aturan Bisnis Utama

- Kamar hanya dapat ditempati jika berstatus **TERSEDIA**; satu kamar hanya untuk satu penghuni aktif.
- Status kamar **TERISI** dikelola otomatis oleh proses check-in/check-out, bukan diedit manual.
- Tagihan unik per penghuni dan periode. Status **TERLAMBAT** ditentukan otomatis dari tanggal jatuh tempo.
- Status keluhan mengikuti alur **BARU → DIPROSES → SELESAI**.
- Operasi CRUD data master hanya dapat dilakukan oleh **ADMIN**.

## Teknologi

- Jakarta Server Pages (JSP) + Servlet (Jakarta EE 10)
- PostgreSQL 16
- HikariCP, BCrypt, Maven
- Tomcat 10.1
- Docker & Docker Compose

## Deploy

**Railway:** deploy repo sebagai Docker service, tambahkan PostgreSQL, lalu isi
environment service aplikasi:

```text
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USER=<user>
DB_PASSWORD=<password>
ADMIN_EMAIL=admin@dekost.com
ADMIN_PASSWORD=admin1234
```

**DomCloud:** aktifkan fitur Docker dan gunakan `docker-compose.yml` sebagai
konfigurasi service. Siapkan `.env` di server (jangan di-commit) dengan nilai
yang sama seperti di atas.

Gunakan password yang lebih kuat untuk deployment produksi.

## Keamanan

- Password disimpan sebagai hash **BCrypt**.
- Query memakai **PreparedStatement** dan koneksi HikariCP.
- Form POST dilindungi **CSRF token**.
- Akses `/admin/**` dan `/penghuni/**` dijaga filter sesi dan role.
- Input divalidasi di sisi server sebelum disimpan.

## Tim

| Nama | NRP | Peran |
|------|-----|-------|
| Anang Ardhiansyah | 5025251118 | Back-end, database, autentikasi, deployment |
| I Dewa Nyoman Acarya Wibawantra | 5025251222 | Front-end, UI/UX, desain database, JSP |
| Komang Mahatma Langendria | 5025251151 | Front-end, JSP, komponen antarmuka |
