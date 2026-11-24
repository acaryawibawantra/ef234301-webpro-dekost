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
| | | |

### Parameter form

| Parameter form | Tipe | Aturan |
|---|---|---|
| | | |

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
