package com.catan.spring.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.catan.spring.dto.ConstruccioDto;
import com.catan.spring.dto.HexagonDto;
import com.catan.spring.dto.JugadorDto;
import com.catan.spring.dto.NovaPartidaRequest;
import com.catan.spring.dto.PartidaDto;
import com.catan.spring.dto.TiradaDto;
import com.catan.spring.exception.NoTrobatException;
import com.catan.spring.exception.ReglaException;
import com.catan.spring.repository.CartaDesenvolupamentRepository;
import com.catan.spring.repository.CiutatRepository;
import com.catan.spring.repository.JugadorRepository;
import com.catan.spring.repository.PartidaRepository;
import com.catan.spring.repository.PoblatRepository;
import com.catan.spring.repository.RecursJugadorRepository;

import catan.enums.ColorJugador;
import catan.enums.EstatPartida;
import catan.enums.TipusCarta;
import catan.enums.TipusRecurs;
import catan.models.Assentament;
import catan.models.CartaDesenvolupament;
import catan.models.Ciutat;
import catan.models.Hexagon;
import catan.models.Jugador;
import catan.models.Partida;
import catan.models.RecursJugador;
import catan.utils.CarregadorTauler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Lògica de la partida: crear-la, mostrar el tauler, tirar els daus i calcular punts i victòria.
 * Tot passa per repositoris de Spring Data dins de transaccions de Spring.
 */
@Service
public class PartidaService {

    private final Random random = new Random();

    private final PartidaRepository partidaRepo;
    private final JugadorRepository jugadorRepo;
    private final RecursJugadorRepository recursRepo;
    private final PoblatRepository poblatRepo;
    private final CiutatRepository ciutatRepo;
    private final CartaDesenvolupamentRepository cartaRepo;

    @PersistenceContext
    private EntityManager em;

    public PartidaService(PartidaRepository partidaRepo, JugadorRepository jugadorRepo,
            RecursJugadorRepository recursRepo, PoblatRepository poblatRepo, CiutatRepository ciutatRepo,
            CartaDesenvolupamentRepository cartaRepo) {
        this.partidaRepo = partidaRepo;
        this.jugadorRepo = jugadorRepo;
        this.recursRepo = recursRepo;
        this.poblatRepo = poblatRepo;
        this.ciutatRepo = ciutatRepo;
        this.cartaRepo = cartaRepo;
    }

    public Partida obtenirPartida(int idPartida) {
        return partidaRepo.findById(idPartida)
                .orElseThrow(() -> new NoTrobatException("No existeix la partida " + idPartida));
    }

    // ---------------------------------------------------------------- POST /partida

    /**
     * Crea una partida en PREPARACIO: jugadors amb ordre de torn aleatori (el color va
     * segons l'ordre), inventari a 0, pila de cartes i tauler carregat dels CSV amb
     * terrenys i números aleatoris. El lladre comença al desert.
     */
    @Transactional
    public PartidaDto iniciarPartida(NovaPartidaRequest peticio) {
        Partida partida = new Partida();
        if (peticio.puntsPerGuanyar() != null) {
            partida.setPuntsPerGuanyar(peticio.puntsPerGuanyar());
        }
        partidaRepo.save(partida);

        List<String> noms = new ArrayList<>(peticio.jugadors());
        Collections.shuffle(noms);
        ColorJugador[] colors = ColorJugador.values();
        for (int i = 0; i < noms.size(); i++) {
            Jugador jugador = new Jugador(noms.get(i).trim(), colors[i], i + 1, partida);
            for (TipusRecurs tipus : TipusRecurs.values()) {
                jugador.getRecursos().add(new RecursJugador(jugador, tipus, 0));
            }
            jugadorRepo.save(jugador);
            partida.getJugadors().add(jugador);
        }
        partida.setActiu(partida.getJugadors().get(0));

        crearCartes(partida, TipusCarta.CAVALLER, 14);
        crearCartes(partida, TipusCarta.PUNT_VICTORIA, 5);
        crearCartes(partida, TipusCarta.MONOPOLI, 2);

        // Reutilitzem el carregador del projecte Hibernate amb la sessió de la transacció de Spring
        Hexagon desert = CarregadorTauler.carregarTauler(em.unwrap(Session.class), partida);
        partida.setLladre(desert);

        List<HexagonDto> hexagons = new ArrayList<>();
        for (Hexagon h : partidaRepo.hexagonsDePartida(partida.getId())) {
            hexagons.add(new HexagonDto(h.getId(), h.getCodi(), h.getTerreny(), h.getNumero(),
                    h.getId().equals(desert.getId()), List.of()));
        }
        List<JugadorDto> jugadors = partida.getJugadors().stream().map(JugadorDto::de).toList();

        return new PartidaDto(partida.getId(), partida.getEstat(), partida.getRonda(),
                partida.getPuntsPerGuanyar(), JugadorDto.de(partida.getActiu()), jugadors, hexagons);
    }

    private void crearCartes(Partida partida, TipusCarta tipus, int quantitat) {
        for (int i = 0; i < quantitat; i++) {
            cartaRepo.save(new CartaDesenvolupament(partida, tipus));
        }
    }

    // ---------------------------------------------------------- GET /partida/{id}/tauler

    /** Hexàgons de la partida amb terreny, número, lladre i els poblats/ciutats dels seus vèrtexs. */
    @Transactional(readOnly = true)
    public List<HexagonDto> mostrarTauler(int idPartida) {
        Partida partida = obtenirPartida(idPartida);
        Integer idLladre = partida.getLladre() != null ? partida.getLladre().getId() : null;

        Map<Integer, List<ConstruccioDto>> perHexagon = new HashMap<>();
        for (Object[] fila : partidaRepo.assentamentsPerHexagon(idPartida)) {
            Integer idHexagon = (Integer) fila[0];
            Assentament a = (Assentament) fila[1];
            String tipus = (a instanceof Ciutat) ? "CIUTAT" : "POBLAT";
            perHexagon.computeIfAbsent(idHexagon, k -> new ArrayList<>())
                    .add(new ConstruccioDto(a.getId(), tipus, a.getVertex().getId(), a.getVertex().getCodi(),
                            JugadorDto.de(a.getPropietari())));
        }

        List<HexagonDto> tauler = new ArrayList<>();
        for (Hexagon h : partidaRepo.hexagonsDePartida(idPartida)) {
            tauler.add(new HexagonDto(h.getId(), h.getCodi(), h.getTerreny(), h.getNumero(),
                    h.getId().equals(idLladre), perHexagon.getOrDefault(h.getId(), List.of())));
        }
        return tauler;
    }

    // ---------------------------------------------------------- POST /partida/{id}/daus

    /**
     * Tira 2 daus pel jugador actiu i reparteix la producció: 1 recurs per poblat i 2 per
     * ciutat en cada hexàgon amb aquest número que no tingui el lladre.
     * Amb un 7 no hi ha producció (el moviment del lladre no té endpoint a Spring).
     */
    @Transactional
    public TiradaDto tirarDaus(int idPartida) {
        Partida partida = obtenirPartida(idPartida);

        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("Partida no en curs: la partida ja ha finalitzat");
        }
        if (partida.getEstat() != EstatPartida.EN_CURS) {
            throw new ReglaException("Partida no en curs: encara s'està a la fase de PREPARACIO");
        }
        if (partida.getDausTirats()) {
            throw new ReglaException("Daus ja tirats: el jugador actiu ja ha tirat aquest torn");
        }
        if (partida.getLladrePendent()) {
            throw new ReglaException("Lladre pendent: cal moure el lladre abans de continuar");
        }

        int tirada = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
        partida.setUltimaTirada(tirada);
        partida.setDausTirats(true);

        List<Jugador> jugadors = jugadorRepo.findByPartidaIdOrderByOrdreTorn(idPartida);
        Map<Integer, Map<TipusRecurs, Integer>> rebuts = new LinkedHashMap<>();
        for (Jugador j : jugadors) {
            rebuts.put(j.getId(), new EnumMap<>(TipusRecurs.class));
        }

        if (tirada != 7) {
            int idLladre = partida.getLladre() != null ? partida.getLladre().getId() : -1;
            for (Object[] fila : partidaRepo.produccio(idPartida, tirada, idLladre)) {
                Hexagon hexagon = (Hexagon) fila[0];
                Assentament assentament = (Assentament) fila[1];
                TipusRecurs recurs = hexagon.getTerreny().getRecurs();
                if (recurs == null) {
                    continue; // el desert no té número, però per si de cas
                }
                int quantitat = (assentament instanceof Ciutat) ? 2 : 1;
                Jugador propietari = assentament.getPropietari();
                afegirRecurs(propietari.getId(), recurs, quantitat);
                rebuts.get(propietari.getId()).merge(recurs, quantitat, Integer::sum);
            }
        }

        List<TiradaDto.RecursosRebuts> resum = new ArrayList<>();
        for (Jugador j : jugadors) {
            resum.add(new TiradaDto.RecursosRebuts(JugadorDto.de(j), rebuts.get(j.getId())));
        }
        return new TiradaDto(idPartida, tirada, JugadorDto.de(partida.getActiu()), resum);
    }

    /** Suma (o resta, si quantitat és negativa) recursos a l'inventari d'un jugador. */
    public void afegirRecurs(int idJugador, TipusRecurs tipus, int quantitat) {
        RecursJugador rj = recursRepo.findByJugadorIdAndTipusRecurs(idJugador, tipus)
                .orElseThrow(() -> new IllegalStateException("Inventari incomplet del jugador " + idJugador));
        rj.setQuantitat(rj.getQuantitat() + quantitat);
    }

    // ------------------------------------------------------------- Punts i victòria

    /** Punts de victòria: poblats (1), ciutats (2), cartes PUNT_VICTORIA (1) i exèrcit més gran (2). */
    public int calcularPunts(Jugador jugador) {
        long punts = poblatRepo.countByPropietariId(jugador.getId())
                + 2 * ciutatRepo.countByPropietariId(jugador.getId())
                + cartaRepo.countByMaIdAndTipusCarta(jugador.getId(), TipusCarta.PUNT_VICTORIA);
        Jugador exercit = exercitMesGran(jugador.getPartida().getId());
        if (exercit != null && exercit.getId().equals(jugador.getId())) {
            punts += 2;
        }
        return (int) punts;
    }

    /** Jugador amb més cavallers jugats (mínim 3 i sense empat), o null. */
    public Jugador exercitMesGran(int idPartida) {
        Jugador millor = null;
        boolean empat = false;
        for (Jugador j : jugadorRepo.findByPartidaIdOrderByOrdreTorn(idPartida)) {
            if (millor == null || j.getCavallersJugats() > millor.getCavallersJugats()) {
                millor = j;
                empat = false;
            } else if (j.getCavallersJugats() == millor.getCavallersJugats()) {
                empat = true;
            }
        }
        if (millor == null || empat || millor.getCavallersJugats() < 3) {
            return null;
        }
        return millor;
    }

    /** Si algun jugador arriba als punts per guanyar, la partida passa a FINALITZADA. */
    public void comprovarVictoria(Partida partida) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            return;
        }
        for (Jugador j : jugadorRepo.findByPartidaIdOrderByOrdreTorn(partida.getId())) {
            if (calcularPunts(j) >= partida.getPuntsPerGuanyar()) {
                partida.setEstat(EstatPartida.FINALITZADA);
                partida.setGuanyador(j);
                return;
            }
        }
    }
}
