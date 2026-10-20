package catan;

import java.util.ArrayList;
import java.util.List;

import catan.enums.TipusTerreny;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Hexagon")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(length = 30)
public abstract class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codi")
    private Integer codi;

    @NotNull(message = "El tipus de terreny és obligatori")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 15)
    private TipusTerreny terreny;

    @Min(2)
    @Max(12)
    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Column(name = "q")
    private int q;

    @Column(name = "r")
    private int r;

    @OneToMany(mappedBy = "hexagon", cascade = CascadeType.ALL)
    private List<HexagonVertex> HexagonVertexs = new ArrayList<>();

    public Hexagon() {
        
    }

    public Hexagon(Integer codi, Integer numero, int q, int r, TipusTerreny terreny, List<HexagonVertex> HexagonVertexs) {
        this.codi = codi;
        this.numero = numero;
        this.q = q;
        this.r = r;
        this.terreny = terreny;
        this.HexagonVertexs = HexagonVertexs;
    }

    public Integer getCodi() {
        return codi;
    }

    public void setCodi(Integer codi) {
        this.codi = codi;
    }

    public TipusTerreny getTerreny() {
        return terreny;
    }

    public void setTerreny(TipusTerreny terreny) {
        this.terreny = terreny;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public List<HexagonVertex> getHexagonVertexs() {
        return HexagonVertexs;
    }

    public void setHexagonVertexs(List<HexagonVertex> HexagonVertexs) {
        this.HexagonVertexs = HexagonVertexs;
    }

    public Integer getId() {
        return id;
    }

}