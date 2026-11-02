package catan.dao;

import java.util.List;
import java.util.Random;

import org.hibernate.Session;

import catan.Aresta;
import catan.Carretera;
import catan.CartaDesenvolupament;
import catan.Ciutat;
import catan.Hexagon;
import catan.Jugador;
import catan.Partida;
import catan.Poblat;
import catan.RecursJugador;
import catan.Vertex;
import catan.enums.EstatPartida;
import catan.enums.TipusCarta;
import catan.enums.TipusRecurs;
import catan.enums.TipusTerreny;

public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugadorDao {

    @Override
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
            throw e;
        }
    }

    @Override
    public void comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            validarTornEnCurs(partida, idJugador, true, true, "comerciar");

            if (dona == null || rep == null) {
                throw new IllegalArgumentException("Cal indicar el recurs que dónes i el que reps");
            }
            if (dona == rep) {
                throw new IllegalArgumentException("Els recursos dona i rep han de ser diferents");
            }

            RecursJugador recursDona = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", dona)
                    .getSingleResult();
            if (recursDona.getQuantitat() < 4) {
                throw new IllegalStateException(
                        "No tens 4 de " + dona + " (en tens " + recursDona.getQuantitat() + ")");
            }

            RecursJugador recursRep = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", rep)
                    .getSingleResult();

            recursDona.setQuantitat(recursDona.getQuantitat() - 4);
            recursRep.setQuantitat(recursRep.getQuantitat() + 1);

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha canviat 4 " + dona + " per 1 " + rep + " amb la banca");
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    @Override
    public void comprarCarta(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        Integer idPartida = null;
        boolean puntVictoriaComprat = false;
        try {
            session.beginTransaction();
            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            idPartida = partida.getId();
            validarTornEnCurs(partida, idJugador, true, true, "comprar una carta");

            RecursJugador recursDonaBlat = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusRecurs.BLAT)
                    .getSingleResult();
            RecursJugador recursDonaLlana = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusRecurs.LLANA)
                    .getSingleResult();
            RecursJugador recursDonaMineral = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusRecurs.MINERAL)
                    .getSingleResult();
            if (recursDonaBlat.getQuantitat() < 1) {
                throw new IllegalStateException(
                        "No tens 1 de " + TipusRecurs.BLAT + " (en tens " + recursDonaBlat.getQuantitat() + ")");
            }
            if (recursDonaLlana.getQuantitat() < 1) {
                throw new IllegalStateException(
                        "No tens 1 de " + TipusRecurs.LLANA + " (en tens " + recursDonaLlana.getQuantitat() + ")");
            }
            if (recursDonaMineral.getQuantitat() < 1) {
                throw new IllegalStateException(
                        "No tens 1 de " + TipusRecurs.MINERAL + " (en tens " + recursDonaMineral.getQuantitat() + ")");
            }

            recursDonaBlat.setQuantitat(recursDonaBlat.getQuantitat() - 1);
            recursDonaLlana.setQuantitat(recursDonaLlana.getQuantitat() - 1);
            recursDonaMineral.setQuantitat(recursDonaMineral.getQuantitat() - 1);

            List<CartaDesenvolupament> pilaLliure = session
                    .createQuery("FROM CartaDesenvolupament c WHERE c.pila.id = :idPartida AND c.ma IS NULL",
                            CartaDesenvolupament.class)
                    .setParameter("idPartida", partida.getId())
                    .getResultList();
            if (pilaLliure.isEmpty()) {
                throw new IllegalStateException("La pila de cartes de desenvolupament és buida");
            }

            CartaDesenvolupament carta = pilaLliure.get(new Random().nextInt(pilaLliure.size()));
            partida.getPila().remove(carta);
            carta.setPila(null);
            carta.setMa(jugador);
            carta.setRondaCompra(partida.getRonda());
            jugador.getCartesDesenvolupament().add(carta);
            puntVictoriaComprat = carta.getTipusCarta() == TipusCarta.PUNT_VICTORIA;

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha comprat una carta de desenvolupament");

        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }

        if (puntVictoriaComprat) {
            new PartidaDao().comprovarVictoria(idPartida);
        }
    }

    @Override
    public void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
        Session session = sessionFactory.getCurrentSession();
        Integer idPartida = null;
        boolean cavallerJugat = false;
        try {
            session.beginTransaction();

            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            idPartida = partida.getId();
            validarTornEnCurs(partida, idJugador, true, false, "jugar una carta");
            CartaDesenvolupament carta = session.get(CartaDesenvolupament.class, idCarta);
            if (carta == null) {
                throw new IllegalArgumentException("Carta no encontrada");
            }
            if (!jugador.getCartesDesenvolupament().contains(carta)) {
                throw new IllegalStateException("No tens aquesta carta");
            }
            if (carta.isUsada()) {
                throw new IllegalStateException("Aquesta carta ja ha estat usada");
            }
            if (carta.getRondaCompra() == null || carta.getRondaCompra() >= partida.getRonda()) {
                throw new IllegalStateException("No pots jugar una carta en la mateixa ronda que l'has comprada");
            }
            if (carta.getTipusCarta() == TipusCarta.PUNT_VICTORIA) {
                throw new IllegalStateException("Les cartes de punt de victòria no es juguen");
            }
            switch (carta.getTipusCarta()) {
                case MONOPOLI:
                    if (recurs == null) {
                        throw new IllegalArgumentException("Cal indicar el recurs a monopolitzar");
                    }
                    RecursJugador recursJugadorActiu = session
                            .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                            .setParameter("idJugador", idJugador)
                            .setParameter("tipus", recurs)
                            .getSingleResult();
                    List<Jugador> TotsJugadors = partida.getJugadors();
                    for (Jugador j : TotsJugadors) {
                        if (!j.getId().equals(idJugador)) {
                            RecursJugador recursJugador = session
                                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                                    .setParameter("idJugador", j.getId())
                                    .setParameter("tipus", recurs)
                                    .getSingleResult();
                            int quantitat = recursJugador.getQuantitat();
                            recursJugador.setQuantitat(0);
                            recursJugadorActiu.setQuantitat(recursJugadorActiu.getQuantitat() + quantitat);
                        }
                    }
                    break;
                case PUNT_VICTORIA:

                    throw new IllegalStateException("Les cartes de punt de victòria no es juguen");
                case CAVALLER:
                    int cantitatCavallers = jugador.getCavallersJugats() + 1;
                    jugador.setCavallersJugats(cantitatCavallers);
                    partida.setLladrePendent(true);
                    cavallerJugat = true;
                    break;
                default:
                    throw new IllegalStateException("Tipus de carta desconegut: " + carta.getTipusCarta());
            }

            carta.setUsada(true);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }

        if (cavallerJugat) {
            new PartidaDao().comprovarVictoria(idPartida);
        }
    }

    @Override
    public void construirCarretera(int idJugador, int idAresta) {
        Session session = sessionFactory.getCurrentSession();

        try {
            session.beginTransaction();

            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            validarTorn(partida, idJugador, true, false, false, "construir");
            boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
            if (!preparacio) {
                if (!partida.getDausTirats()) {
                    throw new IllegalStateException("Has de tirar els daus abans de construir");
                }
                if (partida.getLladrePendent()) {
                    throw new IllegalStateException("Has de moure el lladre abans de construir");
                }
            }

            Aresta aresta = session.get(Aresta.class, idAresta);
            if (aresta == null) {
                throw new IllegalArgumentException("No existeix l'aresta: " + idAresta);
            }
            if (!aresta.getPartida().getId().equals(partida.getId())) {
                throw new IllegalStateException("L'aresta pertany a una altra partida");
            }
            long ocupada = session
                    .createQuery("SELECT COUNT(c) FROM Carretera c WHERE c.aresta.id = :idAresta", Long.class)
                    .setParameter("idAresta", idAresta)
                    .uniqueResult();
            if (ocupada > 0) {
                throw new IllegalStateException("L'aresta ja està ocupada");
            }

            long carreteres = totalCarreteres(idJugador, session);
            if (carreteres >= 15) {
                throw new IllegalStateException("Has assolit el límit de 15 carreteres");
            }

            int vertexA = aresta.getVertexA().getId();
            int vertexB = aresta.getVertexB().getId();

            if (preparacio) {
                long poblats = totalPoblats(idJugador, session);
                if (poblats != partida.getRonda()) {
                    throw new IllegalStateException("Primer has de col·locar el poblat d'aquest torn");
                }
                if (carreteres != partida.getRonda() - 1) {
                    throw new IllegalStateException("Ja has col·locat la carretera d'aquest torn");
                }

                long poblatsLliures = session.createQuery(
                        "SELECT COUNT(p) FROM Poblat p "
                                + "WHERE p.propietari.id = :id "
                                + "AND (p.vertex.id = :vertexA OR p.vertex.id = :vertexB) "
                                + "AND NOT EXISTS (SELECT 1 FROM Carretera c "
                                + "    WHERE c.propietari.id = :id "
                                + "    AND (c.aresta.vertexA = p.vertex OR c.aresta.vertexB = p.vertex))",
                        Long.class)
                        .setParameter("id", idJugador)
                        .setParameter("vertexA", vertexA)
                        .setParameter("vertexB", vertexB)
                        .uniqueResult();
                if (poblatsLliures == 0) {
                    throw new IllegalStateException("La carretera ha de tocar el poblat que acabes de col·locar");
                }
            } else {
                long assentaments = session.createQuery(
                        "SELECT COUNT(a) FROM Assentament a "
                                + "WHERE a.propietari.id = :id AND (a.vertex.id = :vertexA OR a.vertex.id = :vertexB)",
                        Long.class)
                        .setParameter("id", idJugador)
                        .setParameter("vertexA", vertexA)
                        .setParameter("vertexB", vertexB)
                        .uniqueResult();
                long carreteresVeines = session.createQuery(
                        "SELECT COUNT(c) FROM Carretera c "
                                + "WHERE c.propietari.id = :id "
                                + "AND (c.aresta.vertexA.id = :vertexA OR c.aresta.vertexA.id = :vertexB "
                                + "  OR c.aresta.vertexB.id = :vertexA OR c.aresta.vertexB.id = :vertexB)",
                        Long.class)
                        .setParameter("id", idJugador)
                        .setParameter("vertexA", vertexA)
                        .setParameter("vertexB", vertexB)
                        .uniqueResult();
                if (assentaments == 0 && carreteresVeines == 0) {
                    throw new IllegalStateException("L'aresta no està connectada a cap construcció teva");
                }

                RecursJugador fusta = session
                        .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                        .setParameter("idJugador", idJugador)
                        .setParameter("tipus", TipusRecurs.FUSTA)
                        .getSingleResult();
                RecursJugador mao = session
                        .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                        .setParameter("idJugador", idJugador)
                        .setParameter("tipus", TipusRecurs.MAO)
                        .getSingleResult();
                if (fusta.getQuantitat() < 1) {
                    throw new IllegalStateException("No tens 1 de FUSTA (en tens " + fusta.getQuantitat() + ")");
                }
                if (mao.getQuantitat() < 1) {
                    throw new IllegalStateException("No tens 1 de MAO (en tens " + mao.getQuantitat() + ")");
                }
                fusta.setQuantitat(fusta.getQuantitat() - 1);
                mao.setQuantitat(mao.getQuantitat() - 1);
            }

            session.persist(new Carretera(jugador, aresta));

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha construït una carretera a l'aresta " + aresta.getCodi());
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    @Override
    public void construirPoblat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        int idPartida;
        try {
            session.beginTransaction();

            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            idPartida = partida.getId();

            validarTorn(partida, idJugador, true, false, false, "construir");
            boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
            if (!preparacio) {
                if (!partida.getDausTirats()) {
                    throw new IllegalStateException("Has de tirar els daus abans de construir");
                }
                if (partida.getLladrePendent()) {
                    throw new IllegalStateException("Has de moure el lladre abans de construir");
                }
            }

            Vertex vertex = session.get(Vertex.class, idVertex);
            if (vertex == null) {
                throw new IllegalArgumentException("No existeix el vèrtex: " + idVertex);
            }
            if (!vertex.getPartida().getId().equals(partida.getId())) {
                throw new IllegalStateException("El vèrtex pertany a una altra partida");
            }
            long ocupat = session
                    .createQuery("SELECT COUNT(a) FROM Assentament a WHERE a.vertex.id = :idVertex", Long.class)
                    .setParameter("idVertex", idVertex)
                    .uniqueResult();
            if (ocupat > 0) {
                throw new IllegalStateException("El vèrtex ja està ocupat");
            }

            long veinsOcupats = session.createQuery(
                    "SELECT COUNT(a) FROM Assentament a, Aresta ar "
                            + "WHERE (ar.vertexA.id = :idVertex AND a.vertex = ar.vertexB) "
                            + "   OR (ar.vertexB.id = :idVertex AND a.vertex = ar.vertexA)",
                    Long.class)
                    .setParameter("idVertex", idVertex)
                    .uniqueResult();
            if (veinsOcupats > 0) {
                throw new IllegalStateException("Regla de la distància: hi ha una construcció en un vèrtex adjacent");
            }

            long poblats = totalPoblats(idJugador, session);
            if (poblats >= 5) {
                throw new IllegalStateException("Has assolit el límit de 5 poblats");
            }

            if (preparacio) {
                long carreteres = totalCarreteres(idJugador, session);
                if (poblats != partida.getRonda() - 1) {
                    throw new IllegalStateException("Ja has col·locat el poblat d'aquest torn");
                }
                if (carreteres != partida.getRonda() - 1) {
                    throw new IllegalStateException("Primer has de col·locar la carretera del torn anterior");
                }
            } else {
                long carreteresPropies = session.createQuery(
                        "SELECT COUNT(c) FROM Carretera c "
                                + "WHERE c.propietari.id = :id "
                                + "AND (c.aresta.vertexA.id = :idVertex OR c.aresta.vertexB.id = :idVertex)",
                        Long.class)
                        .setParameter("id", idJugador)
                        .setParameter("idVertex", idVertex)
                        .uniqueResult();
                if (carreteresPropies == 0) {
                    throw new IllegalStateException("El vèrtex no està connectat a cap carretera teva");
                }

                TipusRecurs[] cost = { TipusRecurs.FUSTA, TipusRecurs.MAO, TipusRecurs.LLANA, TipusRecurs.BLAT };
                RecursJugador[] inventari = new RecursJugador[cost.length];
                for (int i = 0; i < cost.length; i++) {
                    inventari[i] = session
                            .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                            .setParameter("idJugador", idJugador)
                            .setParameter("tipus", cost[i])
                            .getSingleResult();
                    if (inventari[i].getQuantitat() < 1) {
                        throw new IllegalStateException("No tens 1 de " + cost[i]
                                + " (en tens " + inventari[i].getQuantitat() + ")");
                    }
                }
                for (RecursJugador rj : inventari) {
                    rj.setQuantitat(rj.getQuantitat() - 1);
                }
            }

            session.persist(new Poblat(jugador, vertex));

            if (preparacio && poblats + 1 == 2) {
                List<Hexagon> hexagons = session
                        .createQuery("SELECT hv.hexagon FROM HexagonVertex hv WHERE hv.vertex.id = :idVertex",
                                Hexagon.class)
                        .setParameter("idVertex", idVertex)
                        .getResultList();
                for (Hexagon hexagon : hexagons) {
                    TipusRecurs recurs = recursDelTerreny(hexagon.getTerreny());
                    if (recurs == null) {
                        continue;
                    }
                    RecursJugador rj = session
                            .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                            .setParameter("idJugador", idJugador)
                            .setParameter("tipus", recurs)
                            .getSingleResult();
                    rj.setQuantitat(rj.getQuantitat() + 1);
                }
            }

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha construït un poblat al vèrtex " + vertex.getCodi());
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }

        new PartidaDao().comprovarVictoria(idPartida);
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

    @Override
    public void millorarACiutat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        int idPartida;
        try {
            session.beginTransaction();

            Jugador jugador = obtenirJugador(session, idJugador);
            Partida partida = jugador.getPartida();
            idPartida = partida.getId();
            validarTornEnCurs(partida, idJugador, true, true, "construir");

            Vertex vertex = session.get(Vertex.class, idVertex);
            if (vertex == null) {
                throw new IllegalArgumentException("No existeix el vèrtex: " + idVertex);
            }
            if (!vertex.getPartida().getId().equals(partida.getId())) {
                throw new IllegalStateException("El vèrtex pertany a una altra partida");
            }

            List<Poblat> poblats = session
                    .createQuery("FROM Poblat p WHERE p.vertex.id = :idVertex AND p.propietari.id = :id", Poblat.class)
                    .setParameter("idVertex", idVertex)
                    .setParameter("id", idJugador)
                    .getResultList();
            if (poblats.isEmpty()) {
                throw new IllegalStateException("No hi ha cap poblat teu en aquest vèrtex per millorar");
            }
            Poblat poblat = poblats.get(0);

            long ciutats = session
                    .createQuery("SELECT COUNT(c) FROM Ciutat c WHERE c.propietari.id = :id", Long.class)
                    .setParameter("id", idJugador)
                    .uniqueResult();
            if (ciutats >= 4) {
                throw new IllegalStateException("Has assolit el límit de 4 ciutats");
            }

            RecursJugador blat = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusRecurs.BLAT)
                    .getSingleResult();
            RecursJugador mineral = session
                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusRecurs.MINERAL)
                    .getSingleResult();
            if (blat.getQuantitat() < 2) {
                throw new IllegalStateException("No tens 2 de BLAT (en tens " + blat.getQuantitat() + ")");
            }
            if (mineral.getQuantitat() < 3) {
                throw new IllegalStateException("No tens 3 de MINERAL (en tens " + mineral.getQuantitat() + ")");
            }
            blat.setQuantitat(blat.getQuantitat() - 2);
            mineral.setQuantitat(mineral.getQuantitat() - 3);

            session.remove(poblat);
            session.flush();
            session.persist(new Ciutat(jugador, vertex));

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha millorat a ciutat el vèrtex " + vertex.getCodi());
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }

        new PartidaDao().comprovarVictoria(idPartida);
    }

    @Override
    public int calcularPunts(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        boolean transaccioIniciada = !session.getTransaction().isActive();

        try {
            if (transaccioIniciada) {
                session.beginTransaction();
            }

            Jugador jugador = session.get(Jugador.class, idJugador);

            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }

            Long numPoblats = session.createQuery(
                    "SELECT COUNT(p) " +
                            "FROM Poblat p " +
                            "WHERE p.propietari.id = :idJugador",
                    Long.class)
                    .setParameter("idJugador", idJugador)
                    .getSingleResult();

            Long numCiutats = session.createQuery(
                    "SELECT COUNT(c) " +
                            "FROM Ciutat c " +
                            "WHERE c.propietari.id = :idJugador",
                    Long.class)
                    .setParameter("idJugador", idJugador)
                    .getSingleResult();

            Long numCartesPuntVictoria = session.createQuery(
                    "SELECT COUNT(c) " +
                            "FROM CartaDesenvolupament c " +
                            "WHERE c.ma.id = :idJugador " +
                            "AND c.tipusCarta = :tipus",
                    Long.class)
                    .setParameter("idJugador", idJugador)
                    .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                    .getSingleResult();

            int punts = numPoblats.intValue() + (numCiutats.intValue() * 2) + numCartesPuntVictoria.intValue();

            Jugador exercitMesGran = new PartidaDao().exercitMesGran(jugador.getPartida().getId());

            if (exercitMesGran != null && exercitMesGran.getId().equals(idJugador)) {
                punts += 2;
            }

            if (transaccioIniciada) {
                session.getTransaction().commit();
            }

            return punts;

        } catch (RuntimeException e) {
            if (transaccioIniciada) {
                handleException(session, e);
            }
            throw e;
        }
    }

    private long totalPoblats(int idJugador, Session session) {
        return session
                .createQuery("SELECT COUNT(p) FROM Poblat p WHERE p.propietari.id = :id", Long.class)
                .setParameter("id", idJugador).uniqueResult();
    }

    private Jugador obtenirJugador(Session session, int idJugador) {
        Jugador jugador = session.get(Jugador.class, idJugador);
        if (jugador == null) {
            throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
        }
        return jugador;
    }

    private void validarTornEnCurs(Partida partida, int idJugador, boolean requereixDaus,
            boolean requereixLladreMogut, String accio) {
        validarTorn(partida, idJugador, false, requereixDaus, requereixLladreMogut, accio);
    }

    private void validarTorn(Partida partida, int idJugador, boolean permetPreparacio, boolean requereixDaus,
            boolean requereixLladreMogut, String accio) {
        if (partida.getEstat() == EstatPartida.FINALITZADA) {
            throw new IllegalStateException("La partida ja ha finalitzat");
        }
        if ((!permetPreparacio && partida.getEstat() != EstatPartida.EN_CURS)
                || (permetPreparacio && partida.getEstat() != EstatPartida.EN_CURS
                        && partida.getEstat() != EstatPartida.PREPARACIO)) {
            throw new IllegalStateException("La partida no està en curs");
        }
        if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
            throw new IllegalStateException("No és el teu torn");
        }
        if (requereixDaus && !partida.getDausTirats()) {
            throw new IllegalStateException("Has de tirar els daus abans de " + accio);
        }
        if (requereixLladreMogut && partida.getLladrePendent()) {
            throw new IllegalStateException("Has de moure el lladre abans de " + accio);
        }
    }

    private long totalCarreteres(int idJugador, Session session) {
        return session
                .createQuery("SELECT COUNT(c) FROM Carretera c WHERE c.propietari.id = :id", Long.class)
                .setParameter("id", idJugador).uniqueResult();
    }

    @Override
    public void passarTorn(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }
            Partida partida = jugador.getPartida();

            if (partida.getEstat() == EstatPartida.FINALITZADA) {
                throw new IllegalStateException("La partida ja ha finalitzat");
            }
            if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
                throw new IllegalStateException("No és el teu torn");
            }
            if (partida.getLladrePendent()) {
                throw new IllegalStateException("Has de moure el lladre abans de passar el torn");
            }

            boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;

            if (!preparacio && partida.getEstat() != EstatPartida.EN_CURS) {
                throw new IllegalStateException("La partida no està en curs");
            }

            if (!preparacio && !partida.getDausTirats()) {
                throw new IllegalStateException("Has de tirar els daus abans de passar el torn");
            }

            int ordreActual = jugador.getOrdreTorn();
            int nombreJugadors = partida.getJugadors().size();
            int ordreSeguent;

            if (preparacio) {
                long poblats = totalPoblats(idJugador, session);
                long carreteres = totalCarreteres(idJugador, session);

                if (poblats != partida.getRonda() || carreteres != partida.getRonda()) {
                    throw new IllegalStateException(
                            "Has de col·locar un poblat i una carretera abans de passar el torn");
                }

                if (partida.getRonda() == 1) {
                    if (ordreActual < nombreJugadors) {
                        ordreSeguent = ordreActual + 1;
                    } else {
                        partida.setRonda(2);
                        ordreSeguent = ordreActual;
                    }
                } else {
                    if (ordreActual > 1) {
                        ordreSeguent = ordreActual - 1;
                    } else {
                        long totalPoblats = session
                                .createQuery(
                                        "SELECT COUNT(p) FROM Poblat p WHERE p.propietari.partida.id = :idPartida",
                                        Long.class)
                                .setParameter("idPartida", partida.getId())
                                .getSingleResult();
                        long totalCarreteres = session
                                .createQuery(
                                        "SELECT COUNT(c) FROM Carretera c WHERE c.propietari.partida.id = :idPartida",
                                        Long.class)
                                .setParameter("idPartida", partida.getId())
                                .getSingleResult();

                        if (totalPoblats != nombreJugadors * 2L || totalCarreteres != nombreJugadors * 2L) {
                            throw new IllegalStateException(
                                    "Tots els jugadors han de tenir 2 poblats i 2 carreteres");
                        }

                        partida.setEstat(EstatPartida.EN_CURS);
                        partida.setRonda(1);
                        ordreSeguent = 1;
                    }
                }
            } else {
                ordreSeguent = ordreActual == nombreJugadors ? 1 : ordreActual + 1;
                if (ordreSeguent == 1) {
                    partida.setRonda(partida.getRonda() + 1);
                }
            }

            Jugador jugadorSeguent = session.createQuery(
                    "FROM Jugador j WHERE j.partida.id = :idPartida AND j.ordreTorn = :ordreTorn",
                    Jugador.class)
                    .setParameter("idPartida", partida.getId())
                    .setParameter("ordreTorn", ordreSeguent)
                    .getSingleResult();
            partida.setActiu(jugadorSeguent);
            partida.setDausTirats(false);

            session.getTransaction().commit();
            System.out.println("Torn passat. Ara és el torn de " + jugadorSeguent.getNom());

        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    @Override
    public void moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }
            Partida partida = jugador.getPartida();

            if (partida.getEstat() == EstatPartida.FINALITZADA) {
                throw new IllegalStateException("La partida ja ha finalitzat");
            }
            if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
                throw new IllegalStateException("No és el teu torn");
            }
            if (!partida.getLladrePendent()) {
                throw new IllegalStateException("No hi ha cap moviment de lladre pendent");
            }

            Hexagon hexagon = session.get(Hexagon.class, idHexagon);
            if (hexagon == null) {
                throw new IllegalArgumentException("No existeix l'hexàgon: " + idHexagon);
            }
            if (!hexagon.getPartida().getId().equals(partida.getId())) {
                throw new IllegalStateException("L'hexàgon pertany a una altra partida");
            }
            if (partida.getLladre() != null && partida.getLladre().getId().equals(idHexagon)) {
                throw new IllegalStateException("El lladre ja és en aquest hexàgon");
            }

            Jugador victima = null;
            if (idJugadorVictima != null) {
                if (idJugadorVictima == idJugador) {
                    throw new IllegalArgumentException("No et pots robar a tu mateix");
                }
                victima = session.get(Jugador.class, idJugadorVictima);
                if (victima == null) {
                    throw new IllegalArgumentException("No existeix el jugador víctima: " + idJugadorVictima);
                }
                if (!victima.getPartida().getId().equals(partida.getId())) {
                    throw new IllegalStateException("El jugador víctima pertany a una altra partida");
                }

                Long assentaments = session.createQuery(
                        "SELECT COUNT(a) FROM Assentament a, HexagonVertex hv "
                                + "WHERE hv.hexagon.id = :idHexagon AND a.vertex = hv.vertex "
                                + "AND a.propietari.id = :idVictima",
                        Long.class)
                        .setParameter("idHexagon", idHexagon)
                        .setParameter("idVictima", idJugadorVictima)
                        .getSingleResult();
                if (assentaments == 0) {
                    throw new IllegalStateException("La víctima no té cap assentament en aquest hexàgon");
                }
            }

            partida.setLladre(hexagon);

            if (victima != null) {
                List<RecursJugador> recursosVictima = session
                        .createNamedQuery("RecursJugador.perJugador", RecursJugador.class)
                        .setParameter("idJugador", idJugadorVictima)
                        .getResultList();

                // carta aleatoria de la víctima
                int total = 0;
                for (RecursJugador rj : recursosVictima) {
                    total += rj.getQuantitat();
                }
                if (total > 0) {
                    int sorteig = new Random().nextInt(total);
                    RecursJugador robat = null;
                    for (RecursJugador recursJugador : recursosVictima) {
                        if (sorteig < recursJugador.getQuantitat()) {
                            robat = recursJugador;
                            break;
                        }
                        sorteig -= recursJugador.getQuantitat();
                    }

                    RecursJugador delLladre = session
                            .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                            .setParameter("idJugador", idJugador)
                            .setParameter("tipus", robat.getTipusRecurs())
                            .getSingleResult();
                    robat.setQuantitat(robat.getQuantitat() - 1);
                    delLladre.setQuantitat(delLladre.getQuantitat() + 1);
                    System.out.println(jugador.getNom() + " ha robat 1 de " + robat.getTipusRecurs()
                            + " a " + victima.getNom());
                } else {
                    System.out.println(victima.getNom() + " no té recursos per robar");
                }
            }

            partida.setLladrePendent(false);
            session.getTransaction().commit();
            System.out.println("Lladre mogut a l'hexàgon " + hexagon.getCodi());
        } catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }
}