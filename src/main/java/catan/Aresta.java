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
@Table(name = "Aresta")
@Inheritance(strategy = InheritanceType.JOINED)


public class Aresta extends Carretera{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Aresta")

    int id;
    int codi;


    protected Aresta(){

    }

    public Aresta (int id, int codi){
        this.id = id;
        this.codi = codi;
    }

}