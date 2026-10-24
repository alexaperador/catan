
package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
<<<<<<< HEAD
=======
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
>>>>>>> dev/badre
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Carretera")
public class Carretera extends Construccio{

   @OneToOne(optional = false, fetch = FetchType.LAZY)
   @JoinColumn(name = "aresta_id", nullable = false, unique = true)
   private Aresta aresta;

    protected Carretera() {

    }

    public Carretera(Jugador propietari, Aresta aresta) {
        super(propietari);
        this.aresta = aresta;
    }
    
    public Aresta getAresta() {
        return aresta;
    }

}