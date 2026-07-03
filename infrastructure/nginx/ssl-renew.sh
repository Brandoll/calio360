#!/bin/bash
# ==========================================
# CALIO360 — SSL Certificate Renewal
# Run via cron: 0 3 * * * /path/to/ssl-renew.sh
# ==========================================

set -euo pipefail

COMPOSE_FILE="/opt/calio360/docker-compose.prod.yml"

echo "[$(date)] Starting SSL renewal check..."

docker compose -f "$COMPOSE_FILE" run --rm certbot renew --quiet

docker compose -f "$COMPOSE_FILE" exec nginx nginx -s reload

echo "[$(date)] SSL renewal check completed."
