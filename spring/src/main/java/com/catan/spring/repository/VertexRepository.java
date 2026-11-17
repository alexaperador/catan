public interface VertexRepository extends JpaRepository<Vertex, Integer> {
    boolean existsByIdAndPartidaId(int id, int idPartida);
}