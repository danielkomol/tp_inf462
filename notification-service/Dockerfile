# ============================================
# DOCKERFILE — notification-service (Node.js)
# ============================================

# ETAPE 1 : BUILD
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json .
RUN npm ci --only=production

# ETAPE 2 : RUN
FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/node_modules ./node_modules
COPY . .
EXPOSE 3000
CMD ["node", "src/index.js"]
