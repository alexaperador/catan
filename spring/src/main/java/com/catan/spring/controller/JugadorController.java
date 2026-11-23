package com.catan.spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.catan.spring.dto.JugadorRecursosDto;
import com.catan.spring.service.JugadorService;

@RestController
@RequestMapping("/jugador/{idJugador}")
public class JugadorController {

    private final JugadorService service;

    public JugadorController(JugadorService service) {
        this.service = service;
    }

    @GetMapping("/recursos")
    public ResponseEntity<JugadorRecursosDto> mostrarRecursos(@PathVariable int idJugador) {
        return ResponseEntity.ok(service.mostrarRecursos(idJugador));
    }

    @PostMapping("/construir")
    public ResponseEntity<String> construir(@PathVariable int idJugador,
            @RequestParam String tipus,
            @RequestParam int posicio) {
        return ResponseEntity.ok(service.construirResultat(idJugador, tipus, posicio));
    }

    @PostMapping("/passar")
    public ResponseEntity<String> passarTorn(@PathVariable int idJugador) {
        return ResponseEntity.ok(service.passarTornResultat(idJugador));
    }
}
