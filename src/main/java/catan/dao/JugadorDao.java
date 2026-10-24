package catan.dao;

import java.util.List;

import org.hibernate.Session;

import catan.Jugador;
import catan.RecursJugador;
import catan.SessionManager;

public class JugadorDao implements IJugadorDao {
    public void mostrarRecursos(int idJugador) {
        try (Session session = SessionManager.getSessionFactory().openSession()) {
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("No existeix el jugador: " + idJugador);
            }

            List<RecursJugador> recursos = session
                    .createNamedQuery("RecursJugador.perJugador", RecursJugador.class)
                    .setParameter("idJugador", idJugador)
                    .getResultList();

            System.out.println("Recursos de " + jugador.getNom()
                    + " (" + jugador.getColor() + ")");
            for (RecursJugador r : recursos) {
                System.out.println("  " + r.getTipusRecurs() + ": " + r.getQuantitat());
            }
        }
    }
}
