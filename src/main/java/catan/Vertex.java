package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Vertex")
public class Vertex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    int codi;

    protected Vertex() {

    }

    public Vertex(int codi) {
        this.codi = codi;
    }

}