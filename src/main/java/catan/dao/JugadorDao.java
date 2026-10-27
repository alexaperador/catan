package catan.dao;

import java.util.List;

import org.hibernate.Session;

import catan.Carretera;
import catan.CartaDesenvolupament;
import catan.Ciutat;
import catan.Jugador;
import catan.Partida;
import catan.Poblat;
import catan.RecursJugador;
import catan.enums.EstatPartida;
import catan.enums.TipusCarta;
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

    @Override
    public void comprarCarta(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try{
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

            CartaDesenvolupament carta = partida.getPila().remove(0);
            jugador.getCartesDesenvolupament().add(carta);

            session.getTransaction().commit();
            System.out.println(jugador.getNom() + " ha comprat una carta de desenvolupament");

        }catch(RuntimeException e){
            handleException(session, e);
            throw e;
        }
    }

    @Override
    public void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
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
            CartaDesenvolupament carta = session.get(CartaDesenvolupament.class, idCarta);
            if (carta == null) {
                throw new IllegalArgumentException("carta no encontrada");
            }
            if (!jugador.getCartesDesenvolupament().contains(carta)) {
                throw new IllegalStateException("No tens aquesta carta");
            }
            if (carta.isUsada()) {
                throw new IllegalStateException("Aquesta carta ja ha estat usada");
            }
            switch (carta.getTipusCarta()) {
                case MONOPOLI:
                    if (recurs == null) {
                        throw new IllegalArgumentException("Cal indicar el recurs a monopolitzar");
                    }
                    List<Jugador> TotsJugadors = partida.getJugadors();
                    for (Jugador j : TotsJugadors) {
                        if (!j.getId().equals(idJugador)) {
                            RecursJugador recursJugador = session
                                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                                    .setParameter("idJugador", j.getId())
                                    .setParameter("tipus", recurs)
                                    .getSingleResult();
                            int quantitat = recursJugador.getQuantitat() - 1;
                            recursJugador.setQuantitat(quantitat < 0 ? 0 : quantitat);

                            RecursJugador recursJugadorActiu = session
                                    .createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class)
                                    .setParameter("idJugador", idJugador)
                                    .setParameter("tipus", recurs)
                                    .getSingleResult();
                            recursJugadorActiu.setQuantitat(recursJugadorActiu.getQuantitat() + 1);
                        }
                    }
                    break;
                case PUNT_VICTORIA:
                    calcularPunts(idJugador);
                    break;
                case CAVALLER:
                    int cantitatCavallers = jugador.getCavallersJugats() + 1;
                    jugador.setCavallersJugats(cantitatCavallers);
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
    }

    @Override
    public void construirCarretera(int idJugador, int idAresta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'construirCarretera'");
    }

    @Override
    public void construirPoblat(int idJugador, int idVertex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'construirPoblat'");
    }

    @Override
    public void millorarACiutat(int idJugador, int idVertex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'millorarACiutat'");
    }

    @Override
    public int calcularPunts(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            int puntsJugador = 0;
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
            for(int i = 0; i < jugador.getConstruccions().size(); i++){
                List<Poblat> poblats = session.createNamedQuery("Poblat.perJugador", Poblat.class)
                        .setParameter("idJugador", idJugador)
                        .getResultList();
                puntsJugador += poblats.size();
                List<Ciutat> ciutats = session.createNamedQuery("Ciutat.perJugador", Ciutat.class)
                        .setParameter("idJugador", idJugador)
                        .getResultList();
                puntsJugador += ciutats.size() * 2;
                List<CartaDesenvolupament> cartes = session.createNamedQuery("CartaDesenvolupament.perJugador", CartaDesenvolupament.class)
                        .setParameter("idJugador", idJugador)
                        .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                        .getResultList();
                puntsJugador += cartes.size();
            }
            List<Jugador> TotsJugadors = partida.getJugadors();
            for (Jugador j : TotsJugadors) {
                boolean exercitMesGran = false;
                if (!j.getId().equals(idJugador)) {
                    if (j.getCavallersJugats() > jugador.getCavallersJugats() && !exercitMesGran) {
                        puntsJugador += 2;
                        exercitMesGran = true;
                    }
                    else if (j.getCavallersJugats() < jugador.getCavallersJugats()) {
                        exercitMesGran = false;
                    }
                    else if (j.getCavallersJugats() < jugador.getCavallersJugats() && !exercitMesGran) {
                        puntsJugador -= 2;
                        exercitMesGran = false;
                    }
                    else if (j.getCavallersJugats() == jugador.getCavallersJugats() && !exercitMesGran) {
                        exercitMesGran = false;
                    }
                    else if (j.getCavallersJugats() == jugador.getCavallersJugats() && exercitMesGran) {
                        puntsJugador -= 2;
                        exercitMesGran = false;
                    }
                    List<Carretera> carreteres = session.createNamedQuery("Carretera.perJugador", Carretera.class)
                            .setParameter("idJugador", idJugador)
                            .getResultList();
                    List<Carretera> carreteresRivals = session.createNamedQuery("Carretera.perJugador", Carretera.class)
                            .setParameter("idJugador", j.getId())
                            .getResultList();
                    boolean carreteraMesGran = false;
                    if(carreteres.size() > carreteresRivals.size() && !carreteraMesGran){
                        puntsJugador += 2;
                        carreteraMesGran = true;
                    }
                    else if(carreteres.size() < carreteresRivals.size()){
                        carreteraMesGran = false;
                    }
                    else if(carreteres.size() < carreteresRivals.size() && !carreteraMesGran){
                        puntsJugador -= 2;
                        carreteraMesGran = false;
                    }
                    else if(carreteres.size() == carreteresRivals.size() && !carreteraMesGran){
                        carreteraMesGran = false;
                    }
                    else if(carreteres.size() == carreteresRivals.size() && carreteraMesGran){
                        puntsJugador -= 2;
                        carreteraMesGran = false;
                    }
                }
            }

            return puntsJugador;

        }catch (RuntimeException e) {
            handleException(session, e);
            throw e;
        }
    }

    @Override
    public void passarTorn(int idJugador) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'passarTorn'");
    }

    @Override
    public void moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moureLladre'");
    }
}