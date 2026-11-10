package catan.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Poblat")
public class Poblat extends Assentament {

    public Poblat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

    protected Poblat() {
    }

}
