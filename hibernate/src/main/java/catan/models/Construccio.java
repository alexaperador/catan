package catan.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Construccio")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Construccio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El propietari és obligatori")
    @ManyToOne(optional = false)
    @JoinColumn(name = "propietari_id", nullable = false)
    private Jugador propietari;

    protected Construccio() {

    }

    protected Construccio(Jugador propietari) {
        this.propietari = propietari;
    }

    public Integer getId() {
        return id;
    }

    public Jugador getPropietari() {
        return propietari;
    }

    public void setPropietari(Jugador propietari) {
        this.propietari = propietari;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Construccio))
            return false;
        Construccio other = (Construccio) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Construccio.class.hashCode();
    }
}