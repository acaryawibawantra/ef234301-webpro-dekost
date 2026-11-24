# UI Kit — De' Kost

Panduan singkat class CSS yang dipakai bersama FE public dan FE admin.
Sumber token: `src/main/webapp/assets/css/tokens.css`.
Urutan load: `tokens.css` → `base.css` → `components.css` → `pages.css`.

## Button

```html
<a class="button button--primary" href="/admin/kamar">Tambah kamar</a>
<button class="button button--accent" type="submit">Simpan</button>
<button class="button button--secondary" type="button">Batal</button>
<button class="button button--ghost button--sm" type="button">Detail</button>
<button class="button button--primary button--block" type="submit">Masuk</button>
```

| Class | Fungsi |
|---|---|
| `button` | Bentuk pill, padding 12×24, label 14/600 |
| `button--primary` | Isi `--ink`, teks putih |
| `button--accent` | Isi `--accent` |
| `button--secondary` | Outline 1.5px `--ink` |
| `button--ghost` | Tanpa isi/border, hover `--surface` |
| `button--block` | Lebar 100% |
| `button--sm` | Versi kecil (8×16) |

## Badge status

```html
<span class="badge badge--success"><span class="badge__dot"></span>Tersedia</span>
<span class="badge badge--warning"><span class="badge__dot"></span>Perbaikan</span>
<span class="badge badge--danger"><span class="badge__dot"></span>Terlambat</span>
<span class="badge badge--accent"><span class="badge__dot"></span>Terisi</span>
<span class="badge badge--neutral"><span class="badge__dot"></span>Nonaktif</span>
```

## Form field

```html
<div class="field">
  <label class="field__label" for="nama">Nama</label>
  <input class="field__input" id="nama" name="nama" type="text">
  <p class="field__error">Nama wajib diisi.</p>
</div>
```

Tersedia `field__input`, `field__select`, `field__textarea`, `field__hint`, `field__error`.
Tambahkan `aria-invalid="true"` pada input yang error. Checkbox memakai `.checkbox`.

## Card dan stat card

```html
<section class="card card--light">...</section>
<section class="card card--dark">...</section>
<section class="card card--outline">...</section>

<div class="stat-card">
  <p class="stat-card__label">Kamar terisi</p>
  <p class="stat-card__value">21 / 24</p>
  <p class="stat-card__hint">3 kamar masih tersedia</p>
</div>

<div class="stat-card stat-card--dark">...</div>
```

## Table

```html
<section class="table-card">
  <header class="table-card__header">
    <div>
      <h2 class="table-card__title">Daftar kamar</h2>
      <p class="table-card__subtitle">24 kamar</p>
    </div>
    <a class="button button--primary" href="#">Tambah kamar</a>
  </header>

  <div class="table-wrap">
    <table class="table">
      <thead>
        <tr><th>Nomor</th><th>Status</th><th class="table__cell--actions">Aksi</th></tr>
      </thead>
      <tbody>
        <tr>
          <td>101</td>
          <td><span class="badge badge--accent"><span class="badge__dot"></span>Terisi</span></td>
          <td class="table__cell--actions">
            <div class="table__actions">
              <button class="table__action" type="button" aria-label="Ubah">…</button>
              <button class="table__action" type="button" aria-label="Hapus">…</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <footer class="table-card__footer">
    <p>Menampilkan 1–10 dari 24</p>
    <nav class="pager" aria-label="Pagination">
      <a class="pager__link pager__link--active" href="#">1</a>
      <a class="pager__link" href="#">2</a>
    </nav>
  </footer>
</section>
```

## Modal

```html
<div class="modal is-open" role="dialog" aria-modal="true" aria-labelledby="modal-title">
  <div class="modal__dialog">
    <header class="modal__header">
      <h2 class="modal__title" id="modal-title">Tambah kamar</h2>
      <button class="modal__close" type="button" aria-label="Tutup">×</button>
    </header>
    <div class="modal__body">
      <div class="form-grid">
        <div class="field">...</div>
        <div class="field">...</div>
      </div>
    </div>
    <footer class="modal__footer">
      <button class="button button--secondary" type="button">Batal</button>
      <button class="button button--primary" type="submit">Simpan</button>
    </footer>
  </div>
</div>
```

`is-open` ditambahkan/dihapus oleh `assets/js/app.js`.

## Toast dan empty state

```html
<div class="toast-stack" aria-live="polite">
  <p class="toast">Kamar berhasil disimpan.</p>
  <p class="toast toast--error">Nomor kamar sudah dipakai.</p>
</div>

<div class="empty-state">
  <p class="empty-state__title">Belum ada kamar</p>
  <p>Tambahkan kamar pertama untuk mulai mengelola hunian.</p>
</div>
```

## Catatan

- Jangan menulis warna/ukuran baru di JSP; pakai token dan class di `components.css`.
- Satu-satunya gradient adalah fade foto footer di landing (lihat `docs/DECISIONS.md`).
- Perubahan class bersama diumumkan ke Acarya sebelum merge.
