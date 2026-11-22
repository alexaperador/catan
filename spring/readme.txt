CATAN - Part Spring (M0486 BA2)
================================
Grup: (posar aquí el nom de tots els membres)

Requisits
---------
- Java 21 o superior i MySQL a localhost:3306 (usuari/contrasenya a application.properties).
- El projecte fa servir les entitats i enums del projecte Hibernate (paquets catan.models i
  catan.enums) com a dependència Maven. Abans d'arrencar-lo cal instal·lar-lo:

      cd spring
      mvnw -f ../hibernate/pom.xml install -DskipTests
      mvnw spring-boot:run

- Swagger UI: http://localhost:8080/swagger-ui.html

Estructura
----------
- controller/  PartidaController, JugadorController (només reben la petició i criden el servei)
- service/     PartidaService, JugadorService (lògica del joc, @Transactional)
- repository/  Un JpaRepository per entitat, amb consultes JPQL (@Query) i derivades
- dto/         Records que es tornen com a JSON. No es serialitzen les entitats directament per
               evitar la recursió infinita de les relacions bidireccionals i els errors de LAZY.
- exception/   ReglaException (400), NoTrobatException (404) i GestorErrors (@RestControllerAdvice)

Endpoints
---------
POST /partida                         cos: {"jugadors": ["Anna","Biel"], "puntsPerGuanyar": 10}
                                      (puntsPerGuanyar opcional). Torna la partida amb jugadors i hexàgons.
GET  /partida/{idPartida}/tauler      Array JSON d'hexàgons amb terreny, número, lladre i construccions.
POST /partida/{idPartida}/daus        Tirada i recursos rebuts per cada jugador.
GET  /jugador/{idJugador}/recursos    Jugador amb el seu inventari (404 si no existeix).
POST /jugador/{idJugador}/construir?tipus=CARRETERA|POBLAT|CIUTAT&posicio={id aresta o vèrtex}
POST /jugador/{idJugador}/passar

Les posicions són sempre l'id de la BD (no el codi del CSV).
Quan una acció no es pot fer es torna un 400 amb el motiu com a text, per exemple:
"No és el teu torn", "Posició ocupada", "Regla de la distància", "No connectat",
"Sense recursos", "Límit assolit", "Partida no en curs", "Daus no tirats",
"No hi ha cap poblat teu per millorar", "Posició d'una altra partida".

Decisions i simplificacions
---------------------------
- El tauler es carrega amb CarregadorTauler del projecte Hibernate, usant la sessió de la
  transacció de Spring (EntityManager.unwrap(Session.class)).
- Amb un 7 no hi ha producció. No es marca el lladre pendent perquè a Spring no hi ha
  endpoint per moure'l i la partida quedaria bloquejada.
- La victòria es comprova després de construir un poblat o una ciutat. Els punts inclouen
  poblats, ciutats, cartes PUNT_VICTORIA i exèrcit més gran.
- application.properties fa servir PhysicalNamingStrategyStandardImpl perquè les taules
  tinguin el mateix nom que al projecte Hibernate.
