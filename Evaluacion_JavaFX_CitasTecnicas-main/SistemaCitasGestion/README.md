# SistemaCitasGestion - Evaluacion JavaFX & JDBC

Sistema de gestión de clientes y citas técnicas para la empresa **TecnoAsistencia Nicaragua S.A.**

## Integrantes del Equipo
- **Erick Arana** (@erick-espz) - *Integración final y arquitectura.*
- **Brandon Rocha** (@BJavi2106) - *Capa de datos y modelo CitaTecnica.*
- **Josue Reyes** (@castillozzzz) - *Vista FXML y controlador CitaController.*
- **Julián Torrez** (@torrez-julian) - *Validación de calidad, pruebas de aceptación y documentación.*

---

## Requisitos
- JDK 17+
- JavaFX 17+
- PostgreSQL 14+ (Docker o instalación nativa)
- Maven 3.8+

---

## Configuración de PostgreSQL
1. Crear contenedor de PostgreSQL (o usar servicio local):
   ```bash
   docker run --name base-postgres -e POSTGRES_PASSWORD=su_clave -p 5433:5432 -d postgres
   ```

## Ejecución

Las consultas y operaciones JDBC se ejecutan en hilos de trabajo para mantener
disponible la interfaz JavaFX mientras se comunica con PostgreSQL. Los resultados
se aplican a los controles desde el hilo de JavaFX.