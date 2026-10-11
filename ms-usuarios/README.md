# ms-usuarios (Gas Mapocho)

Microservicio de autenticación y gestión de usuarios. Versión rápida para presentar en clase (AP1) — usa una base H2 en memoria para no depender de tener PostgreSQL en la nube ya configurado. Cuando se tenga la base definitiva, solo se cambian 4 líneas en `application.properties`.

## Requisitos
- JDK 25 (si da error de versión, revisar `JAVA_HOME`)

## Cómo correrlo
```
./mvnw spring-boot:run
```
Queda escuchando en `http://localhost:8081`.

## Usuarios de prueba (ya sembrados al arrancar)
| email | password | rol |
|---|---|---|
| admin@gasmapocho.cl | Admin123 | ADMIN |
| almacen@gasmapocho.cl | Almacen123 | ALMACEN |

## Endpoints
- `POST /api/auth/login` → `{ "email": "...", "password": "..." }` → devuelve JWT + nombre + rol. 401 si falla.
- `GET /api/usuarios` → lista todos
- `GET /api/usuarios/{id}`
- `POST /api/usuarios` → `{ "nombre", "email", "password", "rol": "ADMIN|ALMACEN" }`
- `PUT /api/usuarios/{id}` → `{ "nombre", "email", "rol" }`
- `DELETE /api/usuarios/{id}`

Probado end-to-end (login válido, login con clave incorrecta → 401, crear, listar, actualizar, eliminar). 5/5 pruebas unitarias de la capa de servicio (JUnit + Mockito) pasando: `./mvnw test`.

## Seguridad (actualizado, igual al patrón de "tema 4" del profesor)
- Login real vía `AuthenticationManager` + `DaoAuthenticationProvider` + `UserDetailsServiceImpl` (no validación manual).
- Filtro `FiltroJwtAuth` (`OncePerRequestFilter`) valida el JWT en cada request y llena el `SecurityContextHolder`.
- `/api/usuarios/**` requiere rol `ADMIN` (`@PreAuthorize`) — ALMACEN recibe 403, igual que indica el cuadro de permisos del informe.
- Probado en vivo: login malo → 401, sin token → 403, token ALMACEN → 403, token ADMIN → 200/404 reales según corresponda.

## Pendiente para la versión final (no bloquea el avance)
- Reemplazar H2 por PostgreSQL en la nube (Aiven/Supabase) en `application.properties`.
- Registrar el servicio en Eureka y exponerlo a través del Gateway.
- Notas al pie pendientes en el informe (ver conversación aparte).
