package com.catan.spring.dto;

import java.util.List;

import catan.enums.TipusTerreny;

/**
 * Un hexàgon del tauler. A GET /tauler porta les construccions dels seus vèrtexs;
 * a POST /partida encara no n'hi ha cap i la llista és buida.
 */
public record HexagonDto(Integer id, Integer codi, TipusTerreny terreny, Integer numero,
        boolean lladre, List<ConstruccioDto> construccions) {
}
