package com.catan.spring.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catan.spring.model.*;
import com.catan.spring.repository.*;

@Service
public class JugadorService {

    private final JugadorRepository jugadorRepo;
    private final PartidaRepository partidaRepo;
    private final AssentamentRepository assentamentRepo;
    private final CarreteraRepository carreteraRepo;
    private final PoblatRepository poblatRepo;
    private final CiutatRepository ciutatRepo;
    private final RecursJugadorRepository recursRepo;
    private final VertexRepository vertexRepo;
    private final ArestaRepository arestaRepo;
    private final HexagonVertexRepository hexagonVertexRepo;

    public JugadorService(JugadorRepository jugadorRepo, PartidaRepository partidaRepo,
            AssentamentRepository assentamentRepo, CarreteraRepository carreteraRepo,
            PoblatRepository poblatRepo, CiutatRepository ciutatRepo,
            RecursJugadorRepository recursRepo, VertexRepository vertexRepo,
            ArestaRepository arestaRepo, HexagonVertexRepository hexagonVertexRepo) {
        this.jugadorRepo = jugadorRepo;
        this.partidaRepo = partidaRepo;
        this.assentamentRepo = assentamentRepo;
        this.carreteraRepo = carreteraRepo;
        this.poblatRepo = poblatRepo;
        this.ciutatRepo = ciutatRepo;
        this.recursRepo = recursRepo;
        this.vertexRepo = vertexRepo;
        this.arestaRepo = arestaRepo;
        this.hexagonVertexRepo = hexagonVertexRepo;
    }

    private static final Map<TipusRecurs, Integer> COST_CARRETERA =
        Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1);
    private static final Map<TipusRecurs, Integer> COST_POBLAT =
        Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1, TipusRecurs.LLANA, 1, TipusRecurs.BLAT, 1);
    private static final Map<TipusRecurs, Integer> COST_CIUTAT =
        Map.of(TipusRecurs.BLAT, 2, TipusRecurs.MINERAL, 3);

    @Transactional
    public String construir(int idJugador, String tipus, int posicio) {
        Jugador j = jugadorRepo.findById(idJugador)
            .orElseThrow(() -> new ReglaException("Jugador no trobat"));
        Partida p = j.getPartida();
        validarTorn(j, p);

        return switch (tipus.toUpperCase()) {
            case "CARRETERA" -> carretera(j, p, posicio);
            case "POBLAT"    -> poblat(j, p, posicio);
            case "CIUTAT"    -> ciutat(j, p, posicio);
            default -> throw new ReglaException("Tipus desconegut");
        };
    }

    private void validarTorn(Jugador j, Partida p) {
        if (p.getEstat() == EstatPartida.FINALITZADA)
            throw new ReglaException("Partida no en curs");
        if (p.getJugadorActiu() == null || p.getJugadorActiu().getId() != j.getId())
            throw new ReglaException("No és el teu torn");
        if (p.getEstat() == EstatPartida.EN_CURS) {
            if (!p.isDausTirats()) throw new ReglaException("Daus no tirats");
            if (p.isLladrePendent()) throw new ReglaException("Lladre pendent de moure");
        }
    }

    private String carretera(Jugador j, Partida p, int idAresta) {
        if (!arestaRepo.existsByIdAndPartidaId(idAresta, p.getId()))
            throw new ReglaException("Posició d'una altra partida");
        Aresta a = arestaRepo.findById(idAresta).get();

        if (carreteraRepo.existsByArestaId(idAresta))
            throw new ReglaException("Posició ocupada");

        int v1 = a.getVertexA().getId();
        int v2 = a.getVertexB().getId();
        boolean prep = p.getEstat() == EstatPartida.PREPARACIO;
        long poblats = poblatRepo.countByPropietariId(j.getId())
                     + ciutatRepo.countByPropietariId(j.getId());
        long carreteres = carreteraRepo.countByPropietariId(j.getId());

        if (prep) {
            if (carreteres >= poblats)
                throw new ReglaException("No connectat: col·loca primer el poblat");
            if (assentamentRepo.poblatsSenseCarretera(j.getId(), v1, v2) == 0)
                throw new ReglaException("No connectat");
        } else {
            if (assentamentRepo.assentamentsPropisEn(j.getId(), v1, v2) == 0
                && carreteraRepo.carreteresPropiesTocant(j.getId(), v1, v2) == 0)
                throw new ReglaException("No connectat");
        }

        if (carreteres >= 15) throw new ReglaException("Límit assolit");
        if (!prep) cobrar(j, COST_CARRETERA);

        Carretera c = new Carretera();
        c.setPropietari(j);
        c.setAresta(a);
        carreteraRepo.save(c);
        return "Carretera construïda";
    }

    private String poblat(Jugador j, Partida p, int idVertex) {
        if (!vertexRepo.existsByIdAndPartidaId(idVertex, p.getId()))
            throw new ReglaException("Posició d'una altra partida");
        Vertex v = vertexRepo.findById(idVertex).get();

        if (assentamentRepo.existsByVertexId(idVertex))
            throw new ReglaException("Posició ocupada");
        if (assentamentRepo.assentamentsAdjacents(idVertex) > 0)
            throw new ReglaException("Regla de la distància");

        boolean prep = p.getEstat() == EstatPartida.PREPARACIO;
        long nPoblats = poblatRepo.countByPropietariId(j.getId());

        if (prep) {
            long totalAssentaments = nPoblats + ciutatRepo.countByPropietariId(j.getId());
            if (totalAssentaments >= p.getRonda())
                throw new ReglaException("Ja has col·locat el poblat d'aquesta ronda");
        } else if (carreteraRepo.carreteresPropiesTocant(j.getId(), idVertex, idVertex) == 0) {
            throw new ReglaException("No connectat");
        }

        if (nPoblats >= 5) throw new ReglaException("Límit assolit");
        if (!prep) cobrar(j, COST_POBLAT);

        Poblat pb = new Poblat();
        pb.setPropietari(j);
        pb.setVertex(v);
        poblatRepo.save(pb);

        if (prep && nPoblats + 1 == 2) {
            for (Hexagon h : hexagonVertexRepo.hexagonsDelVertex(idVertex)) {
                TipusRecurs r = recursDelTerreny(h.getTerreny());
                if (r != null) afegir(j, r, 1);
            }
        }

        comprovarVictoria(p);
        return "Poblat construït";
    }

    private String ciutat(Jugador j, Partida p, int idVertex) {
        if (p.getEstat() != EstatPartida.EN_CURS)
            throw new ReglaException("Partida no en curs");
        if (!vertexRepo.existsByIdAndPartidaId(idVertex, p.getId()))
            throw new ReglaException("Posició d'una altra partida");

        Poblat pb = poblatRepo.findByVertexIdAndPropietariId(idVertex, j.getId())
            .orElseThrow(() -> new ReglaException("No hi ha cap poblat teu per millorar"));

        if (ciutatRepo.countByPropietariId(j.getId()) >= 4)
            throw new ReglaException("Límit assolit");
        cobrar(j, COST_CIUTAT);

        Vertex v = pb.getVertex();
        poblatRepo.delete(pb);
        poblatRepo.flush();   
        Ciutat c = new Ciutat();
        c.setPropietari(j);
        c.setVertex(v);
        ciutatRepo.save(c);

        comprovarVictoria(p);
        return "Ciutat construïda";
    }

    private TipusRecurs recursDelTerreny(TipusTerreny t) {
        return switch (t) {
            case BOSC -> TipusRecurs.FUSTA;
            case TURONS -> TipusRecurs.MAO;
            case PASTURES -> TipusRecurs.LLANA;
            case CAMPS -> TipusRecurs.BLAT;
            case MUNTANYES -> TipusRecurs.MINERAL;
            case DESERT -> null;
        };
    }

    private void cobrar(Jugador j, Map<TipusRecurs, Integer> cost) {
        for (var e : cost.entrySet()) {
            int tinc = recursRepo.findByJugadorIdAndTipus(j.getId(), e.getKey())
                .map(RecursJugador::getQuantitat).orElse(0);
            if (tinc < e.getValue()) throw new ReglaException("Sense recursos");
        }
        for (var e : cost.entrySet()) afegir(j, e.getKey(), -e.getValue());
    }

    private void afegir(Jugador j, TipusRecurs t, int q) {
        RecursJugador r = recursRepo.findByJugadorIdAndTipus(j.getId(), t).orElseThrow();
        r.setQuantitat(r.getQuantitat() + q);
    }

    private void comprovarVictoria(Partida p) {
        for (Jugador x : jugadorRepo.findByPartidaIdOrderByOrdreTorn(p.getId())) {
            long punts = poblatRepo.countByPropietariId(x.getId())
                       + 2 * ciutatRepo.countByPropietariId(x.getId());
            if (punts >= p.getPuntsPerGuanyar()) {
                p.setEstat(EstatPartida.FINALITZADA);
                p.setGuanyador(x);
                return;
            }
        }
    }

    @Transactional
    public String passarTorn(int idJugador) {
        Jugador j = jugadorRepo.findById(idJugador)
            .orElseThrow(() -> new ReglaException("Jugador no trobat"));
        Partida p = j.getPartida();

        if (p.getEstat() == EstatPartida.FINALITZADA)
            throw new ReglaException("Partida no en curs");
        if (p.getJugadorActiu().getId() != j.getId())
            throw new ReglaException("No és el teu torn");

        if (p.getEstat() == EstatPartida.EN_CURS) {
            if (!p.isDausTirats())
                throw new ReglaException("Has de tirar els daus abans de passar");
            if (p.isLladrePendent())
                throw new ReglaException("Has de moure el lladre abans de passar");
        } else { 
            long poblats = poblatRepo.countByPropietariId(j.getId())
                         + ciutatRepo.countByPropietariId(j.getId());
            long carreteres = carreteraRepo.countByPropietariId(j.getId());
            if (poblats < p.getRonda() || carreteres < p.getRonda())
                throw new ReglaException("Has de col·locar el poblat i la carretera d'aquest torn");
        }

        List<Jugador> jugadors = jugadorRepo.findByPartidaIdOrderByOrdreTorn(p.getId());
        int n = jugadors.size();
        int ordre = j.getOrdreTorn();
        int seguent;

        if (p.getEstat() == EstatPartida.EN_CURS) {
            seguent = ordre % n + 1;
            if (seguent == 1) p.setRonda(p.getRonda() + 1);
        } else if (p.getRonda() == 1) {          
            if (ordre < n) seguent = ordre + 1;
            else { p.setRonda(2); seguent = n; } 
        } else {                                  
            if (ordre > 1) seguent = ordre - 1;
            else { p.setEstat(EstatPartida.EN_CURS); p.setRonda(1); seguent = 1; }
        }

        p.setJugadorActiu(jugadors.get(seguent - 1));
        p.setDausTirats(false);
        partidaRepo.save(p);
        return "Torn passat. Ara juga " + p.getJugadorActiu().getNom();
    }
}