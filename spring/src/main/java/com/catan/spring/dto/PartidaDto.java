package com.catan.spring.dto;

import java.util.List;

import catan.enums.EstatPartida;

public record PartidaDto(Integer id, EstatPartida estat, int ronda, int puntsPerGuanyar,
        JugadorDto jugadorActiu, List<JugadorDto> jugadors, List<HexagonDto> hexagons) {
}
