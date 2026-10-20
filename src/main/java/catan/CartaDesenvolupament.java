package catan;

import catan.enums.TipusCarta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cartes_desenvolupament")
public class CartaDesenvolupament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus_carta", nullable = false, length = 20)
    private TipusCarta tipusCarta;

    @Column(name = "usada", nullable = false)
    private boolean usada;

    @Column(name = "ronda_compra", nullable = true)
    private Integer rondaCompra;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador ma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida pila;

    public CartaDesenvolupament() {

    }

    public CartaDesenvolupament(Jugador ma, Partida pila, Integer rondaCompra, TipusCarta tipusCarta, boolean usada) {
        this.ma = ma;
        this.pila = pila;
        this.rondaCompra = rondaCompra;
        this.tipusCarta = tipusCarta;
        this.usada = usada;
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

    public int getId() {
        return id;
    }

    

}