package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;



@Entity
@Table(name = "Poblat")
@Inheritance(strategy = InheritanceType.JOINED)
public class Poblat extends Assentament {



    protected Poblat(){  
    }

    public Poblat(Jugador propietari, Vertex vertex) {
         super(propietari, vertex); 
        }

}


