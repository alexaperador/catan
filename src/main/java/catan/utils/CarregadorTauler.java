package catan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import catan.Hexagon;
import catan.Main;
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

    public void carregarHexagons(Map<String, Hexagon> hexagons, List<String[]> files) {
        for (String[] camps : llegirCsv("hexagons.csv")) {
            String codi = camps[0];
            String q = camps[1];
            String r = camps[2];

            int tipusTerrenyIndex = (int) (Math.random() * 6 + 1);

            TipusTerreny terreny = TipusTerreny.valueOf(camps[tipusTerrenyIndex]);
        }
    }

}
