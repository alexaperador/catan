package catan.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import catan.Assentament;
import catan.CartaDesenvolupament;
import catan.Ciutat;
import catan.Hexagon;
import catan.Jugador;
import catan.Partida;
import catan.RecursJugador;
import catan.SessionManager;
import catan.enums.ColorJugador;
import catan.enums.TipusCarta;
import catan.enums.TipusRecurs;
import catan.utils.CarregadorTauler;

public class PartidaDao extends GenericDao<Partida, Integer> implements IPartidaDao {

    public int iniciarPartida(List<String> nomsJugadors) {
        if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
            throw new IllegalArgumentException("Hi ha d'haver entre 2 i 4 jugadors");
        }

        Transaction tx = null;
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

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
                    partida.setActiu(jugador); // comença l'ordreTorn 1
                }
            }

            afegirCartes(session, partida, TipusCarta.CAVALLER, 14);
            afegirCartes(session, partida, TipusCarta.PUNT_VICTORIA, 5);
            afegirCartes(session, partida, TipusCarta.MONOPOLI, 2);

            Hexagon desert = CarregadorTauler.carregarTauler(session, partida);
            partida.setLladre(desert);

            tx.commit();
            return partida.getId();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    private void afegirCartes(Session session, Partida partida, TipusCarta tipus, int quantitat) {
        for (int i = 0; i < quantitat; i++) {
            session.persist(new CartaDesenvolupament(partida, tipus));
        }
    }

    public void mostrarTauler(int idPartida) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
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
        }
    }

    public void mostrarPartida(int idPartida) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
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
                                + "WHERE c.jugador.id = :id AND c.tipusCarta = :tipus",
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
        }
    }

}