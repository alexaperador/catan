package catan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import catan.Aresta;
import catan.Hexagon;
import catan.HexagonVertex;
import catan.Main;
import catan.Partida;
import catan.Vertex;
import catan.enums.TipusTerreny;

public class CarregadorTauler {
    private final static String CARPETA_CSV = "csv/";

    public static List<String[]> llegirCsv(String nomFitxer) {
        InputStream is = Main.class.getClassLoader().getResourceAsStream(CARPETA_CSV + nomFitxer);
        if (is == null) {
            throw new IllegalStateException("No s'ha trobat el fitxer " + CARPETA_CSV + nomFitxer);
        }
        List<String[]> files = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String linia = br.readLine();
            while ((linia = br.readLine()) != null) {
                if (!linia.isBlank()) {
                    String[] camps = linia.split(",", -1);
                    for (int i = 0; i < camps.length; i++) {
                        camps[i] = camps[i].trim();
                    }
                    files.add(camps);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error llegint el fitxer " + nomFitxer, e);
        }
        return files;
    }

    public static <T> T obtenir(Map<String, T> mapa, String clau, String fitxer) {
        T valor = mapa.get(clau);
        if (valor == null) {
            throw new IllegalStateException(fitxer + ": no existeix '" + clau + "'");
        }
        return valor;
    }

    private static Hexagon carregarHexagons(Session session, Partida partida, Map<String, Hexagon> mapa) {
        List<String[]> files = llegirCsv("hexagons.csv");
        if (files.size() != 19) {
            throw new IllegalStateException("hexagons.csv: s'esperaven 19 hexàgons i n'hi ha " + files.size());
        }
        List<TipusTerreny> terrenys = terrenysBarrejats();
        List<Integer> numeros = numerosBarrejats();

        Hexagon desert = null;
        int contador = 0;
        for (int i = 0; i < files.size(); i++) {
            String[] camps = files.get(i);
            int codi = Integer.parseInt(camps[0]);
            int q = Integer.parseInt(camps[1]);
            int r = Integer.parseInt(camps[2]);

            TipusTerreny terreny = terrenys.get(i);
            Integer numero = (terreny == TipusTerreny.DESERT) ? null : numeros.get(contador++);

            Hexagon hexagon = new Hexagon(partida, codi, terreny, numero, q, r);
            session.persist(hexagon);
            mapa.put(camps[0], hexagon);
            if (terreny == TipusTerreny.DESERT) {
                desert = hexagon;
            }
        }
        return desert;
    }

    public static Hexagon carregarTauler(Session session, Partida partida) {
        Map<String, Hexagon> hexagons = new HashMap<>();
        Hexagon desert = carregarHexagons(session, partida, hexagons);
        Map<String, Vertex> vertexs = carregarVertexs(session, partida);
        carregarArestes(session, partida, vertexs);
        carregarHexagonVertex(session, hexagons, vertexs);
        return desert;
    }

    private static Map<String, Vertex> carregarVertexs(Session session, Partida partida) {
        Map<String, Vertex> mapa = new HashMap<>();
        for (String[] camps : llegirCsv("vertexs.csv")) {
            Vertex vertex = new Vertex(partida, Integer.parseInt(camps[0]));
            session.persist(vertex);
            mapa.put(camps[0], vertex);
        }
        return mapa;
    }

    private static void carregarArestes(Session session, Partida partida, Map<String, Vertex> vertexs) {
        for (String[] camps : llegirCsv("arestes.csv")) {
            Vertex vertexA = obtenir(vertexs, camps[1], "arestes.csv");
            Vertex vertexB = obtenir(vertexs, camps[2], "arestes.csv");
            session.persist(new Aresta(partida, Integer.parseInt(camps[0]), vertexA, vertexB));
        }
    }

    private static void carregarHexagonVertex(Session session, Map<String, Hexagon> hexagons,
            Map<String, Vertex> vertexs) {
        List<String[]> dades = llegirCsv("hexagon_vertex.csv");
        for (String[] camps : dades) {
            Hexagon hexagon = obtenir(hexagons, camps[0], "hexagon_vertex.csv");
            Vertex vertex = obtenir(vertexs, camps[1], "hexagon_vertex.csv");
            session.persist(new HexagonVertex(hexagon, vertex, Integer.parseInt(camps[2])));
        }
    }

    // Asignacion de numeros y terrenos a los hexagonos

    private static List<TipusTerreny> terrenysBarrejats() {
        List<TipusTerreny> llista = new ArrayList<>();
        afegir(llista, TipusTerreny.BOSC, 4);
        afegir(llista, TipusTerreny.PASTURES, 4);
        afegir(llista, TipusTerreny.CAMPS, 4);
        afegir(llista, TipusTerreny.TURONS, 3);
        afegir(llista, TipusTerreny.MUNTANYES, 3);
        afegir(llista, TipusTerreny.DESERT, 1);
        Collections.shuffle(llista);
        return llista;
    }

    private static void afegir(List<TipusTerreny> llista, TipusTerreny terreny, int vegades) {
        for (int i = 0; i < vegades; i++) {
            llista.add(terreny);
        }
    }

    private static List<Integer> numerosBarrejats() {
        List<Integer> llista = new ArrayList<>(
                Arrays.asList(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        Collections.shuffle(llista);
        return llista;
    }

}
