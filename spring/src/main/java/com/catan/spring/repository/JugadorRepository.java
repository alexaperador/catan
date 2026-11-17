package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import com.catan.spring.model.Jugador;


public interface JugadorRepository extends JpaRepository<Jugador, Integer> {
    
}
