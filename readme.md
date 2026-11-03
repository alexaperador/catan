## Miembros del grupo

- Badre
- Erik
- Alex Aperador Salcedo

# 1. Modelo de datos: relaciones y por qué se han elegido así

## 1.1 Criterios generales

- Clave primaria autogenerada (GenerationType.IDENTITY, AUTO_INCREMENT de MySQL) en todas las tablas. No hay claves compuestas.
- Enumerados con @Enumerated(EnumType.STRING): en la BD se guarda el nombre (por ejemplo "FUSTA").
- Se usa Integer solo cuando el valor puede ser null: Hexagon.numero (el desierto no tiene número), Partida.ultimaTirada (aún no se ha tirado) y CartaDesenvolupament.rondaCompra (la carta está en la pila). El resto son tipos primitivos, que no admiten null.
- Cada partida crea su propio tablero. El campo codi de Hexagon, Vertex y Aresta guarda el id del CSV; los métodos trabajan siempre con el id de la BD, que no coincide con el código a partir de la segunda partida.

## 1.2 Relaciones (todas bidireccionales salvo que se indique)

En las relaciones bidireccionales la clave foránea está en el lado "muchos" (@ManyToOne, que es el propietario de la relación) y el lado "uno" usa mappedBy. Se han dejado bidireccionales las que se navegan desde el padre (partida.getJugadors(), jugador.getRecursos()...) y donde necesitamos cascade/orphanRemoval; las demás son unidireccionales para no mantener dos lados sin necesidad.

### Partida 1 - N Jugador (jugadors.partida_id)

Un jugador pertenece a una única partida (enunciado). cascade = ALL y orphanRemoval: los jugadores viven y mueren con su partida.

### Partida 0..1 - Jugador "actiu" y "guanyador" (@ManyToOne unidireccional, nullable)

Al crear la partida todavía no hay ganador, y el activo se asigna después de crear a los jugadores, por eso admiten null. Es @ManyToOne y no @OneToOne porque no hace falta el lado inverso. Sin cascade: el jugador ya existe y pertenece a la partida por la otra relación.

### Partida 0..1 - Hexagon "lladre" (@ManyToOne unidireccional, nullable)

Apunta al hexágono donde está el ladrón. Sin cascade: el hexágono ya pertenece al tablero.

### Partida 1 - N Hexagon, Vertex y Aresta (tauler, vertexs, arestes)

Cada partida tiene su propio tablero, creado al iniciarla a partir de los CSV. cascade = ALL y orphanRemoval: si se borra la partida se borra su tablero. Los tres tienen el @ManyToOne a Partida con optional = false.

### Hexagon N - N Vertex → entidad HexagonVertex

La relación guarda un dato propio (la posición 0-5 del vértice dentro del hexágono). Por eso no se usa un @ManyToMany sino una entidad intermedia con id propio y dos @ManyToOne (a Hexagon y a Vertex). Hexagon tiene la lista inversa hexagonVertexs (cascade = ALL + orphanRemoval).

### Aresta N - 1 Vertex (vertexA y vertexB)

Cada arista une exactamente dos vértices: dos @ManyToOne obligatorios. Dos vértices son adyacentes si existe una arista que los une, esa consulta se resuelve en JPQL.

### Jugador 1 - N RecursJugador (inventario)

Una fila por jugador y tipo de recurso, creadas a 0 al crear al jugador. Es una tabla y no un Map para poder consultarla en JPQL y respetar el diagrama del enunciado. unique (jugador, tipus): no puede haber dos filas del mismo recurso para el mismo jugador. cascade = ALL + orphanRemoval.

### Jugador 1 - N CartaDesenvolupament "mà", y Partida 1 - N CartaDesenvolupament "pila"

La carta tiene dos @ManyToOne opcionales: pila (partida) y ma (jugador). En cada momento solo uno de los dos tiene valor: en la pila pertenece a la partida y sin propietario, al comprarla pasa a la mano (pila = null, ma = jugador). La carta no se borra al cambiar de sitio, por eso la lista Partida.pila no tiene orphanRemoval (sí lo tiene Jugador.cartesDesenvolupament).

### Jugador 1 - N Construccio (propietari)

Toda construcción tiene propietario obligatorio. cascade = ALL + orphanRemoval en Jugador.

### Assentament 1 - 1 Vertex y Carretera 1 - 1 Aresta (@OneToOne unidireccional con unique)

Un vértice solo puede tener un asentamiento y una arista solo una carretera (enunciado). Se garantiza con la restricción unique de la columna vertex_id / aresta_id. Es @OneToOne y no @ManyToOne justamente porque cada vértice/arista tiene como máximo una construcción.

## 1.3 Herencia de Construccio: JOINED

```
Construccio (id, propietari)
  ├─ Assentament (vertex, único)   [abstracta]
  │    ├─ Poblat
  │    └─ Ciutat
  └─ Carretera (aresta, única)
```

Cada clase tiene su tabla y no hay columnas a null por pertenecer a otro subtipo. La clase intermedia abstracta Assentament permite consultar lo que haya en un vértice sin distinguir poblado de ciudad. Mejorar a ciudad = borrar el poblado y crear la ciudad en el mismo vértice, como vertex_id es unique, hay un flush entre el borrado y la inserción.

## 1.4 LAZY o EAGER

- Todas las relaciones están marcadas explícitamente como FetchType.LAZY, excepto Construccio.propietari.
- Por qué LAZY: un @ManyToOne es EAGER por defecto y arrastraría objetos en cascada. Con una partida real, cargar una Partida con todo EAGER traería 19 hexágonos, 54 vértices, 72 aristas, 114 filas hexágono-vértice, los jugadores, sus recursos, construcciones y cartas, solo para, por ejemplo, construir una carretera. Con LAZY cada método carga solo lo que necesita y lo demás se consulta con JPQL (joins, COUNT, NOT EXISTS...) sin recorrer listas en Java.
- LAZY exige que la sesión siga abierta al acceder a la relación. Se cumple porque cada método DAO trabaja dentro de su transacción y solo se accede a las relaciones dentro de ella. Los objetos que se devuelven al llamador ya cargados son los que se necesitan (por ejemplo: Vertex en vertexsDisponibles).
- Excepción: Construccio.propietari se deja en el valor por defecto (EAGER), porque cada vez que se lee una construcción también se necesita su propietario (mostrarTauler imprime el propietario, tirarDaus reparte recursos al dueño, calcular puntos...).

## 1.5 Cascade

- cascade = ALL (+ orphanRemoval) solo en relaciones padre → hijos propios, por ejemplo: hexágono → hexagonVertexs.
- No hay cascade en las referencias a entidades que existen por otro camino (actiu, guanyador, lladre, vertex y aresta de las construcciones), para no borrar o crear objetos ajenos por accidente.

## 1.6 Restricciones y validaciones

- **unique:** Assentament.vertex_id, Carretera.aresta_id y RecursJugador (jugador_id, tipus)
- **check:** Hexagon.numero IS NULL OR numero <> 7.
- nullable = false en todo lo obligatorio, length en cadenas (nom 50, enumerados 15-20).
- **Bean Validation:** @NotNull, @NotBlank, @Size, @Min, @Max, @PastOrPresent con mensajes. Hibernate Validator valida antes de cada INSERT/UPDATE.
- **Consultas con @NamedQuery:**
  - Jugador.perPartida
  - Hexagon.perPartida
  - HexagonVertex.assentamentsPerPartida
  - RecursJugador.perJugador
  - RecursJugador.perJugadorITipus

  El resto de consultas son HQL/JPQL dentro de los DAO.

# 2. Metodos Dao
## 2.1 PartidaDao
- `int iniciarPartida(List<String> nomsJugadors)`: Crea la partida, los jugadores y el tablero a partir de los CSV. Devuelve el id de la partida.
- `void mostrarTauler(int idPartida)`: Imprime el tablero en consola, con hexágonos, vértices y aristas, y las construcciones de cada jugador.
- `void mostrarPartida(int idPartida)`: Imprime la partida en consola, con el tablero y los jugadores y sus recursos y construcciones.
- `Jugador exercitMesGran(int idPartida)`: Devuelve el jugador con el ejercito más grande (3 o más cartas de caballero jugadas). Si hay empate, devuelve null.
- `List<Vertex> vertexsDisponibles(int idJugador)`: Devuelve los vértices donde el jugador puede construir un poblado: libres, sin asentamientos contiguos y conectados a una carretera suya.
- `Map<Integer, Map<TipusRecurs, Integer>> tirarDaus(int idPartida)`: Tira los dos dados y reparte los recursos según el resultado. Devuelve, por cada jugador los recursos recibidos. Si sale 7, cada jugador descarta la mitad de sus recursos y queda pendiente mover el ladrón.
- `void comprovarVictoria(int idPartida)`: Comprueba por cada jugador si ha llegado a los puntos necesarios para ganar, entonces marca la partida como finalizada y asigna el ganador.

## 2.2 JugadorDao
- `void mostrarRecursor(int idJugador)`: Imprime en consola los recursos del jugador.
- `void comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep)`: Permite al jugador intercambiar 4 cartas de un recurso por 1 carta de otro recurso con la banca.
- `void comprarCarta(int idJugador)`: Permite al jugador comprar una carta de desarrollo de la pila de la partida, si tiene recursos suficientes. Se descuenta el coste y se añade la carta a su mano. Si le toca una carta de victoria, se comprueba si ha ganado la partida.
- `void jugarCarta(int idJugador, int idCarta, TipusRecurs recurs)`: Juega una carta de desarrollo del jugador. Con MONOPOLI, el jugador recibe todos los recursos del tipo recurs de los demás jugadores. Con CAVALLER, suma un caballero jugado, deja el ladrón pendiente de mover y comprueba si hay victoria. Lanza IllegalStateException si es de punto de victoria.
- `void construirCarretera(int idJugador, int idAresta)`: Construye una carretera del jugador en la arista indicada. La arista debe estar libre y conectada a un asentamiento o carretera del jugador. Fuera de la preparación cuesta 1 de FUSTA y 1 de MAO, y requiere haber tirado los dados y no tener el ladrón pendiente. En la preparación es gratis, pero debe tocar el poblado recién colocado en ese turno. El máximo es de 15 carreteras por jugador.
- `void millorarACiutat(int idJugador, int idVertex)`: Mejora a ciudad un poblado del jugador situado en el vértice indicado. Cuesta 2 de BLAT y 3 de MINERAL, y el jugador puede tener como máximo 4 ciudades. Al terminar comprueba si el jugador ha ganado.
- `int calcularPunts(int idJugador)`: Calcula los puntos de victoria del jugador: 1 por cada poblado, 2 por cada ciudad, 2 por ejercito más grande y 2 por carretera más larga. Devuelve el total.
- `void passarTorn(int idJugador)`:
- `void moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima)`:
