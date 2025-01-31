# Stage 1: Build the React app
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm install

# Define build arguments #depends what your project needs
ARG REACT_APP_FIREBASE_SIGN_IN_URL
ARG REACT_APP_FIREBASE_API_KEY
ARG REACT_APP_BACKEND_ADDRESS

# Use build argument in a layer to force rebuild on change
RUN echo "REACT_APP_FIREBASE_SIGN_IN_URL=${REACT_APP_FIREBASE_SIGN_IN_URL}" > .env
RUN echo "REACT_APP_FIREBASE_API_KEY=${REACT_APP_FIREBASE_API_KEY}" > .env
RUN echo "REACT_APP_BACKEND_ADDRESS=${REACT_APP_BACKEND_ADDRESS}" > .env

# Set environment variables for the build
ENV REACT_APP_FIREBASE_SIGN_IN_URL=${REACT_APP_FIREBASE_SIGN_IN_URL}
ENV REACT_APP_FIREBASE_API_KEY=${REACT_APP_FIREBASE_API_KEY}
ENV REACT_APP_BACKEND_ADDRESS=${REACT_APP_BACKEND_ADDRESS}

# Copy the rest of the application code
COPY . .

# Build the React app
RUN npm run build

# Stage 2: Serve the app with Nginx
FROM nginx:alpine

# Copy the build output to nginx's html directory
COPY --from=build /app/build /usr/share/nginx/html

# Copy custom Nginx configuration
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
