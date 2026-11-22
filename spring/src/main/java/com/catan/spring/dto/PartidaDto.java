package com.catan.spring.dto;

import java.util.List;

import catan.enums.EstatPartida;

/** Resposta de POST /partida: la partida creada amb els jugadors i el tauler. */
public record PartidaDto(Integer id, EstatPartida estat, int ronda, int puntsPerGuanyar,
        JugadorDto jugadorActiu, List<JugadorDto> jugadors, List<HexagonDto> hexagons) {
}
