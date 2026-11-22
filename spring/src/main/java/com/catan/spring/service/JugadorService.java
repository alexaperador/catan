package com.catan.spring.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@Service
public class JugadorService {
    private static final int MAX_CARRETERES = 15;
    private static final int MAX_POBLATS = 5;
    private static final int MAX_CIUTATS = 4;

    private final PartidaService partidaService;
    private final JugadorRepository jugadorRepository;
    private final AssentamentRepository assentamentRepository;
    private final CarreteraRepository carreteraRepository;
    private final PoblatRepository poblatRepository;
    private final CiutatRepository ciutatRepository;
    private final RecursJugadorRepository recursRepository;
    private final VertexRepository vertexRepository;
    private final ArestaRepository arestaRepository;
    private final HexagonVertexRepository hexagonVertexRepository;

    public JugadorService(PartidaService partidaService, JugadorRepository jugadorRepository,
            AssentamentRepository assentamentRepository, CarreteraRepository carreteraRepository,
            PoblatRepository poblatRepository, CiutatRepository ciutatRepository,
            RecursJugadorRepository recursRepository, VertexRepository vertexRepository,
            ArestaRepository arestaRepository, HexagonVertexRepository hexagonVertexRepository) {
        this.partidaService = partidaService;
        this.jugadorRepository = jugadorRepository;
        this.assentamentRepository = assentamentRepository;
        this.carreteraRepository = carreteraRepository;
        this.poblatRepository = poblatRepository;
        this.ciutatRepository = ciutatRepository;
        this.recursRepository = recursRepository;
        this.vertexRepository = vertexRepository;
        this.arestaRepository = arestaRepository;
        this.hexagonVertexRepository = hexagonVertexRepository;
    }

    private Jugador obtenirJugador(int idJugador) {
        return jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new NoTrobatException("No existeix el jugador " + idJugador));
    }

    @Transactional(readOnly = true)
    public JugadorRecursosDto mostrarRecursos(int idJugador) {
        Jugador jugador = obtenirJugador(idJugador);
        Map<TipusRecurs, Integer> recursos = new HashMap<>();

        int total = 0;
        for (RecursJugador recursJugador : recursRepository.findByJugadorIdOrderById(idJugador)) {
            recursos.put(recursJugador.getTipusRecurs(), recursJugador.getQuantitat());
            total += recursJugador.getQuantitat();
        }

        return new JugadorRecursosDto(jugador.getId(), jugador.getNom(), jugador.getColor(), jugador.getOrdreTorn(),
                jugador.getPartida().getId(), recursos, total);
    }

    @Transactional
    public String construir(int idJugador, String tipus, int posicio) {
        Jugador jugador = obtenirJugador(idJugador);
        Partida partida = jugador.getPartida();

        validarTorn(jugador, partida);

        return switch (tipus.trim().toUpperCase()) {
            case "CARRETERA" -> construirCarretera(jugador, partida, posicio);
            case "POBLAT" -> construirPoblat(jugador, partida, posicio);
            case "CIUTAT" -> millorarACiutat(jugador, partida, posicio);
            default -> throw new ReglaException("Tipus de construcció desconegut: " + tipus);
        };
    }

    private void validarTorn(Jugador jugador, Partida partida) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("La partida ja ha finalitzat");
        }
        if (partida.getActiu() == null || !partida.getActiu().getId().equals(jugador.getId())) {
            throw new ReglaException("No és el teu torn");
        }
        if (partida.getEstat() == EstatPartida.EN_CURS) {
            if (!partida.getDausTirats()) {
                throw new ReglaException("Has de tirar els daus abans de fer cap acció");
            }
            if (partida.getLladrePendent()) {
                throw new ReglaException("Has de moure el lladre abans de fer cap acció");
            }
        }
    }

    private String construirCarretera(Jugador jugador, Partida partida, int idAresta) {
        Aresta aresta = arestaRepository.findById(idAresta).orElse(null);
        if (aresta == null) {
            throw new ReglaException("No s'ha trobat l'aresta: " + idAresta);
        }
        if (!aresta.getPartida().getId().equals(partida.getId())) {
            throw new ReglaException("Posició d'una altra partida");
        }
        if (carreteraRepository.existsByArestaId(idAresta)) {
            throw new ReglaException("Ja hi ha una carretera en aquesta aresta");
        }

        int idJugador = jugador.getId();
        int vertexA = aresta.getVertexA().getId();
        int vertexB = aresta.getVertexB().getId();
        boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
        long carreteres = carreteraRepository.countByPropietariId(idJugador);

        if (preparacio) {
            if (carreteres >= partida.getRonda()) {
                throw new ReglaException("Ja has col·locat la carretera d'aquest torn de preparació");
            }
            if (carreteres >= assentamentRepository.countByPropietariId(idJugador)) {
                throw new ReglaException("Primer has de col·locar el poblat d'aquest torn");
            }
            if (assentamentRepository.assentamentsPropisSenseCarretera(idJugador, vertexA, vertexB) == 0) {
                throw new ReglaException("La carretera ha de tocar el poblat que acabes de col·locar");
            }
        } else if (assentamentRepository.assentamentsPropisEn(idJugador, vertexA, vertexB) == 0
                && carreteraRepository.carreteresPropiesTocant(idJugador, vertexA, vertexB) == 0) {
            throw new ReglaException("La carretera ha de tocar un poblat, ciutat o carretera teva");
        }

        if (carreteres >= MAX_CARRETERES) {
            throw new ReglaException("Ja tens " + MAX_CARRETERES + " carreteres");
        }
        if (!preparacio) {
            cobrar(idJugador, Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1));
        }

        carreteraRepository.save(new Carretera(jugador, aresta));
        return "Carretera construida a l'aresta: " + idAresta;
    }

    private String construirPoblat(Jugador jugador, Partida partida, int idVertex) {
        Vertex vertex = obtenirVertex(partida, idVertex);
        if (assentamentRepository.existsByVertexId(idVertex)) {
            throw new ReglaException("Posició ocupada: ja hi ha un assentament en aquest vèrtex");
        }
        if (assentamentRepository.assentamentsAdjacents(idVertex) > 0) {
            throw new ReglaException("Regla de la distància: hi ha un assentament en un vèrtex adjacent");
        }

        int idJugador = jugador.getId();
        boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
        long assentaments = assentamentRepository.countByPropietariId(idJugador);

        if (preparacio) {
            if (assentaments >= partida.getRonda()) {
                throw new ReglaException("Ja has col·locat el poblat d'aquest torn de preparació");
            }
        } else if (carreteraRepository.carreteresPropiesTocant(idJugador, idVertex, idVertex) == 0) {
            throw new ReglaException("El vèrtex ha de tocar una carretera teva");
        }

        if (poblatRepository.countByPropietariId(idJugador) >= MAX_POBLATS) {
            throw new ReglaException("Ja tens " + MAX_POBLATS + " poblats");
        }
        if (!preparacio) {
            cobrar(idJugador,
                    Map.of(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1, TipusRecurs.LLANA, 1, TipusRecurs.BLAT, 1));
        }

        poblatRepository.save(new Poblat(jugador, vertex));

        if (preparacio && assentaments + 1 == 2) {
            for (Hexagon hexagon : hexagonVertexRepository.hexagonsDelVertex(idVertex)) {
                TipusRecurs recurs = hexagon.getTerreny().getRecurs();
                if (recurs != null) {
                    partidaService.afegirRecurs(idJugador, recurs, 1);
                }
            }
        }

        partidaService.comprovarVictoria(partida);
        return "Poblat construit al vèrtex " + idVertex + guanyador(partida);
    }

    private String millorarACiutat(Jugador jugador, Partida partida, int idVertex) {
        if (partida.getEstat() != EstatPartida.EN_CURS) {
            throw new ReglaException("Les ciutats no es poden construir a la PREPARACIO");
        }
        obtenirVertex(partida, idVertex);
        int idJugador = jugador.getId();
        Poblat poblat = poblatRepository.findByVertexIdAndPropietariId(idVertex, idJugador)
                .orElseThrow(() -> new ReglaException("No hi ha cap poblat teu per millorar en aquest vèrtex"));

        if (ciutatRepository.countByPropietariId(idJugador) >= MAX_CIUTATS) {
            throw new ReglaException("Ja tens " + MAX_CIUTATS + " ciutats");
        }

        cobrar(idJugador, Map.of(TipusRecurs.BLAT, 2, TipusRecurs.MINERAL, 3));

        Vertex vertexPoblat = poblat.getVertex();
        poblatRepository.delete(poblat);
        poblatRepository.flush(); // vertex_id és únic a Assentament: cal esborrar el poblat abans d'inserir la ciutat
        ciutatRepository.save(new Ciutat(jugador, vertexPoblat));

        partidaService.comprovarVictoria(partida);
        return "Ciutat construida al vèrtex " + idVertex + guanyador(partida);
    }

    private Vertex obtenirVertex(Partida partida, int idVertex) {
        Vertex vertex = vertexRepository.findById(idVertex)
                .orElseThrow(() -> new ReglaException("Posició inexistent: no hi ha cap vèrtex " + idVertex));
        if (!vertex.getPartida().getId().equals(partida.getId())) {
            throw new ReglaException("Posició d'una altra partida");
        }
        return vertex;
    }

    private void cobrar(int idJugador, Map<TipusRecurs, Integer> cost) {
        for (Map.Entry<TipusRecurs, Integer> costEntry : cost.entrySet()) {
            int tinc = recursRepository.findByJugadorIdAndTipusRecurs(idJugador, costEntry.getKey())
                    .map(recursJugador -> recursJugador.getQuantitat()).orElse(0);
            if (tinc < costEntry.getValue()) {
                throw new ReglaException("No tens suficients recursos");
            }
        }
        for (Map.Entry<TipusRecurs, Integer> costEntry : cost.entrySet()) {
            partidaService.afegirRecurs(idJugador, costEntry.getKey(), -costEntry.getValue());
        }
    }

    private String guanyador(Partida partida) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            return " La partida ha finalitzat: el guanyador és " + partida.getGuanyador().getNom();
        }
        return "";
    }

    @Transactional
    public String passarTorn(int idJugador) {
        Jugador jugador = obtenirJugador(idJugador);
        Partida partida = jugador.getPartida();

        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("La partida ja ha finalitzat");
        }
        if (partida.getActiu() == null || !partida.getActiu().getId().equals(jugador.getId())) {
            throw new ReglaException("No és el teu torn");
        }

        if (partida.getEstat() == EstatPartida.EN_CURS) {
            if (!partida.getDausTirats()) {
                throw new ReglaException("Has de tirar els daus abans de passar");
            }
            if (partida.getLladrePendent()) {
                throw new ReglaException("Has de moure el lladre abans de passar");
            }
        } else {
            long assentaments = assentamentRepository.countByPropietariId(jugador.getId());
            long carreteres = carreteraRepository.countByPropietariId(jugador.getId());

            if (assentaments < partida.getRonda() || carreteres < partida.getRonda()) {
                throw new ReglaException("Has de col·locar el poblat i la carretera d'aquest torn");
            }
        }

        List<Jugador> jugadors = jugadorRepository.findByPartidaIdOrderByOrdreTorn(partida.getId());
        int totalJugadors = jugadors.size();
        int ordre = jugador.getOrdreTorn();
        int seguent;
        String extra = "";

        if (partida.getEstat() == EstatPartida.EN_CURS) {
            seguent = ordre % totalJugadors + 1;
            if (seguent == 1) {
                partida.setRonda(partida.getRonda() + 1);
            }
        } else if (partida.getRonda() == 1) {
            if (ordre < totalJugadors) {
                seguent = ordre + 1;
            } else {
                partida.setRonda(2);
                seguent = totalJugadors;
            }
        } else {
            if (ordre > 1) {
                seguent = ordre - 1;
            } else {
                partida.setEstat(EstatPartida.EN_CURS);
                partida.setRonda(1);
                seguent = 1;
                extra = " La partida ésta en curs";
            }
        }

        Jugador nouActiu = jugadors.get(seguent - 1);
        partida.setActiu(nouActiu);
        partida.setDausTirats(false);
        return "Torn passat." + extra + " Ara juga " + nouActiu.getNom()
                + " (ronda " + partida.getRonda() + ", " + partida.getEstat() + ")";
    }
}
