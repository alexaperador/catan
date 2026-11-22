package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import catan.models.Carretera;

public interface CarreteraRepository extends JpaRepository<Carretera, Integer> {

    boolean existsByArestaId(int idAresta);

    long countByPropietariId(int idJugador);

    @Query("""
            SELECT count(c) FROM Carretera c WHERE c.propietari.id = :j
              AND (c.aresta.vertexA.id IN (:v1, :v2) OR c.aresta.vertexB.id IN (:v1, :v2))
            """)
    long carreteresPropiesTocant(@Param("j") int idJugador, @Param("v1") int v1, @Param("v2") int v2);
}
