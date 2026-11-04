package catan.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "HexagonVertex")
@NamedQuery(name = "HexagonVertex.assentamentsPerPartida", query = "SELECT hv.hexagon.id, a "
        + "FROM HexagonVertex hv, Assentament a "
        + "WHERE a.vertex = hv.vertex AND hv.hexagon.partida.id = :idPartida "
        + "ORDER BY hv.hexagon.codi, hv.posicio")
public class HexagonVertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El hexàgon és obligatori")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hexagon_id", nullable = false)
    private Hexagon hexagon;

    @NotNull(message = "El vertex és obligatori")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;

    @Min(0)
    @Max(5)
    @Column(name = "posicio", nullable = false)
    private int posicio;

    protected HexagonVertex() {
    }

    public HexagonVertex(Hexagon hexagon, Vertex vertex, int posicio) {
        this.hexagon = hexagon;
        this.vertex = vertex;
        this.posicio = posicio;
    }

    public Integer getId() {
        return id;
    }

    public Hexagon getHexagon() {
        return hexagon;
    }

    public void setHexagon(Hexagon hexagon) {
        this.hexagon = hexagon;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public int getPosicio() {
        return posicio;
    }

    public void setPosicio(int posicio) {
        this.posicio = posicio;
    }
}