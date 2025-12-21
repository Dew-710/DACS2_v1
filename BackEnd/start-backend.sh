#!/bin/bash

# Script để load .env và start Backend
# Usage: ./start-backend.sh

echo "🚀 Starting Backend with environment variables..."

# Check if .env exists
if [ ! -f ".env" ]; then
    echo "❌ .env file not found!"
    echo "📝 Creating .env from env.example..."
    
    if [ -f "env.example" ]; then
        cp env.example .env
        echo "✅ Created .env file"
        echo "⚠️  Please update .env with your actual credentials!"
    else
        echo "❌ env.example not found either!"
        exit 1
    fi
fi

# Load .env file
echo "📄 Loading environment variables from .env..."
export $(cat .env | grep -v '^#' | xargs)

# Verify critical env vars
echo ""
echo "🔍 Verifying configuration..."
echo "  Server Port: ${SERVER_PORT:-8080}"
echo "  Database: ${DB_NAME:-restaurant}"
echo "  PayOS Client ID: ${PAYOS_CLIENT_ID:0:20}..." # Show first 20 chars only

echo ""
echo "🔨 Building and starting Spring Boot..."
./mvnw spring-boot:run
