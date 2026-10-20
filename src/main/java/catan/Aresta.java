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


@Entity
@Table(name = "Aresta")
public class Aresta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "codi", nullable = false)
    private int codi;

    public int getId() {
        return id;
    }
    public int getCodi() {
        return codi;
    }
    public Vertex getVertexA() {
        return vertexA;
    }
    public Vertex getVertexB() {
        return vertexB;
    }
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_a_id", nullable = false)
    private Vertex vertexA;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_b_id", nullable = false)
    private Vertex vertexB;

    protected Aresta() {}
    public Aresta(int codi, Vertex vertexA, Vertex vertexB) {
        this.codi = codi;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
    }
}