public interface PoblatRepository extends JpaRepository<Poblat, Integer> {
    long countByPropietariId(int idJugador);
    Optional<Poblat> findByVertexIdAndPropietariId(int idVertex, int idJugador);
}