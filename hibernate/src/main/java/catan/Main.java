package catan;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;

import catan.dao.JugadorDao;
import catan.dao.PartidaDao;
import catan.enums.EstatPartida;
import catan.enums.TipusRecurs;
import catan.models.Hexagon;
import catan.models.Jugador;
import catan.models.Vertex;
import catan.utils.SessionManager;

public class Main {

    private static final PartidaDao partidaDao = new PartidaDao();
    private static final JugadorDao jugadorDao = new JugadorDao();

    private static int idPartida;
    private static int fetes = 0;
    private static int fallades = 0;

    public static void main(String[] args) {
        System.out.println("PROVES DEL JOC DE CATAN");

        try {
            provaInici();
            provaPreparacio();
            provaTorns();
            provaConsultes();
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
            e.printStackTrace();
            fallades++;
        } finally {
            System.out.println();
            System.out.println("Proves: " + fetes + " | correctes: " + (fetes - fallades)
                    + " | fallades: " + fallades);
            sessio().close();
            SessionManager.tancar();
        }
    }

    private static void provaInici() {
        titol("Iniciar partida");

        idPartida = partidaDao.iniciarPartida(Arrays.asList("Badre", "Erik", "Alex"));
        comprova("iniciarPartida torna un id", idPartida > 0);

        comprova("hi ha 19 hexàgons",
                comptar("SELECT COUNT(h) FROM Hexagon h WHERE h.partida.id = " + idPartida) == 19);
        comprova("hi ha 54 vèrtexs",
                comptar("SELECT COUNT(v) FROM Vertex v WHERE v.partida.id = " + idPartida) == 54);
        comprova("hi ha 72 arestes",
                comptar("SELECT COUNT(a) FROM Aresta a WHERE a.partida.id = " + idPartida) == 72);
        comprova("hi ha 114 relacions hexàgon-vèrtex",
                comptar("SELECT COUNT(hv) FROM HexagonVertex hv WHERE hv.hexagon.partida.id = " + idPartida) == 114);

        comprova("la partida comença en PREPARACIO", valor("SELECT p.estat FROM Partida p WHERE p.id = " + idPartida,
                EstatPartida.class) == EstatPartida.PREPARACIO);
        comprova("la partida comença a la ronda 1",
                valor("SELECT p.ronda FROM Partida p WHERE p.id = " + idPartida, Integer.class) == 1);
        comprova("el desert no té número",
                comptar("SELECT COUNT(h) FROM Hexagon h WHERE h.partida.id = " + idPartida
                        + " AND h.terreny = catan.enums.TipusTerreny.DESERT AND h.numero IS NULL") == 1);
        comprova("el lladre comença al desert",
                comptar("SELECT COUNT(p) FROM Partida p WHERE p.id = " + idPartida
                        + " AND p.lladre.terreny = catan.enums.TipusTerreny.DESERT") == 1);

        List<Jugador> jugadors = llista("FROM Jugador j WHERE j.partida.id = " + idPartida
                + " ORDER BY j.ordreTorn", Jugador.class);
        comprova("s'han creat 3 jugadors", jugadors.size() == 3);
        comprova("els torns són 1, 2 i 3",
                jugadors.get(0).getOrdreTorn() == 1
                        && jugadors.get(1).getOrdreTorn() == 2
                        && jugadors.get(2).getOrdreTorn() == 3);
        comprova("cada jugador té els 5 recursos a cero",
                comptar("SELECT COUNT(r) FROM RecursJugador r WHERE r.jugador.partida.id = " + idPartida
                        + " AND r.quantitat <> 0") == 0);

        // Comprovem la topologia que surt del CSV.
        List<Integer> vertexs = llista(
                "SELECT v.codi FROM HexagonVertex hv JOIN hv.vertex v JOIN hv.hexagon h "
                        + "WHERE h.partida.id = " + idPartida + " AND h.codi = 10 ORDER BY hv.posicio",
                Integer.class);
        comprova("l'hexàgon 10 té els vèrtexs del CSV",
                vertexs.equals(Arrays.asList(32, 33, 21, 20, 19, 31)));

        // Si els jugadors no són entre 2 i 4 ha de petar.
        try {
            partidaDao.iniciarPartida(Arrays.asList("jugador1"));
            comprova("1 jugador hauria de fallar", false);
        } catch (IllegalArgumentException e) {
            comprova("1 jugador es rebutja", true);
        }

        try {
            partidaDao.iniciarPartida(Arrays.asList("jugador1", "jugador2", "jugador3", "jugador4", "jugador5"));
            comprova("5 jugadors haurien de fallar", false);
        } catch (IllegalArgumentException e) {
            comprova("5 jugadors es rebutgen", true);
        }

        comprova("les partides invàlides no s'han guardat", comptar("SELECT COUNT(p) FROM Partida p") == 1);
    }

    private static void provaPreparacio() {
        titol("Preparació");

        prepararPartida();

        comprova("la partida passa a EN_CURS", valor("SELECT p.estat FROM Partida p WHERE p.id = " + idPartida,
                EstatPartida.class) == EstatPartida.EN_CURS);
        comprova("hi ha 6 poblats",
                comptar("SELECT COUNT(p) FROM Poblat p WHERE p.propietari.partida.id = " + idPartida) == 6);
        comprova("hi ha 6 carreteres",
                comptar("SELECT COUNT(c) FROM Carretera c WHERE c.propietari.partida.id = " + idPartida) == 6);

        for (Jugador jugador : llista("FROM Jugador j WHERE j.partida.id = " + idPartida, Jugador.class)) {
            comprova(jugador.getNom() + " té 2 punts", jugadorDao.calcularPunts(jugador.getId()) == 2);
        }
    }

    private static void prepararPartida() {
        for (int ronda = 0; ronda < 2; ronda++) {
            for (int i = 0; i < 3; i++) {
                int idJugador = valor("SELECT p.actiu.id FROM Partida p WHERE p.id = " + idPartida, Integer.class);
                Vertex vertex = jugadorDao.vertexsDisponibles(idJugador).get(0);
                int idAresta = llista("SELECT a.id FROM Aresta a "
                        + "WHERE (a.vertexA.id = " + vertex.getId() + " OR a.vertexB.id = " + vertex.getId() + ")"
                        + " AND NOT EXISTS (SELECT c FROM Carretera c WHERE c.aresta = a) "
                        + "ORDER BY a.codi", Integer.class).get(0);

                jugadorDao.construirPoblat(idJugador, vertex.getId());
                jugadorDao.construirCarretera(idJugador, idAresta);
                jugadorDao.passarTorn(idJugador);
            }
        }
    }

    private static void provaTorns() {
        titol("Torns, construccions i recursos");

        int idJugador = valor("SELECT p.actiu.id FROM Partida p WHERE p.id = " + idPartida, Integer.class);
        partidaDao.tirarDaus(idPartida);

        comprova("els daus queden tirats",
                valor("SELECT p.dausTirats FROM Partida p WHERE p.id = " + idPartida, Boolean.class));
        int tirada = valor("SELECT p.ultimaTirada FROM Partida p WHERE p.id = " + idPartida, Integer.class);
        comprova("la tirada està entre 2 i 12", tirada >= 2 && tirada <= 12);

        // Si ha sortit un 7 el lladre s'ha de moure abans de construir.
        if (valor("SELECT p.lladrePendent FROM Partida p WHERE p.id = " + idPartida, Boolean.class)) {
            int hexagon = llista("SELECT h.id FROM Hexagon h WHERE h.partida.id = " + idPartida
                    + " AND h.id <> (SELECT p.lladre.id FROM Partida p WHERE p.id = " + idPartida + ")"
                    + " ORDER BY h.codi", Integer.class).get(0);
            jugadorDao.moureLladre(idJugador, hexagon, null);
            comprova("després de moure'l el lladre ja no està pendent",
                    !valor("SELECT p.lladrePendent FROM Partida p WHERE p.id = " + idPartida, Boolean.class));
        }

        comprova("de moment té 2 punts", jugadorDao.calcularPunts(idJugador) == 2);

        // Li posem recursos a un jugador per poder fer proves de comerç i construcció.
        executa("UPDATE recursos_jugador SET quantitat = 10 WHERE jugador_id = " + idJugador);

        jugadorDao.comerciarBanca(idJugador, TipusRecurs.FUSTA, TipusRecurs.BLAT);
        comprova("la banca canvia 4 fusta per 1 blat",
                quantitatRecurs(idJugador, TipusRecurs.FUSTA) == 6
                        && quantitatRecurs(idJugador, TipusRecurs.BLAT) == 11);

        int vertexPoblat = llista("SELECT p.vertex.id FROM Poblat p WHERE p.propietari.id = "
                + idJugador + " ORDER BY p.id", Integer.class).get(0);
        jugadorDao.millorarACiutat(idJugador, vertexPoblat);
        comprova("en millorar un poblat a ciutat passa a 3 punts",
                jugadorDao.calcularPunts(idJugador) == 3);
        comprova("ara té 1 ciutat",
                comptar("SELECT COUNT(c) FROM Ciutat c WHERE c.propietari.id = " + idJugador) == 1);

        int cartesAbans = (int) comptar("SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.ma.id = " + idJugador);
        int pilaAbans = (int) comptar("SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.pila.id = " + idPartida);
        jugadorDao.comprarCarta(idJugador);
        comprova("ha comprat una carta",
                comptar("SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.ma.id = " + idJugador) == cartesAbans + 1);
        comprova("la pila ha baixat",
                comptar("SELECT COUNT(c) FROM CartaDesenvolupament c WHERE c.pila.id = " + idPartida) == pilaAbans - 1);
    }

    private static void provaConsultes() {
        titol("Consultes de sortida");

        partidaDao.mostrarTauler(idPartida);
        partidaDao.mostrarPartida(idPartida);
        jugadorDao.mostrarRecursos(valor("SELECT p.actiu.id FROM Partida p WHERE p.id = " + idPartida, Integer.class));

        Hexagon lladre = valor("SELECT p.lladre FROM Partida p WHERE p.id = " + idPartida, Hexagon.class);
        comprova("el lladre està col·locat", lladre != null);
        comprova("el lladre té un número o és el desert",
                lladre.getNumero() != null || lladre.getTerreny() == catan.enums.TipusTerreny.DESERT);

        boolean taulerOk = true;
        try {
            partidaDao.mostrarTauler(-1);
        } catch (RuntimeException e) {
            taulerOk = false;
        }
        comprova("un id inexistent no trenca mostrarTauler", taulerOk);

        boolean partidaOk = true;
        try {
            partidaDao.mostrarPartida(-1);
        } catch (RuntimeException e) {
            partidaOk = false;
        }
        comprova("un id inexistent no trenca mostrarPartida", partidaOk);
    }

    private static void titol(String text) {
        System.out.println();
        System.out.println("======== " + text + " ========");
    }

    private static void comprova(String text, boolean correcte) {
        fetes++;
        if (correcte) {
            System.out.println("[OK]    " + text);
        } else {
            fallades++;
            System.out.println("[FALLA] " + text);
        }
    }

    private static Session sessio() {
        return SessionManager.getSessionFactory().getCurrentSession();
    }

    private static long comptar(String hql) {
        Session session = sessio();
        session.beginTransaction();
        long resultat = session.createQuery(hql, Long.class).getSingleResult();
        session.getTransaction().commit();
        return resultat;
    }

    private static <T> T valor(String hql, Class<T> tipus) {
        Session session = sessio();
        session.beginTransaction();
        T resultat = session.createQuery(hql, tipus).getSingleResult();
        session.getTransaction().commit();
        return resultat;
    }

    private static <T> List<T> llista(String hql, Class<T> tipus) {
        Session session = sessio();
        session.beginTransaction();
        List<T> resultat = session.createQuery(hql, tipus).getResultList();
        session.getTransaction().commit();
        return resultat;
    }

    private static int quantitatRecurs(int idJugador, TipusRecurs tipus) {
        return valor("SELECT r.quantitat FROM RecursJugador r WHERE r.jugador.id = " + idJugador
                + " AND r.tipusRecurs = catan.enums.TipusRecurs." + tipus.name(), Integer.class);
    }

    private static void executa(String sql) {
        Session session = sessio();
        session.beginTransaction();
        session.createNativeMutationQuery(sql).executeUpdate();
        session.getTransaction().commit();
    }
}
