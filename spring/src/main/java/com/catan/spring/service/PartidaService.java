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

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final JugadorRepository jugadorRepository;
    private final RecursJugadorRepository recursRepository;
    private final PoblatRepository poblatRepository;
    private final CiutatRepository ciutatRepository;
    private final CartaDesenvolupamentRepository cartaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PartidaService(PartidaRepository partidaRepository, JugadorRepository jugadorRepository,
            RecursJugadorRepository recursRepository, PoblatRepository poblatRepository,
            CiutatRepository ciutatRepository,
            CartaDesenvolupamentRepository cartaRepository) {
        this.partidaRepository = partidaRepository;
        this.jugadorRepository = jugadorRepository;
        this.recursRepository = recursRepository;
        this.poblatRepository = poblatRepository;
        this.ciutatRepository = ciutatRepository;
        this.cartaRepository = cartaRepository;
    }

    public Partida obtenirPartida(int idPartida) {
        return partidaRepository.findById(idPartida)
                .orElseThrow(() -> new NoTrobatException("No existeix la partida " + idPartida));
    }

    @Transactional
    public PartidaDto iniciarPartida(NovaPartidaRequest peticio) {
        Partida partida = new Partida();
        if (peticio.puntsPerGuanyar() != null) {
            partida.setPuntsPerGuanyar(peticio.puntsPerGuanyar());
        }
        partidaRepository.save(partida);

        List<String> noms = new ArrayList<>(peticio.jugadors());
        Collections.shuffle(noms);
        ColorJugador[] colors = ColorJugador.values();
        for (int i = 0; i < noms.size(); i++) {
            Jugador jugador = new Jugador(noms.get(i).trim(), colors[i], i + 1, partida);
            for (TipusRecurs tipus : TipusRecurs.values()) {
                jugador.getRecursos().add(new RecursJugador(jugador, tipus, 0));
            }
            jugadorRepository.save(jugador);
            partida.getJugadors().add(jugador);
        }
        partida.setActiu(partida.getJugadors().get(0));

        crearCartes(partida, TipusCarta.CAVALLER, 14);
        crearCartes(partida, TipusCarta.PUNT_VICTORIA, 5);
        crearCartes(partida, TipusCarta.MONOPOLI, 2);

        // Necessitem la sessió de Hibernate per poder utilitzar CarregadorTauler.carregarTauler
        // unwrap fa que l'EntityManager es converteixi en una sessió de Hibernate
        Session sessio = entityManager.unwrap(Session.class);
        Hexagon desert = CarregadorTauler.carregarTauler(sessio, partida);
        partida.setLladre(desert);

        List<HexagonDto> hexagons = new ArrayList<>();
        for (Hexagon hexagon : partidaRepository.hexagonsDePartida(partida.getId())) {
            hexagons.add(new HexagonDto(hexagon.getId(), hexagon.getCodi(), hexagon.getTerreny(), hexagon.getNumero(),
                    hexagon.getId().equals(desert.getId()), List.of()));
        }

        List<JugadorDto> jugadors = new ArrayList<>();
        for (Jugador jugador : partida.getJugadors()) {
            jugadors.add(JugadorDto.convertirJugador(jugador));
        }

        return new PartidaDto(partida.getId(), partida.getEstat(), partida.getRonda(),
                partida.getPuntsPerGuanyar(), JugadorDto.convertirJugador(partida.getActiu()), jugadors, hexagons);
    }

    private void crearCartes(Partida partida, TipusCarta tipus, int quantitat) {
        for (int i = 0; i < quantitat; i++) {
            cartaRepository.save(new CartaDesenvolupament(partida, tipus));
        }
    }

    @Transactional(readOnly = true)
    public List<HexagonDto> mostrarTauler(int idPartida) {
        Partida partida = obtenirPartida(idPartida);
        Integer idLladre = partida.getLladre() != null ? partida.getLladre().getId() : null;

        Map<Integer, List<ConstruccioDto>> perHexagon = new HashMap<>();

        for (Object[] fila : partidaRepository.assentamentsPerHexagon(idPartida)) {
            Integer idHexagon = (Integer) fila[0];
            Assentament assentament = (Assentament) fila[1];
            String tipus = (assentament instanceof Ciutat) ? "CIUTAT" : "POBLAT";
            List<ConstruccioDto> construccions = perHexagon.get(idHexagon);

            if (construccions == null) {
                construccions = new ArrayList<>();
                perHexagon.put(idHexagon, construccions);
            }

            construccions.add(new ConstruccioDto(assentament.getId(), tipus, assentament.getVertex().getId(),
                    assentament.getVertex().getCodi(),
                    JugadorDto.convertirJugador(assentament.getPropietari())));
        }

        List<HexagonDto> tauler = new ArrayList<>();
        for (Hexagon hexagon : partidaRepository.hexagonsDePartida(idPartida)) {
            tauler.add(new HexagonDto(hexagon.getId(), hexagon.getCodi(), hexagon.getTerreny(), hexagon.getNumero(),
                    hexagon.getId().equals(idLladre), perHexagon.get(hexagon.getId())));
        }
        return tauler;
    }

    @Transactional
    public TiradaDto tirarDaus(int idPartida) {
        Partida partida = obtenirPartida(idPartida);

        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new ReglaException("La partida ja ha finalitzat");
        }
        if (partida.getEstat() != EstatPartida.EN_CURS) {
            throw new ReglaException("Encara s'està a la fase de PREPARACIO");
        }
        if (partida.getDausTirats()) {
            throw new ReglaException("El jugador actiu ja ha tirat aquest torn");
        }
        if (partida.getLladrePendent()) {
            throw new ReglaException("Cal moure el lladre abans de continuar");
        }

        Random random = new Random();

        int tirada = (random.nextInt(6) + 1) + (random.nextInt(6) + 1);
        partida.setUltimaTirada(tirada);
        partida.setDausTirats(true);

        List<Jugador> jugadors = jugadorRepository.findByPartidaIdOrderByOrdreTorn(idPartida);
        Map<Integer, Map<TipusRecurs, Integer>> rebuts = new LinkedHashMap<>();
        for (Jugador jugador : jugadors) {
            rebuts.put(jugador.getId(), new EnumMap<>(TipusRecurs.class));
        }

        if (tirada != 7) {
            int idLladre = partida.getLladre() != null ? partida.getLladre().getId() : -1;
            for (Object[] fila : partidaRepository.produccio(idPartida, tirada, idLladre)) {
                Hexagon hexagon = (Hexagon) fila[0];
                Assentament assentament = (Assentament) fila[1];
                TipusRecurs recurs = hexagon.getTerreny().getRecurs();
                if (recurs == null) {
                    continue;
                }
                int quantitat = (assentament instanceof Ciutat) ? 2 : 1;
                Jugador propietari = assentament.getPropietari();
                afegirRecurs(propietari.getId(), recurs, quantitat);
                Map<TipusRecurs, Integer> recursosRebuts = rebuts.get(propietari.getId());
                recursosRebuts.put(recurs, recursosRebuts.getOrDefault(recurs, 0) + quantitat);
            }
        }

        List<TiradaDto.RecursosRebuts> resum = new ArrayList<>();
        for (Jugador jugador : jugadors) {
            resum.add(new TiradaDto.RecursosRebuts(JugadorDto.convertirJugador(jugador), rebuts.get(jugador.getId())));
        }

        return new TiradaDto(idPartida, tirada, JugadorDto.convertirJugador(partida.getActiu()), resum);
    }

    public void afegirRecurs(int idJugador, TipusRecurs tipus, int quantitat) {
        RecursJugador recursJugador = recursRepository.findByJugadorIdAndTipusRecurs(idJugador, tipus)
                .orElseThrow(() -> new IllegalStateException("Inventari incomplet del jugador " + idJugador));
        recursJugador.setQuantitat(recursJugador.getQuantitat() + quantitat);
    }

    public int calcularPunts(Jugador jugador) {
        long punts = poblatRepository.countByPropietariId(jugador.getId())
                + 2 * ciutatRepository.countByPropietariId(jugador.getId())
                + cartaRepository.countByMaIdAndTipusCarta(jugador.getId(), TipusCarta.PUNT_VICTORIA);
        Jugador exercit = exercitMesGran(jugador.getPartida().getId());
        if (exercit != null && exercit.getId().equals(jugador.getId())) {
            punts += 2;
        }
        return (int) punts;
    }

    public Jugador exercitMesGran(int idPartida) {
        Jugador millor = null;
        boolean empat = false;
        for (Jugador jugador : jugadorRepository.findByPartidaIdOrderByOrdreTorn(idPartida)) {
            if (millor == null || jugador.getCavallersJugats() > millor.getCavallersJugats()) {
                millor = jugador;
                empat = false;
            } else if (jugador.getCavallersJugats() == millor.getCavallersJugats()) {
                empat = true;
            }
        }
        if (millor == null || empat || millor.getCavallersJugats() < 3) {
            return null;
        }
        return millor;
    }

    public void comprovarVictoria(Partida partida) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            return;
        }
        for (Jugador jugador : jugadorRepository.findByPartidaIdOrderByOrdreTorn(partida.getId())) {
            if (calcularPunts(jugador) >= partida.getPuntsPerGuanyar()) {
                partida.setEstat(EstatPartida.FINALITZADA);
                partida.setGuanyador(jugador);
                return;
            }
        }
    }
}
