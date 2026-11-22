package com.catan.spring.service;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.catan.spring.repository.PartidaRepository;

import catan.dao.PartidaDao;
import catan.enums.TipusRecurs;
import catan.models.Assentament;
import catan.models.Construccio;
import catan.models.Hexagon;
import catan.models.Partida;

@Service
public class PartidaService {

    private final PartidaRepository repository;

    PartidaService(PartidaRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<Map<String, Object>> iniciarPartidaNova(List<String> nomsJugadors) {
        int idPartida = new PartidaDao().iniciarPartida(nomsJugadors);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("Id Partida", idPartida);
        resposta.put("Missatge", "Partida creada correctament");

        return ResponseEntity.ok(resposta);
    }

    public ResponseEntity<Map<String, Object>> tirarDaus(int idPartida) {
        Optional<Partida> partidaOptional = repository.findById(idPartida);

        if (!partidaOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Header-Customitzat", "Partida no trobada amb id: " + idPartida).build();
        }

        PartidaDao partidaDao = new PartidaDao();
        Map<Integer, Map<TipusRecurs, Integer>> resum = partidaDao.tirarDaus(idPartida);
        if (resum == null) {
            return ResponseEntity.internalServerError().header("Header-Customitzat", "Error al tirar els daus").build();
        }

        Partida partidaActualitzada = repository.findById(idPartida).orElse(null);
        Integer tirada = partidaActualitzada != null ? partidaActualitzada.getUltimaTirada() : null;

        if (tirada == null) {
            return ResponseEntity.internalServerError().header("Header-Customitzat", "Error al obtenir la tirada")
                    .build();
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("Id Partida", idPartida);
        resposta.put("Tirada", tirada);
        resposta.put("Recursos per jugador", resum);

        return ResponseEntity.ok(resposta);
    }

    public ResponseEntity<Map<String, Object>> mostrarHexagons(int idPartida){
        Optional<Partida> partidaOptional = repository.findById(idPartida);

        if (!partidaOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Partida partida = partidaOptional.get();
        Integer idLladre = partida.getLladre() != null ? partida.getLladre().getId() : null;
        Map<Integer, List<Assentament>> assentamentsPerHexagon = new HashMap<>();

        for (Object[] fila : repository.findAssentamentsByPartidaId(idPartida)) {
            Integer idHexagon = (Integer) fila[0];
            Assentament assentament = (Assentament) fila[1];
            if (!assentamentsPerHexagon.containsKey(idHexagon)) {
                assentamentsPerHexagon.put(idHexagon, new ArrayList<>());
            }
            assentamentsPerHexagon.get(idHexagon).add(assentament);
        }

        List<Map<String, Object>> hexagons = new ArrayList<>();
        for (Hexagon hexagon : repository.findHexagonsByPartidaId(idPartida)) {
            Map<String, Object> dadesHexagon = new LinkedHashMap<>();
            dadesHexagon.put("Codi", hexagon.getCodi());
            dadesHexagon.put("Terreny", hexagon.getTerreny());
            dadesHexagon.put("Numero", hexagon.getNumero());
            dadesHexagon.put("Lladre", hexagon.getId().equals(idLladre));

            List<Map<String, Object>> construccions = new ArrayList<>();
            for (Construccio construccio : assentamentsPerHexagon
                    .getOrDefault(hexagon.getId(), List.of())) {
                Map<String, Object> dadesConstruccio = new LinkedHashMap<>();
                dadesConstruccio.put("Tipus", construccio.getClass().getSimpleName());
                dadesConstruccio.put("Id", construccio.getId());
                dadesConstruccio.put("Vertex", ((Assentament) construccio).getVertex().getCodi());
                dadesConstruccio.put("Jugador", construccio.getPropietari().getNom());
                construccions.add(dadesConstruccio);
            }
            dadesHexagon.put("Construccions adjacents", construccions);
            hexagons.add(dadesHexagon);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("Id Partida", idPartida);
        resposta.put("Hexagons", hexagons);
        return ResponseEntity.ok(resposta);
    }
}
