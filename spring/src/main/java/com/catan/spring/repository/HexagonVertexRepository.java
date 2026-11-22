package com.catan.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import catan.models.Hexagon;
import catan.models.HexagonVertex;

public interface HexagonVertexRepository extends JpaRepository<HexagonVertex, Integer> {

    /** Hexàgons que toquen un vèrtex (entre 1 i 3). */
    @Query("SELECT hv.hexagon FROM HexagonVertex hv WHERE hv.vertex.id = :idVertex")
    List<Hexagon> hexagonsDelVertex(@Param("idVertex") int idVertex);
}
