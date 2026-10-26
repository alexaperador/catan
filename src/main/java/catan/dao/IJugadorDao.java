package catan.dao;

import catan.enums.TipusRecurs;

public interface IJugadorDao {
    void mostrarRecursos(int idJugador);
    void comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);
    void comprarCarta(int idJugador);
    void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);
}
