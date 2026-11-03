package catan.dao;

import java.util.List;
import java.util.Map;

import catan.Jugador;
import catan.Vertex;
import catan.enums.TipusRecurs;

public interface IPartidaDao {
    int iniciarPartida(List<String> nomsJugadors);

    void mostrarTauler(int idPartida);
    
    void mostrarPartida(int idPartida);

    Map<Integer, Map<TipusRecurs, Integer>> tirarDaus(int idPartida); // idJugador -> recursos rebuts

    void comprovarVictoria(int idPartida);

    Jugador exercitMesGran(int idPartida);

    List<Vertex> vertexsDisponibles(int idJugador);

}
