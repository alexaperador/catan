package com.catan.spring.dto;

/** Poblat o ciutat situat en un vèrtex d'un hexàgon. */
public record ConstruccioDto(Integer id, String tipus, Integer idVertex, int codiVertex, JugadorDto propietari) {
}
