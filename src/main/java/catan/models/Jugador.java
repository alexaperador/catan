package catan.models;

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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jugadors")
@NamedQuery(name = "Jugador.perPartida", query = "FROM Jugador j WHERE j.partida.id = :idPartida ORDER BY j.ordreTorn")
public class Jugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El nom és obligatori")
    @NotBlank(message = "El nom no pot estar buit")
    @Size(max = 50, message = "El nom no pot superar els {max} caràcters")
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El color és obligatori")
    @Column(name = "color", nullable = false, length = 20)
    private ColorJugador color;

    @Min(value = 1, message = "L'ordre del torn ha de ser com a mínim {value}")
    @Max(value = 4, message = "L'ordre del torn ha de ser com a màxim {value}")
    @Column(name = "ordre_torn", nullable = false)
    private int ordreTorn;

    @Min(value = 0, message = "El nombre de cavallers jugats ha de ser com a mínim {value}")
    @Column(name = "cavallers_jugats", nullable = false)
    private int cavallersJugats;

    @NotNull(message = "La partida és obligatòria")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecursJugador> recursos = new ArrayList<>();

    @OneToMany(mappedBy = "ma", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartaDesenvolupament> cartesDesenvolupament = new ArrayList<>();

    @OneToMany(mappedBy = "propietari", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Construccio> construccions = new ArrayList<>();

    protected Jugador() {
    }

    public Jugador(String nom, ColorJugador color, int ordreTorn, Partida partida) {
        this.nom = nom;
        this.color = color;
        this.ordreTorn = ordreTorn;
        this.partida = partida;
    }

    public Integer getId() {
        return id;
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

    public List<CartaDesenvolupament> getCartesDesenvolupament() {
        return cartesDesenvolupament;
    }

    public void setCartesDesenvolupament(List<CartaDesenvolupament> cartesDesenvolupament) {
        this.cartesDesenvolupament = cartesDesenvolupament;
    }

    public List<Construccio> getConstruccions() {
        return construccions;
    }

    public void setConstruccions(List<Construccio> construccions) {
        this.construccions = construccions;
    }

    @Override
    public String toString() {
        return "Jugador{id=" + id + ", nom=" + nom + ", color=" + color
                + ", ordreTorn=" + ordreTorn + ", cavallersJugats=" + cavallersJugats + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Jugador))
            return false;
        Jugador other = (Jugador) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Jugador.class.hashCode();
    }

}
