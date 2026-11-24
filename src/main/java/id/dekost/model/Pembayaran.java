package id.dekost.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pembayaran {

    private Integer id;
    private Integer penghuniId;
    private String penghuniNama;
    private String nomorKamar;
    private LocalDate periode;
    private BigDecimal jumlah;
    private LocalDate jatuhTempo;
    private LocalDate tanggalBayar;
    private String metode;
    private String status;
    private String buktiUrl;

    public Pembayaran() {
    }

    public Pembayaran(Integer id, Integer penghuniId, LocalDate periode, BigDecimal jumlah,
                      LocalDate jatuhTempo, LocalDate tanggalBayar, String metode, String status,
                      String buktiUrl) {
        this.id = id;
        this.penghuniId = penghuniId;
        this.periode = periode;
        this.jumlah = jumlah;
        this.jatuhTempo = jatuhTempo;
        this.tanggalBayar = tanggalBayar;
        this.metode = metode;
        this.status = status;
        this.buktiUrl = buktiUrl;
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

    public String getPenghuniNama() {
        return penghuniNama;
    }

    public void setPenghuniNama(String penghuniNama) {
        this.penghuniNama = penghuniNama;
    }

    public String getNomorKamar() {
        return nomorKamar;
    }

    public void setNomorKamar(String nomorKamar) {
        this.nomorKamar = nomorKamar;
    }

    public LocalDate getPeriode() {
        return periode;
    }

    public void setPeriode(LocalDate periode) {
        this.periode = periode;
    }

    public BigDecimal getJumlah() {
        return jumlah;
    }

    public void setJumlah(BigDecimal jumlah) {
        this.jumlah = jumlah;
    }

    public LocalDate getJatuhTempo() {
        return jatuhTempo;
    }

    public void setJatuhTempo(LocalDate jatuhTempo) {
        this.jatuhTempo = jatuhTempo;
    }

    public LocalDate getTanggalBayar() {
        return tanggalBayar;
    }

    public void setTanggalBayar(LocalDate tanggalBayar) {
        this.tanggalBayar = tanggalBayar;
    }

    public String getMetode() {
        return metode;
    }

    public void setMetode(String metode) {
        this.metode = metode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBuktiUrl() {
        return buktiUrl;
    }

    public void setBuktiUrl(String buktiUrl) {
        this.buktiUrl = buktiUrl;
    }
}
