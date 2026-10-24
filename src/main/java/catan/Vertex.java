package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
<<<<<<< HEAD
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
=======
>>>>>>> dev/badre
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;


@Entity
@Table(name = "Vertex")
public class Vertex {
<<<<<<< HEAD
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @Min(1)
    @Column(name = "codi")
    private int codi;

    protected Vertex() {

    }

    public Vertex(Partida partida, int codi) {
        this.partida = partida;
        this.codi = codi;
    }

    public Integer getId() {
        return id;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(int codi) {
        this.codi = codi;
    }

}
=======
    public int getId() {
        return id;
    }
    public int getCodi() {
        return codi;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "codi", nullable = false)
    private int codi;

    protected Vertex() {}
    public Vertex(int codi) { this.codi = codi; }

}
>>>>>>> dev/badre
