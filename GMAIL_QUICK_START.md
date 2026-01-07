# 📧 Gmail SMTP Setup - Quick Start Guide

## ✅ Các Thay Đổi Đã Được Thực Hiện

### 1. **Backend Updates**
- ✅ Thêm Spring Boot Mail dependency vào `pom.xml`
- ✅ Cập nhật `EmailServiceImpl.java` để hỗ trợ Gmail SMTP
- ✅ Cập nhật `application.properties` với SMTP configuration
- ✅ Cập nhật Frontend URL: `https://tcp-iowa-favorite-exams.trycloudflare.com`
- ✅ Cập nhật Telegram Chat ID: `6284223765`

### 2. **Frontend Updates**
- ✅ Cập nhật Backend URL trong `lib/env.ts`: `https://magazine-tent-done-repository.trycloudflare.com`

### 3. **New Files Created**
- ✅ `BackEnd/GMAIL_SMTP_SETUP.md` - Hướng dẫn chi tiết
- ✅ `BackEnd/setup-gmail.ps1` - Script tự động setup (Windows)
- ✅ `GMAIL_QUICK_START.md` - Guide nhanh này

---

## 🚀 Cách Setup Nhanh (3 Bước)

### Bước 1: Tạo Gmail App Password

1. **Truy cập**: https://myaccount.google.com/apppasswords
2. **Bật 2-Step Verification** nếu chưa có
3. **Tạo App Password**:
   - Chọn app: "Mail"
   - Chọn device: "Other (Custom name)" → đặt tên "Restaurant Backend"
   - Click "Generate"
   - **Lưu lại mật khẩu 16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)

### Bước 2: Setup Backend

#### Option A: Dùng Script Tự Động (Khuyến Nghị)

```powershell
# Mở PowerShell trong thư mục BackEnd
cd BackEnd

# Chạy script setup
.\setup-gmail.ps1
```

Script sẽ hỏi:
- Gmail address của bạn
- App password vừa tạo
- Sau đó tự động start backend

#### Option B: Setup Thủ Công

```powershell
# Mở PowerShell
cd BackEnd

# Set environment variables (thay YOUR_EMAIL và YOUR_APP_PASSWORD)
$env:EMAIL_ENABLED="true"
$env:EMAIL_SMTP_ENABLED="true"
$env:SPRING_MAIL_USERNAME="your_email@gmail.com"
$env:SPRING_MAIL_PASSWORD="abcd efgh ijkl mnop"
$env:EMAIL_FROM="your_email@gmail.com"
$env:FRONTEND_URL="https://tcp-iowa-favorite-exams.trycloudflare.com"
$env:TELEGRAM_BOT_CHAT_ID="6284223765"
$env:TELEGRAM_BOT_ENABLED="true"

# Start backend
.\mvnw.cmd spring-boot:run
```

### Bước 3: Test Email

1. **Truy cập Frontend**: https://tcp-iowa-favorite-exams.trycloudflare.com/login
2. **Click**: "Quên mật khẩu?"
3. **Nhập email** của một user trong hệ thống
4. **Kiểm tra inbox** (có thể trong spam folder)

---

## 📋 Thông Số Cấu Hình

### URLs
- **Backend**: `https://magazine-tent-done-repository.trycloudflare.com`
- **Frontend**: `https://tcp-iowa-favorite-exams.trycloudflare.com`

### Gmail SMTP
- **Host**: `smtp.gmail.com`
- **Port**: `587` (STARTTLS)
- **Username**: Your Gmail address
- **Password**: 16-character App Password
- **Authentication**: Required
- **STARTTLS**: Enabled

### Telegram Bot
- **Token**: `8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ`
- **Username**: `RestaurantKitchenBot`
- **Chat ID**: `6284223765`

---

## 🔍 Kiểm Tra Backend Logs

```powershell
# Trong thư mục BackEnd
Get-Content backend-local.log -Wait | Select-String "email|SMTP"
```

### Log Thành Công:
```
INFO: Email sent successfully via SMTP to: user@example.com
```

### Log Lỗi Thường Gặp:

#### Lỗi 1: "Username and Password not accepted"
```
ERROR: Error sending email via SMTP to user@example.com: 535-5.7.8 Username and Password not accepted
```
**Giải pháp**:
- Kiểm tra lại app password (16 ký tự, không có khoảng trắng)
- Đảm bảo 2-Step Verification đã bật
- Tạo app password mới

#### Lỗi 2: "Connection timed out"
```
ERROR: Connection timed out: smtp.gmail.com:587
```
**Giải pháp**:
- Kiểm tra firewall/antivirus
- Đảm bảo internet connection ổn định
- Thử port 465 nếu 587 bị block

---

## 📧 Email Templates

Hệ thống gửi 3 loại email:

### 1. Password Reset (Đặt lại mật khẩu)
- Subject: "Đặt lại mật khẩu - Restaurant Management System"
- Chứa link reset với token
- Link hết hạn sau 1 giờ
- Token chỉ dùng được 1 lần

### 2. Profile Update (Cập nhật thông tin)
- Subject: "Thông tin cá nhân đã được cập nhật"
- Thông báo khi user thay đổi thông tin cá nhân

### 3. Password Change (Thay đổi mật khẩu)
- Subject: "Mật khẩu đã được thay đổi"
- Thông báo khi user đổi mật khẩu thành công

---

## 🧪 Test API Endpoints

### Test Password Reset Request
```bash
curl -X POST https://magazine-tent-done-repository.trycloudflare.com/api/users/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

### Response (Success):
```json
{
  "message": "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn."
}
```

### Test Token Validation
```bash
curl -X GET "https://magazine-tent-done-repository.trycloudflare.com/api/users/validate-reset-token?token=YOUR_TOKEN"
```

### Test Password Reset
```bash
curl -X POST https://magazine-tent-done-repository.trycloudflare.com/api/users/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_TOKEN",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }'
```

---

## 🔧 Troubleshooting

### 1. Email Không Gửi Được

**Kiểm tra environment variables:**
```powershell
echo $env:EMAIL_ENABLED
echo $env:EMAIL_SMTP_ENABLED
echo $env:SPRING_MAIL_USERNAME
```

**Kiểm tra logs:**
```powershell
Get-Content backend-local.log -Tail 50 | Select-String "email"
```

### 2. Email Không Nhận Được

- ✅ Kiểm tra spam/junk folder
- ✅ Kiểm tra email address đúng chưa
- ✅ Kiểm tra backend logs có lỗi không
- ✅ Test với email khác

### 3. Token Invalid/Expired

- Token hết hạn sau 1 giờ
- Token chỉ dùng được 1 lần
- Request password reset mới nếu hết hạn

### 4. Backend Không Start

```powershell
# Clean và rebuild
cd BackEnd
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

---

## 📊 Gmail Limits

- **Daily Limit**: 500 emails/day (free Gmail account)
- **Recipients**: 100 per email
- **Attachment Size**: 25 MB
- **Suitable For**: Development, testing, small-scale production

### Nếu Cần Gửi Nhiều Email Hơn:
- **SendGrid**: 100 emails/day free
- **AWS SES**: $0.10/1000 emails
- **Mailgun**: 5,000 emails/month free (3 months)

Xem chi tiết: `EMAIL_PROVIDER_EXAMPLES.md`

---

## 📖 Tài Liệu Chi Tiết

- **`BackEnd/GMAIL_SMTP_SETUP.md`** - Hướng dẫn chi tiết Gmail setup
- **`EMAIL_SETUP_GUIDE.md`** - Hướng dẫn tổng quan email service
- **`EMAIL_PROVIDER_EXAMPLES.md`** - Các email provider khác

---

## ✅ Checklist Hoàn Thành

- [ ] Tạo Gmail App Password
- [ ] Set environment variables
- [ ] Start backend với SMTP enabled
- [ ] Test forgot password từ frontend
- [ ] Kiểm tra email inbox
- [ ] Verify email link hoạt động
- [ ] Test reset password flow

---

## 🎉 Hoàn Thành!

Sau khi setup xong:

1. **Backend** sẽ chạy tại: `http://localhost:8080`
2. **Frontend** truy cập: `https://tcp-iowa-favorite-exams.trycloudflare.com`
3. **Swagger API Docs**: `http://localhost:8080/swagger-ui.html`
4. **Forgot Password** hoạt động với Gmail SMTP
5. **Telegram Bot** nhận thông báo orders

---

## 🆘 Cần Hỗ Trợ?

Nếu gặp vấn đề:

1. Kiểm tra `backend-local.log`
2. Xem `BackEnd/GMAIL_SMTP_SETUP.md`
3. Kiểm tra Gmail App Password settings
4. Verify environment variables
5. Test với email khác

**Good luck! 🚀**


