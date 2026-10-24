package catan;

import catan.enums.TipusCarta;
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
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "cartes_desenvolupament")
public class CartaDesenvolupament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus_carta", nullable = false, length = 20)
    private TipusCarta tipusCarta;

    @Column(name = "usada", nullable = false)
    private boolean usada;

    @Min(1)
    @Column(name = "ronda_compra", nullable = true)
    private Integer rondaCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id", nullable = true)
    private Jugador ma;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida pila;

    protected CartaDesenvolupament() {
    }

    public CartaDesenvolupament(Partida pila, TipusCarta tipusCarta) {
        this.pila = pila;
        this.tipusCarta = tipusCarta;
        this.usada = false;
    }

    public Integer getId() {
        return id;
    }

    public TipusCarta getTipusCarta() {
        return tipusCarta;
    }

    public void setTipusCarta(TipusCarta tipusCarta) {
        this.tipusCarta = tipusCarta;
    }

    public boolean isUsada() {
        return usada;
    }

    public void setUsada(boolean usada) {
        this.usada = usada;
    }

    public Integer getRondaCompra() {
        return rondaCompra;
    }

    public void setRondaCompra(Integer rondaCompra) {
        this.rondaCompra = rondaCompra;
    }

    public Jugador getMa() {
        return ma;
    }

    public void setMa(Jugador ma) {
        this.ma = ma;
    }

    public Partida getPila() {
        return pila;
    }

    public void setPila(Partida pila) {
        this.pila = pila;
    }
}