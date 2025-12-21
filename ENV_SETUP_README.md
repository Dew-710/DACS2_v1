# 🔧 Environment Variables Setup

## ✅ Tóm tắt

Dự án có **2 file .env riêng biệt**:
- `Backend/.env` - Backend (Spring Boot)
- `FrontEnd/.env` - Frontend (Next.js)

---

## 📁 Cấu trúc

```
DACS2/
├── Backend/
│   ├── .env                      # ✅ Backend env (gitignored)
│   ├── env.example               # Template
│   ├── start-backend.sh          # Script start với .env
│   └── src/main/resources/
│       ├── application.properties           # Đọc từ env vars
│       └── application-postgres.properties  # Đọc từ env vars
│
└── FrontEnd/
    ├── .env                      # ✅ Frontend env (gitignored)
    └── lib/env.ts                # Utility functions
```

---

## 🔑 Backend Environment Variables

### File: `Backend/.env`

```bash
# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=postgres

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=restaurant
DB_USERNAME=dew_x_phatdev
DB_PASSWORD=123456789

# JWT
JWT_SECRET=restaurant_jwt_secret_key_change_this_in_production
JWT_EXPIRATION=86400000

# PayOS
PAYOS_BASE_URL=https://api.payos.vn
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key

# WebSocket
WEBSOCKET_ALLOWED_ORIGINS=*

# File Upload
MAX_FILE_SIZE=10MB
MAX_REQUEST_SIZE=50MB
```

### Start Backend

```bash
cd Backend

# Option 1: Dùng script (tự động load .env)
./start-backend.sh

# Option 2: Maven trực tiếp (cần export env vars trước)
export $(cat .env | grep -v '^#' | xargs)
./mvnw spring-boot:run
```

---

## 🔑 Frontend Environment Variables

### File: `FrontEnd/.env`

```bash
# Backend API
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Frontend App (MUST be ngrok for PayOS!)
NEXT_PUBLIC_APP_URL=http://localhost:3000

# PayOS URLs (auto-generated)
NEXT_PUBLIC_PAYOS_RETURN_URL=${NEXT_PUBLIC_APP_URL}/payment/success
NEXT_PUBLIC_PAYOS_CANCEL_URL=${NEXT_PUBLIC_APP_URL}/payment/cancel
```

### Start Frontend

```bash
cd FrontEnd
npm run dev
```

---

## 🚀 Quick Start (Full Stack)

### 1. Setup Backend
```bash
cd Backend

# Tạo .env nếu chưa có
cp env.example .env

# Edit với credentials của bạn
nano .env

# Start
./start-backend.sh
```

### 2. Setup Frontend
```bash
cd FrontEnd

# File .env đã có sẵn với default values

# Nếu cần PayOS, update với ngrok URL:
# 1. Start ngrok: ngrok http 3000
# 2. Copy URL và update NEXT_PUBLIC_APP_URL trong .env

# Start
npm run dev
```

### 3. Verify
- Backend: http://localhost:8080
- Frontend: http://localhost:3000
- Swagger: http://localhost:8080/swagger-ui.html

---

## ⚠️ Important Notes

### 1. Git Security
Cả 2 file `.env` đều được **gitignored**:
- ❌ KHÔNG commit `.env` lên git
- ✅ Chỉ commit `env.example` (template)

### 2. PayOS Requirements (Frontend)
PayOS **KHÔNG** hỗ trợ localhost URLs!

**Development:**
```bash
# Start ngrok
ngrok http 3000

# Update FrontEnd/.env
NEXT_PUBLIC_APP_URL=https://abc123.ngrok.io
```

**Production:**
```bash
NEXT_PUBLIC_APP_URL=https://yourdomain.com
```

### 3. Database Setup
Backend cần PostgreSQL đang chạy:
```bash
cd Backend
./start-db.sh  # Start PostgreSQL với Docker
```

---

## 📝 Environment Variables Usage

### Backend (Spring Boot)
Spring Boot tự động load env variables:

```properties
# application.properties
server.port=${SERVER_PORT:8080}
payos.client.id=${PAYOS_CLIENT_ID:}
```

### Frontend (Next.js)
Sử dụng utility functions:

```typescript
import { getApiBaseUrl, getAppUrl } from '@/lib/env';

const apiUrl = getApiBaseUrl();  // → http://localhost:8080
const appUrl = getAppUrl();      // → https://abc123.ngrok.io
```

---

## 🔄 Deployment

### Backend (Production)

**Option 1: Set environment variables**
```bash
export PAYOS_CLIENT_ID=your_prod_client_id
export PAYOS_API_KEY=your_prod_api_key
./mvnw spring-boot:run
```

**Option 2: Use .env file**
```bash
# Create production .env
nano .env

# Start
./start-backend.sh
```

### Frontend (Production)

**Vercel:**
Set trong dashboard: Project Settings → Environment Variables

**Docker:**
```dockerfile
ENV NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com
ENV NEXT_PUBLIC_APP_URL=https://yourdomain.com
```

---

## 🧪 Testing

### Test Backend
```bash
cd Backend
./start-backend.sh

# Check logs for:
# "PayOS Client ID: ✓ Configured"
```

### Test Frontend
```bash
cd FrontEnd
npm run dev

# Browser console:
console.log(process.env.NEXT_PUBLIC_APP_URL);
```

---

## 📚 Related Documentation

- **PAYOS_FIX_COMPLETED.md** - PayOS integration fixes
- **ENV_SETUP_GUIDE.md** - Detailed setup guide
- **QUICK_REFERENCE_ENV.md** - Quick reference

---

## ✅ Checklist

### Backend
- [x] Tạo `Backend/.env`
- [x] Update `application.properties` để đọc env vars
- [x] Update `application-postgres.properties` để đọc env vars
- [x] Thêm `.env` vào `.gitignore`
- [x] Tạo `start-backend.sh` script

### Frontend
- [x] Tạo `FrontEnd/.env`
- [x] Tạo `FrontEnd/lib/env.ts` utilities
- [x] Update components để dùng env utilities
- [x] Thêm `.env` vào `.gitignore`

---

**🎉 Environment setup completed! Backend và Frontend có 2 file .env riêng biệt!**
