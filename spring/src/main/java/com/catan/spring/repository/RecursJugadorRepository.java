package com.catan.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.enums.TipusRecurs;
import catan.models.RecursJugador;

public interface RecursJugadorRepository extends JpaRepository<RecursJugador, Integer> {

    Optional<RecursJugador> findByJugadorIdAndTipusRecurs(int idJugador, TipusRecurs tipusRecurs);

    List<RecursJugador> findByJugadorIdOrderById(int idJugador);
}
