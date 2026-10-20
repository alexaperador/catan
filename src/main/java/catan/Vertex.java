package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "Vertex")
public class Vertex {
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
