package catan;

<<<<<<< HEAD


=======
>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
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
@Table(name = "Vertex")
@Inheritance(strategy = InheritanceType.JOINED)


<<<<<<< HEAD
public class Vertex extends Assentament{


@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vertex")
=======

public class Vertex extends Assentament{
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Vertex")
>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
    
    int id;
    int codi;


    protected Vertex(){

    }

    public Vertex (int id, int codi){
        this.id = id;
        this.codi = codi;
    }

}