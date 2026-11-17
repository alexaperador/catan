public interface RecursJugadorRepository extends JpaRepository<RecursJugador, Integer> {
    Optional<RecursJugador> findByJugadorIdAndTipus(int idJugador, TipusRecurs tipus);
    List<Jugador> findByPartidaIdOrderByOrdreTorn(int idPartida);
}
