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
            // Crea una partida, comprova jugadors i cartes inicials.
            System.out.println("\n=== EXEMPLE 1: iniciar una partida ===");
            List<String> nomsJugadors = java.util.Arrays.asList("Alice", "Bob", "Charlie");
            int idPartida = partidaDao.iniciarPartida(nomsJugadors);
            System.out.println("\t Partida iniciada amb ID: " + idPartida);
            comprovarIniciPartida(idPartida, nomsJugadors.size());

            // Mostra el tauler carregat des de la base de dades.
            System.out.println("\n=== EXEMPLE 2: consultar el tauler ===");
            partidaDao.mostrarTauler(idPartida);
            System.out.println("[OK] mostrarTauler ha consultat el tauler");

            List<Jugador> jugadors = obtenirJugadors(idPartida);

            // Completa les dues rondes de preparació del joc.
            System.out.println("\n=== EXEMPLE 3: preparar la partida ===");
            prepararPartida(jugadorDao, jugadors);

            // Tira els daus i comprova el repartiment de recursos.
            System.out.println("\n=== EXEMPLE 4: tirar els daus i consultar la partida ===");
            Map<Integer, Map<TipusRecurs, Integer>> recursosRepartits = partidaDao.tirarDaus(idPartida);
            comprovar(recursosRepartits.size() == jugadors.size(),
                    "tirarDaus ha retornat informació de tots els jugadors");
            partidaDao.mostrarPartida(idPartida);
            System.out.println("[OK] mostrarPartida ha consultat l'estat de la partida");

            Jugador jugadorActiu = jugadors.get(0);
            if (lladrePendent(idPartida)) {
                jugadorDao.moureLladre(jugadorActiu.getId(), obtenirHexagonAlternatiu(idPartida), null);
                System.out.println("[OK] moureLladre ha mogut el lladre després d'un 7");
            }

            // Executa les accions principals del jugador.
            System.out.println("\n=== EXEMPLE 5: operacions del jugador actiu ===");
            prepararRecursosPerProva(jugadorActiu.getId());
            jugadorDao.mostrarRecursos(jugadorActiu.getId());
            jugadorDao.comerciarBanca(jugadorActiu.getId(), TipusRecurs.FUSTA, TipusRecurs.MAO);
            System.out.println("[OK] comerciarBanca ha canviat recursos");
            jugadorDao.comprarCarta(jugadorActiu.getId());
            System.out.println("[OK] comprarCarta ha comprat una carta");
            CartaDesenvolupament cartaJugable = prepararCartaPerJugar(jugadorActiu.getId(), idPartida);
            jugadorDao.jugarCarta(jugadorActiu.getId(), cartaJugable.getId(), TipusRecurs.FUSTA);
            System.out.println("[OK] jugarCarta ha jugat una carta de monopoli");

            int punts = jugadorDao.calcularPunts(jugadorActiu.getId());
            comprovar(punts >= 0, "calcularPunts ha retornat " + punts + " punts");

            int idVertexPoblat = obtenirVertexPoblat(jugadorActiu.getId());
            jugadorDao.millorarACiutat(jugadorActiu.getId(), idVertexPoblat);
            System.out.println("[OK] millorarACiutat ha convertit un poblat en ciutat");

            Jugador exercitMesGran = partidaDao.exercitMesGran(idPartida);
            comprovar(exercitMesGran == null || exercitMesGran.getPartida().getId().equals(idPartida),
                    "exercitMesGran ha consultat la partida correcta");
            partidaDao.comprovarVictoria(idPartida);
            System.out.println("[OK] comprovarVictoria ha revisat la partida");

            // Mostra els recursos finals de tots els jugadors.
            System.out.println("\n=== EXEMPLE 6: consultar els recursos ===");
            for (Jugador jugador : jugadors) {
                partidaDao.mostrarRecursos(jugador.getId());
            }

            // Passa el torn i comprova el nou estat de la partida.
            System.out.println("\n=== EXEMPLE 7: passar el torn ===");
            jugadorDao.passarTorn(jugadorActiu.getId());
            partidaDao.mostrarPartida(idPartida);

            // Prova casos invalids i comprova que les operacions rebutjades no modifiquen dades.
            System.out.println("\n=== EXEMPLE 8: proves de validacio i casos limit ===");
            provarValidacions(jugadorDao, partidaDao, idPartida);

        } catch (Exception e) {
            System.err.println("Error en la prova: " + e.getMessage());
        } finally {
            SessionManager.tancar();
        }
    }

    private static void comprovarIniciPartida(int idPartida, int jugadorsEsperats) {
        // Comprova que la partida, els jugadors i les cartes s'han persistit.
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

    private static void prepararRecursosPerProva(int idJugador) {
        // Donar recursos controlats.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            session.beginTransaction();
            List<RecursJugador> recursos = session.createNamedQuery(
                    "RecursJugador.perJugador", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .getResultList();
            for (RecursJugador recurs : recursos) {
                recurs.setQuantitat(10);
            }
            session.getTransaction().commit();
        }
    }

    private static CartaDesenvolupament prepararCartaPerJugar(int idJugador, int idPartida) {
        // Busca una carta monopoli del jugador o en prepara una de la pila.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            session.beginTransaction();
            CartaDesenvolupament carta = session.createQuery(
                    "FROM CartaDesenvolupament c WHERE c.ma.id = :idJugador "
                        + "AND c.tipusCarta = :tipus", CartaDesenvolupament.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusCarta.MONOPOLI)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (carta == null) {
                carta = session.createQuery(
                        "FROM CartaDesenvolupament c WHERE c.pila.id = :idPartida "
                                + "AND c.tipusCarta = :tipus", CartaDesenvolupament.class)
                        .setParameter("idPartida", idPartida)
                        .setParameter("tipus", TipusCarta.MONOPOLI)
                        .setMaxResults(1)
                        .getSingleResult();
                carta.setPila(null);
                carta.setMa(session.get(Jugador.class, idJugador));
            }

            carta.setRondaCompra(0);
            session.getTransaction().commit();
            return carta;
        }
    }

    private static boolean lladrePendent(int idPartida) {
        // Comprova si la tirada ha obligat a moure el lladre.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT p.lladrePendent FROM Partida p WHERE p.id = :idPartida", Boolean.class)
                    .setParameter("idPartida", idPartida)
                    .getSingleResult();
        }
    }

    private static int obtenirHexagonAlternatiu(int idPartida) {
        // Agafa un hexagon diferent de l'actual per moure el lladre.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT h.id FROM Hexagon h WHERE h.partida.id = :idPartida "
                            + "AND h.id <> (SELECT p.lladre.id FROM Partida p WHERE p.id = :idPartida) "
                            + "ORDER BY h.id", Integer.class)
                    .setParameter("idPartida", idPartida)
                    .setMaxResults(1)
                    .getSingleResult();
        }
    }

    private static int obtenirVertexPoblat(int idJugador) {
        // Obte el vertex d'un poblat propi per provar la millora a ciutat.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT p.vertex.id FROM Poblat p WHERE p.propietari.id = :idJugador ORDER BY p.id",
                    Integer.class)
                    .setParameter("idJugador", idJugador)
                    .setMaxResults(1)
                    .getSingleResult();
        }
    }

    private static List<Jugador> obtenirJugadors(int idPartida) {
        // Carrega els jugadors de la partida en ordre de torn.
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Jugador j WHERE j.partida.id = :idPartida ORDER BY j.ordreTorn", Jugador.class)
                    .setParameter("idPartida", idPartida)
                    .getResultList();
        }
    }

    private static void prepararPartida(JugadorDao jugadorDao, List<Jugador> jugadors) {
        // Coloca un poblat i una carretera per jugador i ronda.
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

    private static int totalRecursos(int idJugador) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "SELECT COALESCE(SUM(r.quantitat), 0) FROM RecursJugador r WHERE r.jugador.id = :id",
                    Long.class)
                    .setParameter("id", idJugador)
                    .getSingleResult();
            return total.intValue();
        }
    }

    private static Jugador obtenirJugadorActiu(int idPartida) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT p.actiu FROM Partida p WHERE p.id = :idPartida", Jugador.class)
                    .setParameter("idPartida", idPartida)
                    .getSingleResult();
        }
    }


        private static void provarValidacions(JugadorDao jugadorDao, PartidaDao partidaDao,
            int idPartida) {
        // Després de passar el torn, el jugador anterior ja no és l'actiu.
        Jugador actiuActual = obtenirJugadorActiu(idPartida);

        esperarExcepcio("jugador inexistent", () -> jugadorDao.mostrarRecursos(-1));

        int recursosAbans = totalRecursos(actiuActual.getId());
        esperarExcepcio("compra sense daus", () -> jugadorDao.comprarCarta(actiuActual.getId()));
        int recursosDespres = totalRecursos(actiuActual.getId());
        comprovar(recursosAbans == recursosDespres,
                "una compra rebutjada no ha consumit recursos");

        // A partir d'aquí ja es poden provar les regles que exigeixen una tirada.
        partidaDao.tirarDaus(idPartida);
        if (lladrePendent(idPartida)) {
            jugadorDao.moureLladre(actiuActual.getId(), obtenirHexagonAlternatiu(idPartida), null);
        }
        

        esperarExcepcio("intercanvi del mateix recurs", () ->
            jugadorDao.comerciarBanca(actiuActual.getId(), TipusRecurs.FUSTA, TipusRecurs.FUSTA));
        esperarExcepcio("arista inexistent",
            () -> jugadorDao.construirCarretera(actiuActual.getId(), -1));
        esperarExcepcio("moure lladre sense moviment pendent",
            () -> jugadorDao.moureLladre(actiuActual.getId(), -1, null));
        esperarExcepcio("tirar els daus dues vegades en el mateix torn",
            () -> partidaDao.tirarDaus(idPartida));

        comprovar(partidaDao.vertexsDisponibles(-1).isEmpty(),
                "vertexsDisponibles retorna buit per a un jugador inexistent");
        Jugador exercit = partidaDao.exercitMesGran(idPartida);
        comprovar(exercit == null || exercit.getPartida().getId().equals(idPartida),
                "exercitMesGran manté la partida correcta després de les proves");
    }

    private static void esperarExcepcio(String prova, Runnable operacio) {
        boolean rebutjada = false;
        try {
            operacio.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            rebutjada = true;
            System.out.println("[OK] " + prova + " rebutjat: " + e.getMessage());
        }
        comprovar(rebutjada, prova + " hauria de ser rebutjat");
    }

    private static int obtenirArestaDelVertex(int idVertex) {
        // Busca una carretera lliure connectada al vertex seleccionat.
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