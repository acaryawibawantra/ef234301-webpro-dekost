package id.dekost.model;

import java.math.BigDecimal;

public class TipeKamar {

    private Integer id;
    private String nama;
    private BigDecimal hargaBulanan;
    private Integer luasM2;
    private String fasilitas;

    public TipeKamar() {
    }

    public TipeKamar(Integer id, String nama, BigDecimal hargaBulanan, Integer luasM2, String fasilitas) {
        this.id = id;
        this.nama = nama;
        this.hargaBulanan = hargaBulanan;
        this.luasM2 = luasM2;
        this.fasilitas = fasilitas;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public BigDecimal getHargaBulanan() {
        return hargaBulanan;
    }

    public void setHargaBulanan(BigDecimal hargaBulanan) {
        this.hargaBulanan = hargaBulanan;
    }

    public Integer getLuasM2() {
        return luasM2;
    }

    public void setLuasM2(Integer luasM2) {
        this.luasM2 = luasM2;
    }

    public String getFasilitas() {
        return fasilitas;
    }

    public void setFasilitas(String fasilitas) {
        this.fasilitas = fasilitas;
    }
}
