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

@Entity
@Table(name = "Aresta")
@Inheritance(strategy = InheritanceType.JOINED)
=======
@Entity
@Table(name = "Aresta")
@Inheritance(strategy = InheritanceType.JOINED)


>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
public class Aresta extends Carretera{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(name = "id")
=======
    @Column(name = "Aresta")

>>>>>>> 3c8b7851f329303d7f256921fc70c8ccf99f37d4
    int id;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getCodi() {
        return codi;
    }
    public void setCodi(int codi) {
        this.codi = codi;
    }
    public Aresta(int id, int codi) {
        this.id = id;
        this.codi = codi;
    }
    int codi;


    protected Aresta(){

    }

    public Aresta (int id, int codi){
        this.id = id;
        this.codi = codi;
    }

}