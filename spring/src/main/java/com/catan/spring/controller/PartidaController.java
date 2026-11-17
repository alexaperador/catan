package com.catan.spring.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catan.spring.service.PartidaService;

@RestController
@RequestMapping("/partida")
public class PartidaController {
    private final PartidaService service;

    PartidaController(PartidaService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<Map<String, Object>> iniciarPartidaNova(@RequestBody List<String> nomsJugadors) {
        return service.iniciarPartidaNova(nomsJugadors);
    }

    @PostMapping(path = "/{idPartida}/daus")
    public ResponseEntity<Map<String, Object>> tirarDaus(@PathVariable int idPartida) {
        return service.tirarDaus(idPartida);
    }

    @GetMapping(path = "/{idPartida}/tauler")
    public ResponseEntity<Map<String, Object>> mostrarHexagons(@PathVariable int idPartida) {
        return service.mostrarHexagons(idPartida);
    }
}
