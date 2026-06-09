# Arquitectura de Cebolla (Onion Architecture) en Java

Ejemplo **sencillo y didáctico** de la arquitectura de cebolla aplicada a un
mini sistema bancario. La estructura sigue exactamente las capas vistas en clase.

> **Regla de oro:** todas las dependencias apuntan **hacia el centro**.
> El Núcleo no depende de nada externo. La base de datos, la interfaz y los
> frameworks son detalles que viven en la Capa Externa.

## Los dos mundos (Anatomía Macro)

La cebolla se divide en **dos mundos separados**:

- **EL NÚCLEO** (`com.banco.nucleo`): puramente lógica de negocio. Reglas
  irrompibles y modelos del mundo real. **Cambia lento.**
- **LA CAPA EXTERNA** (`com.banco.externa`): detalles técnicos, frameworks,
  bases de datos e interfaces gráficas. **Cambia rápido.**

```
┌───────────────────────────────────────────────────────────┐
│  LA CAPA EXTERNA  (com.banco.externa)                       │
│   - ui/           -> Interfaz gráfica (consola) + arranque  │
│   - persistencia/ -> Implementan los contratos (HashMap)    │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  EL NÚCLEO  (com.banco.nucleo)                       │   │
│  │                                                     │   │
│  │  Capa 3: aplicacion/  -> Casos de uso (orquesta)    │   │
│  │  ┌───────────────────────────────────────────────┐ │   │
│  │  │  Capa 2: contratos/ -> Interfaces IRepositorio │ │   │
│  │  │  ┌─────────────────────────────────────────┐  │ │   │
│  │  │  │  Capa 1: modelo/ -> Entidades puras      │  │ │   │
│  │  │  │   Cuenta, Cliente, Movimiento,           │  │ │   │
│  │  │  │   PoliticaInteres (cero dependencias)    │  │ │   │
│  │  │  └─────────────────────────────────────────┘  │ │   │
│  │  └───────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────┘
        Las flechas SIEMPRE apuntan hacia adentro ↓↓↓
```

## Las capas en detalle

### Capa 1 — Modelo de Dominio (El Centro Absoluto) · `nucleo.modelo`
Entidades puras que modelan el problema (`Cuenta`, `Cliente`, `Movimiento`).
Contienen **estado y comportamiento del negocio puro** y las reglas
irrompibles (ej. no se puede retirar más del saldo → `SaldoInsuficienteException`).
`PoliticaInteres` es comportamiento de negocio puro (tasa de interés).

**Regla de oro:** cero dependencias externas. No sabe qué es un ORM, ni SQL,
ni que existe la red.

### Capa 2 — Servicios de Dominio: Los Contratos · `nucleo.contratos`
Interfaces de persistencia que el núcleo **define pero NO implementa**:
`IRepositorioCuenta`, `IRepositorioCliente`. El núcleo dicta **qué** necesita
para guardar datos, no **cómo**. Es un "contrato vacío" que la Capa Externa
estará obligada a firmar.

### Capa 3 — Servicios de Aplicación: La Frontera del Núcleo · `nucleo.aplicacion`
`ServicioCuentas` contiene los **casos de uso** (abrir cuenta, depositar,
retirar, transferir, aplicar interés). Hace **orquestación agnóstica**: no le
importa si la petición viene de una web, una API REST o una app móvil. Depende
solo de los **contratos** (interfaces), nunca de implementaciones.

### Capa Externa — Infraestructura + Interfaz · `externa.*`
- `externa.persistencia`: **firma** los contratos. Hay **dos implementaciones
  intercambiables** del mismo contrato:
  - En memoria (`RepositorioCuentaEnMemoria`, `RepositorioClienteEnMemoria`) — un HashMap.
  - SQLite (`RepositorioCuentaSQLite`, `RepositorioClienteSQLite`) — base de datos
    real embebida en un archivo `banco.db`, con ayuda de `BaseDatosSQLite`.
- `externa.ui`: `Main` es la interfaz (consola) y el *Composition Root* que
  inyecta las implementaciones hacia adentro.

## Intercambiar la persistencia (la gracia de la cebolla)

Cambiar de "memoria" a "base de datos real" **no toca el núcleo** ni una sola
línea: solo cambia qué implementación se conecta en el `Main`.

```bash
bash run.sh           # usa SQLite (archivo banco.db)  [por defecto]
bash run.sh memoria   # usa el repositorio en memoria
```

En `Main` la decisión es literalmente este bloque (lo demás es idéntico):

```java
if (usarMemoria) {
    repoCuenta  = new RepositorioCuentaEnMemoria();
    repoCliente = new RepositorioClienteEnMemoria();
} else {
    BaseDatosSQLite bd = new BaseDatosSQLite("banco.db");
    repoCuenta  = new RepositorioCuentaSQLite(bd);
    repoCliente = new RepositorioClienteSQLite(bd);
}
```

> SQLite es **embebido**: no hay que instalar ningún servidor. Lo único que
> Java necesita es el driver JDBC, incluido en `lib/sqlite-jdbc-*.jar` (junto
> con `slf4j-api` y `slf4j-nop`). Los scripts ya lo agregan al classpath.
> Con SQLite los datos **persisten entre ejecuciones** en el archivo `banco.db`.

## ¿Por qué esto importa?
- El núcleo se puede **probar sin base de datos** (se le pasa un repo falso).
- Cambiar de tecnología (BD, UI, framework) **no toca** el núcleo.
- Las reglas de negocio están en **un solo lugar** y protegidas.

## Cómo ejecutar

Requisitos: solo JDK (probado con Java 26). No usa Maven ni Gradle.

**PowerShell:**
```powershell
.\run.bat
```

**Git Bash / Linux / macOS:**
```bash
bash run.sh
```

**Manual:**
```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
# El driver de SQLite se añade al classpath en tiempo de ejecución:
java --enable-native-access=ALL-UNNAMED -cp "out;lib/*" com.banco.externa.ui.Main
# (en Linux/macOS usa ':' en vez de ';' en el classpath)
```

## Estructura de archivos
```
src/com/banco/
├── nucleo/                       (EL NÚCLEO)
│   ├── modelo/                   Capa 1: Cuenta, Cliente, Movimiento,
│   │                                     TipoMovimiento, PoliticaInteres,
│   │                                     SaldoInsuficienteException
│   ├── contratos/                Capa 2: IRepositorioCuenta, IRepositorioCliente
│   └── aplicacion/               Capa 3: ServicioCuentas
│       └── dto/                          ResumenCuenta, MovimientoDTO
└── externa/                      (LA CAPA EXTERNA)
    ├── persistencia/             En memoria:  RepositorioCuentaEnMemoria, RepositorioClienteEnMemoria
    │                             SQLite:      BaseDatosSQLite,
    │                                          RepositorioCuentaSQLite, RepositorioClienteSQLite
    └── ui/                       Main
lib/                              Driver JDBC de SQLite + slf4j (.jar)
```
