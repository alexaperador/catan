package com.catan.spring.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.catan.spring.repository.PartidaRepository;

import catan.dao.PartidaDao;
import catan.enums.TipusRecurs;
import catan.models.Partida;

@Service
public class PartidaService {

    private final PartidaRepository repository;

    PartidaService(PartidaRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<Map<String, Object>> tirarDaus(int idPartida) {
        Optional<Partida> partidaOptional = repository.findById(idPartida);

        if (!partidaOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        PartidaDao partidaDao = new PartidaDao();
        Map<Integer, Map<TipusRecurs, Integer>> resum = partidaDao.tirarDaus(idPartida);

        Partida partidaActualitzada = repository.findById(idPartida).orElse(null);
        Integer tirada = partidaActualitzada != null ? partidaActualitzada.getUltimaTirada() : null;

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("Id Partida", idPartida);
        resposta.put("Tirada", tirada);
        resposta.put("Recursos per jugador", resum);

        return ResponseEntity.ok(resposta);
    }
}
