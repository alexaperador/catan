package catan.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ciutat")
public class Ciutat extends Assentament {

    public Ciutat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

    protected Ciutat() {
    }

}