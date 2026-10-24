package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
<<<<<<< HEAD
=======
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
>>>>>>> dev/badre
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Assentament")
public abstract class Assentament extends Construccio {

<<<<<<< HEAD
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", unique = true, nullable = false)
    private Vertex vertex;

=======
public abstract class Assentament extends Construccio {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false, unique = true)

    Vertex vertex;

>>>>>>> dev/badre
    protected Assentament() {

    }

<<<<<<< HEAD
    public Assentament(Jugador propietari, Vertex vertex) {
=======
    protected Assentament(Jugador propietari, Vertex vertex) {
>>>>>>> dev/badre
        super(propietari);
        this.vertex = vertex;
    }

<<<<<<< HEAD
    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

=======
>>>>>>> dev/badre
}