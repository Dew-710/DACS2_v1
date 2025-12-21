#!/bin/bash

echo "🚀 Starting Restaurant Management System..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java first."
    exit 1
fi

if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Please install Node.js first."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    print_warning "Docker not found. Database will need to be started manually."
fi

print_success "Prerequisites check passed!"

# Start Database
print_status "Starting Database..."
cd BackEnd

if command -v docker &> /dev/null && docker info &> /dev/null; then
    print_status "Starting PostgreSQL with Docker..."
    docker-compose up -d

    # Wait for database to be ready
    print_status "Waiting for database to be ready..."
    sleep 10

    if docker exec restaurant-postgres pg_isready -h localhost -p 5432 -U dew_x_phatdev -d restaurant &> /dev/null; then
        print_success "Database is ready!"
        print_status "Database URL: localhost:5432"
        print_status "PgAdmin: http://localhost:8085"
    else
        print_warning "Database might not be ready yet. You may need to wait longer."
    fi
else
    print_warning "Docker not available. Please start PostgreSQL manually:"
    print_status "  Host: localhost"
    print_status "  Port: 5432"
    print_status "  Database: restaurant"
    print_status "  Username: dew_x_phatdev"
    print_status "  Password: 123456789"
fi

# Start Backend
print_status "Starting Backend (Spring Boot)..."
cd ../BackEnd

# Try to compile first
if ./mvnw compile -q -Dmaven.test.skip=true 2>/dev/null; then
    print_success "Backend compilation successful!"
else
    print_warning "Backend compilation failed. Attempting to run anyway..."
fi

# Start backend in background
./mvnw spring-boot:run -q -Dmaven.test.skip=true > backend.log 2>&1 &
BACKEND_PID=$!

print_success "Backend starting... (PID: $BACKEND_PID)"
print_status "Backend will be available at: http://localhost:8080"
print_status "Check logs: tail -f BackEnd/backend.log"

# Wait a bit for backend to start
sleep 15

# Check if backend is running
if curl -s http://localhost:8080/api/users/list &> /dev/null; then
    print_success "Backend is running!"
else
    print_warning "Backend might still be starting... Check logs for details."
fi

# Start Frontend
print_status "Starting Frontend (Next.js)..."
cd ../FrontEnd

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    print_status "Installing frontend dependencies..."
    if command -v pnpm &> /dev/null; then
        pnpm install --silent
    elif command -v npm &> /dev/null; then
        npm install --silent
    else
        print_error "Neither pnpm nor npm found. Please install package manager."
        exit 1
    fi
fi

# Start frontend in background
if command -v pnpm &> /dev/null; then
    pnpm dev > ../frontend.log 2>&1 &
elif command -v npm &> /dev/null; then
    npm run dev > ../frontend.log 2>&1 &
else
    print_error "No package manager found."
    exit 1
fi

FRONTEND_PID=$!

print_success "Frontend starting... (PID: $FRONTEND_PID)"
print_status "Frontend will be available at: http://localhost:3000"
print_status "Check logs: tail -f frontend.log"

# Wait for frontend to start
sleep 10

print_success "🎉 Restaurant Management System Started!"
echo ""
echo "🌐 Access URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo "   PgAdmin:  http://localhost:8085 (if Docker running)"
echo ""
echo "📊 Database:"
echo "   Host: localhost:5432"
echo "   Database: restaurant"
echo "   Username: dew_x_phatdev"
echo "   Password: 123456789"
echo ""
echo "📝 Useful commands:"
echo "   Stop all: kill $BACKEND_PID $FRONTEND_PID"
echo "   Backend logs: tail -f BackEnd/backend.log"
echo "   Frontend logs: tail -f frontend.log"
echo "   Database logs: docker logs restaurant-postgres"
echo ""
print_warning "Press Ctrl+C to stop all services"

# Wait for services
wait $BACKEND_PID $FRONTEND_PID