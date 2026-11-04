package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Assentament")
public abstract class Assentament extends Construccio {

    @NotNull(message = "El vertex és obligatori")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", unique = true, nullable = false)
    private Vertex vertex;

    protected Assentament() {

    }

    public Assentament(Jugador propietari, Vertex vertex) {
        super(propietari);
        this.vertex = vertex;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

}