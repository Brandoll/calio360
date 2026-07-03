-- =============================================
-- Calio Platform: Inicialización de Base de Datos
-- Este script se ejecuta automáticamente al iniciar
-- el contenedor de PostgreSQL por primera vez.
-- =============================================

-- Crear la base de datos principal (si no existe)
SELECT 'CREATE DATABASE calio'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio');

-- Las tablas individuales serán creadas automáticamente
-- por Hibernate (ddl-auto: update) de cada microservicio.
