-- =====================================================
-- Seed data contoh: Manajemen Kos (PostgreSQL)
-- Jalankan SETELAH db/schema.sql.
-- JANGAN dijalankan otomatis oleh Docker/init.
-- password_hash berisi hash BCrypt; jangan simpan password plaintext di seed.
-- =====================================================

INSERT INTO users (nama, email, password_hash, role) VALUES
 ('Pengelola Kos', 'admin@kos.test', '$2a$12$LjwKX3H10w0nsEF/YDd6AusVmTX2koerpgpblk7zf7Hr4sycbf9Pm', 'ADMIN'),
 ('Budi Santoso',  'budi@kos.test',  '$2a$12$GVYojplH72DAfMx/Cg6xN.QkqH4ytiztpO4KqakA5tdiGjNyyzFoK', 'PENGHUNI');

INSERT INTO tipe_kamar (nama, harga_bulanan, luas_m2, fasilitas) VALUES
 ('Standar', 1200000, 9,  'Kasur, lemari, kipas angin, WiFi'),
 ('Deluxe',  1800000, 12, 'Kasur, lemari, AC, kamar mandi dalam, WiFi'),
 ('Premium', 2400000, 16, 'Kasur, lemari, AC, kamar mandi dalam, meja kerja, WiFi');

INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status) VALUES
 ('101', 1, 1, 'TERISI'),
 ('102', 1, 1, 'TERSEDIA'),
 ('201', 2, 2, 'TERSEDIA'),
 ('202', 2, 3, 'PERBAIKAN');

INSERT INTO penghuni (user_id, kamar_id, nama_lengkap, no_hp, tanggal_masuk) VALUES
 (2, 1, 'Budi Santoso', '081234567890', '2026-09-01');

INSERT INTO pembayaran (penghuni_id, periode, jumlah, jatuh_tempo, tanggal_bayar, metode, status) VALUES
 (1, '2026-10-01', 1200000, '2026-10-05', '2026-10-03', 'TRANSFER', 'LUNAS'),
 (1, '2026-11-01', 1200000, '2026-11-05', NULL, NULL, 'TERLAMBAT');

INSERT INTO keluhan (penghuni_id, judul, deskripsi, kategori, prioritas) VALUES
 (1, 'Kipas angin mati', 'Kipas angin di kamar 101 tidak menyala sejak kemarin.', 'FASILITAS', 'SEDANG');
