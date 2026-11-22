package com.catan.spring.dto;

import java.util.List;

import catan.enums.TipusTerreny;

public record HexagonDto(Integer id, Integer codi, TipusTerreny terreny, Integer numero,
        boolean lladre, List<ConstruccioDto> construccions) {
}
