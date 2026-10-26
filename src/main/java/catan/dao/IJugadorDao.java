package catan.dao;

import catan.enums.TipusRecurs;

public interface IJugadorDao {
    void mostrarRecursos(int idJugador);

    void comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);
    
    void comprarCarta(int idJugador);

    void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);

    void construirCarretera(int idJugador, int idAresta);

    void construirPoblat(int idJugador, int idVertex);

    void millorarACiutat(int idJugador, int idVertex);

    int calcularPunts(int idJugador);

    void passarTorn(int idJugador);

    void moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

}
