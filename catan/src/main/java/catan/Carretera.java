
package catan;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Carretera")
public class Carretera extends Construccio {

    @NotNull(message = "L'aresta és obligatòria")
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

    public void setAresta(Aresta aresta) {
        this.aresta = aresta;
    }

}