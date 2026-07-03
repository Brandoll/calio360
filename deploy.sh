#!/bin/bash
# ==========================================
# CALIO360 — Deploy Script for Azure VPS
# ==========================================
# Usage: ./deploy.sh [first-run|update|ssl-init]
# ==========================================

set -euo pipefail

DEPLOY_DIR="/opt/calio360"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.prod.yml"
DOMAIN="api.calio360.app"
EMAIL="admin@calio360.app"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# ==========================================
# FIRST RUN: Initial setup
# ==========================================
first_run() {
    log "=== CALIO360 First Run Setup ==="

    # Create required directories
    mkdir -p "$DEPLOY_DIR/infrastructure/certbot/conf"
    mkdir -p "$DEPLOY_DIR/infrastructure/certbot/www"

    # Copy project files
    log "Copying project files..."
    rsync -av --exclude='.git' --exclude='node_modules' --exclude='target' . "$DEPLOY_DIR/"

    # Check .env exists
    if [ ! -f "$DEPLOY_DIR/.env" ]; then
        log "ERROR: .env file not found. Copy .env.example to .env and fill in values."
        exit 1
    fi

    # Build and start infrastructure first
    log "Starting infrastructure..."
    cd "$DEPLOY_DIR"
    docker compose -f docker-compose.prod.yml up -d calio-postgres calio-mongodb calio-rabbitmq

    log "Waiting for infrastructure to be healthy..."
    sleep 15

    # Build and start all services
    log "Building and starting all services..."
    docker compose -f docker-compose.prod.yml up -d --build

    log "=== First Run Complete ==="
    log "Next step: Run './deploy.sh ssl-init' to configure SSL"
}

# ==========================================
# SSL INIT: Obtain initial certificate
# ==========================================
ssl_init() {
    log "=== SSL Certificate Setup ==="

    cd "$DEPLOY_DIR"

    # Use init config for nginx first
    log "Starting nginx with HTTP-only config..."
    docker compose -f docker-compose.prod.yml exec nginx \
        cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.ssl

    # Copy init config
    cp infrastructure/nginx/nginx-init.conf infrastructure/nginx/nginx-active.conf

    docker compose -f docker-compose.prod.yml restart nginx

    # Obtain certificate
    log "Obtaining SSL certificate for $DOMAIN..."
    docker compose -f docker-compose.prod.yml run --rm certbot certonly \
        --webroot \
        --webroot-path=/var/www/certbot \
        --email "$EMAIL" \
        --agree-tos \
        --no-eff-email \
        -d "$DOMAIN"

    # Restore SSL nginx config
    log "Restoring SSL nginx configuration..."
    docker compose -f docker-compose.prod.yml restart nginx

    log "=== SSL Setup Complete ==="
    log "Certificate will auto-renew via certbot sidecar."
}

# ==========================================
# UPDATE: Deploy latest changes
# ==========================================
update() {
    log "=== CALIO360 Update ==="

    cd "$DEPLOY_DIR"

    # Pull latest code
    log "Pulling latest code..."
    git pull origin main

    # Rebuild and restart
    log "Rebuilding services..."
    docker compose -f docker-compose.prod.yml up -d --build

    # Clean up old images
    log "Cleaning up..."
    docker image prune -f

    log "=== Update Complete ==="
}

# ==========================================
# STATUS: Check all services
# ==========================================
status() {
    log "=== CALIO360 Service Status ==="
    cd "$DEPLOY_DIR"
    docker compose -f docker-compose.prod.yml ps
}

# ==========================================
# MAIN
# ==========================================
case "${1:-help}" in
    first-run)  first_run ;;
    ssl-init)   ssl_init ;;
    update)     update ;;
    status)     status ;;
    *)
        echo "Usage: $0 {first-run|ssl-init|update|status}"
        echo ""
        echo "Commands:"
        echo "  first-run  - Initial setup on Azure VPS"
        echo "  ssl-init   - Obtain initial SSL certificate"
        echo "  update     - Deploy latest changes"
        echo "  status     - Check service status"
        exit 1
        ;;
esac
