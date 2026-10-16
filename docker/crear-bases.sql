-- Prepara PostgreSQL para correr Gas Mapocho en una PC (sin Docker).
-- Ejecutar UNA vez como el usuario administrador "postgres" (por ejemplo, en pgAdmin: Query Tool).
-- Las bases quedan vacias a proposito: las tablas y los datos de ejemplo se crean solos la primera vez que
-- arrancan los microservicios (2 usuarios, 4 balones, 3 proveedores, 3 clientes y 12 movimientos).
-- No agregues INSERT aqui: las tablas todavia no existen y los datos quedarian duplicados.

-- 1) Cambia 'CAMBIA_ESTA_CONTRASENA' por una contrasena propia.
--    Esa misma contrasena va en spring.datasource.password del archivo application-local.properties
--    de ms-usuarios, ms-balones y ms-movimientos (ese archivo no se sube a GitHub).
CREATE ROLE gasmapocho LOGIN PASSWORD 'CAMBIA_ESTA_CONTRASENA';

-- 2) Una base de datos por microservicio.
CREATE DATABASE gasmapocho_usuarios    OWNER gasmapocho;
CREATE DATABASE gasmapocho_balones     OWNER gasmapocho;
CREATE DATABASE gasmapocho_movimientos OWNER gasmapocho;
