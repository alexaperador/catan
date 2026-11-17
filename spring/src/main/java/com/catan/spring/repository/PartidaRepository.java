package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.catan.spring.model.Partida;


public interface PartidaRepository extends JpaRepository<Partida, Integer> {

}
