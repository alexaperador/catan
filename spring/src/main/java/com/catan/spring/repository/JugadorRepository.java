package com.catan.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Jugador;

public interface JugadorRepository extends JpaRepository<Jugador, Integer> {

    List<Jugador> findByPartidaIdOrderByOrdreTorn(int idPartida);
}
