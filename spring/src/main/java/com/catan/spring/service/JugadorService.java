package com.catan.spring.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catan.spring.dto.JugadorRecursosDto;
import com.catan.spring.exception.NoTrobatException;
import com.catan.spring.exception.ReglaException;
import com.catan.spring.repository.ArestaRepository;
import com.catan.spring.repository.AssentamentRepository;
import com.catan.spring.repository.CarreteraRepository;
import com.catan.spring.repository.CiutatRepository;
import com.catan.spring.repository.HexagonVertexRepository;
import com.catan.spring.repository.JugadorRepository;
import com.catan.spring.repository.PoblatRepository;
import com.catan.spring.repository.RecursJugadorRepository;
import com.catan.spring.repository.VertexRepository;

import catan.enums.EstatPartida;
import catan.enums.TipusRecurs;
import catan.models.Aresta;
import catan.models.Carretera;
import catan.models.Ciutat;
import catan.models.Hexagon;
import catan.models.Jugador;
import catan.models.Partida;
import catan.models.Poblat;
import catan.models.RecursJugador;
import catan.models.Vertex;

/**
 * Accions d'un jugador: consultar recursos, construir (carretera, poblat, ciutat) i passar torn.
 * Les regles incomplertes es llancen com a ReglaException amb un missatge diferent per cada cas.
 */
@Service
public class JugadorService {

    private static final int MAX_CARRETERES = 15;
    private static final int MAX_POBLATS = 5;
    private static final int MAX_CIUTATS = 4;

    private static final Map<TipusRecurs, Integer> COST_CARRETERA =
            Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1);
    private static final Map<TipusRecurs, Integer> COST_POBLAT =
            Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1, TipusRecurs.LLANA, 1, TipusRecurs.BLAT, 1);
    private static final Map<TipusRecurs, Integer> COST_CIUTAT =
            Map.of(TipusRecurs.BLAT, 2, TipusRecurs.MINERAL, 3);

    private final PartidaService partidaService;
    private final JugadorRepository jugadorRepo;
    private final AssentamentRepository assentamentRepo;
    private final CarreteraRepository carreteraRepo;
    private final PoblatRepository poblatRepo;
    private final CiutatRepository ciutatRepo;
    private final RecursJugadorRepository recursRepo;
    private final VertexRepository vertexRepo;
    private final ArestaRepository arestaRepo;
    private final HexagonVertexRepository hexagonVertexRepo;

    public JugadorService(PartidaService partidaService, JugadorRepository jugadorRepo,
            AssentamentRepository assentamentRepo, CarreteraRepository carreteraRepo,
            PoblatRepository poblatRepo, CiutatRepository ciutatRepo,
            RecursJugadorRepository recursRepo, VertexRepository vertexRepo,
            ArestaRepository arestaRepo, HexagonVertexRepository hexagonVertexRepo) {
        this.partidaService = partidaService;
        this.jugadorRepo = jugadorRepo;
        this.assentamentRepo = assentamentRepo;
        this.carreteraRepo = carreteraRepo;
        this.poblatRepo = poblatRepo;
        this.ciutatRepo = ciutatRepo;
        this.recursRepo = recursRepo;
        this.vertexRepo = vertexRepo;
        this.arestaRepo = arestaRepo;
        this.hexagonVertexRepo = hexagonVertexRepo;
    }

    private Jugador obtenirJugador(int idJugador) {
        return jugadorRepo.findById(idJugador)
                .orElseThrow(() -> new NoTrobatException("No existeix el jugador " + idJugador));
    }

    // ------------------------------------------------------ GET /jugador/{id}/recursos

    /** El jugador amb el seu inventari de recursos (consulta a RecursJugador). */
    @Transactional(readOnly = true)
    public JugadorRecursosDto mostrarRecursos(int idJugador) {
        Jugador j = obtenirJugador(idJugador);
        Map<TipusRecurs, Integer> recursos = new EnumMap<>(TipusRecurs.class);
        int total = 0;
        for (RecursJugador r : recursRepo.findByJugadorIdOrderById(idJugador)) {
            recursos.put(r.getTipusRecurs(), r.getQuantitat());
            total += r.getQuantitat();
        }
        return new JugadorRecursosDto(j.getId(), j.getNom(), j.getColor(), j.getOrdreTorn(),
                j.getPartida().getId(), recursos, total);
    }

    // ---------------------------------------------------- POST /jugador/{id}/construir

    /**
     * Construeix una CARRETERA (posicio = id de l'aresta), un POBLAT o una CIUTAT
     * (posicio = id del vèrtex). Torna el missatge de resultat.
     */
    @Transactional
    public String construir(int idJugador, String tipus, int posicio) {
        Jugador jugador = obtenirJugador(idJugador);
        Partida partida = jugador.getPartida();
        validarTorn(jugador, partida);

        return switch (tipus.trim().toUpperCase()) {
            case "CARRETERA" -> construirCarretera(jugador, partida, posicio);
            case "POBLAT" -> construirPoblat(jugador, partida, posicio);
            case "CIUTAT" -> millorarACiutat(jugador, partida, posicio);
            default -> throw new ReglaException("Tipus desconegut: ha de ser CARRETERA, POBLAT o CIUTAT");
        };
    }

    /** Comprovacions comunes a totes les accions de jugador. */
    private void validarTorn(Jugador jugador, Partida partida) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("Partida no en curs: la partida ja ha finalitzat");
        }
        if (partida.getActiu() == null || !partida.getActiu().getId().equals(jugador.getId())) {
            throw new ReglaException("No és el teu torn");
        }
        if (partida.getEstat() == EstatPartida.EN_CURS) {
            if (!partida.getDausTirats()) {
                throw new ReglaException("Daus no tirats: has de tirar els daus abans de fer cap acció");
            }
            if (partida.getLladrePendent()) {
                throw new ReglaException("Lladre pendent: has de moure el lladre abans de fer cap acció");
            }
        }
    }

    private String construirCarretera(Jugador jugador, Partida partida, int idAresta) {
        Aresta aresta = arestaRepo.findById(idAresta)
                .orElseThrow(() -> new ReglaException("Posició inexistent: no hi ha cap aresta " + idAresta));
        if (!aresta.getPartida().getId().equals(partida.getId())) {
            throw new ReglaException("Posició d'una altra partida");
        }
        if (carreteraRepo.existsByArestaId(idAresta)) {
            throw new ReglaException("Posició ocupada: ja hi ha una carretera en aquesta aresta");
        }

        int idJ = jugador.getId();
        int v1 = aresta.getVertexA().getId();
        int v2 = aresta.getVertexB().getId();
        boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
        long carreteres = carreteraRepo.countByPropietariId(idJ);

        if (preparacio) {
            // Cada torn de preparació: primer 1 poblat i després 1 carretera que el toqui
            if (carreteres >= partida.getRonda()) {
                throw new ReglaException("Límit assolit: ja has col·locat la carretera d'aquest torn de preparació");
            }
            if (carreteres >= assentamentRepo.countByPropietariId(idJ)) {
                throw new ReglaException("No connectat: primer has de col·locar el poblat d'aquest torn");
            }
            if (assentamentRepo.assentamentsPropisSenseCarretera(idJ, v1, v2) == 0) {
                throw new ReglaException("No connectat: la carretera ha de tocar el poblat que acabes de col·locar");
            }
        } else if (assentamentRepo.assentamentsPropisEn(idJ, v1, v2) == 0
                && carreteraRepo.carreteresPropiesTocant(idJ, v1, v2) == 0) {
            throw new ReglaException("No connectat: la carretera ha de tocar un poblat, ciutat o carretera teva");
        }

        if (carreteres >= MAX_CARRETERES) {
            throw new ReglaException("Límit assolit: ja tens " + MAX_CARRETERES + " carreteres");
        }
        if (!preparacio) {
            cobrar(idJ, COST_CARRETERA);
        }

        carreteraRepo.save(new Carretera(jugador, aresta));
        return "Carretera construïda a l'aresta " + idAresta;
    }

    private String construirPoblat(Jugador jugador, Partida partida, int idVertex) {
        Vertex vertex = obtenirVertex(partida, idVertex);
        if (assentamentRepo.existsByVertexId(idVertex)) {
            throw new ReglaException("Posició ocupada: ja hi ha un assentament en aquest vèrtex");
        }
        if (assentamentRepo.assentamentsAdjacents(idVertex) > 0) {
            throw new ReglaException("Regla de la distància: hi ha un assentament en un vèrtex adjacent");
        }

        int idJ = jugador.getId();
        boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
        long assentaments = assentamentRepo.countByPropietariId(idJ);

        if (preparacio) {
            if (assentaments >= partida.getRonda()) {
                throw new ReglaException("Límit assolit: ja has col·locat el poblat d'aquest torn de preparació");
            }
        } else if (carreteraRepo.carreteresPropiesTocant(idJ, idVertex, idVertex) == 0) {
            throw new ReglaException("No connectat: el vèrtex ha de tocar una carretera teva");
        }

        if (poblatRepo.countByPropietariId(idJ) >= MAX_POBLATS) {
            throw new ReglaException("Límit assolit: ja tens " + MAX_POBLATS + " poblats");
        }
        if (!preparacio) {
            cobrar(idJ, COST_POBLAT);
        }

        poblatRepo.save(new Poblat(jugador, vertex));

        // Segon poblat de la preparació: 1 recurs per cada hexàgon (no desert) que el toca
        if (preparacio && assentaments + 1 == 2) {
            for (Hexagon h : hexagonVertexRepo.hexagonsDelVertex(idVertex)) {
                TipusRecurs recurs = h.getTerreny().getRecurs();
                if (recurs != null) {
                    partidaService.afegirRecurs(idJ, recurs, 1);
                }
            }
        }

        partidaService.comprovarVictoria(partida);
        return "Poblat construït al vèrtex " + idVertex + guanyador(partida);
    }

    private String millorarACiutat(Jugador jugador, Partida partida, int idVertex) {
        if (partida.getEstat() != EstatPartida.EN_CURS) {
            throw new ReglaException("Partida no en curs: les ciutats no es poden construir a la PREPARACIO");
        }
        obtenirVertex(partida, idVertex);
        int idJ = jugador.getId();

        Poblat poblat = poblatRepo.findByVertexIdAndPropietariId(idVertex, idJ)
                .orElseThrow(() -> new ReglaException("No hi ha cap poblat teu per millorar en aquest vèrtex"));
        if (ciutatRepo.countByPropietariId(idJ) >= MAX_CIUTATS) {
            throw new ReglaException("Límit assolit: ja tens " + MAX_CIUTATS + " ciutats");
        }
        cobrar(idJ, COST_CIUTAT);

        Vertex vertex = poblat.getVertex();
        poblatRepo.delete(poblat);
        poblatRepo.flush(); // el vèrtex és únic a Assentament: s'ha d'esborrar abans d'inserir la ciutat
        ciutatRepo.save(new Ciutat(jugador, vertex));

        partidaService.comprovarVictoria(partida);
        return "Ciutat construïda al vèrtex " + idVertex + guanyador(partida);
    }

    private Vertex obtenirVertex(Partida partida, int idVertex) {
        Vertex vertex = vertexRepo.findById(idVertex)
                .orElseThrow(() -> new ReglaException("Posició inexistent: no hi ha cap vèrtex " + idVertex));
        if (!vertex.getPartida().getId().equals(partida.getId())) {
            throw new ReglaException("Posició d'una altra partida");
        }
        return vertex;
    }

    /** Comprova que el jugador tingui tots els recursos del cost i els descompta. */
    private void cobrar(int idJugador, Map<TipusRecurs, Integer> cost) {
        for (Map.Entry<TipusRecurs, Integer> e : cost.entrySet()) {
            int tinc = recursRepo.findByJugadorIdAndTipusRecurs(idJugador, e.getKey())
                    .map(RecursJugador::getQuantitat).orElse(0);
            if (tinc < e.getValue()) {
                throw new ReglaException("Sense recursos: necessites " + descriure(cost));
            }
        }
        cost.forEach((tipus, quantitat) -> partidaService.afegirRecurs(idJugador, tipus, -quantitat));
    }

    private String descriure(Map<TipusRecurs, Integer> cost) {
        return cost.entrySet().stream()
                .map(e -> e.getValue() + " " + e.getKey())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private String guanyador(Partida partida) {
        return partida.getEstat() == EstatPartida.FINALITZADA
                ? ". Partida finalitzada! Guanya " + partida.getGuanyador().getNom()
                : "";
    }

    // ------------------------------------------------------- POST /jugador/{id}/passar

    /**
     * Passa el torn al següent jugador. A EN_CURS segueix l'ordre de torn i augmenta la ronda
     * en tornar al jugador 1; a PREPARACIO segueix l'ordre de serp (1..n, n..1) i, en acabar,
     * la partida passa a EN_CURS amb el jugador 1.
     */
    @Transactional
    public String passarTorn(int idJugador) {
        Jugador jugador = obtenirJugador(idJugador);
        Partida partida = jugador.getPartida();

        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("Partida no en curs: la partida ja ha finalitzat");
        }
        if (partida.getActiu() == null || !partida.getActiu().getId().equals(jugador.getId())) {
            throw new ReglaException("No és el teu torn");
        }

        if (partida.getEstat() == EstatPartida.EN_CURS) {
            if (!partida.getDausTirats()) {
                throw new ReglaException("Daus no tirats: has de tirar els daus abans de passar");
            }
            if (partida.getLladrePendent()) {
                throw new ReglaException("Lladre pendent: has de moure el lladre abans de passar");
            }
        } else {
            long assentaments = assentamentRepo.countByPropietariId(jugador.getId());
            long carreteres = carreteraRepo.countByPropietariId(jugador.getId());
            if (assentaments < partida.getRonda() || carreteres < partida.getRonda()) {
                throw new ReglaException("No pots passar: has de col·locar el poblat i la carretera d'aquest torn");
            }
        }

        List<Jugador> jugadors = jugadorRepo.findByPartidaIdOrderByOrdreTorn(partida.getId());
        int n = jugadors.size();
        int ordre = jugador.getOrdreTorn();
        int seguent;
        String extra = "";

        if (partida.getEstat() == EstatPartida.EN_CURS) {
            seguent = ordre % n + 1;
            if (seguent == 1) {
                partida.setRonda(partida.getRonda() + 1);
            }
        } else if (partida.getRonda() == 1) {
            // Ronda 1 de preparació: 1, 2, ..., n. L'últim repeteix i comença la ronda 2
            if (ordre < n) {
                seguent = ordre + 1;
            } else {
                partida.setRonda(2);
                seguent = n;
            }
        } else {
            // Ronda 2 de preparació: n, ..., 1. En acabar comença la partida
            if (ordre > 1) {
                seguent = ordre - 1;
            } else {
                partida.setEstat(EstatPartida.EN_CURS);
                partida.setRonda(1);
                seguent = 1;
                extra = " Fi de la preparació: la partida és EN_CURS.";
            }
        }

        Jugador nouActiu = jugadors.get(seguent - 1);
        partida.setActiu(nouActiu);
        partida.setDausTirats(false);
        return "Torn passat." + extra + " Ara juga " + nouActiu.getNom()
                + " (ronda " + partida.getRonda() + ", " + partida.getEstat() + ")";
    }
}
