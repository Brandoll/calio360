# ==========================================
# CALIO360 — Analytics Service Dockerfile
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

RUN groupadd -r calio && useradd -r -g calio calio
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=dependencies /install /usr/local

COPY . .
RUN chown -R calio:calio /app
USER calio

EXPOSE 8086

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8086/health || exit 1

CMD ["gunicorn", "--bind", "0.0.0.0:8086", "--workers", "2", "--threads", "4", "--timeout", "120", "app:app"]
