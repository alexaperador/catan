package catan.dao;

import java.util.List;

public interface IPartidaDao {
    int iniciarPartida(List<String> nomsJugadors);

    void mostrarTauler(int idPartida);

    void mostrarPartida(int idPartida);

    void mostrarRecursos(int idJugador);
}
