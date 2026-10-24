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
import jakarta.validation.constraints.Min;


@Entity
@Table(name = "Aresta")
public class Aresta {
<<<<<<< HEAD

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_a_id", nullable = false)
    private Vertex vertexA;

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
=======
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
>>>>>>> dev/badre
        this.codi = codi;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
    }
<<<<<<< HEAD

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    protected Aresta() {

    }

    public Aresta(Partida partida, Vertex vertexA, Vertex vertexB, int codi) {
        this.partida = partida;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
        this.codi = codi;
    }

=======
>>>>>>> dev/badre
}