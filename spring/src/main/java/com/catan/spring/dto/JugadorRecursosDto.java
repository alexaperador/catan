package com.catan.spring.dto;

import java.util.Map;

import catan.enums.ColorJugador;
import catan.enums.TipusRecurs;

/** Resposta de GET /jugador/{id}/recursos: el jugador amb el seu inventari. */
public record JugadorRecursosDto(Integer id, String nom, ColorJugador color, int ordreTorn,
        Integer idPartida, Map<TipusRecurs, Integer> recursos, int totalRecursos) {
}
