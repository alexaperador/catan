## Membres del grup

- Badreddine El Khammal
- Erik Gamez
- Alex Aperador

# 1. Model de dades: relacions i per què s'han triat així

## 1.1 Criteris generals

- S'utilitza Integer en els camps que poden ser null: Hexagon.numero (el desert no té número), Partida.ultimaTirada (encara no s'ha tirat) i CartaDesenvolupament.rondaCompra (la carta és a la pila). També en els ids i en alguns camps obligatoris (Partida.ronda, Partida.puntsPerGuanyar, Hexagon.codi), que porten @NotNull. La resta són tipus primitius, que no admeten null.
- Cada partida crea el seu propi tauler. El camp codi de Hexagon, Vertex i Aresta guarda l'id del CSV, els mètodes treballen sempre amb l'id de la BD, que no coincideix amb el codi a partir de la segona partida.

## 1.2 Relacions (totes bidireccionals tret que s'indiqui el contrari)

En les relacions bidireccionals la clau forana és al costat "molts" (@ManyToOne, que és el propietari de la relació) i el costat "un" utilitza mappedBy. S'han deixat bidireccionals les que es naveguen des del pare (partida.getJugadors(), jugador.getRecursos()...) i on necessitem cascade/orphanRemoval; les altres són unidireccionals per no mantenir dos costats sense necessitat.

### Partida 1 - N Jugador (jugadors.partida_id)

Un jugador pertany a una única partida (enunciat). cascade = ALL i orphanRemoval: els jugadors viuen i moren amb la seva partida.

### Partida 0..1 - Jugador "actiu" i "guanyador" (@ManyToOne unidireccional, nullable)

Quan es crea la partida encara no hi ha guanyador, i l'actiu s'assigna després de crear els jugadors, per això admeten null. És @ManyToOne i no @OneToOne perquè no cal el costat invers. Sense cascade: el jugador ja existeix i pertany a la partida per l'altra relació.

### Partida 0..1 - Hexagon "lladre" (@ManyToOne unidireccional, nullable)

Apunta a l'hexàgon on és el lladre. Sense cascade: l'hexàgon ja pertany al tauler.

### Partida 1 - N Hexagon, Vertex i Aresta (tauler, vertexs, arestes)

Cada partida té el seu propi tauler, creat en iniciar-la a partir dels CSV. cascade = ALL i orphanRemoval: si s'esborra la partida s'esborra el seu tauler. Els tres tenen el @ManyToOne a Partida amb optional = false.

### Hexagon N - N Vertex → entitat HexagonVertex

La relació guarda una dada pròpia (la posició 0-5 del vèrtex dins de l'hexàgon). Per això no s'utilitza un @ManyToMany sinó una entitat intermèdia amb id propi i dos @ManyToOne (a Hexagon i a Vertex). Hexagon té la llista inversa hexagonVertexs (cascade = ALL + orphanRemoval).

### Aresta N - 1 Vertex (vertexA i vertexB)

Cada aresta uneix exactament dos vèrtexs: dos @ManyToOne obligatoris. Dos vèrtexs són adjacents si existeix una aresta que els uneix, aquesta consulta es resol en JPQL.

### Jugador 1 - N RecursJugador (inventari)

Una fila per jugador i tipus de recurs, creades a 0 en crear el jugador. És una taula i no un Map per poder consultar-la en JPQL i respectar el diagrama de l'enunciat. unique (jugador, tipus): no pot haver-hi dues files del mateix recurs per al mateix jugador. cascade = ALL + orphanRemoval.

### Jugador 1 - N CartaDesenvolupament "mà", i Partida 1 - N CartaDesenvolupament "pila"

La carta té dos @ManyToOne opcionals: pila (partida) i ma (jugador). En cada moment només un dels dos té valor: a la pila pertany a la partida i sense propietari, en comprar-la passa a la mà (pila = null, ma = jugador). La carta no s'esborra en canviar de lloc, per això la llista Partida.pila no té orphanRemoval (sí que en té Jugador.cartesDesenvolupament).

### Jugador 1 - N Construccio (propietari)

Tota construcció té propietari obligatori. cascade = ALL + orphanRemoval a Jugador.

### Assentament 1 - 1 Vertex i Carretera 1 - 1 Aresta (@OneToOne unidireccional amb unique)

Un vèrtex només pot tenir un assentament i una aresta només una carretera (enunciat). Es garanteix amb la restricció unique de la columna vertex_id / aresta_id. És @OneToOne i no @ManyToOne justament perquè cada vèrtex/aresta té com a màxim una construcció.

## 1.3 Herència de Construccio: JOINED

```
Construccio (id, propietari)
  ├─ Assentament (vertex, únic)   [abstracta]
  │    ├─ Poblat
  │    └─ Ciutat
  └─ Carretera (aresta, única)
```

S'utilitza `@Inheritance(strategy = InheritanceType.JOINED)` a Construccio. Cada classe té la seva taula i no hi ha columnes a null per pertànyer a un altre subtipus. La classe intermèdia abstracta Assentament permet consultar el que hi hagi en un vèrtex sense distingir poblat de ciutat.

## 1.4 LAZY o EAGER

- Totes les relacions estan marcades explícitament com a FetchType.LAZY, excepte Construccio.propietari.
- Per què LAZY: un @ManyToOne és EAGER per defecte i arrossegaria objectes en cascada. Amb una partida real, carregar una Partida amb tot EAGER portaria 19 hexàgons, 54 vèrtexs, 72 arestes, 114 files hexàgon-vèrtex, els jugadors, els seus recursos, construccions i cartes, només per, per exemple, construir una carretera. Amb LAZY cada mètode carrega només el que necessita i la resta es consulta amb JPQL (joins, COUNT, NOT EXISTS...) sense recórrer llistes en Java.
- LAZY exigeix que la sessió segueixi oberta en accedir a la relació. Es compleix perquè cada mètode DAO treballa dins de la seva transacció i només s'accedeix a les relacions dins d'aquesta. Els objectes que es retornen al qui crida ja carregats són els que es necessiten (per exemple: Vertex a vertexsDisponibles).
- Excepció: Construccio.propietari es deixa en el valor per defecte (EAGER), perquè cada vegada que es llegeix una construcció també es necessita el seu propietari (mostrarTauler imprimeix el propietari, tirarDaus reparteix recursos al propietari, calcular punts...).

## 1.5 Cascade

- cascade = ALL (+ orphanRemoval) només en relacions pare → fills propis, per exemple: hexàgon → hexagonVertexs.
- No hi ha cascade en les referències a entitats que existeixen per un altre camí (actiu, guanyador, lladre, vertex i aresta de les construccions), per no esborrar o crear objectes aliens per accident.

## 1.6 Restriccions i validacions

- **unique:** Assentament.vertex_id, Carretera.aresta_id i RecursJugador (jugador_id, tipus)
- **check:** Hexagon.numero IS NULL OR numero <> 7.
- nullable = false en tot el que és obligatori, length en cadenes (nom 50, enumerats 15-20).
- **Bean Validation:** @NotNull, @NotBlank, @Size, @Min, @Max, @PastOrPresent amb missatges. Hibernate Validator valida abans de cada INSERT/UPDATE.
- **Consultes amb @NamedQuery:**
  - Jugador.perPartida
  - Hexagon.perPartida
  - HexagonVertex.assentamentsPerPartida
  - RecursJugador.perJugador
  - RecursJugador.perJugadorITipus

  La resta de consultes són HQL/JPQL dins dels DAO.

# 2. Mètodes DAO

## 2.1 PartidaDao

- `int iniciarPartida(List<String> nomsJugadors)`: Crea la partida, els jugadors i el tauler a partir dels CSV. Retorna l'id de la partida.

- `void mostrarTauler(int idPartida)`: Imprimeix cada hexàgon de la partida (terreny, número i si té el lladre) i, a sota, els poblats i ciutats dels seus vèrtexs amb el seu propietari. No mostra les carreteres.

- `void mostrarPartida(int idPartida)`: Imprimeix l'estat de la partida, la ronda, el jugador actiu i l'última tirada. Per cada jugador mostra els seus punts, poblats, ciutats, carreteres i el total de recursos (sense el tipus).

- `Jugador exercitMesGran(int idPartida)`: Retorna el jugador amb l'exèrcit més gran (3 o més cartes de cavaller jugades). Si hi ha empat o ningú arriba a 3 cavallers, retorna null.

- `Map<Integer, Map<TipusRecurs, Integer>> tirarDaus(int idPartida)`: Tira els dos daus i reparteix els recursos segons el resultat (1 per poblat i 2 per ciutat, sense comptar l'hexàgon del lladre). Retorna, per cada jugador, els recursos rebuts. Si surt 7, els jugadors amb més de 7 recursos descarten la meitat i queda pendent moure el lladre. Falla si ja s'han tirat els daus o hi ha un lladre pendent.

- `void comprovarVictoria(int idPartida)`: Comprova per cada jugador si ha arribat als punts necessaris per guanyar, llavors marca la partida com a finalitzada i assigna el guanyador.

## 2.2 JugadorDao

- `void mostrarRecursos(int idJugador)`: Imprimeix per consola els recursos del jugador.

- `void comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep)`: Permet al jugador intercanviar 4 cartes d'un recurs per 1 carta d'un altre recurs amb la banca.

- `void comprarCarta(int idJugador)`: Permet al jugador comprar una carta de desenvolupament de la pila de la partida. Costa 1 de BLAT, 1 de LLANA i 1 de MINERAL. Es descompta el cost i s'afegeix la carta a la seva mà. Si li toca una carta de victòria, es comprova si ha guanyat la partida.

- `void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs)`: Juga una carta de desenvolupament del jugador. No es pot jugar a la mateixa ronda en què es va comprar. Amb MONOPOLI, el jugador rep tots els recursos del tipus recurs dels altres jugadors. Amb CAVALLER, suma un cavaller jugat, deixa el lladre pendent de moure i comprova si hi ha victòria. Llança IllegalStateException si és de punt de victòria.

- `void construirCarretera(int idJugador, int idAresta)`: Construeix una carretera del jugador a l'aresta indicada. L'aresta ha d'estar lliure i connectada a un assentament o carretera del jugador. Fora de la preparació costa 1 de FUSTA i 1 de MAO, i requereix haver tirat els daus i no tenir el lladre pendent. A la preparació és gratis, però ha de tocar el poblat acabat de col·locar en aquell torn. El màxim és de 15 carreteres per jugador.

- `void millorarACiutat(int idJugador, int idVertex)`: Millora a ciutat un poblat del jugador situat al vèrtex indicat. Costa 2 de BLAT i 3 de MINERAL, i el jugador pot tenir com a màxim 4 ciutats. En acabar comprova si el jugador ha guanyat.

- `int calcularPunts(int idJugador)`: Calcula els punts de victòria del jugador: 1 per cada poblat, 2 per cada ciutat, 1 per cada carta de punt de victòria, i 2 per l'exèrcit més gran. Retorna el total.

- `void construirPoblat(int idJugador, int idVertex)`: Construeix un poblat al vèrtex indicat. El vèrtex ha d'estar lliure, sense assentaments contigus, i el màxim és de 5 poblats per jugador. Fora de la preparació costa 1 de FUSTA, MAO, LLANA i BLAT, i requereix haver tirat els daus, no tenir el lladre pendent i estar connectat a una carretera seva. A la preparació és gratis i només es pot col·locar un per ronda, el segon dóna 1 recurs per cada hexàgon adjacent. En acabar comprova si el jugador ha guanyat.

- `void passarTorn(int idJugador)`: Passa el torn al jugador següent. Ha de ser el jugador actiu, no tenir el lladre pendent i haver tirat els daus (en EN_CURS). A la preparació ha d'haver col·locat un poblat i una carretera, i l'ordre és de serp (1→n, n→1). En acabar la preparació la partida passa a EN_CURS. La ronda puja quan el torn torna al jugador 1.

- `void moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima)`: Mou el lladre a un altre hexàgon quan hi ha un moviment pendent (després d'un 7 o un CAVALLER). Si s'indica una víctima amb un assentament en aquest hexàgon, se li roba 1 recurs aleatori. En acabar, el lladre deixa d'estar pendent.

- `List<Vertex> vertexsDisponibles(int idJugador)`: Retorna els vèrtexs on el jugador pot construir un poblat: lliures, sense assentaments contigus i connectats a una carretera seva. A la preparació no s'exigeix la carretera, i retorna una llista buida si el jugador encara no ha de col·locar un poblat.

# 3. Decisions d'implementació

- **Transaccions:** `calcularPunts` i `exercitMesGran` només obren transacció si no n'hi ha una d'activa, per poder cridar-se des d'altres mètodes. `comprovarVictoria` es crida després del commit del mètode que dóna punts.
- **millorarACiutat:** s'esborra el poblat, es fa `flush()` i després es crea la ciutat, perquè `vertex_id` és unique.
- **Tauler:** `CarregadorTauler` carrega hexàgons, vèrtexs, arestes i hexagon_vertex des dels CSV, i barreja terrenys i números.
