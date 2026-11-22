package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.enums.TipusCarta;
import catan.models.CartaDesenvolupament;

public interface CartaDesenvolupamentRepository extends JpaRepository<CartaDesenvolupament, Integer> {

    /** Cartes d'un tipus a la mà d'un jugador (per comptar les PUNT_VICTORIA). */
    long countByMaIdAndTipusCarta(int idJugador, TipusCarta tipusCarta);
}
