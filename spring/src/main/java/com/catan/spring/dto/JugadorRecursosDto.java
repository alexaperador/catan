package com.catan.spring.dto;

import java.util.Map;

import catan.enums.ColorJugador;
import catan.enums.TipusRecurs;

public record JugadorRecursosDto(Integer id, String nom, ColorJugador color, int ordreTorn,
        Integer idPartida, Map<TipusRecurs, Integer> recursos, int totalRecursos) {
}
