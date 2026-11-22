package com.catan.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Jugador;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    /** Jugadors d'una partida en ordre de torn (1, 2, 3, 4). */
    List<Jugador> findByPartidaIdOrderByOrdreTorn(int idPartida);
}
