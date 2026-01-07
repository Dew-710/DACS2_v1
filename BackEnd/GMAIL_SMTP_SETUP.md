# Hướng Dẫn Cấu Hình Gmail SMTP cho Forgot Password

## ⚠️ QUAN TRỌNG: Đã Cập Nhật Cấu Hình

Hệ thống đã được cập nhật để hỗ trợ gửi email qua Gmail SMTP cho chức năng forgot password.

## 🔧 Thông Số Đã Cập Nhật

### URLs Cloudflare Tunnel
- **Backend URL**: `https://magazine-tent-done-repository.trycloudflare.com`
- **Frontend URL**: `https://tcp-iowa-favorite-exams.trycloudflare.com`

### Telegram Bot
- **Chat ID**: `6284223765`

## 📋 Bước 1: Tạo App Password cho Gmail

### Yêu Cầu:
1. Bạn phải có tài khoản Gmail
2. Bật xác thực 2 bước (2-Factor Authentication)

### Các Bước:

1. **Truy cập Google Account Security**:
   - Đi tới: https://myaccount.google.com/security
   - Đăng nhập bằng Gmail của bạn

2. **Bật 2-Step Verification** (nếu chưa bật):
   - Tìm "2-Step Verification"
   - Click "Get Started" và làm theo hướng dẫn

3. **Tạo App Password**:
   - Truy cập: https://myaccount.google.com/apppasswords
   - Hoặc tìm "App passwords" trong Security settings
   - Chọn "Mail" và "Other (Custom name)"
   - Đặt tên: "Restaurant Backend"
   - Click "Generate"
   - **Lưu lại mật khẩu 16 ký tự** (dạng: xxxx xxxx xxxx xxxx)

## 📋 Bước 2: Cấu Hình Environment Variables

### Option A: Windows (PowerShell)

Mở PowerShell và chạy các lệnh sau (thay YOUR_EMAIL và YOUR_APP_PASSWORD):

```powershell
# Navigate to backend directory
cd BackEnd

# Set environment variables
$env:EMAIL_ENABLED="true"
$env:EMAIL_SMTP_ENABLED="true"
$env:SPRING_MAIL_HOST="smtp.gmail.com"
$env:SPRING_MAIL_PORT="587"
$env:SPRING_MAIL_USERNAME="your_email@gmail.com"
$env:SPRING_MAIL_PASSWORD="xxxx xxxx xxxx xxxx"
$env:EMAIL_FROM="your_email@gmail.com"
$env:FRONTEND_URL="https://tcp-iowa-favorite-exams.trycloudflare.com"
$env:TELEGRAM_BOT_CHAT_ID="6284223765"
$env:TELEGRAM_BOT_ENABLED="true"
```

### Option B: Tạo File `.env`

Tạo file `.env` trong thư mục `BackEnd` với nội dung:

```env
# Email Configuration
EMAIL_ENABLED=true
EMAIL_SMTP_ENABLED=true
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=xxxx xxxx xxxx xxxx
EMAIL_FROM=your_email@gmail.com

# Frontend URL
FRONTEND_URL=https://tcp-iowa-favorite-exams.trycloudflare.com

# Telegram
TELEGRAM_BOT_ENABLED=true
TELEGRAM_BOT_CHAT_ID=6284223765
```

**⚠️ QUAN TRỌNG**: Thay thế:
- `your_email@gmail.com` → Gmail address của bạn
- `xxxx xxxx xxxx xxxx` → App Password bạn vừa tạo (16 ký tự)

## 📋 Bước 3: Restart Backend

### Windows:

```powershell
# Stop backend nếu đang chạy (Ctrl+C)

# Start lại backend
cd BackEnd
.\mvnw.cmd spring-boot:run
```

### Linux/Mac:

```bash
# Stop backend nếu đang chạy (Ctrl+C)

# Start lại backend
cd BackEnd
./mvnw spring-boot:run
```

## ✅ Bước 4: Test Email Functionality

### Test 1: Từ Frontend

1. Truy cập: https://tcp-iowa-favorite-exams.trycloudflare.com/login
2. Click "Quên mật khẩu?"
3. Nhập email của một user có trong hệ thống
4. Kiểm tra email inbox (có thể check cả spam folder)

### Test 2: Từ API (Postman/curl)

```bash
curl -X POST https://magazine-tent-done-repository.trycloudflare.com/api/users/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

## 🔍 Kiểm Tra Logs

Xem backend logs để kiểm tra:

```bash
# Trong thư mục BackEnd
tail -f backend-local.log | grep -i email
```

Logs thành công sẽ hiển thị:
```
INFO: Email sent successfully via SMTP to: user@example.com
```

## 🐛 Troubleshooting

### Lỗi: "535-5.7.8 Username and Password not accepted"

**Nguyên nhân**: App password sai hoặc 2FA chưa bật

**Giải pháp**:
1. Kiểm tra lại app password (16 ký tự, không có khoảng trắng khi nhập vào environment variable)
2. Đảm bảo 2-Step Verification đã bật
3. Tạo app password mới

### Lỗi: "Connection timeout"

**Nguyên nhân**: Port 587 bị firewall block

**Giải pháp**:
1. Kiểm tra firewall/antivirus
2. Thử port 465 (SSL):
   ```
   SPRING_MAIL_PORT=465
   spring.mail.properties.mail.smtp.ssl.enable=true
   ```

### Email không nhận được

**Kiểm tra**:
1. Spam folder
2. Gmail "All Mail" folder
3. Backend logs có lỗi không
4. Email address có đúng không

### Email bị Gmail block

**Giải pháp**:
1. Kiểm tra https://myaccount.google.com/notifications
2. Xác nhận activity nếu có cảnh báo
3. Đảm bảo "Less secure app access" không cần thiết với app password

## 📊 Các Tính Năng Email

Hệ thống sẽ gửi email cho:

1. **Password Reset** (Đặt lại mật khẩu)
   - Link reset có hiệu lực 1 giờ
   - Token chỉ dùng được 1 lần

2. **Profile Update Confirmation** (Xác nhận cập nhật thông tin)
   - Khi user thay đổi email, tên, số điện thoại

3. **Password Change Confirmation** (Xác nhận đổi mật khẩu)
   - Khi user đổi mật khẩu thành công

## 🔐 Bảo Mật

- ✅ App password được sử dụng (không dùng mật khẩu chính)
- ✅ STARTTLS enabled (mã hóa kết nối)
- ✅ Token reset password hết hạn sau 1 giờ
- ✅ Token chỉ dùng được 1 lần
- ✅ Email không được hardcode trong code

## 📝 Giới Hạn Gmail

- **Giới hạn gửi**: 500 emails/ngày (free Gmail account)
- **Giới hạn**: 100 recipients/email
- Phù hợp cho: Development, testing, và small-scale production

## 🚀 Cho Production Scale Lớn Hơn

Nếu cần gửi nhiều email hơn, xem xét:
- SendGrid (100 emails/day free)
- AWS SES ($0.10/1000 emails)
- Mailgun (5000 emails/month)

Xem chi tiết: `EMAIL_PROVIDER_EXAMPLES.md`

## ✨ Quick Start Command (Windows)

Copy và chạy lệnh này (remember to replace YOUR_EMAIL and YOUR_APP_PASSWORD):

```powershell
cd BackEnd
$env:EMAIL_ENABLED="true"; $env:EMAIL_SMTP_ENABLED="true"; $env:SPRING_MAIL_USERNAME="your_email@gmail.com"; $env:SPRING_MAIL_PASSWORD="your_app_password"; $env:EMAIL_FROM="your_email@gmail.com"; $env:FRONTEND_URL="https://tcp-iowa-favorite-exams.trycloudflare.com"; $env:TELEGRAM_BOT_CHAT_ID="6284223765"; .\mvnw.cmd spring-boot:run
```

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra backend logs: `backend-local.log`
2. Kiểm tra environment variables đã set đúng chưa
3. Test Gmail SMTP manually với telnet/openssl
4. Xem EMAIL_SETUP_GUIDE.md và EMAIL_PROVIDER_EXAMPLES.md

---

**Chúc bạn cấu hình thành công! 🎉**


