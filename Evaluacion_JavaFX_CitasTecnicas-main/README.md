# SistemaCitasGestion - Evaluación JavaFX & JDBC

Sistema de escritorio para gestionar **clientes y citas de atención técnica** de **TecnoAsistencia Nicaragua S.A.**, desarrollado con JavaFX y JDBC sobre PostgreSQL.

Asignatura: Programación de Aplicaciones de Escritorio, Unidad II (Acceso a bases de datos). Universidad Americana (UAM), Ingeniería de Sistemas.

---

## Integrantes del equipo

| Nombre completo | Usuario GitHub | Responsabilidad |
|---|---|---|
| Erick Antonio Arana Espinoza | [@erick-espz](https://github.com/erick-espz) | Base de datos, conexión JDBC, CRUD de clientes, modelo y DAO de citas, estilos e integración final |
| Brandon Rocha | [@BJavi2106](https://github.com/BJavi2106) | *(completar con su aporte real)* |
| Josue Reyes | [@castillozzzz](https://github.com/castillozzzz) | Vista de citas (`citas-view.fxml`) y `CitaController` |
| Julián Torrez | [@torrez-julian](https://github.com/torrez-julian) | Validaciones, pruebas de aceptación y documentación |

---

## Funcionalidades

- **Clientes:** registrar, listar, seleccionar, editar y eliminar. Búsqueda por nombres, apellidos o correo, sin distinguir mayúsculas.
- **Citas técnicas:** registrar, listar, seleccionar, editar y eliminar, eligiendo el cliente de un ComboBox.
- **Filtros de citas:** por estado y por fecha, con opción para limpiarlos.
- **Validaciones:** campos obligatorios, nombres solo con letras, teléfono, formato de correo, correo duplicado, fecha y hora, estado permitido y cliente existente.
- **Integridad:** un cliente con citas asociadas no se puede eliminar, y la aplicación explica el motivo.
- **Alertas** de éxito, error SQL y confirmación antes de eliminar.
- **Persistencia** real en PostgreSQL: los datos se conservan al cerrar y abrir la aplicación.

---

## Requisitos

- JDK 21
- Maven 3.8+ (incluye Maven Wrapper) o IntelliJ IDEA
- Docker, o PostgreSQL 14+ instalado
- JavaFX 21 se descarga automáticamente con Maven

---

## Configuración de la base de datos

1. Crear el contenedor de PostgreSQL (puerto `5433` del equipo):
```bash
   docker run --name base-postgres -e POSTGRES_PASSWORD=su_clave -p 5433:5432 -d postgres
```
2. Crear la base de datos:
```bash
   docker exec -it base-postgres psql -U postgres -c "CREATE DATABASE gestion_citas;"
```
3. Ejecutar el script desde la raíz del repositorio:
```bash
   docker exec -i base-postgres psql -U postgres -d gestion_citas < SistemaCitasGestion/db/gestion_citas.sql
```
   También se puede abrir `SistemaCitasGestion/db/gestion_citas.sql` en pgAdmin y ejecutarlo sobre la base `gestion_citas`.
4. Configurar las credenciales locales. En `SistemaCitasGestion/src/main/resources/` copiar `db.properties.example` como `db.properties` y poner la clave real:
```properties
   db.url=jdbc:postgresql://localhost:5433/gestion_citas
   db.user=postgres
   db.password=su_clave
```
   `db.properties` está en `.gitignore`: las contraseñas reales nunca se suben al repositorio.

Si PostgreSQL está instalado de forma nativa, el puerto suele ser `5432`; se ajusta en `db.url`.

---

## Cómo ejecutar

1. Clonar el repositorio y abrir la carpeta **`SistemaCitasGestion`** en IntelliJ IDEA.
2. Esperar a que Maven descargue las dependencias.
3. Ejecutar la clase `Launcher` (`com.tuempresa.sistemacitasgestion.Launcher`).

Por terminal, dentro de `SistemaCitasGestion`:
```bash
./mvnw javafx:run
```

---

## Estructura del proyecto

```
SistemaCitasGestion/
├── db/gestion_citas.sql                  Script de tablas y relaciones
├── pom.xml
└── src/main/
    ├── java/com/tuempresa/sistemacitasgestion/
    │   ├── Launcher.java                 Punto de arranque
    │   ├── HelloApplication.java         Ventana principal
    │   ├── config/ConexionBD.java        Conexión JDBC centralizada
    │   ├── model/                        Cliente, CitaTecnica
    │   ├── dao/                          ClienteDAO, CitaTecnicaDAO
    │   ├── controller/                   ClienteController, CitaController
    │   └── util/                         Alertas, Validador, ValidadorCita
    └── resources/
        ├── db.properties.example
        └── com/tuempresa/sistemacitasgestion/
            ├── view/                     main-view, cliente-view, citas-view (FXML)
            └── css/estilos.css
```

### Flujo de la aplicación

```
Vista (FXML) → Controlador → Validador → DAO → ConexionBD → PostgreSQL
```

- Los DAO usan `Connection`, `PreparedStatement` y `ResultSet` dentro de `try-with-resources`.
- Todas las operaciones con valores usan parámetros (`?`): no se concatena texto del usuario en el SQL.
- `Statement` se usa en `ConexionBD.probar()` para la sentencia fija `SELECT 1`.
- No se usa ORM: solo JDBC.

---

## Modelo de datos

- **cliente** (1) — (N) **cita_tecnica**, mediante la clave foránea `cita_tecnica.cliente_id`.
- Estados permitidos de una cita: `Pendiente`, `En proceso`, `Finalizada`, `Cancelada`.
- Eliminar es físico, y se rechaza si el cliente tiene citas asociadas.

---

## Pruebas de aceptación

| # | Prueba | Resultado | Evidencia |
|---|---|---|---|
| 1 | Registrar un cliente y verlo en la tabla | *(pendiente)* | *(captura)* |
| 2 | Rechazar un correo repetido con mensaje | *(pendiente)* | *(captura)* |
| 3 | Modificar el teléfono y comprobar que persiste | *(pendiente)* | *(captura)* |
| 4 | Registrar una cita y ver el nombre del cliente en la tabla | *(pendiente)* | *(captura)* |
| 5 | Buscar por parte del apellido | *(pendiente)* | *(captura)* |
| 6 | Filtrar citas por estado y fecha, y limpiar filtros | *(pendiente)* | *(captura)* |
| 7 | Cambiar el estado de una cita a Finalizada | *(pendiente)* | *(captura)* |
| 8 | Intentar eliminar un cliente con citas (debe rechazarse) | *(pendiente)* | *(captura)* |
| 9 | Eliminar la cita y luego el cliente, confirmando cada operación | *(pendiente)* | *(captura)* |
| 10 | Cerrar y abrir la aplicación: los datos persisten | *(pendiente)* | *(captura)* |
| 11 | Mostrar commits y autores del 12, 13 y 14 de octubre | *(pendiente)* | *(captura)* |

---

## Capturas de funcionamiento

<img width="1043" height="694" alt="image" src="https://github.com/user-attachments/assets/e1a15f6e-94c2-4be5-b409-6db80465e306" />
<img width="994" height="656" alt="image" src="https://github.com/user-attachments/assets/0ff7e35a-708b-44d9-b10e-75a3856af6f5" />
<img width="1002" height="667" alt="image" src="https://github.com/user-attachments/assets/7abe7a89-0b0a-4356-9f27-88cdf5880033" />
<img width="994" height="665" alt="image" src="https://github.com/user-attachments/assets/3bcbfcb3-de1e-4241-ae9b-ae6622426c4a" />




---

## Flujo de trabajo en Git

- Rama de integración: `erick-espz-desarrollo`.
- Cada integrante trabajó en su propia rama y con su propia cuenta de GitHub.
- Los cambios se integraron mediante Pull Requests con *merge commit*, para conservar la autoría de cada commit.
- Para ver el historial: `git log --pretty=format:"%h %an %ad %s" --date=short`
