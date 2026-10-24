package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;



@Entity
@Table(name = "Ciutat")
<<<<<<< HEAD
public class Ciutat extends Assentament {

    public Ciutat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

=======
@Inheritance(strategy = InheritanceType.JOINED)
public class Ciutat extends Assentament {
>>>>>>> dev/badre
    protected Ciutat(){  
    }


public Ciutat(Jugador propietari, Vertex vertex) {
     super(propietari, vertex); 
    }

}