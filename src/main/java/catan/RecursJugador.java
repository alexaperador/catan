package catan;

import catan.enums.TipusRecurs;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "recursos_jugador", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "jugador_id", "tipus" })
})
@NamedQuery(name = "RecursJugador.perJugador", query = "FROM RecursJugador r WHERE r.jugador.id = :idJugador ORDER BY r.id")
@NamedQuery(name = "RecursJugador.perJugadorITipus", query = "FROM RecursJugador r WHERE r.jugador.id = :idJugador AND r.tipusRecurs = :tipus")
public class RecursJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El tipus de recurs és obligatori")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 20)
    private TipusRecurs tipusRecurs;

    @Min(value = 0, message = "La quantitat de recursos no pot ser negativa")
    @Column(name = "quantitat", nullable = false)
    private int quantitat;

    @NotNull(message = "El jugador és obligatori")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    protected RecursJugador() {
    }

    public RecursJugador(Jugador jugador, TipusRecurs tipusRecurs, int quantitat) {
        this.jugador = jugador;
        this.tipusRecurs = tipusRecurs;
        this.quantitat = quantitat;
    }

    public Integer getId() {
        return id;
    }

    public TipusRecurs getTipusRecurs() {
        return tipusRecurs;
    }

    public void setTipusRecurs(TipusRecurs tipusRecurs) {
        this.tipusRecurs = tipusRecurs;
    }

    public int getQuantitat() {
        return quantitat;
    }

    public void setQuantitat(int quantitat) {
        this.quantitat = quantitat;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }
}
