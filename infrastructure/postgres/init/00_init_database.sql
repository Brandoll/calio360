-- =============================================
-- CALIO360 Platform: Inicialización de Bases de Datos
-- Este script se ejecuta automáticamente al iniciar
-- el contenedor de PostgreSQL por primera vez.
-- =============================================
-- Cada microservicio tiene su propia base de datos
-- para garantizar aislamiento y escalabilidad.
-- =============================================

-- La base de datos principal (POSTGRES_DB) se crea
-- automáticamente por la imagen Docker. Creamos las
-- bases adicionales para cada servicio.

-- Identity Service
SELECT 'CREATE DATABASE calio_identity_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_identity_db')\gexec

-- Food Catalog Service
SELECT 'CREATE DATABASE calio_food_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_food_db')\gexec

-- Tracking Service
SELECT 'CREATE DATABASE calio_tracking_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_tracking_db')\gexec

-- Recipe Service
SELECT 'CREATE DATABASE calio_recipe_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_recipe_db')\gexec

-- Wearable Service
SELECT 'CREATE DATABASE calio_wearable_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_wearable_db')\gexec

-- Exercise Service
SELECT 'CREATE DATABASE calio_exercise_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'calio_exercise_db')\gexec

-- Las tablas individuales serán creadas automáticamente
-- por Hibernate (ddl-auto: update) de cada microservicio.
