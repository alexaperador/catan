public interface CiutatRepository extends JpaRepository<Ciutat, Integer> {
    long countByPropietariId(int idJugador);
}