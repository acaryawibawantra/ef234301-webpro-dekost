package id.dekost.model;

import java.time.LocalDate;

public class Penghuni {

    private Integer id;
    private Integer userId;
    private Integer kamarId;
    private String namaLengkap;
    private String noHp;
    private String pekerjaanInstansi;
    private String kontakDarurat;
    private LocalDate tanggalMasuk;
    private LocalDate tanggalKeluar;
    private String status;

    public Penghuni() {
    }

    public Penghuni(Integer id, Integer userId, Integer kamarId, String namaLengkap, String noHp,
                    String pekerjaanInstansi, String kontakDarurat, LocalDate tanggalMasuk,
                    LocalDate tanggalKeluar, String status) {
        this.id = id;
        this.userId = userId;
        this.kamarId = kamarId;
        this.namaLengkap = namaLengkap;
        this.noHp = noHp;
        this.pekerjaanInstansi = pekerjaanInstansi;
        this.kontakDarurat = kontakDarurat;
        this.tanggalMasuk = tanggalMasuk;
        this.tanggalKeluar = tanggalKeluar;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getKamarId() {
        return kamarId;
    }

    public void setKamarId(Integer kamarId) {
        this.kamarId = kamarId;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getPekerjaanInstansi() {
        return pekerjaanInstansi;
    }

    public void setPekerjaanInstansi(String pekerjaanInstansi) {
        this.pekerjaanInstansi = pekerjaanInstansi;
    }

    public String getKontakDarurat() {
        return kontakDarurat;
    }

    public void setKontakDarurat(String kontakDarurat) {
        this.kontakDarurat = kontakDarurat;
    }

    public LocalDate getTanggalMasuk() {
        return tanggalMasuk;
    }

    public void setTanggalMasuk(LocalDate tanggalMasuk) {
        this.tanggalMasuk = tanggalMasuk;
    }

    public LocalDate getTanggalKeluar() {
        return tanggalKeluar;
    }

    public void setTanggalKeluar(LocalDate tanggalKeluar) {
        this.tanggalKeluar = tanggalKeluar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
