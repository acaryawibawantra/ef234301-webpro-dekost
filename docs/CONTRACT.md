# Kontrak FE–BE

Kerangka kontrak antara frontend dan backend. Detail nama request attribute dan
parameter form **belum diisi**; akan disepakati tim pada akhir Fase 1.

## Dashboard

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `roomOccupancy` | `Map<String, Long>` | Jumlah kamar berdasarkan status `TERSEDIA`, `TERISI`, dan `PERBAIKAN` |
| `floorOccupancy` | `List<DashboardDao.FloorOccupancy>` | Jumlah kamar dan kamar terisi per lantai |
| `monthlyRevenue` | `BigDecimal` | Total pembayaran berstatus `LUNAS` untuk periode bulan berjalan |
| `overdueBilling` | `DashboardDao.BillingSummary` | Jumlah dan total nominal tagihan belum lunas yang jatuh tempo sebelum hari ini |
| `openComplaints` | `DashboardDao.OpenComplaintSummary` | Jumlah keluhan belum selesai dan jumlah berprioritas `TINGGI` |
| `duePayments` | `List<Pembayaran>` | Maksimal 5 pembayaran belum lunas, diurutkan dari jatuh tempo terdekat; status tampilan dihitung dengan SQL `CASE` |
| `recentComplaints` | `List<Keluhan>` | Maksimal 3 keluhan terbaru berdasarkan `created_at` |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| — | — | Dashboard hanya membaca data dan tidak memiliki parameter form |

`DashboardDao` menyediakan query agregat untuk seluruh statistik di atas. Servlet dashboard
meneruskan hasilnya ke atribut request; JSP hanya menampilkan data tersebut.

## Kamar

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `kamarList` | `List<Kamar>` | Hasil pencarian yang sudah dipaginasi; tiap kamar juga memiliki `tipeKamarNama` |
| `tipeKamarOptions` | `List<TipeKamar>` | Pilihan tipe kamar untuk form |
| `pagination` | `Pagination` | Metadata pagination, 10 baris per halaman |
| `q` | String | Nilai pencarian nomor kamar yang sudah di-trim |
| `status` | String | Filter status aktif |
| `form` | `Kamar` | Nilai form tambah/ubah, termasuk nilai input saat validasi gagal |
| `errors` | `Map<String, String>` | Pesan validasi per nama field atau `general` |
| `pageTitle` | String | Judul halaman: `Kamar` |
| `activeNav` | String | Item sidebar aktif: `kamar` |
| `flash` | `Flash.Message` | Pesan setelah operasi POST berhasil/gagal |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `nomorKamar` | String | Wajib, maksimal 10 karakter, unik |
| `lantai` | Integer | Wajib, bilangan bulat lebih besar dari 0 |
| `tipeKamarId` | Integer | Wajib, harus menunjuk tipe kamar yang ada |
| `status` | String | Saat dibuat: `TERSEDIA` (default) atau `PERBAIKAN`; saat diubah hanya untuk status manual `TERSEDIA`/`PERBAIKAN`, bukan `TERISI` |
| `catatan` | String | Opsional |
| `id` | Integer | Wajib untuk update/delete |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`GET /admin/kamar` menerima `q`, `status`, `page`, dan `edit=ID`. Operasi perubahan memakai
`POST /admin/kamar/create`, `/update`, dan `/delete`. Status `TERISI`/`TERSEDIA` dikelola
transaksi penghuni; perubahan manual hanya mengubah status ke/dari `PERBAIKAN`. Kamar dengan
penghuni berstatus `AKTIF` tidak dapat dihapus.

## Penghuni

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `penghuniList` | `List<Penghuni>` | Hasil pencarian yang sudah dipaginasi |
| `kamarOptions` | `List<Kamar>` | Kamar tersedia; kamar saat ini juga disertakan saat membuka form edit |
| `pagination` | `Pagination` | Metadata pagination, 10 baris per halaman |
| `q` | String | Nilai pencarian nama/nomor HP/nomor kamar yang sudah di-trim |
| `status` | String | Filter status aktif |
| `form` | `Penghuni` | Nilai form check-in/ubah/pindah, termasuk input saat validasi gagal |
| `errors` | `Map<String, String>` | Pesan validasi per nama field atau `general` |
| `pageTitle` | String | Judul halaman: `Penghuni` |
| `activeNav` | String | Item sidebar aktif: `penghuni` |
| `flash` | `Flash.Message` | Pesan setelah operasi POST berhasil/gagal |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `namaLengkap` | String | Wajib, maksimal 100 karakter |
| `noHp` | String | Wajib, 9–15 digit |
| `pekerjaanInstansi` | String | Opsional, maksimal 100 karakter |
| `kontakDarurat` | String | Opsional, maksimal 100 karakter |
| `tanggalMasuk` | Date | Wajib, format ISO `yyyy-MM-dd` |
| `kamarId` | Integer | Wajib untuk check-in/pindah; hanya kamar tersedia yang dapat dipilih |
| `id` | Integer | Wajib untuk update/check-out |
| `tanggalKeluar` | Date | Opsional saat check-out; default tanggal hari ini dan tidak boleh sebelum tanggal masuk |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`GET /admin/penghuni` menerima `q`, `status`, `page`, dan `edit=ID`. Check-in memakai
`POST /admin/penghuni/create`, update/pindah memakai `/update`, dan check-out memakai
`/checkout`. Check-in, pindah kamar, dan check-out mengubah data penghuni serta status kamar
dalam satu transaksi; kamar `PERBAIKAN` atau yang sudah memiliki penempatan tidak dapat dipilih.

## Pembayaran

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `pembayaranList` | `List<Pembayaran>` | Hasil pencarian yang sudah dipaginasi; status dihitung dari status tersimpan dan jatuh tempo |
| `penghuniOptions` | `List<PembayaranDao.OccupantOption>` | Pilihan penghuni, kamar, tarif kamar saat ini, dan status penghuni |
| `paymentMethods` | `Set<String>` | Metode yang diizinkan: `TUNAI`, `TRANSFER`, `QRIS` |
| `pagination` | `Pagination` | Metadata pagination, 10 baris per halaman |
| `q` | String | Nilai pencarian nama penghuni/nomor kamar/periode yang sudah di-trim |
| `status` | String | Filter status pembayaran hasil perhitungan SQL |
| `form` | `Pembayaran` | Nilai form tambah/ubah, termasuk nilai input saat validasi gagal |
| `errors` | `Map<String, String>` | Pesan validasi per nama field atau `general` |
| `pageTitle` | String | Judul halaman: `Pembayaran` |
| `activeNav` | String | Item sidebar aktif: `pembayaran` |
| `flash` | `Flash.Message` | Pesan setelah operasi POST berhasil/gagal |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `penghuniId` | Integer | Wajib menunjuk penghuni yang ada; tagihan baru memakai tarif kamar saat ini sebagai jumlah default jika jumlah tidak dikirim |
| `periode` | Date | Wajib format ISO `yyyy-MM-dd` dan harus tanggal pertama bulan tagihan |
| `jumlah` | Decimal | Wajib, 0 atau lebih, maksimal 10 digit dan 2 angka desimal |
| `jatuhTempo` | Date | Wajib format ISO `yyyy-MM-dd`, tidak boleh sebelum periode |
| `buktiUrl` | String | Opsional, maksimal 255 karakter |
| `id` | Integer | Wajib untuk update/delete/pay |
| `tanggalBayar` | Date | Wajib untuk menandai lunas, format ISO `yyyy-MM-dd` |
| `metode` | String | Wajib untuk menandai lunas: `TUNAI`, `TRANSFER`, atau `QRIS` |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`GET /admin/pembayaran` menerima `q`, `status`, `page`, dan `edit=ID`. Operasi perubahan
memakai `POST /admin/pembayaran/create`, `/update`, `/pay`, `/delete`, dan `/generate`.
Status list dihitung oleh SQL: `LUNAS` tetap lunas; selain itu tagihan dengan
`jatuh_tempo < CURRENT_DATE` ditampilkan `TERLAMBAT`, dan sisanya `BELUM_BAYAR`.
Pembayaran yang sudah lunas tidak dapat diubah atau ditandai lunas kembali. Generate
membuat tagihan periode bulan berjalan dengan jatuh tempo tanggal 5 hanya untuk penghuni
aktif yang belum memiliki tagihan; constraint unik dan `ON CONFLICT` mencegah duplikasi.

## Keluhan

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `keluhanList` | `List<Keluhan>` | Hasil pencarian yang sudah dipaginasi; tiap keluhan memuat nama penghuni dan nama admin penangan |
| `penghuniOptions` | `List<KeluhanDao.OccupantOption>` | Pilihan penghuni untuk mencatat keluhan |
| `categories` | `List<String>` | Kategori yang diizinkan |
| `priorities` | `List<String>` | Prioritas yang diizinkan |
| `statuses` | `List<String>` | Status yang diizinkan |
| `pagination` | `Pagination` | Metadata pagination, 10 baris per halaman |
| `q` | String | Nilai pencarian judul/deskripsi/nama penghuni yang sudah di-trim |
| `status` | String | Filter status aktif |
| `form` | `Keluhan` | Nilai form tambah/ubah, termasuk input saat validasi gagal |
| `errors` | `Map<String, String>` | Pesan validasi per nama field atau `general` |
| `pageTitle` | String | Judul halaman: `Keluhan` |
| `activeNav` | String | Item sidebar aktif: `keluhan` |
| `flash` | `Flash.Message` | Pesan setelah operasi POST berhasil/gagal |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `penghuniId` | Integer | Wajib menunjuk penghuni yang ada |
| `judul` | String | Wajib, maksimal 150 karakter |
| `deskripsi` | String | Wajib |
| `kategori` | String | `FASILITAS`, `KEBERSIHAN`, `KEBISINGAN`, atau `LAINNYA`; default `LAINNYA` |
| `prioritas` | String | `RENDAH`, `SEDANG`, atau `TINGGI`; default `SEDANG` |
| `status` | String | Saat dibuat otomatis `BARU`; saat diubah `BARU`, `DIPROSES`, atau `SELESAI` |
| `tanggapan` | String | Opsional |
| `id` | Integer | Wajib untuk update/delete |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`GET /admin/keluhan` menerima `q`, `status`, `page`, dan `edit=ID`. Operasi perubahan
memakai `POST /admin/keluhan/create`, `/update`, dan `/delete`. Saat status berubah menjadi
`DIPROSES`, admin yang sedang login dicatat sebagai `ditangani_oleh`; saat menjadi `SELESAI`,
`resolved_at` diisi oleh database. Status `SELESAI` tidak dapat dikembalikan ke `BARU`.

## Tipe kamar

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `tipeKamarList` | `List<TipeKamar>` | Hasil pencarian yang sudah dipaginasi |
| `pagination` | `Pagination` | Metadata pagination, 10 baris per halaman |
| `q` | String | Nilai pencarian nama yang sudah di-trim |
| `form` | `TipeKamar` | Nilai form tambah/ubah, termasuk nilai input saat validasi gagal |
| `errors` | `Map<String, String>` | Pesan validasi per nama field atau `general` |
| `pageTitle` | String | Judul halaman: `Tipe Kamar` |
| `activeNav` | String | Item sidebar aktif: `tipe-kamar` |
| `flash` | `Flash.Message` | Pesan setelah operasi POST berhasil/gagal |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `nama` | String | Wajib, maksimal 50 karakter, unik |
| `hargaBulanan` | Decimal | Wajib, 0 atau lebih, maksimal 10 digit dan 2 angka desimal |
| `luasM2` | Integer | Wajib, bilangan bulat lebih besar dari 0 |
| `fasilitas` | String | Opsional |
| `id` | Integer | Wajib untuk update/delete |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`GET /admin/tipe-kamar` menerima `q`, `page`, dan `edit=ID`. Operasi perubahan memakai
`POST /admin/tipe-kamar/create`, `/update`, dan `/delete`. Penghapusan ditolak jika tipe
kamar masih direferensikan oleh kamar.

## Landing

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

## Login

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| `csrfToken` | String | Token CSRF untuk form POST |
| `error` | String | Pesan autentikasi gagal; ditampilkan dengan output HTML ter-escape |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| `email` | String | Email akun |
| `password` | String | Password akun; hanya diverifikasi, tidak disimpan di sesi |
| `_csrf` | String | Wajib untuk semua POST; token harus cocok dengan sesi |

`POST /login` memulai sesi dengan timeout 30 menit. `POST /logout` menghapus sesi dan
wajib menyertakan `_csrf`. Area `/admin/*` hanya dapat diakses oleh sesi dengan user ber-role
`ADMIN`; permintaan tanpa sesi dialihkan ke `/login`.
