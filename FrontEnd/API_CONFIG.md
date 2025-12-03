# Backend API Configuration

The frontend expects the backend to be running on `http://localhost:8080`.

## To change the backend URL:

1. Create a `.env.local` file in the frontend root directory
2. Add the following line:
   ```
   NEXT_PUBLIC_API_BASE_URL=http://your-backend-url:port
   ```

## Current Configuration:
- **Default URL**: `http://localhost:8080`
- **Environment Variable**: `NEXT_PUBLIC_API_BASE_URL`

## Backend Startup Instructions:

1. **Start PostgreSQL Database:**
   ```bash
   cd BackEnd
   ./start-db.sh
   ```

2. **Start Spring Boot Backend:**
   ```bash
   cd BackEnd
   mvn spring-boot:run
   ```

3. **Verify Backend is Running:**
   ```bash
   curl http://localhost:8080/api/users/list
   ```
