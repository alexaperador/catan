package catan;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "HexagonVertex")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(length = 30)
public abstract class HexagonVertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotNull(message = "La pocisio és obligatoria")
    @Column(name = "posicio", nullable = false)
    private int posicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hexagon_id", nullable = false)
	private Hexagon hexagon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vertex_id", nullable = false)
	private Vertex vertex;

    public HexagonVertex() {
    }

    public HexagonVertex(Hexagon hexagon, int posicio, Vertex vertex) {
        this.hexagon = hexagon;
        this.posicio = posicio;
        this.vertex = vertex;
    }

    public int getPosicio() {
        return posicio;
    }

    public void setPosicio(int posicio) {
        this.posicio = posicio;
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

    public Integer getId() {
        return id;
    }

}