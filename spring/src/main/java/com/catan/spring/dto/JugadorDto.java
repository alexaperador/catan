package com.catan.spring.dto;

import catan.enums.ColorJugador;
import catan.models.Jugador;

public record JugadorDto(Integer id, String nom, ColorJugador color, int ordreTorn) {

    public static JugadorDto convertirJugador(Jugador j) {
        if (j == null) {
            return null;
        }
        
        return new JugadorDto(j.getId(), j.getNom(), j.getColor(), j.getOrdreTorn());
    }
}
