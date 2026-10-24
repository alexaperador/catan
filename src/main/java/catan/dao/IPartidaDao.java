package catan.dao;

public interface IPartidaDao {
    int iniciarPartida(java.util.List<String> nomsJugadors);

    void mostrarTauler(int idPartida);

    void mostrarPartida(int idPartida);
}
