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

/**
 * Endpoints de jugador. Si una acció no es pot fer, GestorErrors torna un 400 amb el
 * motiu com a String (o un 404 si el jugador no existeix).
 */
@RestController
@RequestMapping("/jugador/{idJugador}")
public class JugadorController {

    private final JugadorService service;

    public JugadorController(JugadorService service) {
        this.service = service;
    }

    /** 
     * GET /jugador/{idJugador}/recursos  Jugador amb el seu inventari, o 404.  
     */
    @GetMapping("/recursos")
    public ResponseEntity<JugadorRecursosDto> mostrarRecursos(@PathVariable int idJugador) {
        return ResponseEntity.ok(service.mostrarRecursos(idJugador));
    }

    /** 
     * POST /jugador/{idJugador}/construir?tipus=POBLAT&posicio=123
     * Crea una nova construcció per al jugador.
     */
    @PostMapping("/construir")
    public ResponseEntity<String> construir(@PathVariable int idJugador,
            @RequestParam String tipus,
            @RequestParam int posicio) {
        return ResponseEntity.ok(service.construir(idJugador, tipus, posicio));
    }

    /** 
     * POST /jugador/{idJugador}/passar 
     * Passa el torn al següent jugador. 
     * Retorna un missatge amb el nom del següent jugador, o 404 si el jugador no existeix.
     */
    @PostMapping("/passar")
    public ResponseEntity<String> passarTorn(@PathVariable int idJugador) {
        return ResponseEntity.ok(service.passarTorn(idJugador));
    }
}
