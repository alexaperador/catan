package catan.dao;

import java.util.List;
import java.util.Map;

import catan.enums.TipusRecurs;
import catan.models.Jugador;

public interface IPartidaDao {
    int iniciarPartida(List<String> nomsJugadors);

    void mostrarTauler(int idPartida);

    void mostrarPartida(int idPartida);

    Map<Integer, Map<TipusRecurs, Integer>> tirarDaus(int idPartida);

    void comprovarVictoria(int idPartida);

    Jugador exercitMesGran(int idPartida);
}
