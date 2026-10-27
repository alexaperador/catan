package catan.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Session;

import catan.Assentament;
import catan.CartaDesenvolupament;
import catan.Ciutat;
import catan.Hexagon;
import catan.Jugador;
import catan.Partida;
import catan.RecursJugador;
import catan.Vertex;
import catan.enums.ColorJugador;
import catan.enums.EstatPartida;
import catan.enums.TipusCarta;
import catan.enums.TipusRecurs;
import catan.enums.TipusTerreny;
import catan.utils.CarregadorTauler;

public class PartidaDao extends GenericDao<Partida, Integer> implements IPartidaDao {

    @Override
    public int iniciarPartida(List<String> nomsJugadors) {
        if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
            throw new IllegalArgumentException("El nombre de jugadors ha de ser entre 2 i 4.");
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Partida partida = new Partida();
            session.persist(partida);

            List<String> noms = new ArrayList<>(nomsJugadors);
            Collections.shuffle(noms);
            ColorJugador[] colors = ColorJugador.values();
            for (int i = 0; i < noms.size(); i++) {
                Jugador jugador = new Jugador(noms.get(i), colors[i], i + 1, partida);
                for (TipusRecurs tipus : TipusRecurs.values()) {
                    jugador.getRecursos().add(new RecursJugador(jugador, tipus, 0));
                }
                session.persist(jugador);
                partida.getJugadors().add(jugador);
                if (i == 0) {
                    partida.setActiu(jugador);
                }
            }

            afegirCartes(session, partida, TipusCarta.CAVALLER, 14);
            afegirCartes(session, partida, TipusCarta.PUNT_VICTORIA, 5);
            afegirCartes(session, partida, TipusCarta.MONOPOLI, 2);

            Hexagon desert = CarregadorTauler.carregarTauler(session, partida);
            partida.setLladre(desert);

            session.getTransaction().commit();

            return partida.getId();
        } catch (RuntimeException e) {
            handleException(session, e);
            return -1;
        }
    }

    private void afegirCartes(Session session, Partida partida, TipusCarta tipus, int quantitat) {
        for (int i = 0; i < quantitat; i++) {
            session.persist(new CartaDesenvolupament(partida, tipus));
        }
    }

    public void mostrarTauler(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                throw new IllegalArgumentException("No existeix la partida: " + idPartida);
            }

            List<Hexagon> hexagons = session
                    .createNamedQuery("Hexagon.perPartida", Hexagon.class)
                    .setParameter("idPartida", idPartida)
                    .getResultList();

            List<Object[]> files = session
                    .createNamedQuery("HexagonVertex.assentamentsPerPartida", Object[].class)
                    .setParameter("idPartida", idPartida)
                    .getResultList();

            Map<Integer, List<Assentament>> porHexagon = new HashMap<>();
            for (Object[] fila : files) {
                Integer idHexagon = (Integer) fila[0];
                Assentament assentament = (Assentament) fila[1];
                if (!porHexagon.containsKey(idHexagon)) {
                    porHexagon.put(idHexagon, new ArrayList<>());
                }
                porHexagon.get(idHexagon).add(assentament);
            }

            Integer idLladre = (partida.getLladre() != null) ? partida.getLladre().getId() : null;

            System.out.println("Tauler de la partida " + idPartida);
            for (Hexagon hexagon : hexagons) {
                String numero = hexagon.getNumero() != null ? "número " + hexagon.getNumero() : "sense número";

                String lladre = hexagon.getId().equals(idLladre) ? "  (LLADRE)" : "";

                System.out
                        .println("Hexàgon " + hexagon.getCodi() + ": " + hexagon.getTerreny() + ", " + numero + lladre);

                for (Assentament assentament : porHexagon.getOrDefault(hexagon.getId(), new ArrayList<>())) {
                    String tipus = (assentament instanceof Ciutat) ? "Ciutat" : "Poblat";
                    System.out.println("    " + tipus + " de " + assentament.getPropietari().getNom()
                            + " (" + assentament.getPropietari().getColor() + ")"
                            + " al vèrtex " + assentament.getVertex().getCodi());
                }
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    public void mostrarRecursos(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }

            List<RecursJugador> recursos = session
                    .createNamedQuery("RecursJugador.perJugador", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .getResultList();

            System.out.println("Recursos de " + jugador.getNom() + " (" + jugador.getColor() + ")");
            for (RecursJugador r : recursos) {
                System.out.println("  " + r.getTipusRecurs() + ": " + r.getQuantitat());
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    public void mostrarPartida(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                throw new IllegalArgumentException("No existeix la partida: " + idPartida);
            }

            String actiu = (partida.getActiu() != null) ? partida.getActiu().getNom() : "cap";
            String tirada = (partida.getUltimaTirada() != null) ? String.valueOf(partida.getUltimaTirada()) : "cap";

            System.out.println("Partida " + idPartida);
            System.out.println("Estat: " + partida.getEstat() + " (Ronda: " + partida.getRonda() + ")");
            System.out.println("Jugador actiu: " + actiu + " (Última tirada: " + tirada + ")");

            List<Jugador> jugadors = session
                    .createNamedQuery("Jugador.perPartida", Jugador.class)
                    .setParameter("idPartida", idPartida)
                    .getResultList();

            for (Jugador jugador : jugadors) {
                int id = jugador.getId();
                long poblats = session
                        .createQuery("SELECT COUNT(p) FROM Poblat p WHERE p.propietari.id = :id", Long.class)
                        .setParameter("id", id).uniqueResult();
                long ciutats = session
                        .createQuery("SELECT COUNT(c) FROM Ciutat c WHERE c.propietari.id = :id", Long.class)
                        .setParameter("id", id).uniqueResult();
                long carreteres = session
                        .createQuery("SELECT COUNT(r) FROM Carretera r WHERE r.propietari.id = :id", Long.class)
                        .setParameter("id", id).uniqueResult();
                long cartesPunt = session.createQuery(
                        "SELECT COUNT(c) FROM CartaDesenvolupament c "
                                + "WHERE c.ma.id = :id AND c.tipusCarta = :tipus",
                        Long.class)
                        .setParameter("id", id)
                        .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                        .uniqueResult();
                Long totalRecursos = session.createQuery(
                        "SELECT SUM(r.quantitat) FROM RecursJugador r WHERE r.jugador.id = :id", Long.class)
                        .setParameter("id", id)
                        .uniqueResult();

                long punts = poblats + 2 * ciutats + cartesPunt;

                System.out.println(
                        "- " + jugador.getNom() + " (" + jugador.getColor() + ", torn " + jugador.getOrdreTorn() + ")");
                System.out.println("Punts: " + punts
                        + " | Poblats: " + poblats
                        + " | Ciutats: " + ciutats
                        + " | Carreteres: " + carreteres);
                System.out.println("Total de recursos: " + totalRecursos.intValue());
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    public Jugador exercitMesGran(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        boolean transaccioIniciada = !session.getTransaction().isActive();
        try {
            if (transaccioIniciada) {
                session.beginTransaction();
            }
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                throw new IllegalArgumentException("No existeix la partida: " + idPartida);
            }

            List<Jugador> top = session
                    .createQuery("FROM Jugador j WHERE j.partida.id = :idPartida "
                            + "ORDER BY j.cavallersJugats DESC", Jugador.class)
                    .setParameter("idPartida", idPartida)
                    .setMaxResults(2)
                    .getResultList();

            Jugador guanyador = null;
            if (!top.isEmpty()) {
                Jugador primer = top.get(0);
                boolean prouCavallers = primer.getCavallersJugats() >= 3;
                boolean empat = top.size() > 1 && top.get(1).getCavallersJugats() == primer.getCavallersJugats();
                if (prouCavallers && !empat) {
                    guanyador = primer;
                }
            }

            session.getTransaction().commit();
            return guanyador;

        } catch (RuntimeException e) {
            handleException(session, e);
            return null;
        }
    }

    public List<Vertex> vertexsDisponibles(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }

            Partida partida = jugador.getPartida();
            boolean preparacio = partida.getEstat().equals(EstatPartida.PREPARACIO);

            String hql = "SELECT v FROM Vertex v WHERE v.partida.id = :idPartida "
                    + "AND NOT EXISTS (SELECT 1 FROM Assentament a WHERE a.vertex = v) "
                    + "AND NOT EXISTS (SELECT 1 FROM Aresta ar, Assentament a2 "
                    + "    WHERE (ar.vertexA = v AND a2.vertex = ar.vertexB) "
                    + "    OR (ar.vertexB = v AND a2.vertex = ar.vertexA)) ";

            if (!preparacio) {
                hql += "AND EXISTS (SELECT 1 FROM Carretera c "
                        + "    WHERE c.propietari.id = :idJugador "
                        + "      AND (c.aresta.vertexA = v OR c.aresta.vertexB = v)) ";
            }

            hql += "ORDER BY v.codi";

            var query = session.createQuery(hql, Vertex.class)
                    .setParameter("idPartida", partida.getId());
            if (!preparacio) {
                query.setParameter("idJugador", idJugador);
            }
            List<Vertex> vertexs = query.getResultList();
            session.getTransaction().commit();
            return vertexs;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<Integer, Map<TipusRecurs, Integer>> tirarDaus(int idPartida) {
        Random RANDOM = new Random();
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                throw new IllegalArgumentException("No existeix la partida: " + idPartida);
            }
            if (partida.getEstat() == EstatPartida.FINALITZADA) {
                throw new IllegalStateException("La partida ja ha finalitzat");
            }
            if (partida.getEstat() != EstatPartida.EN_CURS) {
                throw new IllegalStateException("La partida no està en curs");
            }
            if (partida.getDausTirats()) {
                throw new IllegalStateException("Ja has tirat els daus aquest torn");
            }
            if (partida.getLladrePendent()) {
                throw new IllegalStateException("Has de moure el lladre abans");
            }

            Map<Integer, Map<TipusRecurs, Integer>> resum = new LinkedHashMap<>();
            for (Jugador j : partida.getJugadors()) {
                resum.put(j.getId(), new EnumMap<>(TipusRecurs.class));
            }

            int tirada = (RANDOM.nextInt(6) + 1) + (RANDOM.nextInt(6) + 1);
            partida.setUltimaTirada(tirada);
            partida.setDausTirats(true);

            if (tirada == 7) {
                for (Jugador j : partida.getJugadors()) {
                    descartarMeitat(session, j);
                }
                partida.setLladrePendent(true);
            } else {
                Integer idLladre = (partida.getLladre() != null) ? partida.getLladre().getId() : null;

                List<Object[]> files = session.createQuery(
                        "SELECT h, a FROM HexagonVertex hv JOIN hv.hexagon h, Assentament a "
                                + "WHERE a.vertex = hv.vertex "
                                + "AND h.partida.id = :idPartida AND h.numero = :numero",
                        Object[].class)
                        .setParameter("idPartida", idPartida)
                        .setParameter("numero", tirada)
                        .getResultList();

                for (Object[] fila : files) {
                    Hexagon hexagon = (Hexagon) fila[0];
                    Assentament assentament = (Assentament) fila[1];

                    if (hexagon.getId().equals(idLladre)) {
                        continue;
                    }
                    TipusRecurs recurs = recursDelTerreny(hexagon.getTerreny());
                    if (recurs == null) {
                        continue;
                    }
                    int quantitat = (assentament instanceof Ciutat) ? 2 : 1;

                    Jugador propietari = assentament.getPropietari();
                    RecursJugador rj = session
                            .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                            .setParameter("idJugador", propietari.getId())
                            .setParameter("tipus", recurs)
                            .getSingleResult();
                    rj.setQuantitat(rj.getQuantitat() + quantitat);

                    resum.get(propietari.getId()).merge(recurs, quantitat, Integer::sum);
                }
            }

            session.getTransaction().commit();
            System.out.println("Tirada: " + tirada);
            return resum;
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    private TipusRecurs recursDelTerreny(TipusTerreny terreny) {
        switch (terreny) {
            case BOSC:
                return TipusRecurs.FUSTA;
            case TURONS:
                return TipusRecurs.MAO;
            case PASTURES:
                return TipusRecurs.LLANA;
            case CAMPS:
                return TipusRecurs.BLAT;
            case MUNTANYES:
                return TipusRecurs.MINERAL;
            default:
                return null;
        }
    }

    private void descartarMeitat(Session session, Jugador jugador) {
        List<RecursJugador> inventari = session
                .createNamedQuery("RecursJugador.perJugador", RecursJugador.class)
                .setParameter("idJugador", jugador.getId())
                .getResultList();

        int total = inventari.stream().mapToInt(RecursJugador::getQuantitat).sum();
        if (total <= 7) {
            return;
        }
        List<RecursJugador> fitxes = new ArrayList<>();
        for (RecursJugador rj : inventari) {
            for (int i = 0; i < rj.getQuantitat(); i++) {
                fitxes.add(rj);
            }
        }
        Collections.shuffle(fitxes);
        for (int i = 0; i < total / 2; i++) {
            RecursJugador rj = fitxes.get(i);
            rj.setQuantitat(rj.getQuantitat() - 1);
        }
    }

    @Override
    public void comprovarVictoria(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                throw new IllegalArgumentException("No existeix la partida: " + idPartida);
            }
            if (partida.getEstat() != EstatPartida.EN_CURS) {
                session.getTransaction().commit();
                return;
            }

            Jugador exercit = jugadorExercitMesGran(session, idPartida);

            Jugador guanyador = null;
            int milloresPunts = 0;
            for (Jugador j : partida.getJugadors()) {
                int punts = calcularPunts(session, j, exercit);
                if (punts >= partida.getPuntsPerGuanyar() && punts > milloresPunts) {
                    guanyador = j;
                    milloresPunts = punts;
                }
            }

            if (guanyador != null) {
                partida.setEstat(EstatPartida.FINALITZADA);
                partida.setGuanyador(guanyador);
                System.out.println(guanyador.getNom() + " ha guanyat amb " + milloresPunts + " punts!");
            }

            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    private int calcularPunts(Session session, Jugador jugador, Jugador exercitMesGran) {
        int id = jugador.getId();
        long poblats = session
                .createQuery("SELECT COUNT(p) FROM Poblat p WHERE p.propietari.id = :id", Long.class)
                .setParameter("id", id).uniqueResult();
        long ciutats = session
                .createQuery("SELECT COUNT(c) FROM Ciutat c WHERE c.propietari.id = :id", Long.class)
                .setParameter("id", id).uniqueResult();
        long cartesPunt = session.createQuery(
                "SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.ma.id = :id AND c.tipusCarta = :tipus",
                Long.class)
                .setParameter("id", id)
                .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                .uniqueResult();

        int punts = (int) (poblats + 2 * ciutats + cartesPunt);
        if (exercitMesGran != null && exercitMesGran.getId().equals(id)) {
            punts += 2;
        }
        return punts;
    }

    private Jugador jugadorExercitMesGran(Session session, int idPartida) {
        List<Jugador> top = session
                .createQuery("FROM Jugador j WHERE j.partida.id = :idPartida ORDER BY j.cavallersJugats DESC",
                        Jugador.class)
                .setParameter("idPartida", idPartida)
                .setMaxResults(2)
                .getResultList();
        if (top.isEmpty() || top.get(0).getCavallersJugats() < 3) {
            return null;
        }
        if (top.size() > 1 && top.get(1).getCavallersJugats() == top.get(0).getCavallersJugats()) {
            return null;
        }
        return top.get(0);
    }
}