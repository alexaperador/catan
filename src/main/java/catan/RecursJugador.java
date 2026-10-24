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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "recursos_jugador", uniqueConstraints = @UniqueConstraint(columnNames = { "jugador_id", "tipus" }))
public class RecursJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 20)
    private TipusRecurs tipusRecurs;

    @Min(value = 0, message = "La quantitat de recursos no pot ser negativa")
    @Column(name = "quantitat", nullable = false)
    private int quantitat;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    protected RecursJugador() {
    }

    public RecursJugador(TipusRecurs tipusRecurs, int quantitat, Jugador jugador) {
        this.tipusRecurs = tipusRecurs;
        this.quantitat = quantitat;
        this.jugador = jugador;
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
