package com.catan.spring.dto;

import catan.enums.ColorJugador;
import catan.models.Jugador;

/** Dades bàsiques d'un jugador, sense relacions (així el JSON no entra en recursió). */
public record JugadorDto(Integer id, String nom, ColorJugador color, int ordreTorn) {

    public static JugadorDto de(Jugador j) {
        return j == null ? null : new JugadorDto(j.getId(), j.getNom(), j.getColor(), j.getOrdreTorn());
    }
}
