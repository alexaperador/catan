package catan;

import catan.enums.TipusRecurs;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "recursos_jugador", uniqueConstraints = @UniqueConstraint(columnNames = { "jugador_id", "tipus" }))
public class RecursJugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", nullable = false, length = 20)
    private TipusRecurs tipusRecurs;

    @Column(name = "quantitat", nullable = false)
    private int quantitat;

    @ManyToOne(optional = false)
    @jakarta.persistence.JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;
}
