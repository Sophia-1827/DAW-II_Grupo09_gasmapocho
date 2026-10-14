# backend-gas-mapocho

Backend de microservicios del proyecto Gas Mapocho (Spring Boot 4.1.1, Spring Cloud 2025.1.3, Java 25).

## Servicios

| Servicio | Puerto | Depende de |
|---|---|---|
| `eureka-server` | 8761 | - |
| `api-gateway` | 8080 | Eureka |
| `ms-usuarios` | 8081 | Eureka, PostgreSQL |
| `ms-balones` | 8082 | Eureka, PostgreSQL, RabbitMQ |
| `ms-movimientos` | 8083 | Eureka, PostgreSQL, RabbitMQ, ms-balones (Feign) |
| `ms-reportes` | 8084 | Eureka, ms-balones y ms-movimientos (Feign) — sin base propia |

## Requisitos para correr el proyecto completo

1. **JDK 25** (`JAVA_HOME` apuntando a esa versión).
2. **PostgreSQL** instalado y corriendo en `localhost:5432`.
3. **RabbitMQ** instalado y corriendo en `localhost:5672` (lo usan `ms-balones` y `ms-movimientos` para actualizar el stock de forma asíncrona cuando se registra un movimiento).

## Configurar PostgreSQL

Cada quien tiene su propia copia local de la base — no es una base compartida.
1. Instala PostgreSQL si no lo tienes: https://www.postgresql.org/download/ (durante la instalación te va a pedir una contraseña para el usuario `postgres`, anótala).
2. Abre una terminal y conéctate como `postgres` (te va a pedir la contraseña que pusiste al instalar):
   ```bash
   psql -U postgres -h 127.0.0.1
   ```
3. Dentro de `psql`, crea el rol y las 3 bases del proyecto (copia y pega todo junto, cambia la contraseña por una tuya):
   ```sql
   CREATE ROLE gasmapocho LOGIN PASSWORD 'TuContraseñaAqui';
   CREATE DATABASE gasmapocho_usuarios OWNER gasmapocho;
   CREATE DATABASE gasmapocho_balones OWNER gasmapocho;
   CREATE DATABASE gasmapocho_movimientos OWNER gasmapocho;
   \q
   ```
4. Las tablas y los datos de ejemplo se crean solos la primera vez que corras cada microservicio (Hibernate + un `DataSeeder`), no hay que correr ningún script `.sql` aparte.

## Variables de entorno (antes de correr cualquier microservicio con base de datos)

Cada quien usa su propia contraseña, la que eligió en el paso 3. Nunca se escribe en ningún archivo del repo.

PowerShell:
```powershell
$env:DB_USER="gasmapocho"
$env:DB_PASSWORD="TuContraseñaAqui"
```

Bash:
```bash
export DB_USER=gasmapocho
export DB_PASSWORD='TuContraseñaAqui'
```

Esto vale solo para esa ventana de terminal — hay que volver a setearlo si la cierras.

## Orden para levantar todo

```bash
cd eureka-server && ./mvnw spring-boot:run      # esperar a que termine de arrancar
cd ms-usuarios && ./mvnw spring-boot:run
cd ms-balones && ./mvnw spring-boot:run
cd ms-movimientos && ./mvnw spring-boot:run
cd ms-reportes && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run        # al final, cuando los demas ya esten registrados en Eureka
```

Eureka tarda ~30 segundos en propagar un registro nuevo a los demás servicios, así que si algo da 503/500 justo después de arrancar, espera un poco y reintenta.

## Correr todo con Docker (alternativa a instalar todo a mano)

Hay un `docker-compose.yml` en la raíz que levanta los 6 microservicios + PostgreSQL + RabbitMQ con un solo comando, sin instalar nada más que Docker Desktop:

Antes crea tu archivo `.env` a partir de la plantilla y pon tu propia contraseña (el `.env` no se sube a GitHub):

```bash
cp .env.example .env
docker compose up --build
```

La primera vez tarda varios minutos (compila cada servicio con Maven dentro del contenedor). `api-gateway` queda en `http://localhost:8080` y `postgres`/`rabbitmq` quedan accesibles igual que si estuvieran instalados localmente. **Nota**: esto todavía no se probó en vivo en ningún equipo del grupo (Docker Desktop no arrancó en esta PC durante el desarrollo) — probarlo antes de depender de esto para la sustentación.

## Usuarios de prueba (sembrados automáticamente)

| email | password | rol |
|---|---|---|
| admin@gasmapocho.cl | Admin123 | ADMIN |
| almacen@gasmapocho.cl | Almacen123 | ALMACEN |
