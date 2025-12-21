# 🚀 Quick Reference - Environment Setup

## 📁 Có 2 file .env riêng biệt

```
Backend/.env   → Spring Boot (Java)  
FrontEnd/.env  → Next.js (TypeScript)
```

---

## ⚡ Quick Start

### Backend
```bash
cd Backend
./start-backend.sh  # Tự động load .env và start
```

### Frontend  
```bash
cd FrontEnd
npm run dev
```

---

## 🔑 Backend/.env

```bash
# Server
SERVER_PORT=8080

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=restaurant
DB_USERNAME=dew_x_phatdev
DB_PASSWORD=123456789

# PayOS
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
```

---

## 🔑 FrontEnd/.env

```bash
# Backend API
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Frontend (dùng ngrok cho PayOS!)
NEXT_PUBLIC_APP_URL=https://abc123.ngrok.io
```

---

## ⚠️ Important

1. **PayOS cần ngrok URL:**
   ```bash
   ngrok http 3000
   # Copy URL vào FrontEnd/.env
   ```

2. **Sau khi sửa .env, restart:**
   ```bash
   # Backend
   Ctrl+C → ./start-backend.sh
   
   # Frontend
   Ctrl+C → npm run dev
   ```

3. **KHÔNG commit .env lên git!** (đã gitignored)

---

## 📚 Full Docs
**ENV_SETUP_README.md** - Chi tiết đầy đủ
