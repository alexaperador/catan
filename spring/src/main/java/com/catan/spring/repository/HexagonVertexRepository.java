public interface HexagonVertexRepository extends JpaRepository<HexagonVertex, Integer> {
    @Query("select hv.hexagon from HexagonVertex hv where hv.vertex.id = :v")
    List<Hexagon> hexagonsDelVertex(@Param("v") int idVertex);
}