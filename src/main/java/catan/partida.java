package catan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import catan.enums.EstatPartida;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "Partida")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(length = 30)
public abstract class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El estat és obligatori")
    @Enumerated(EnumType.STRING)
    @Column(name = "estat", nullable = false, length = 10)
    private EstatPartida estat;

    @NotNull(message = "La data d'inici és obligatòria")
    @PastOrPresent(message = "La data de llançament no pot ser futura")
    @Column(name = "data_inici", nullable = false)
    private LocalDate dataInici;

    @Min(value = 1)
    @Column(name = "ronda")
    private Integer ronda;

    @Column(name = "ultima_tirada")
    private Integer ultimaTirada;

    @Column(name = "punts_per_guanyar")
    private Integer puntsPerGuanyar;

    @Column(name = "daus_tirats")
    private Boolean dausTirats;

    @Column(name = "lladre_pendent")
    private Boolean lladrePendent;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Jugador> jugadors = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "jugador_actiu_id", nullable = true)
    private Jugador actiu;

    @ManyToOne
    @JoinColumn(name = "guanyador_id", nullable = true)
    private Jugador guanyador;

    @OneToOne
    @JoinColumn(name = "lladre", nullable = true)
    private Hexagon lladre;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartaDesenvolupament> pila = new ArrayList<>();
    
    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Hexagon> tauler = new ArrayList<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Aresta> arestes = new ArrayList<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vertex> vertexs = new ArrayList<>();

    public Partida() {
    }

    public Partida(Jugador actiu, LocalDate dataInici, Boolean dausTirats, EstatPartida estat, Jugador guanyador, Boolean lladrePendent, 
            Integer puntsPerGuanyar, Integer ronda, Integer ultimaTirada, List<Jugador> jugadors, List<CartaDesenvolupament> pila, 
            List<Hexagon> tauler, List<Aresta> arestes, List<Vertex> vertexs, Hexagon lladre) {
        this.actiu = actiu;
        this.dataInici = dataInici;
        this.dausTirats = dausTirats;
        this.estat = estat;
        this.guanyador = guanyador; 
        this.lladrePendent = lladrePendent;
        this.puntsPerGuanyar = puntsPerGuanyar;
        this.ronda = ronda;
        this.jugadors = jugadors;
        this.pila = pila;
        this.tauler = tauler;
        this.arestes = arestes;
        this.vertexs = vertexs;
        this.lladre = lladre;
    }

    public EstatPartida getEstat() {
        return estat;
    }

    public void setEstat(EstatPartida estat) {
        this.estat = estat;
    }

    public LocalDate getDataInici() {
        return dataInici;
    }

    public void setDataInici(LocalDate dataInici) {
        this.dataInici = dataInici;
    }

    public Integer getRonda() {
        return ronda;
    }

    public void setRonda(Integer ronda) {
        this.ronda = ronda;
    }

    public Integer getUltimaTirada() {
        return ultimaTirada;
    }

    public void setUltimaTirada(Integer ultimaTirada) {
        this.ultimaTirada = ultimaTirada;
    }

    public Integer getPuntsPerGuanyar() {
        return puntsPerGuanyar;
    }

    public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {
        this.puntsPerGuanyar = puntsPerGuanyar;
    }

    public Boolean getDausTirats() {
        return dausTirats;
    }

    public void setDausTirats(Boolean dausTirats) {
        this.dausTirats = dausTirats;
    }

    public Boolean getLladrePendent() {
        return lladrePendent;
    }

    public void setLladrePendent(Boolean lladrePendent) {
        this.lladrePendent = lladrePendent;
    }

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public Jugador getActiu() {
        return actiu;
    }

    public void setActiu(Jugador actiu) {
        this.actiu = actiu;
    }

    public Jugador getGuanyador() {
        return guanyador;
    }

    public void setGuanyador(Jugador guanyador) {
        this.guanyador = guanyador;
    }

    public List<CartaDesenvolupament> getPila() {
        return pila;
    }

    public void setPila(List<CartaDesenvolupament> pila) {
        this.pila = pila;
    }

    public Integer getId() {
        return id;
    }

    public List<Hexagon> getTauler() {
        return tauler;
    }

    public void setTauler(List<Hexagon> tauler) {
        this.tauler = tauler;
    }

    public List<Aresta> getArestes() {
        return arestes;
    }

    public void setArestes(List<Aresta> arestes) {
        this.arestes = arestes;
    }

    public List<Vertex> getVertexs() {
        return vertexs;
    }

    public void setVertexs(List<Vertex> vertexs) {
        this.vertexs = vertexs;
    }

    public Hexagon getLladre() {
        return lladre;
    }

    public void setLladre(Hexagon lladre) {
        this.lladre = lladre;
    }

    
    
}