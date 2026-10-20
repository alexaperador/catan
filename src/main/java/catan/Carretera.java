
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
@Table(name = "Carretera")
@Inheritance(strategy = InheritanceType.JOINED)


<<<<<<< HEAD

public class Carretera extends Construccio{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Aresta")
=======
public class Carretera extends Construccio{
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Carretera")
>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4

    Aresta aresta;

    protected Carretera(){

    }

    public Carretera (Aresta aresta){
        this.aresta = aresta;
    }

}