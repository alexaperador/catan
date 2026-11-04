package catan;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Check;

import catan.enums.TipusTerreny;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Hexagon")
@NamedQuery(name = "Hexagon.perPartida", query = "FROM Hexagon h WHERE h.partida.id = :idPartida ORDER BY h.codi")
public class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El codi és obligatori")
    @Min(1)
    @Column(name = "codi", nullable = false)
    private Integer codi;

    @NotNull(message = "El tipus de terreny és obligatori")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 15)
    private TipusTerreny terreny;

    @Min(2)
    @Max(12)
    @Check(constraints = "numero IS NULL OR numero <> 7")
    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Column(name = "q", nullable = false)
    private int q;

    @Column(name = "r", nullable = false)
    private int r;

    @NotNull(message = "La partida és obligatòria")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @OneToMany(mappedBy = "hexagon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HexagonVertex> hexagonVertexs = new ArrayList<>();

    public Hexagon(Partida partida, int codi, TipusTerreny terreny, Integer numero, int q, int r) {
        this.partida = partida;
        this.codi = codi;
        this.terreny = terreny;
        this.numero = numero;
        this.q = q;
        this.r = r;
    }

    protected Hexagon() {
    }

    public Integer getId() {
        return id;
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

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<HexagonVertex> getHexagonVertexs() {
        return hexagonVertexs;
    }

    public void setHexagonVertexs(List<HexagonVertex> hexagonVertexs) {
        this.hexagonVertexs = hexagonVertexs;
    }

    @Override
    public String toString() {
        return "Hexagon [id=" + id + ", codi=" + codi + ", terreny=" + terreny + ", numero=" + numero + ", q=" + q
                + ", r=" + r + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Hexagon))
            return false;
        Hexagon other = (Hexagon) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hexagon.class.hashCode();
    }

}