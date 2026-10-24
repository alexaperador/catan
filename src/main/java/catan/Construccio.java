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
<<<<<<< HEAD
=======

>>>>>>> dev/badre
public abstract class Construccio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
<<<<<<< HEAD
    private Integer id;

    @ManyToOne(optional = false)
=======
    private int id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
>>>>>>> dev/badre
    @JoinColumn(name = "propietari_id", nullable = false)
    private Jugador propietari;

    protected Construccio() {

    }

    protected Construccio(Jugador propietari) {
        this.propietari = propietari;
    }

<<<<<<< HEAD
    public Integer getId() {
=======
    public int getId() {
>>>>>>> dev/badre
        return id;
    }

    public Jugador getPropietari() {
        return propietari;
<<<<<<< HEAD
    }

    public void setPropietari(Jugador propietari) {
        this.propietari = propietari;
=======
>>>>>>> dev/badre
    }
}