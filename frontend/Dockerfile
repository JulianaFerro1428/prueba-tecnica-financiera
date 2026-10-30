# Etapa 1: compila Angular a archivos estáticos (HTML, JS y CSS).
FROM node:22-alpine AS build
WORKDIR /app

# Primero solo los archivos de dependencias, para reutilizar la capa de "npm ci" si no cambian.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npx ng build

# Etapa 2: nginx sirve esos archivos y reenvía /api al backend. Node ya no hace falta.
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/bank-frontend/browser /usr/share/nginx/html

EXPOSE 80
