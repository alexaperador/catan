package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;





@Entity
@Table(name = "Assentament")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Assentament extends Construccio{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Assentament")
    Vertex vertex;


    protected Assentament(){

    }

    public Assentament(Vertex vertex) {
        this.vertex = vertex;
    }   
}