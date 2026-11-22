package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.enums.TipusCarta;
import catan.models.CartaDesenvolupament;

public interface CartaDesenvolupamentRepository extends JpaRepository<CartaDesenvolupament, Integer> {

    long countByMaIdAndTipusCarta(int idJugador, TipusCarta tipusCarta);
}
