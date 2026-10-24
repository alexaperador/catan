package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Poblat")
<<<<<<< HEAD
public class Poblat extends Assentament {

    public Poblat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

    protected Poblat() {
=======
@Inheritance(strategy = InheritanceType.JOINED)
public class Poblat extends Assentament {



    protected Poblat(){  
>>>>>>> dev/badre
    }

    public Poblat(Jugador propietari, Vertex vertex) {
         super(propietari, vertex); 
        }

}
