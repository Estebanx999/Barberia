# Imagen del frontend: se compila con Node y la sirve Nginx.

# --- Etapa 1: compilación ---------------------------------------------------
FROM node:20-alpine AS compilacion
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# --- Etapa 2: servidor web --------------------------------------------------
FROM nginx:1.27-alpine

# Dirección del backend a la que Nginx reenvía /api. Se cambia al desplegar en la nube.
ENV BACKEND_URL=http://backend:8080
# Expone el DNS del contenedor como ${NGINX_LOCAL_RESOLVERS} para la plantilla: así Nginx
# resuelve el backend en cada petición y arranca aunque el backend aún no exista.
ENV NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1

# Nginx reemplaza ${BACKEND_URL} en la plantilla al arrancar.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=compilacion /app/dist /usr/share/nginx/html

EXPOSE 80
