## Membres del grup

- Badreddine El Khammal
- Erik Gamez
- Alex Aperador

# 1. Com s'executa

## 1.1 Requisits

- Java 21 o superior.
- MySQL a `localhost:3306`. L'usuari i la contrasenya són a `src/main/resources/application.properties`. La BD `catan` es crea sola si no existeix (`createDatabaseIfNotExist=true`) i les taules les crea Hibernate (`ddl-auto=update`).
- El projecte fa servir les entitats i els enums del projecte Hibernate (paquets `catan.models` i `catan.enums`) com a dependència Maven (`catan:catan:1.0-SNAPSHOT`). Per això cal instal·lar-lo primer al repositori local.

L'API queda a `http://localhost:8080`. La documentació Swagger és a `http://localhost:8080/swagger-ui.html`.

# 2. Estructura

- **controller**: `PartidaController` i `JugadorController`. Només reben la petició, criden el servei i tornen la resposta. No tenen lògica del joc.
- **service**: `PartidaService` i `JugadorService`. Tota la lògica del joc.
- **repository**: un `JpaRepository` per entitat. Les consultes senzilles són mètodes derivats (`countByPropietariId`, `existsByVertexId`...) i les complexes són JPQL amb `@Query` i paràmetres amb nom.
- **dto**: records que es tornen com a JSON (`PartidaDto`, `HexagonDto`, `JugadorDto`, `JugadorRecursosDto`, `TiradaDto`...) i el cos de la petició `NovaPartidaRequest`.
- **exception**: `ReglaException` (400), `NoTrobatException` (404) i `GestorErrors` (`@RestControllerAdvice`), que converteix les excepcions en la resposta HTTP amb el missatge com a text.

# 3. Endpoints

## 3.1 PartidaController (`/partida`)

- `POST /partida`: Crea una partida. Cos: `{"jugadors": ["Badre", "Erik", "Alex"], "puntsPerGuanyar": 10}` (`puntsPerGuanyar` és opcional, per defecte 10). Barreja l'ordre de torn, assigna els colors per ordre, crea els 5 recursos a 0 de cada jugador, la pila de 21 cartes i el tauler a partir dels CSV, amb el lladre al desert. Torna **201** amb la partida, els jugadors i els 19 hexàgons. Amb menys de 2 o més de 4 jugadors, un nom buit o `puntsPerGuanyar` < 1 torna **400** i no crea res.

- `GET /partida/{idPartida}/tauler`: Torna un array JSON amb els 19 hexàgons (codi, terreny, número i si hi ha el lladre). Cada hexàgon porta els poblats i ciutats dels seus vèrtexs amb el propietari. No es mostren les carreteres. **404** si la partida no existeix.

- `POST /partida/{idPartida}/daus`: Tira els dos daus i reparteix els recursos (1 per poblat i 2 per ciutat, sense l'hexàgon del lladre). Torna la tirada i, per cada jugador, els recursos rebuts. **400** si la partida no és EN_CURS o si ja s'han tirat els daus aquest torn. **404** si la partida no existeix.

## 3.2 JugadorController (`/jugador`)

- `GET /jugador/{idJugador}/recursos`: Torna el jugador amb els 5 tipus de recurs i el total. **404** si el jugador no existeix.

- `POST /jugador/{idJugador}/construir?tipus=CARRETERA|POBLAT|CIUTAT&posicio={id}`: Construeix al vèrtex (poblat, ciutat) o a l'aresta (carretera) indicats. Aplica les mateixes regles que el projecte Hibernate: torn, daus tirats, posició lliure, regla de la distància, connexió, recursos i límits (15 carreteres, 5 poblats, 4 ciutats). A la preparació és gratis, el segon poblat dóna 1 recurs per hexàgon no desert i la carretera ha de tocar el poblat acabat de posar. Després d'un poblat o una ciutat es comprova la victòria. Torna **200** amb un missatge, o **400** amb el motiu.

- `POST /jugador/{idJugador}/passar`: Passa el torn al següent jugador. A la preparació l'ordre és de serp (1 -> n, n -> 1) i al final la partida passa a EN_CURS. En EN_CURS cal haver tirat els daus, i la ronda puja quan el torn torna al jugador 1. Torna **200** amb qui juga ara, o **400** amb el motiu.

## 3.3 Errors

Quan una acció no es pot fer, el servei llança una `ReglaException` amb un missatge diferent per a cada cas i `GestorErrors` la converteix en un **400** amb el missatge com a cos.

Si la partida o el jugador no existeixen es llança una `NoTrobatException`, que torna un **404**. Els errors de validació del cos (`@Valid`) també tornen un **400** amb el missatge de la restricció.

# 4. Decisions d'implementació

- **Entitats del projecte Hibernate:** no es dupliquen les entitats. `Application` té `@EntityScan("catan.models")` perquè Spring les trobi al paquet del projecte Hibernate.
- **DTOs:** no es tornen mai les entitats com a JSON. Les relacions bidireccionals (`Partida` ↔ `Jugador`...) farien una recursió infinita, i les relacions LAZY fallarien fora de la transacció. Els records només porten el que necessita cada resposta.
- **Repositoris en lloc dels DAO:** la part Spring no fa servir `PartidaDao` ni `JugadorDao`, perquè obren la seva pròpia sessió i transacció i quedarien fora de la transacció de Spring. Les mateixes consultes són als repositoris.
- **Tauler:** es carrega amb `CarregadorTauler` del projecte Hibernate. Se li passa la sessió de la transacció de Spring (`EntityManager.unwrap(Session.class)`), així que la partida i el tauler es guarden o es desfan junts.