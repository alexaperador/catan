package com.catan.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import catan.models.Hexagon;
import catan.models.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Integer> {

	@Query("SELECT h FROM Hexagon h WHERE h.partida.id = :idPartida ORDER BY h.codi")
	List<Hexagon> hexagonsDePartida(@Param("idPartida") int idPartida);

	@Query("SELECT hv.hexagon.id, a FROM HexagonVertex hv, Assentament a "
			+ "WHERE a.vertex = hv.vertex AND hv.hexagon.partida.id = :idPartida "
			+ "ORDER BY hv.hexagon.codi, hv.posicio")
	List<Object[]> assentamentsPerHexagon(@Param("idPartida") int idPartida);

	@Query("SELECT h, a FROM HexagonVertex hv JOIN hv.hexagon h, Assentament a "
			+ "WHERE a.vertex = hv.vertex AND h.partida.id = :idPartida "
			+ "AND h.numero = :numero AND h.id <> :idLladre")
	List<Object[]> produccio(@Param("idPartida") int idPartida, @Param("numero") int numero,
			@Param("idLladre") int idLladre);
}
