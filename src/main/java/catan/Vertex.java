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
@Table(name = "Vertex")
@Inheritance(strategy = InheritanceType.JOINED)



public class Vertex extends Assentament{
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Vertex")
    
    int id;
    int codi;


    protected Vertex(){

    }

    public Vertex (int id, int codi){
        this.id = id;
        this.codi = codi;
    }

}