CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    nama          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'PENGHUNI'
                  CHECK (role IN ('ADMIN', 'PENGHUNI')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS tipe_kamar (
    id            SERIAL PRIMARY KEY,
    nama          VARCHAR(50)   NOT NULL UNIQUE,
    harga_bulanan NUMERIC(12,2) NOT NULL CHECK (harga_bulanan >= 0),
    luas_m2       INT           NOT NULL CHECK (luas_m2 > 0),
    fasilitas     TEXT
);
CREATE TABLE IF NOT EXISTS kamar (
    id            SERIAL PRIMARY KEY,
    nomor_kamar   VARCHAR(10) NOT NULL UNIQUE,
    lantai        INT         NOT NULL CHECK (lantai > 0),
    tipe_kamar_id INT         NOT NULL REFERENCES tipe_kamar(id) ON DELETE RESTRICT,
    status        VARCHAR(10) NOT NULL DEFAULT 'TERSEDIA'
                  CHECK (status IN ('TERSEDIA', 'TERISI', 'PERBAIKAN')),
    catatan       TEXT
);
CREATE TABLE IF NOT EXISTS penghuni (
    id                 SERIAL PRIMARY KEY,
    user_id            INT UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    kamar_id           INT UNIQUE REFERENCES kamar(id) ON DELETE SET NULL,
    nama_lengkap       VARCHAR(100) NOT NULL,
    no_hp              VARCHAR(20)  NOT NULL,
    pekerjaan_instansi VARCHAR(100),
    kontak_darurat     VARCHAR(100),
    tanggal_masuk      DATE NOT NULL,
    tanggal_keluar     DATE,
    status             VARCHAR(10) NOT NULL DEFAULT 'AKTIF'
                       CHECK (status IN ('AKTIF', 'NONAKTIF')),
    CHECK (tanggal_keluar IS NULL OR tanggal_keluar >= tanggal_masuk)
);
CREATE TABLE IF NOT EXISTS pembayaran (
    id            SERIAL PRIMARY KEY,
    penghuni_id   INT           NOT NULL REFERENCES penghuni(id) ON DELETE CASCADE,
    periode       DATE          NOT NULL,
    jumlah        NUMERIC(12,2) NOT NULL CHECK (jumlah >= 0),
    jatuh_tempo   DATE          NOT NULL,
    tanggal_bayar DATE,
    metode        VARCHAR(10)   CHECK (metode IN ('TUNAI', 'TRANSFER', 'QRIS')),
    status        VARCHAR(12)   NOT NULL DEFAULT 'BELUM_BAYAR'
                  CHECK (status IN ('BELUM_BAYAR', 'LUNAS', 'TERLAMBAT')),
    bukti_url     VARCHAR(255),
    UNIQUE (penghuni_id, periode)
);
CREATE TABLE IF NOT EXISTS keluhan (
    id             SERIAL PRIMARY KEY,
    penghuni_id    INT          NOT NULL REFERENCES penghuni(id) ON DELETE CASCADE,
    ditangani_oleh INT          REFERENCES users(id) ON DELETE SET NULL,
    judul          VARCHAR(150) NOT NULL,
    deskripsi      TEXT         NOT NULL,
    kategori       VARCHAR(12)  NOT NULL DEFAULT 'LAINNYA'
                   CHECK (kategori IN ('FASILITAS', 'KEBERSIHAN', 'KEBISINGAN', 'LAINNYA')),
    prioritas      VARCHAR(8)   NOT NULL DEFAULT 'SEDANG'
                   CHECK (prioritas IN ('RENDAH', 'SEDANG', 'TINGGI')),
    status         VARCHAR(10)  NOT NULL DEFAULT 'BARU'
                   CHECK (status IN ('BARU', 'DIPROSES', 'SELESAI')),
    tanggapan      TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    resolved_at    TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_kamar_status       ON kamar(status);
CREATE INDEX IF NOT EXISTS idx_pembayaran_status  ON pembayaran(status, jatuh_tempo);
CREATE INDEX IF NOT EXISTS idx_keluhan_status     ON keluhan(status);
