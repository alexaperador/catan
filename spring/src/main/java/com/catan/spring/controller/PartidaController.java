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

@RestController
@RequestMapping("/partida")
public class PartidaController {

    private final PartidaService service;

    public PartidaController(PartidaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PartidaDto> iniciarPartida(@Valid @RequestBody NovaPartidaRequest peticio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciarPartida(peticio));
    }

    @GetMapping("/{idPartida}/tauler")
    public ResponseEntity<List<HexagonDto>> mostrarTauler(@PathVariable int idPartida) {
        return ResponseEntity.ok(service.mostrarTauler(idPartida));
    }

    @PostMapping("/{idPartida}/daus")
    public ResponseEntity<TiradaDto> tirarDaus(@PathVariable int idPartida) {
        return ResponseEntity.ok(service.tirarDaus(idPartida));
    }
}
