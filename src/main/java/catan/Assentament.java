package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Assentament")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Assentament extends Construccio {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false, unique = true)

    Vertex vertex;

    protected Assentament() {

    }

    protected Assentament(Jugador propietari, Vertex vertex) {
        super(propietari);
        this.vertex = vertex;
    }

}