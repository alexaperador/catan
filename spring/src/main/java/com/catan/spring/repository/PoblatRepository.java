package com.catan.spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Poblat;

public interface PoblatRepository extends JpaRepository<Poblat, Integer> {

    long countByPropietariId(int idJugador);

    Optional<Poblat> findByVertexIdAndPropietariId(int idVertex, int idJugador);
}
