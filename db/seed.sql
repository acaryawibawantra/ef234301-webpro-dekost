\getenv admin_email ADMIN_EMAIL
\getenv admin_password ADMIN_PASSWORD

SELECT length(:'admin_password') BETWEEN 8 AND 72
   AND octet_length(:'admin_password') <= 72 AS admin_password_valid
\gset
\if :admin_password_valid
\else
\echo 'ADMIN_PASSWORD must be 8-72 characters and at most 72 UTF-8 bytes.'
\quit 1
\endif

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (nama, email, password_hash, role) VALUES
 ('Pengelola Kos', LOWER(BTRIM(:'admin_email')),
  crypt(:'admin_password', gen_salt('bf', 12)), 'ADMIN'),
 ('Budi Santoso',  'budi@kos.test', '$2a$12$rFtazLwGLwMGr2JpJ9Ki4.wa96E7DOAPzJqJBe/YgdZZcpWsk7bDS', 'PENGHUNI');
INSERT INTO tipe_kamar (nama, harga_bulanan, luas_m2, fasilitas) VALUES
 ('Standar', 1200000, 9,  'Kasur, lemari, kipas angin, WiFi'),
 ('Deluxe',  1800000, 12, 'Kasur, lemari, AC, kamar mandi dalam, WiFi'),
 ('Premium', 2400000, 16, 'Kasur, lemari, AC, kamar mandi dalam, meja kerja, WiFi');
INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status) VALUES
 ('101', 1, 1, 'TERISI'),
 ('102', 1, 1, 'TERSEDIA'),
 ('201', 2, 2, 'TERSEDIA'),
 ('202', 2, 3, 'PERBAIKAN');
INSERT INTO penghuni (user_id, kamar_id, nama_lengkap, no_hp, tanggal_masuk)
SELECT id, 1, 'Budi Santoso', '081234567890', '2026-09-01'
FROM users
WHERE email = 'budi@kos.test';
INSERT INTO pembayaran (penghuni_id, periode, jumlah, jatuh_tempo, tanggal_bayar, metode, status) VALUES
 (1, '2026-10-01', 1200000, '2026-10-05', '2026-10-03', 'TRANSFER', 'LUNAS'),
 (1, '2026-11-01', 1200000, '2026-11-05', NULL, NULL, 'TERLAMBAT');
INSERT INTO keluhan (penghuni_id, judul, deskripsi, kategori, prioritas) VALUES
 (1, 'Kipas angin mati', 'Kipas angin di kamar 101 tidak menyala sejak kemarin.', 'FASILITAS', 'SEDANG');
