public interface ArestaRepository extends JpaRepository<Aresta, Integer> {
    boolean existsByIdAndPartidaId(int id, int idPartida);
}