# 🔧 Environment Variables Setup Guide

## ✅ Hoàn thành

Tất cả các URL đã được chuyển sang environment variables để dễ dàng quản lý và deploy.

---

## 📁 Files đã tạo

### 1. `.env.local` (Development)
```bash
FrontEnd/.env.local
```
Dùng cho môi trường development. **ĐÃ TẠO** với default values.

### 2. `.env.production` (Production)
```bash
FrontEnd/.env.production
```
Dùng cho môi trường production. **ĐÃ TẠO** với template.

### 3. `.env.example` (Template)
```bash
FrontEnd/.env.example
```
Template để team members copy và config. **ĐÃ TẠO**.

### 4. `lib/env.ts` (Utility functions)
```bash
FrontEnd/lib/env.ts
```
Centralized utility functions để get URLs. **ĐÃ TẠO**.

---

## 🔑 Environment Variables

### Required Variables

| Variable | Description | Example (Dev) | Example (Prod) |
|----------|-------------|---------------|----------------|
| `NEXT_PUBLIC_API_BASE_URL` | Backend API URL | `http://localhost:8080` | `https://api.yourdomain.com` |
| `NEXT_PUBLIC_APP_URL` | Frontend URL | `https://abc123.ngrok.io` | `https://yourdomain.com` |
| `NEXT_PUBLIC_PAYOS_RETURN_URL` | PayOS success URL | `${NEXT_PUBLIC_APP_URL}/payment/success` | `${NEXT_PUBLIC_APP_URL}/payment/success` |
| `NEXT_PUBLIC_PAYOS_CANCEL_URL` | PayOS cancel URL | `${NEXT_PUBLIC_APP_URL}/payment/cancel` | `${NEXT_PUBLIC_APP_URL}/payment/cancel` |

---

## 🚀 Setup Instructions

### Step 1: Copy template
```bash
cd FrontEnd
cp .env.example .env.local
```

### Step 2: Config for Development

#### Option A: Sử dụng ngrok (RECOMMENDED cho PayOS)
```bash
# 1. Start ngrok
ngrok http 3000

# 2. Copy HTTPS URL (e.g., https://abc123.ngrok.io)

# 3. Update .env.local
NEXT_PUBLIC_APP_URL=https://abc123.ngrok.io
```

#### Option B: Localhost (PayOS sẽ bị lỗi)
```bash
# .env.local
NEXT_PUBLIC_APP_URL=http://localhost:3000
```
⚠️ **Warning:** PayOS **KHÔNG** hỗ trợ localhost URLs!

### Step 3: Start development
```bash
npm run dev
```

---

## 📝 Usage Examples

### 1. Get API Base URL
```typescript
import { getApiBaseUrl } from '@/lib/env';

const apiUrl = getApiBaseUrl();
// Returns: http://localhost:8080 (dev) or https://api.yourdomain.com (prod)

fetch(`${apiUrl}/api/orders`);
```

### 2. Get App URL
```typescript
import { getAppUrl } from '@/lib/env';

const appUrl = getAppUrl();
// Returns: https://abc123.ngrok.io (dev) or https://yourdomain.com (prod)

const qrUrl = `${appUrl}/menu/${qrCode}`;
```

### 3. Get PayOS URLs
```typescript
import { getPayOSReturnUrl, getPayOSCancelUrl } from '@/lib/env';

const returnUrl = getPayOSReturnUrl([123, 456]);
// Returns: https://abc123.ngrok.io/payment/success?orderIds=123,456

const cancelUrl = getPayOSCancelUrl([123, 456]);
// Returns: https://abc123.ngrok.io/payment/cancel?orderIds=123,456
```

### 4. Validate PayOS URLs
```typescript
import { validatePayOSUrls } from '@/lib/env';

const validation = validatePayOSUrls();
if (!validation.valid) {
  validation.warnings.forEach(w => console.warn(w));
}
```

### 5. Log Environment Config
```typescript
import { logEnvConfig } from '@/lib/env';

// Debug - xem tất cả config
logEnvConfig();
```

---

## 🔧 Files Modified

### Backend (No changes needed)
Backend đã config trong `application.properties`:
```properties
payos.base-url=https://api.payos.vn
payos.client.id=...
payos.api.key=...
payos.checksum.key=...
```

### Frontend

#### 1. `lib/env.ts` ✅ **CREATED**
Utility functions để get URLs từ env variables.

#### 2. `components/payos-payment.tsx` ✅ **UPDATED**
```typescript
// Before
const baseUrl = window.location.origin;
const returnUrl = `${baseUrl}/payment/success?orderIds=${orderIds.join(',')}`;

// After
import { getPayOSReturnUrl, getPayOSCancelUrl } from '@/lib/env';
const returnUrl = getPayOSReturnUrl(orderIds);
const cancelUrl = getPayOSCancelUrl(orderIds);
```

#### 3. `app/dashboard/staff/page.tsx` ✅ **UPDATED**
```typescript
// Before
await fetch('http://localhost:8080/api/orders/...');
const url = window.location.origin;

// After
import { getApiBaseUrl, getAppUrl } from '@/lib/env';
const apiUrl = getApiBaseUrl();
await fetch(`${apiUrl}/api/orders/...`);
const url = getAppUrl();
```

#### 4. `lib/api.ts` ✅ **ALREADY USING ENV**
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
```
Đã sử dụng env variable từ trước. ✅ No changes needed.

---

## 🧪 Testing

### 1. Check environment config
```bash
cd FrontEnd
npm run dev
```

Open browser console and run:
```javascript
// In browser console
console.log('API URL:', process.env.NEXT_PUBLIC_API_BASE_URL);
console.log('App URL:', process.env.NEXT_PUBLIC_APP_URL);
```

### 2. Test PayOS URLs validation
Trong component, PayOS sẽ tự động validate và warning nếu dùng localhost:
```
⚠️ App URL is localhost - PayOS will reject this!
   → Set NEXT_PUBLIC_APP_URL in .env.local to your ngrok URL
   → Example: NEXT_PUBLIC_APP_URL=https://abc123.ngrok.io
```

### 3. Verify all URLs
Chạy test script:
```bash
cd /Users/macintosh/DACS2
./test_payos_fix.sh
```

---

## 🌍 Deployment

### Vercel
Vercel tự động load `.env.production` hoặc set variables trong dashboard:

1. Go to: Project Settings → Environment Variables
2. Add:
   - `NEXT_PUBLIC_API_BASE_URL` = `https://api.yourdomain.com`
   - `NEXT_PUBLIC_APP_URL` = `https://yourdomain.com`

### Docker
```dockerfile
# Dockerfile
ENV NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com
ENV NEXT_PUBLIC_APP_URL=https://yourdomain.com
```

### Docker Compose
```yaml
# docker-compose.yml
services:
  frontend:
    environment:
      - NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com
      - NEXT_PUBLIC_APP_URL=https://yourdomain.com
```

---

## ⚠️ Important Notes

### 1. PayOS Requirements
- ❌ **KHÔNG** dùng `localhost` URLs
- ✅ Development: Dùng **ngrok**
- ✅ Production: Dùng **real domain**

### 2. Environment Variables Naming
- `NEXT_PUBLIC_*`: Variables exposed to browser
- Without `NEXT_PUBLIC_`: Server-side only

### 3. Restart Required
Sau khi thay đổi `.env.*` files, **PHẢI restart** dev server:
```bash
# Stop (Ctrl+C)
# Then restart
npm run dev
```

### 4. `.gitignore`
Đã add vào `.gitignore`:
```
.env.local
.env.production.local
```
→ Không commit sensitive data lên git!

---

## 🆘 Troubleshooting

### Issue 1: PayOS vẫn báo localhost
**Solution:**
1. Check `.env.local` có config đúng không
2. Restart dev server
3. Clear browser cache
4. Verify trong console: `console.log(process.env.NEXT_PUBLIC_APP_URL)`

### Issue 2: API calls bị CORS
**Solution:**
Backend cần config CORS cho ngrok domain:
```java
// Backend: WebConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:3000",
            "https://*.ngrok.io",  // Add this
            "https://yourdomain.com"
        )
        .allowedMethods("*");
}
```

### Issue 3: Environment variables không load
**Solution:**
```bash
# Verify file exists
ls -la FrontEnd/.env.local

# Check syntax
cat FrontEnd/.env.local

# Restart dev server
npm run dev
```

---

## 📋 Checklist

- [x] Tạo `.env.local` với default values
- [x] Tạo `.env.production` template
- [x] Tạo `.env.example` cho team
- [x] Tạo `lib/env.ts` utility functions
- [x] Update `components/payos-payment.tsx`
- [x] Update `app/dashboard/staff/page.tsx`
- [x] Verify `lib/api.ts` đã dùng env
- [x] Add validation & warnings
- [x] Create documentation

---

## 🎉 Summary

✅ **Tất cả URLs đã được chuyển sang environment variables!**

**Benefits:**
1. ✅ Dễ config cho dev/staging/production
2. ✅ Không hardcode URLs trong code
3. ✅ Centralized configuration
4. ✅ Type-safe với utility functions
5. ✅ Auto-validation cho PayOS URLs
6. ✅ Better security (không commit credentials)

**Next Steps:**
1. Config `.env.local` với ngrok URL của bạn
2. Restart dev server
3. Test PayOS payment flow
4. Deploy với proper production URLs

---

## 📚 Related Files

- ✅ `FrontEnd/.env.local` - Development config
- ✅ `FrontEnd/.env.production` - Production config
- ✅ `FrontEnd/.env.example` - Template
- ✅ `FrontEnd/lib/env.ts` - Utility functions
- ✅ `FrontEnd/components/payos-payment.tsx` - Updated
- ✅ `FrontEnd/app/dashboard/staff/page.tsx` - Updated
- ✅ `FrontEnd/lib/api.ts` - Already using env

---

**🔗 See also:**
- `PAYOS_FIX_COMPLETED.md` - PayOS 400 error fixes
- `Backend/PAYOS_SETUP.md` - Backend PayOS setup
