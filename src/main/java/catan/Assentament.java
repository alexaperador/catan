package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
<<<<<<< HEAD
=======

>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4




@Entity
@Table(name = "Assentament")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Assentament extends Construccio{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(name = "Vertex")
=======
    @Column(name = "Assentament")
>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
    Vertex vertex;


    protected Assentament(){

    }

    public Assentament(Vertex vertex) {
        this.vertex = vertex;
<<<<<<< HEAD
    }    
=======
    }   
>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
}