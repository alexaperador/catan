package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;



@Entity
@Table(name = "Ciutat")
@Inheritance(strategy = InheritanceType.JOINED)
public class Ciutat extends Assentament {
    protected Ciutat(){  
    }


public Ciutat(Jugador propietari, Vertex vertex) {
     super(propietari, vertex); 
    }

}