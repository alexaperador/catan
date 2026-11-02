package catan;

import java.util.List;

import catan.dao.PartidaDao;

public class Main {
    public static void main(String[] args) {
        PartidaDao partidaDao = new PartidaDao();

        try {
            System.out.println("\t Iniciant partida...");
            List<String> nomsJugadors = java.util.Arrays.asList("Alice", "Bob", "Charlie");
            int idPartida = partidaDao.iniciarPartida(nomsJugadors);
            System.out.println("\t Partida iniciada amb ID: " + idPartida);

            System.out.println("\t Mostrant tauler...");
            partidaDao.mostrarTauler(idPartida);

            System.out.println("\t Tirar daus...");
            partidaDao.tirarDaus(idPartida);

            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}