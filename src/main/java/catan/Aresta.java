package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne 
    private Partida partida;
    
    public int getId() {
        return id;
    }

    public int getCodi() {
        return codi;
    }
    public void setCodi(int codi) {
        this.codi = codi;
    }

    protected Aresta(){

    }

    public Aresta ( int codi){
        this.codi = codi;
    }

}