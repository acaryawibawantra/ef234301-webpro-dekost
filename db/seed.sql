INSERT INTO users (nama, email, password_hash, role) VALUES
 ('Budi Santoso', 'penghuni@gmail.com', '$2a$12$iJpzGAGKkwgTuhy1OGFvTuQ4s5MuqJVNsikVGiULiW/0ZrpQ05OM.', 'PENGHUNI')
ON CONFLICT (email) DO NOTHING;
INSERT INTO tipe_kamar (nama, harga_bulanan, luas_m2, fasilitas) VALUES
 ('Standar', 1200000, 9,  'Kasur, lemari, kipas angin, WiFi'),
 ('Deluxe',  1800000, 12, 'Kasur, lemari, AC, kamar mandi dalam, WiFi'),
 ('Premium', 2400000, 16, 'Kasur, lemari, AC, kamar mandi dalam, meja kerja, WiFi')
ON CONFLICT (nama) DO NOTHING;
INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status)
SELECT '101', 1, id, 'TERISI' FROM tipe_kamar WHERE nama = 'Standar'
ON CONFLICT (nomor_kamar) DO NOTHING;
INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status)
SELECT '102', 1, id, 'TERSEDIA' FROM tipe_kamar WHERE nama = 'Standar'
ON CONFLICT (nomor_kamar) DO NOTHING;
INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status)
SELECT '201', 2, id, 'TERSEDIA' FROM tipe_kamar WHERE nama = 'Deluxe'
ON CONFLICT (nomor_kamar) DO NOTHING;
INSERT INTO kamar (nomor_kamar, lantai, tipe_kamar_id, status)
SELECT '202', 2, id, 'PERBAIKAN' FROM tipe_kamar WHERE nama = 'Premium'
ON CONFLICT (nomor_kamar) DO NOTHING;
INSERT INTO penghuni (user_id, kamar_id, nama_lengkap, no_hp, tanggal_masuk)
SELECT u.id, k.id, 'Budi Santoso', '081234567890', DATE '2026-09-01'
FROM users u
JOIN kamar k ON k.nomor_kamar = '101'
WHERE u.email = 'penghuni@gmail.com'
ON CONFLICT (user_id) DO NOTHING;
INSERT INTO pembayaran (penghuni_id, periode, jumlah, jatuh_tempo, tanggal_bayar, metode, status)
SELECT p.id, DATE '2026-10-01', 1200000, DATE '2026-10-05', DATE '2026-10-03', 'TRANSFER', 'LUNAS'
FROM penghuni p
JOIN users u ON u.id = p.user_id
WHERE u.email = 'penghuni@gmail.com'
ON CONFLICT (penghuni_id, periode) DO NOTHING;
INSERT INTO pembayaran (penghuni_id, periode, jumlah, jatuh_tempo, tanggal_bayar, metode, status)
SELECT p.id, DATE '2026-11-01', 1200000, DATE '2026-11-05', NULL, NULL, 'TERLAMBAT'
FROM penghuni p
JOIN users u ON u.id = p.user_id
WHERE u.email = 'penghuni@gmail.com'
ON CONFLICT (penghuni_id, periode) DO NOTHING;
INSERT INTO keluhan (penghuni_id, judul, deskripsi, kategori, prioritas)
SELECT p.id, 'Kipas angin mati',
       'Kipas angin di kamar 101 tidak menyala sejak kemarin.', 'FASILITAS', 'SEDANG'
FROM penghuni p
JOIN users u ON u.id = p.user_id
WHERE u.email = 'penghuni@gmail.com'
  AND NOT EXISTS (
      SELECT 1 FROM keluhan k
      WHERE k.penghuni_id = p.id AND k.judul = 'Kipas angin mati'
  );
