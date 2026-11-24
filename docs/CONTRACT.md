# Kontrak FE–BE

Kerangka kontrak antara frontend dan backend. Detail nama request attribute dan
parameter form **belum diisi**; akan disepakati tim pada akhir Fase 1.

## Dashboard

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

## Kamar

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

## Penghuni

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

## Pembayaran

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

## Keluhan

### Request attribute

| Request attribute | Tipe | Keterangan |
|---|---|---|
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

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
