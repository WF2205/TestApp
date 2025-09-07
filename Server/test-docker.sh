#!/bin/bash

# Test script for Docker setup
echo "🐳 Testing Docker Setup for TodoList Backend"
echo "=============================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop."
    exit 1
fi

echo "✅ Docker is running"

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp env.example .env
    echo "⚠️  Please update .env file with your GitHub OAuth credentials"
fi

# Build the backend image
echo "🔨 Building backend Docker image..."
if docker-compose build backend; then
    echo "✅ Backend image built successfully"
else
    echo "❌ Failed to build backend image"
    exit 1
fi

# Start services
echo "🚀 Starting all services..."
if docker-compose up -d; then
    echo "✅ Services started successfully"
else
    echo "❌ Failed to start services"
    exit 1
fi

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Test health endpoints
echo "🔍 Testing service health..."

# Test MongoDB
if docker-compose exec -T mongodb mongosh --eval "db.runCommand('ping')" > /dev/null 2>&1; then
    echo "✅ MongoDB is healthy"
else
    echo "❌ MongoDB is not responding"
fi

# Test RabbitMQ
if curl -s http://localhost:15672 > /dev/null; then
    echo "✅ RabbitMQ Management UI is accessible"
else
    echo "❌ RabbitMQ Management UI is not accessible"
fi

# Test Backend
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Backend is healthy"
else
    echo "❌ Backend is not responding"
fi

echo ""
echo "🎉 Docker setup test completed!"
echo ""
echo "📋 Service URLs:"
echo "   Backend API: http://localhost:8080"
echo "   MongoDB: localhost:27017"
echo "   RabbitMQ Management: http://localhost:15672 (guest/guest)"
echo "   Health Check: http://localhost:8080/actuator/health"
echo ""
echo "📝 Useful commands:"
echo "   View logs: docker-compose logs"
echo "   Stop services: docker-compose down"
echo "   Restart: docker-compose restart"
echo "   Clean restart: docker-compose down -v && docker-compose up --build"
