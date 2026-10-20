package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Assentament")
public abstract class Assentament extends Construccio {

    @Column(name = "Vertex")
    @OneToOne(optional = false)
    @JoinColumn(name = "vertex_id", unique = true)
    Vertex vertex;

    protected Assentament() {

    }

    public Assentament(Vertex vertex) {
        this.vertex = vertex;
    }
}