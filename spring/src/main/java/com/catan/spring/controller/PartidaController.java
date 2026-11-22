package com.catan.spring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catan.spring.dto.HexagonDto;
import com.catan.spring.dto.NovaPartidaRequest;
import com.catan.spring.dto.PartidaDto;
import com.catan.spring.dto.TiradaDto;
import com.catan.spring.service.PartidaService;

import jakarta.validation.Valid;

/**
 * Endpoints de partida. Els errors (partida inexistent, regles) els converteix
 * GestorErrors en 404 / 400 amb el missatge.
 */
@RestController
@RequestMapping("/partida")
public class PartidaController {

    private final PartidaService service;

    public PartidaController(PartidaService service) {
        this.service = service;
    }

    /** POST /partida  cos: {"jugadors": ["A","B"], "puntsPerGuanyar": 10} */
    @PostMapping
    public ResponseEntity<PartidaDto> iniciarPartida(@Valid @RequestBody NovaPartidaRequest peticio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciarPartida(peticio));
    }

    /** GET /partida/{idPartida}/tauler  Torna un array JSON amb els hexàgons. */
    @GetMapping("/{idPartida}/tauler")
    public ResponseEntity<List<HexagonDto>> mostrarTauler(@PathVariable int idPartida) {
        return ResponseEntity.ok(service.mostrarTauler(idPartida));
    }

    /** POST /partida/{idPartida}/daus */
    @PostMapping("/{idPartida}/daus")
    public ResponseEntity<TiradaDto> tirarDaus(@PathVariable int idPartida) {
        return ResponseEntity.ok(service.tirarDaus(idPartida));
    }
}
