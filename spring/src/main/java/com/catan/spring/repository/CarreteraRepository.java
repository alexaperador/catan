public interface CarreteraRepository extends JpaRepository<Carretera, Integer> {

    boolean existsByArestaId(int idAresta);

    long countByPropietariId(int idJugador);

    @Query("""
      select count(c) from Carretera c where c.propietari.id = :j
       and (c.aresta.vertexA.id in (:v1,:v2) or c.aresta.vertexB.id in (:v1,:v2))""")
    long carreteresPropiesTocant(@Param("j") int j, @Param("v1") int v1, @Param("v2") int v2);
}