# ==========================================
# CALIO360 — AI Service Dockerfile
# Multi-stage build for production
# ==========================================

# Stage 1: Dependencies
FROM python:3.12-slim AS dependencies
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Production
FROM python:3.12-slim AS production
WORKDIR /app

# Security: non-root user
RUN groupadd -r calio && useradd -r -g calio calio

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Copy installed packages
COPY --from=dependencies /install /usr/local

COPY . .
RUN chown -R calio:calio /app
USER calio

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8082/health || exit 1

CMD ["gunicorn", "--bind", "0.0.0.0:8082", "--workers", "2", "--threads", "4", "--timeout", "120", "app:app"]
