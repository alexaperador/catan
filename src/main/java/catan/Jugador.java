package catan;

import java.util.ArrayList;
import java.util.List;

import catan.enums.ColorJugador;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jugadors")
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "El nom és obligatori")
    @Size(max = 50, message = "El nom no pot superar els {max} caràcters")
    @Column(name = "nom", nullable = false, unique = true, length = 50)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 20)
    private ColorJugador color;

    @Column(name = "ordre_torn", nullable = false)
    private int ordreTorn;

    @Column(name = "cavallers_jugats", nullable = false)
    private int cavallersJugats;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecursJugador> recursos = new ArrayList<>();

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartaDesenvolupament> ma = new ArrayList<>();

    public Jugador() {
    }

    public Jugador(int cavallersJugats, ColorJugador color, String nom, int ordreTorn, Partida partida, 
            List<RecursJugador>recursos, List<CartaDesenvolupament> ma) {
        this.cavallersJugats = cavallersJugats;
        this.color = color;
        this.nom = nom;
        this.ordreTorn = ordreTorn;
        this.partida = partida;
        this.recursos = recursos;
        this.ma = ma;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ColorJugador getColor() {
        return color;
    }

    public void setColor(ColorJugador color) {
        this.color = color;
    }

    public int getOrdreTorn() {
        return ordreTorn;
    }

    public void setOrdreTorn(int ordreTorn) {
        this.ordreTorn = ordreTorn;
    }

    public int getCavallersJugats() {
        return cavallersJugats;
    }

    public void setCavallersJugats(int cavallersJugats) {
        this.cavallersJugats = cavallersJugats;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<RecursJugador> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<RecursJugador> recursos) {
        this.recursos = recursos;
    }

    public List<CartaDesenvolupament> getma() {
        return ma;
    }

    public void setCartesDesenvolupament(List<CartaDesenvolupament> ma) {
        this.ma = ma;
    }

    public int getId() {
        return id;
    }


}
