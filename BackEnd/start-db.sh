#!/bin/bash

# Restaurant Management System - Database Startup Script

echo "🐳 Starting PostgreSQL Database with Docker..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL and PgAdmin
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

# Check if PostgreSQL is healthy
if docker exec restaurant-postgres pg_isready -h localhost -p 5432 -U dew_x_phatdev -d restaurant > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready!"
    echo ""
    echo "📊 Database Information:"
    echo "   Host: localhost"
    echo "   Port: 5432"
    echo "   Database: restaurant"
    echo "   Username: dew_x_phatdev"
    echo "   Password: 123456789"
    echo ""
    echo "🌐 PgAdmin Web Interface:"
    echo "   URL: http://localhost:8085"
    echo "   Email: admin@restaurant.com"
    echo "   Password: admin123"
    echo ""
    echo "🚀 You can now start your Spring Boot application:"
    echo "   mvn spring-boot:run"
else
    echo "❌ PostgreSQL failed to start. Check logs:"
    echo "   docker logs restaurant-postgres"
    exit 1
fi
