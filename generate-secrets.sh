#!/bin/bash
# ==========================================
# CALIO360 — Generate Secrets
# ==========================================
# Run this script to generate secure random values
# for all secrets in the .env file.
# Usage: ./generate-secrets.sh > .env.generated
# ==========================================

set -euo pipefail

# Generate a random string of given length
random_string() {
    openssl rand -base64 "$1" | tr -dc 'a-zA-Z0-9' | head -c "$1"
}

cat <<EOF
# ==========================================
# CALIO360 — Generated Secrets
# Generated at: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
# ==========================================

# DOMINIO
CALIO_DOMAIN=calio360.app
CALIO_API_DOMAIN=api.calio360.app
CALIO_PUBLIC_URL=https://calio360.app
CALIO_API_URL=https://api.calio360.app
CALIO_SERVER_IP="${1:-20.104.183.219}"

# POSTGRESQL
CALIO_POSTGRES_HOST=calio-postgres
CALIO_POSTGRES_PORT=5432
CALIO_POSTGRES_USER=postgres
CALIO_POSTGRES_PASSWORD=$(random_string 32)

# IDENTITY
CALIO_IDENTITY_DB_NAME=calio_identity_db
CALIO_IDENTITY_DB_HOST=calio-postgres
CALIO_IDENTITY_DB_PORT=5432
CALIO_IDENTITY_DB_USER=postgres
CALIO_IDENTITY_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# FOOD
CALIO_FOOD_DB_NAME=calio_food_db
CALIO_FOOD_DB_HOST=calio-postgres
CALIO_FOOD_DB_PORT=5432
CALIO_FOOD_DB_USER=postgres
CALIO_FOOD_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# TRACKING
CALIO_TRACKING_DB_NAME=calio_tracking_db
CALIO_TRACKING_DB_HOST=calio-postgres
CALIO_TRACKING_DB_PORT=5432
CALIO_TRACKING_DB_USER=postgres
CALIO_TRACKING_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# RECIPE
CALIO_RECIPE_DB_NAME=calio_recipe_db
CALIO_RECIPE_DB_HOST=calio-postgres
CALIO_RECIPE_DB_PORT=5432
CALIO_RECIPE_DB_USER=postgres
CALIO_RECIPE_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# WEARABLE
CALIO_WEARABLE_DB_NAME=calio_wearable_db
CALIO_WEARABLE_DB_HOST=calio-postgres
CALIO_WEARABLE_DB_PORT=5432
CALIO_WEARABLE_DB_USER=postgres
CALIO_WEARABLE_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# EXERCISE
CALIO_EXERCISE_DB_NAME=calio_exercise_db
CALIO_EXERCISE_DB_HOST=calio-postgres
CALIO_EXERCISE_DB_PORT=5432
CALIO_EXERCISE_DB_USER=postgres
CALIO_EXERCISE_DB_PASSWORD=\${CALIO_POSTGRES_PASSWORD}

# MONGODB
CALIO_ANALYTICS_MONGO_URI=mongodb://calio-mongodb:27017/calio_analytics_db
CALIO_NOTIFICATION_MONGO_URI=mongodb://calio-mongodb:27017/calio_notification_db

# RABBITMQ
CALIO_RABBITMQ_HOST=calio-rabbitmq
CALIO_RABBITMQ_PORT=5672
CALIO_RABBITMQ_USER=calio_admin
CALIO_RABBITMQ_PASSWORD=$(random_string 24)

# JWT
CALIO_JWT_SECRET=$(random_string 64)
CALIO_JWT_EXPIRATION=86400000
CALIO_REFRESH_EXPIRATION=604800000

# GEMINI
CALIO_GEMINI_API_KEY=
CALIO_GEMINI_MODEL=gemini-2.5-flash

# SERVICE URLS
CALIO_IDENTITY_SERVICE_URL=http://calio-identity-service:8081
CALIO_AI_SERVICE_URL=http://calio-ai-service:8082
CALIO_FOOD_SERVICE_URL=http://calio-food-service:8083
CALIO_RECIPE_SERVICE_URL=http://calio-recipe-service:8084
CALIO_TRACKING_SERVICE_URL=http://calio-tracking-service:8085
CALIO_ANALYTICS_SERVICE_URL=http://calio-analytics-service:8086
CALIO_NOTIFICATION_SERVICE_URL=http://calio-notification-service:8087
CALIO_WEARABLE_SERVICE_URL=http://calio-wearable-service:8088
CALIO_EXERCISE_SERVICE_URL=http://calio-exercise-service:8089
EOF
