# ==========================================
# CALIO360 — Notification Service Dockerfile
# Production build for Node.js (Hono)
# ==========================================

FROM node:18-alpine AS production
WORKDIR /app

# Security: use built-in node user
RUN apk add --no-cache curl

COPY package*.json ./
RUN npm ci --omit=dev

COPY src ./src

RUN chown -R node:node /app
USER node

ENV NODE_ENV=production

EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8087/health || exit 1

CMD ["node", "src/index.js"]
