package catan.dao;

import java.util.List;

import org.hibernate.Session;

import catan.Jugador;
import catan.Partida;
import catan.RecursJugador;
import catan.enums.EstatPartida;
import catan.enums.TipusRecurs;

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

            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }
            Partida partida = jugador.getPartida();

            if (partida.getEstat() == EstatPartida.FINALITZADA) {
                throw new IllegalStateException("La partida ja ha finalitzat");
            }
            if (partida.getEstat() != EstatPartida.EN_CURS) {
                throw new IllegalStateException("La partida no està en curs");
            }
            if (partida.getActiu() == null || !partida.getActiu().getId().equals(idJugador)) {
                throw new IllegalStateException("No és el teu torn");
            }
            if (!partida.getDausTirats()) {
                throw new IllegalStateException("Has de tirar els daus abans de comerciar");
            }
            if (partida.getLladrePendent()) {
                throw new IllegalStateException("Has de moure el lladre abans de comerciar");
            }

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
}