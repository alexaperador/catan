package com.catan.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import catan.models.Assentament;

/** Poblats i ciutats (classe abstracta Assentament). */
public interface AssentamentRepository extends JpaRepository<Assentament, Integer> {

    boolean existsByVertexId(int idVertex);

    long countByPropietariId(int idJugador);

    /** Assentaments (de qualsevol jugador) en vèrtexs adjacents: regla de la distància. */
    @Query("""
        SELECT count(s) FROM Assentament s
        WHERE s.vertex IN (SELECT a.vertexB FROM Aresta a WHERE a.vertexA.id = :v)
           OR s.vertex IN (SELECT a.vertexA FROM Aresta a WHERE a.vertexB.id = :v)""")
    long assentamentsAdjacents(@Param("v") int idVertex);

    /** Assentaments del jugador en algun dels dos vèrtexs d'una aresta. */
    @Query("SELECT count(s) FROM Assentament s WHERE s.propietari.id = :j AND s.vertex.id IN (:v1, :v2)")
    long assentamentsPropisEn(@Param("j") int idJugador, @Param("v1") int v1, @Param("v2") int v2);

    /**
     * Assentaments del jugador en algun dels dos vèrtexs que encara no tenen cap
     * carretera pròpia adjacent (PREPARACIO: la carretera ha de tocar el poblat nou).
     */
    @Query("""
        SELECT count(s) FROM Assentament s
        WHERE s.propietari.id = :j AND s.vertex.id IN (:v1, :v2)
          AND NOT EXISTS (SELECT c FROM Carretera c WHERE c.propietari.id = :j
              AND (c.aresta.vertexA = s.vertex OR c.aresta.vertexB = s.vertex))""")
    long assentamentsPropisSenseCarretera(@Param("j") int idJugador, @Param("v1") int v1, @Param("v2") int v2);
}
