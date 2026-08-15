# Stage 1: Build React/Vite application
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

# VITE_API_URL is supplied at build time by the CD workflow
# (--build-arg VITE_API_URL="${{ vars.VITE_API_URL }}") and baked into
# the bundle by Vite (import.meta.env.VITE_API_URL in src/api/client.ts).
# It must point at the existing devlaunch-backend Container App URL.
# Left empty for local development and CI validation builds.
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build


# Stage 2: Serve with Nginx
FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]