@RestController
@RequestMapping("/jugador")
public class JugadorController {
    private final JugadorService service;

    public JugadorController(JugadorService service) {
        this.service = service;
    }

    @PostMapping("/{idJugador}/construir")
    public String construir(@PathVariable int idJugador,
            @RequestParam String tipus,
            @RequestParam int posicio) {
        try {
            return service.construir(idJugador, tipus, posicio);
        } catch (ReglaException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/{idJugador}/passar")
    public String passar(@PathVariable int idJugador) {
        try {
            return service.passarTorn(idJugador);
        } catch (ReglaException e) {
            return e.getMessage();
        }
    }
}