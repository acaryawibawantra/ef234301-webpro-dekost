package id.dekost.model;

public class Kamar {

    private Integer id;
    private String nomorKamar;
    private Integer lantai;
    private Integer tipeKamarId;
    private String status;
    private String catatan;

    public Kamar() {
    }

    public Kamar(Integer id, String nomorKamar, Integer lantai, Integer tipeKamarId, String status,
                 String catatan) {
        this.id = id;
        this.nomorKamar = nomorKamar;
        this.lantai = lantai;
        this.tipeKamarId = tipeKamarId;
        this.status = status;
        this.catatan = catatan;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomorKamar() {
        return nomorKamar;
    }

    public void setNomorKamar(String nomorKamar) {
        this.nomorKamar = nomorKamar;
    }

    public Integer getLantai() {
        return lantai;
    }

    public void setLantai(Integer lantai) {
        this.lantai = lantai;
    }

    public Integer getTipeKamarId() {
        return tipeKamarId;
    }

    public void setTipeKamarId(Integer tipeKamarId) {
        this.tipeKamarId = tipeKamarId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}
