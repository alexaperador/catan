package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Construccio")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Construccio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "propietari_id", nullable = false)
    private Jugador propietari;

    protected Construccio() {

    }

    protected Construccio(Jugador propietari) {
        this.propietari = propietari;
    }

    public int getId() {
        return id;
    }

    public Jugador getPropietari() {
        return propietari;
    }
}