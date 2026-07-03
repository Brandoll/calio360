-- ============================================================
-- S-03 Food Catalog Service - Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS categorias (
    id           SERIAL PRIMARY KEY,
    codigo_letra VARCHAR(2) NOT NULL UNIQUE,
    nombre       VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS alimentos (
    id                       SERIAL PRIMARY KEY,
    codigo_ins               VARCHAR(10) NOT NULL UNIQUE,
    categoria_id             INT REFERENCES categorias(id),
    nombre                   VARCHAR(150) NOT NULL,
    porcion_ref              VARCHAR(20) DEFAULT '100g',
    energia_kcal             DECIMAL(6,2),
    proteinas_g              DECIMAL(6,2),
    grasa_total_g            DECIMAL(6,2),
    carbohidratos_totales_g  DECIMAL(6,2),
    fibra_dietaria_g         DECIMAL(6,2),
    calcio_mg                DECIMAL(6,2),
    hierro_mg                DECIMAL(6,2),
    vitamina_c_mg            DECIMAL(6,2)
);

CREATE INDEX IF NOT EXISTS idx_alimentos_nombre ON alimentos USING gin(to_tsvector('spanish', nombre));
CREATE INDEX IF NOT EXISTS idx_alimentos_categoria ON alimentos(categoria_id);

-- Tabla de favoritos del usuario (referencia externa a S-01)
CREATE TABLE IF NOT EXISTS user_favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    alimento_id INT NOT NULL REFERENCES alimentos(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, alimento_id)
);
CREATE INDEX IF NOT EXISTS idx_fav_user ON user_favorites(user_id);
