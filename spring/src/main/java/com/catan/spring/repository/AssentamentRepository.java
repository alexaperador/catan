package com.catan.spring.repository;

import com.catan.spring.model.Assentament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssentamentRepository extends JpaRepository<Assentament, Integer> {

    boolean existsByVertexId(int idVertex);

    @Query("""
      select count(s) from Assentament s
      where s.vertex in (select a.vertexB from Aresta a where a.vertexA.id = :v)
         or s.vertex in (select a.vertexA from Aresta a where a.vertexB.id = :v)""")
    long assentamentsAdjacents(@Param("v") int idVertex);

    @Query("select count(s) from Assentament s where s.propietari.id = :j and s.vertex.id in (:v1, :v2)")
    long assentamentsPropisEn(@Param("j") int j, @Param("v1") int v1, @Param("v2") int v2);

    @Query("""
      select count(s) from Assentament s
      where s.propietari.id = :j and s.vertex.id in (:v1, :v2)
        and not exists (select c from Carretera c where c.propietari.id = :j
            and (c.aresta.vertexA = s.vertex or c.aresta.vertexB = s.vertex))""")
    long poblatsSenseCarretera(@Param("j") int j, @Param("v1") int v1, @Param("v2") int v2);
}