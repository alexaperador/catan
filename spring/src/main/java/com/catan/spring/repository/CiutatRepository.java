package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Ciutat;

public interface CiutatRepository extends JpaRepository<Ciutat, Integer> {

    long countByPropietariId(int idJugador);
}
