package com.catan.spring.dto;

import java.util.List;
import java.util.Map;

import catan.enums.TipusRecurs;

/** Resposta de POST /partida/{id}/daus. */
public record TiradaDto(Integer idPartida, int tirada, JugadorDto jugadorActiu, List<RecursosRebuts> jugadors) {

    /** Recursos que ha rebut un jugador amb aquesta tirada (buit si no n'ha rebut cap). */
    public record RecursosRebuts(JugadorDto jugador, Map<TipusRecurs, Integer> recursos) {
    }
}
