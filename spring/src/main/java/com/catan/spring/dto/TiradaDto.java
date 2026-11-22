package com.catan.spring.dto;

import java.util.List;
import java.util.Map;

import catan.enums.TipusRecurs;

public record TiradaDto(Integer idPartida, int tirada, JugadorDto jugadorActiu, List<RecursosRebuts> jugadors) {

    public record RecursosRebuts(JugadorDto jugador, Map<TipusRecurs, Integer> recursos) {
    }
}
