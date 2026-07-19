#!/bin/bash
# ==========================================
# CALIO360 — Deploy Script for Azure/Generic VPS
# ==========================================
# Usage: ./deploy.sh [first-run|update|ssl-init|status|check-system]
# ==========================================

set -euo pipefail

DEPLOY_DIR="/opt/calio360"
COMPOSE_FILE="$DEPLOY_DIR/docker-compose.prod.yml"
DOMAIN="api.calio360.app"
EMAIL="admin@calio360.app"

# Colors for log output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0;0m' # No Color

log_info() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] ${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] ${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] ${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] ${RED}[ERROR]${NC} $1" >&2
}

# ==========================================
# UTILS: Pre-flight checks & System Helpers
# ==========================================

check_prereqs() {
    log_info "Verifying prerequisites..."
    local missing=()
    
    for cmd in docker git rsync curl openssl; do
        if ! command -v "$cmd" &> /dev/null; then
            missing+=("$cmd")
        fi
    done

    # Check for docker compose subcommand as well
    if command -v docker &> /dev/null; then
        if ! docker compose version &> /dev/null; then
            missing+=("docker compose (plugin)")
        fi
    fi

    if [ ${#missing[@]} -ne 0 ]; then
        log_error "Missing required tools/packages: ${missing[*]}"
        log_info "Please install them before running this script."
        exit 1
    fi
    log_success "All prerequisites are satisfied."
}

detect_ip() {
    log_info "Detecting VPS public IP address..."
    local ip=""
    # Try multiple external services in case one is down
    for service in "https://api.ipify.org" "https://ifconfig.me" "https://ipinfo.io/ip"; do
        ip=$(curl -s --max-time 5 "$service" || echo "")
        if [[ "$ip" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            break
        fi
    done

    if [ -z "$ip" ]; then
        log_error "Could not automatically determine public IP. Please make sure you have internet access."
        exit 1
    fi

    log_success "Detected Public IP: $ip"
    echo "$ip"
}

check_dns() {
    local detected_ip="$1"
    log_info "Checking DNS resolution for $DOMAIN..."
    
    # Try resolving domain to IP
    local resolved_ip=""
    resolved_ip=$(getent hosts "$DOMAIN" | awk '{print $1}' | head -n 1 || echo "")
    
    if [ -z "$resolved_ip" ] && command -v dig &> /dev/null; then
        resolved_ip=$(dig +short "$DOMAIN" | tail -n 1 || echo "")
    fi

    if [ "$resolved_ip" != "$detected_ip" ]; then
        log_warn "DNS mismatch or not propagated yet!"
        log_warn "Domain '$DOMAIN' resolves to: '${resolved_ip:-None}'"
        log_warn "This VPS public IP is: '$detected_ip'"
        log_warn "WARNING: Certbot SSL validation (ssl-init) will fail if DNS does not point here!"
        
        read -p "Do you want to continue anyway? (y/N): " -r response
        if [[ ! "$response" =~ ^[yY]$ ]]; then
            log_error "Deployment cancelled by user due to DNS mismatch."
            exit 1
        fi
    else
        log_success "DNS correctly points to this VPS ($DOMAIN -> $detected_ip)"
    fi
}

check_and_setup_swap() {
    log_info "Checking system memory configuration..."
    
    local total_ram
    total_ram=$(free -m | awk '/^Mem:/{print $2}')
    log_info "Total RAM: ${total_ram}MB"

    local total_swap
    total_swap=$(free -m | awk '/^Swap:/{print $2}')

    if [ "$total_swap" -eq 0 ]; then
        log_warn "No SWAP space active!"
        log_warn "Running multiple Spring Boot, Python, Node, and database containers on low-RAM VPS (< 4GB) without swap can trigger Out of Memory (OOM) crashes."
        
        read -p "Would you like to automatically configure a 2GB SWAP file? (y/N): " -r response
        if [[ "$response" =~ ^[yY]$ ]]; then
            log_info "Creating 2GB swap file..."
            if [ "$EUID" -ne 0 ]; then
                log_info "Requesting sudo privileges for swap configuration..."
                sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
                sudo chmod 600 /swapfile
                sudo mkswap /swapfile
                sudo swapon /swapfile
                echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
            else
                fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
                chmod 600 /swapfile
                mkswap /swapfile
                swapon /swapfile
                echo '/swapfile none swap sw 0 0' >> /etc/fstab
            fi
            log_success "2GB Swap file successfully created and activated!"
        else
            log_warn "Skipping SWAP configuration. Monitor RAM usage carefully."
        fi
    else
        log_success "Swap space is active (${total_swap}MB)."
    fi
}

setup_env_file() {
    local ip="$1"
    local env_file="$DEPLOY_DIR/.env"

    # Use -s to check if file exists AND is not empty
    if [ ! -s "$env_file" ]; then
        log_info "No .env file found or it is empty at $DEPLOY_DIR."
        log_info "Generating a secure, customized .env file with detected IP $ip..."
        
        # Ensure target deployment folder exists
        mkdir -p "$DEPLOY_DIR"
        
        # Call generate-secrets.sh to generate random secure credentials and set detected IP
        if [ -f "./generate-secrets.sh" ]; then
            bash ./generate-secrets.sh "$ip" > "$env_file"
        elif [ -f "$DEPLOY_DIR/generate-secrets.sh" ]; then
            bash "$DEPLOY_DIR/generate-secrets.sh" "$ip" > "$env_file"
        else
            log_warn "generate-secrets.sh not found. Copying .env.example..."
            cp .env.example "$env_file"
            # Update IP in copied .env.example
            sed -i "s/CALIO_SERVER_IP=.*/CALIO_SERVER_IP=$ip/" "$env_file"
        fi
        
        log_success "Generated secure .env file at $env_file"
        
        # Interactively ask for Gemini API Key if possible
        if [ -t 0 ]; then
            read -p "Enter your CALIO_GEMINI_API_KEY (press Enter to skip): " -r gemini_key
            if [ -n "$gemini_key" ]; then
                sed -i "s/CALIO_GEMINI_API_KEY=.*/CALIO_GEMINI_API_KEY=$gemini_key/" "$env_file"
                log_success "Gemini API Key updated."
            fi
        else
            log_warn "Non-interactive shell. Please remember to edit $env_file and add your CALIO_GEMINI_API_KEY."
        fi
    else
        log_info "Existing .env file detected."
        
        # Check if CALIO_SERVER_IP exists in the file at all
        if ! grep -q "^CALIO_SERVER_IP=" "$env_file"; then
            log_info "Adding CALIO_SERVER_IP=$ip to your .env..."
            echo "" >> "$env_file"
            echo "CALIO_SERVER_IP=$ip" >> "$env_file"
            log_success "Added CALIO_SERVER_IP to $env_file"
        else
            # Verify and update CALIO_SERVER_IP if needed
            local current_env_ip
            current_env_ip=$(grep -E "^CALIO_SERVER_IP=" "$env_file" | cut -d'=' -f2 | tr -d '\r' || echo "")
            if [ "$current_env_ip" != "$ip" ]; then
                log_warn "Your .env file has CALIO_SERVER_IP=$current_env_ip but the VPS current public IP is $ip."
                read -p "Would you like to update CALIO_SERVER_IP to $ip in your .env? (Y/n): " -r response
                if [[ ! "$response" =~ ^[nN]$ ]]; then
                    sed -i "s/CALIO_SERVER_IP=.*/CALIO_SERVER_IP=$ip/" "$env_file"
                    log_success "Updated CALIO_SERVER_IP to $ip in $env_file"
                fi
            fi
        fi
    fi
}

# ==========================================
# FIRST RUN: Initial setup
# ==========================================
first_run() {
    log_info "=== CALIO360 First Run Setup ==="
    
    check_prereqs
    check_and_setup_swap
    
    local ip
    ip=$(detect_ip)
    check_dns "$ip"

    # Create required directories
    mkdir -p "$DEPLOY_DIR/infrastructure/certbot/conf"
    mkdir -p "$DEPLOY_DIR/infrastructure/certbot/www"

    # Copy project files
    log_info "Copying project files to $DEPLOY_DIR..."
    rsync -av --exclude='.git' --exclude='node_modules' --exclude='target' . "$DEPLOY_DIR/"

    # Setup the env file with the detected IP
    setup_env_file "$ip"

    # Build and start infrastructure first
    log_info "Starting databases and queue broker..."
    cd "$DEPLOY_DIR"
    docker compose -f docker-compose.prod.yml up -d calio-postgres calio-mongodb calio-rabbitmq

    log_info "Waiting for database/broker services to be healthy..."
    sleep 15

    # Build and start all services
    log_info "Building and starting all microservices (this can take a few minutes)..."
    docker compose -f docker-compose.prod.yml up -d --build

    log_success "=== First Run Complete ==="
    log_info "Next step: Run './deploy.sh ssl-init' to configure SSL Certificates."
}

# ==========================================
# SSL INIT: Obtain initial certificate
# ==========================================
ssl_init() {
    log_info "=== SSL Certificate Setup ==="
    
    check_prereqs
    local ip
    ip=$(detect_ip)
    check_dns "$ip"

    cd "$DEPLOY_DIR"

    # Use init config for nginx first
    log_info "Starting nginx with HTTP-only config..."
    docker compose -f docker-compose.prod.yml exec nginx \
        cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.ssl || true

    # Copy init config
    cp infrastructure/nginx/nginx-init.conf infrastructure/nginx/nginx-active.conf

    docker compose -f docker-compose.prod.yml restart nginx

    # Obtain certificate
    log_info "Obtaining Let's Encrypt SSL certificate for $DOMAIN..."
    docker compose -f docker-compose.prod.yml run --rm certbot certonly \
        --webroot \
        --webroot-path=/var/www/certbot \
        --email "$EMAIL" \
        --agree-tos \
        --no-eff-email \
        -d "$DOMAIN"

    # Restore SSL nginx config
    log_info "Restoring active SSL nginx configuration..."
    docker compose -f docker-compose.prod.yml restart nginx

    log_success "=== SSL Setup Complete ==="
    log_info "Certificate will auto-renew via the certbot sidecar container."
}

# ==========================================
# UPDATE: Deploy latest changes
# ==========================================
update() {
    log_info "=== CALIO360 Update ==="
    
    check_prereqs
    cd "$DEPLOY_DIR"

    # Pull latest code
    log_info "Pulling latest code from git..."
    git pull origin main

    # Update IP in case it changed
    local ip
    ip=$(detect_ip)
    setup_env_file "$ip"

    # Rebuild and restart
    log_info "Rebuilding and restarting updated containers..."
    docker compose -f docker-compose.prod.yml up -d --build

    # Clean up old images
    log_info "Pruning unused docker objects..."
    docker image prune -f

    log_success "=== Update Complete ==="
}

# ==========================================
# STATUS: Check all services
# ==========================================
status() {
    log_info "=== CALIO360 Service Status ==="
    cd "$DEPLOY_DIR"
    docker compose -f docker-compose.prod.yml ps
}

# ==========================================
# SYSTEM CHECK: Diagnosis
# ==========================================
system_check() {
    log_info "=== CALIO360 System Diagnostic ==="
    check_prereqs
    detect_ip
    free -m
    df -h
}

# ==========================================
# MAIN
# ==========================================
case "${1:-help}" in
    first-run)    first_run ;;
    ssl-init)     ssl_init ;;
    update)       update ;;
    status)       status ;;
    check-system) system_check ;;
    *)
        echo "Usage: $0 {first-run|ssl-init|update|status|check-system}"
        echo ""
        echo "Commands:"
        echo "  first-run    - Initial setup on Azure/generic VPS"
        echo "  ssl-init     - Obtain initial SSL certificate"
        echo "  update       - Pull latest changes and rebuild services"
        echo "  status       - Check active Docker container status"
        echo "  check-system - Diagnosticate VPS system specs and prerequisites"
        exit 1
        ;;
esac

