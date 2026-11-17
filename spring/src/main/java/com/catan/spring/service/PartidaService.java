package com.catan.spring.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PartidaService {

    public ResponseEntity<Map<String, Object>> tirarDaus(int idPartida) {
        // TODO: implementar
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("missatge", "pendent");
        return ResponseEntity.ok(resposta);
    }
}