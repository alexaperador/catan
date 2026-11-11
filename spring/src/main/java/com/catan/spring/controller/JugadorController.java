package com.catan.spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catan.spring.service.JugadorService;

@RestController
@RequestMapping("/jugador")
public class JugadorController {
    private final JugadorService service;

    JugadorController(JugadorService service) {
        this.service = service;
    }

    @PostMapping(path = "/{idJugador}/passar")
    public ResponseEntity<String> passarTorn(@PathVariable int idJugador) {
        return service.passarTorn(idJugador);
    }
}
