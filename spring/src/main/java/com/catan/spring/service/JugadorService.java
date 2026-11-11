package com.catan.spring.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.catan.spring.repository.JugadorRepository;

import catan.dao.JugadorDao;
import catan.models.Jugador;

public class JugadorService {
    private final JugadorRepository repository;

    JugadorService(JugadorRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<String> passarTorn(int idJugador) {
        Optional<Jugador> jugadorOptional = repository.findById(idJugador);

        if (jugadorOptional.isPresent()) {
            JugadorDao jugador = new JugadorDao();
            jugador.passarTorn(idJugador);
            return ResponseEntity.ok("Torn passat correctament per al jugador amb ID: " + idJugador);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
