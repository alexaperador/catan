package com.catan.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import catan.models.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Integer> {

	@Query("SELECT h FROM Hexagon h WHERE h.partida.id = :idPartida ORDER BY h.codi")
	List<catan.models.Hexagon> findHexagonsByPartidaId(@Param("idPartida") int idPartida);

	@Query("SELECT h.id, a FROM HexagonVertex hv JOIN hv.hexagon h, Assentament a "
			+ "WHERE a.vertex = hv.vertex AND h.partida.id = :idPartida "
			+ "ORDER BY h.codi, hv.posicio")
	List<Object[]> findAssentamentsByPartidaId(@Param("idPartida") int idPartida);
}
