package catan.dao;

import java.util.List;

import catan.Jugador;
import catan.Vertex;

public interface IPartidaDao {
    int iniciarPartida(List<String> nomsJugadors);

    void mostrarTauler(int idPartida);
    
    void mostrarPartida(int idPartida);

    void tirarDaus(int idPartida);

    void comprovarVictoria(int idPartida);

    Jugador exercitMesGran(int idPartida);

    List<Vertex> vertexsDisponibles(int idJugador);

}
