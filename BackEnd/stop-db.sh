#!/bin/bash

# Restaurant Management System - Database Stop Script

echo "🛑 Stopping PostgreSQL Database..."

# Stop all services
docker-compose down

echo "✅ Database stopped successfully!"

# Optional: Remove volumes (uncomment if you want to delete all data)
# echo "🗑️  Removing volumes..."
# docker volume rm backend_postgres_data 2>/dev/null || true

echo ""
echo "💡 To start database again: ./start-db.sh"
