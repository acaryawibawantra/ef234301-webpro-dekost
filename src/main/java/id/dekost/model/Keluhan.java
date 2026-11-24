package id.dekost.model;

import java.time.LocalDateTime;

public class Keluhan {

    private Integer id;
    private Integer penghuniId;
    private Integer ditanganiOleh;
    private String judul;
    private String deskripsi;
    private String kategori;
    private String prioritas;
    private String status;
    private String tanggapan;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Keluhan() {
    }

    public Keluhan(Integer id, Integer penghuniId, Integer ditanganiOleh, String judul, String deskripsi,
                   String kategori, String prioritas, String status, String tanggapan,
                   LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.penghuniId = penghuniId;
        this.ditanganiOleh = ditanganiOleh;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.prioritas = prioritas;
        this.status = status;
        this.tanggapan = tanggapan;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPenghuniId() {
        return penghuniId;
    }

    public void setPenghuniId(Integer penghuniId) {
        this.penghuniId = penghuniId;
    }

    public Integer getDitanganiOleh() {
        return ditanganiOleh;
    }

    public void setDitanganiOleh(Integer ditanganiOleh) {
        this.ditanganiOleh = ditanganiOleh;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTanggapan() {
        return tanggapan;
    }

    public void setTanggapan(String tanggapan) {
        this.tanggapan = tanggapan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
