package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import catan.models.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Integer> {

}
