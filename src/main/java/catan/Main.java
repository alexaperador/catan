package catan;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import catan.dao.JugadorDao;
import catan.dao.PartidaDao;
import catan.enums.TipusCarta;
import catan.enums.TipusRecurs;

public class Main {
    public static void main(String[] args) {
        PartidaDao partidaDao = new PartidaDao();
        JugadorDao jugadorDao = new JugadorDao();

        try {
            System.out.println("\n=== EXEMPLE 1: iniciar una partida ===");
            List<String> nomsJugadors = java.util.Arrays.asList("Alice", "Bob", "Charlie");
            int idPartida = partidaDao.iniciarPartida(nomsJugadors);
            System.out.println("\t Partida iniciada amb ID: " + idPartida);
            comprovarIniciPartida(idPartida, nomsJugadors.size());

            System.out.println("\n=== EXEMPLE 2: consultar el tauler ===");
            partidaDao.mostrarTauler(idPartida);
            System.out.println("[OK] mostrarTauler ha consultat el tauler");

            List<Jugador> jugadors = obtenirJugadors(idPartida);

            System.out.println("\n=== EXEMPLE 3: preparar la partida ===");
            prepararPartida(jugadorDao, jugadors);

            System.out.println("\n=== EXEMPLE 4: tirar els daus i consultar la partida ===");
                Map<Integer, Map<TipusRecurs, Integer>> recursosRepartits = partidaDao.tirarDaus(idPartida);
                comprovar(recursosRepartits.size() == jugadors.size(),
                    "tirarDaus ha retornat informació de tots els jugadors");
            partidaDao.mostrarPartida(idPartida);
                System.out.println("[OK] mostrarPartida ha consultat l'estat de la partida");

            System.out.println("\n=== EXEMPLE 5: consultar els recursos ===");
            for (Jugador jugador : jugadors) {
                partidaDao.mostrarRecursos(jugador.getId());
            }

            System.out.println("\n=== EXEMPLE 6: passar el torn ===");
            jugadorDao.passarTorn(jugadors.get(0).getId());
            partidaDao.mostrarPartida(idPartida);

        } catch (Exception e) {
            System.err.println("Error en la prova: " + e.getMessage());
        } finally {
            SessionManager.tancar();
        }

    }

    private static void comprovarIniciPartida(int idPartida, int jugadorsEsperats) {
        comprovar(idPartida > 0, "iniciarPartida ha retornat un ID vàlid");

        List<Jugador> jugadors = obtenirJugadors(idPartida);
        comprovar(jugadors.size() == jugadorsEsperats,
                "iniciarPartida ha creat " + jugadorsEsperats + " jugadors");

        try (Session session = SessionManager.getSessionFactory().openSession()) {
            Long partides = session.createQuery(
                    "SELECT COUNT(p) FROM Partida p WHERE p.id = :idPartida", Long.class)
                    .setParameter("idPartida", idPartida)
                    .getSingleResult();
            comprovar(partides == 1, "iniciarPartida ha guardat la partida");

            for (TipusCarta tipus : TipusCarta.values()) {
                long esperades = quantitatCartesEsperada(tipus);
                Long actuals = session.createQuery(
                        "SELECT COUNT(c) FROM CartaDesenvolupament c "
                                + "WHERE c.pila.id = :idPartida AND c.tipusCarta = :tipus", Long.class)
                        .setParameter("idPartida", idPartida)
                        .setParameter("tipus", tipus)
                        .getSingleResult();
                comprovar(actuals == esperades,
                        "afegirCartes ha creat " + esperades + " cartes de tipus " + tipus);
            }
        }
    }

    private static long quantitatCartesEsperada(TipusCarta tipus) {
        return switch (tipus) {
            case CAVALLER -> 14;
            case PUNT_VICTORIA -> 5;
            case MONOPOLI -> 2;
        };
    }

    private static void comprovar(boolean condicio, String missatge) {
        if (!condicio) {
            throw new IllegalStateException("[FALLA] " + missatge);
        }
        System.out.println("[OK] " + missatge);
    }

    private static List<Jugador> obtenirJugadors(int idPartida) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Jugador j WHERE j.partida.id = :idPartida ORDER BY j.ordreTorn", Jugador.class)
                    .setParameter("idPartida", idPartida)
                    .getResultList();
        }
    }

    private static void prepararPartida(JugadorDao jugadorDao, List<Jugador> jugadors) {
        List<Jugador> ordreNormal = jugadors.stream()
                .sorted(Comparator.comparingInt(Jugador::getOrdreTorn))
                .toList();

        for (int ronda = 1; ronda <= 2; ronda++) {
            List<Jugador> ordreRonda = ronda == 1 ? ordreNormal : ordreNormal.reversed();

            for (Jugador jugador : ordreRonda) {
                List<Vertex> vertexs = new PartidaDao().vertexsDisponibles(jugador.getId());
                Vertex vertex = vertexs.get(0);
                jugadorDao.construirPoblat(jugador.getId(), vertex.getId());

                int idAresta = obtenirArestaDelVertex(vertex.getId());
                jugadorDao.construirCarretera(jugador.getId(), idAresta);
                jugadorDao.passarTorn(jugador.getId());
            }
        }
    }

    private static int obtenirArestaDelVertex(int idVertex) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            Aresta aresta = session.createQuery(
                "FROM Aresta a WHERE (a.vertexA.id = :idVertex OR a.vertexB.id = :idVertex) "
                    + "AND NOT EXISTS (SELECT c FROM Carretera c WHERE c.aresta = a)",
                Aresta.class)
                    .setParameter("idVertex", idVertex)
                    .setMaxResults(1)
                    .getSingleResult();
            return aresta.getId();
        }
    }
}