package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Aresta", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "partida_id", "codi" }),
        @UniqueConstraint(columnNames = { "partida_id", "vertex_a_id", "vertex_b_id" })
})
public class Aresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La partida és obligatòria")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @NotNull(message = "El vertex A és obligatori")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_a_id", nullable = false)
    private Vertex vertexA;

    @NotNull(message = "El vertex B és obligatori")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_b_id", nullable = false)
    private Vertex vertexB;

    @Min(1)
    @Column(name = "codi", nullable = false)
    private int codi;

    public Integer getId() {
        return id;
    }

    public Vertex getVertexA() {
        return vertexA;
    }

    public void setVertexA(Vertex vertexA) {
        this.vertexA = vertexA;
    }

    public Vertex getVertexB() {
        return vertexB;
    }

    public void setVertexB(Vertex vertexB) {
        this.vertexB = vertexB;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(int codi) {
        this.codi = codi;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    protected Aresta() {

    }

    public Aresta(Partida partida, int codi, Vertex vertexA, Vertex vertexB) {
        this.partida = partida;
        this.codi = codi;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
    }

}