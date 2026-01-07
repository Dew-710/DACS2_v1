# 📧 Gmail SMTP Configuration - Summary of Changes

## 🎯 Overview

Hệ thống đã được cấu hình để gửi email forgot password qua Gmail SMTP với các thông số bạn cung cấp:

- **Backend URL**: `https://magazine-tent-done-repository.trycloudflare.com`
- **Frontend URL**: `https://tcp-iowa-favorite-exams.trycloudflare.com`
- **Telegram Bot Chat ID**: `6284223765`

---

## ✅ Changes Made

### 1. Backend Changes

#### A. `pom.xml`
- ✅ Thêm dependency `spring-boot-starter-mail` để hỗ trợ SMTP

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### B. `application.properties`
- ✅ Thêm Gmail SMTP configuration
- ✅ Cập nhật `frontend.url` → `https://tcp-iowa-favorite-exams.trycloudflare.com`
- ✅ Cập nhật `telegram.bot.chat-id` → `6284223765`
- ✅ Cập nhật PayOS return/cancel URLs

**New SMTP Settings:**
```properties
email.smtp.enabled=${EMAIL_SMTP_ENABLED:false}
spring.mail.host=${SPRING_MAIL_HOST:smtp.gmail.com}
spring.mail.port=${SPRING_MAIL_PORT:587}
spring.mail.username=${SPRING_MAIL_USERNAME:}
spring.mail.password=${SPRING_MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### C. `EmailServiceImpl.java`
- ✅ Thêm `JavaMailSender` dependency injection
- ✅ Thêm method `sendEmailViaSMTP()` để gửi email qua SMTP
- ✅ Update `sendEmail()` để support cả REST API và SMTP
- ✅ Tự động chọn SMTP nếu `email.smtp.enabled=true`

**Key Changes:**
```java
@Value("${email.smtp.enabled:false}")
private boolean smtpEnabled;

private final JavaMailSender javaMailSender;

private void sendEmailViaSMTP(String toEmail, String subject, String htmlContent) {
    MimeMessage message = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);
    javaMailSender.send(message);
}
```

### 2. Frontend Changes

#### A. `lib/env.ts`
- ✅ Cập nhật default Backend URL → `https://magazine-tent-done-repository.trycloudflare.com`

```typescript
export const getApiBaseUrl = (): string => {
  return process.env.NEXT_PUBLIC_API_BASE_URL || 
    'https://magazine-tent-done-repository.trycloudflare.com';
};
```

### 3. New Documentation Files

- ✅ `GMAIL_QUICK_START.md` - Quick start guide tổng quan
- ✅ `BackEnd/GMAIL_SMTP_SETUP.md` - Hướng dẫn chi tiết Gmail setup
- ✅ `BackEnd/setup-gmail.ps1` - PowerShell script tự động setup
- ✅ `BackEnd/SETUP_COMMANDS.txt` - Quick reference commands
- ✅ `GMAIL_SETUP_SUMMARY.md` - Document này

---

## 🚀 What You Need To Do

### Step 1: Tạo Gmail App Password (5 phút)

1. **Truy cập Gmail Account Settings:**
   - URL: https://myaccount.google.com/security

2. **Bật 2-Step Verification:**
   - Nếu chưa có, bật tính năng này
   - Follow hướng dẫn của Google

3. **Tạo App Password:**
   - Truy cập: https://myaccount.google.com/apppasswords
   - Chọn app: "Mail"
   - Chọn device: "Other (Custom name)" → đặt tên "Restaurant Backend"
   - Click "Generate"
   - **Lưu lại mật khẩu 16 ký tự** (ví dụ: `abcd efgh ijkl mnop`)
   - ⚠️ QUAN TRỌNG: Lưu lại vì sẽ không hiển thị lại

### Step 2: Setup và Start Backend (2 phút)

#### Option A: Dùng Script Tự Động (Khuyến Nghị) ⭐

```powershell
# Mở PowerShell trong project root
cd BackEnd

# Chạy script setup
.\setup-gmail.ps1
```

Script sẽ tự động:
- Hỏi Gmail address và App Password
- Set tất cả environment variables
- Start backend server

#### Option B: Manual Setup

```powershell
cd BackEnd

# Set environment variables (THAY YOUR_EMAIL và YOUR_APP_PASSWORD)
$env:EMAIL_ENABLED="true"
$env:EMAIL_SMTP_ENABLED="true"
$env:SPRING_MAIL_USERNAME="youremail@gmail.com"
$env:SPRING_MAIL_PASSWORD="abcd efgh ijkl mnop"
$env:EMAIL_FROM="youremail@gmail.com"
$env:FRONTEND_URL="https://tcp-iowa-favorite-exams.trycloudflare.com"
$env:TELEGRAM_BOT_ENABLED="true"
$env:TELEGRAM_BOT_CHAT_ID="6284223765"

# Start backend
.\mvnw.cmd spring-boot:run
```

### Step 3: Test Email Functionality (1 phút)

1. **Truy cập Frontend:**
   - URL: https://tcp-iowa-favorite-exams.trycloudflare.com/login

2. **Test Forgot Password:**
   - Click "Quên mật khẩu?"
   - Nhập email của một user trong hệ thống
   - Submit form

3. **Kiểm tra Email:**
   - Mở inbox của email vừa nhập
   - Kiểm tra cả spam/junk folder
   - Bạn sẽ nhận email với subject: "Đặt lại mật khẩu - Restaurant Management System"

4. **Test Reset Password:**
   - Click link trong email
   - Nhập mật khẩu mới
   - Verify login với mật khẩu mới

---

## 🔍 Verify Setup

### Check Backend Logs

```powershell
# Trong thư mục BackEnd
Get-Content backend-local.log -Wait | Select-String "email|SMTP"
```

**Expected Success Log:**
```
INFO: Email sent successfully via SMTP to: user@example.com
```

### Test với API Direct

```bash
curl -X POST https://magazine-tent-done-repository.trycloudflare.com/api/users/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

**Expected Response:**
```json
{
  "message": "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn."
}
```

---

## 📊 Configuration Summary

### Backend Configuration
```
Host: localhost:8080
Public URL: https://magazine-tent-done-repository.trycloudflare.com
Profiles: postgres
Email Service: Gmail SMTP (smtp.gmail.com:587)
```

### Frontend Configuration
```
Public URL: https://tcp-iowa-favorite-exams.trycloudflare.com
API Base URL: https://magazine-tent-done-repository.trycloudflare.com
```

### Email Configuration
```
Provider: Gmail SMTP
Host: smtp.gmail.com
Port: 587
Security: STARTTLS
Authentication: App Password
Daily Limit: 500 emails
```

### Telegram Bot
```
Token: 8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ
Username: @RestaurantKitchenBot
Chat ID: 6284223765
```

---

## 🎯 Features Enabled

### Email Features:
1. ✅ **Forgot Password** - Gửi link reset password
2. ✅ **Profile Update Confirmation** - Thông báo khi cập nhật thông tin
3. ✅ **Password Change Confirmation** - Thông báo khi đổi mật khẩu

### Security Features:
- ✅ Reset token expires after 1 hour
- ✅ Token can only be used once
- ✅ Secure SMTP with STARTTLS
- ✅ App Password (không dùng mật khẩu chính)

---

## 🐛 Common Issues & Solutions

### Issue 1: "Username and Password not accepted"

**Symptoms:**
```
ERROR: 535-5.7.8 Username and Password not accepted
```

**Solutions:**
1. Verify app password (16 ký tự, không có khoảng trắng)
2. Đảm bảo 2-Step Verification đã bật
3. Tạo app password mới
4. Kiểm tra Gmail address đúng chưa

### Issue 2: "Connection timed out"

**Symptoms:**
```
ERROR: Connection timed out: smtp.gmail.com:587
```

**Solutions:**
1. Kiểm tra firewall/antivirus settings
2. Verify internet connection
3. Thử port 465 thay vì 587:
   ```
   $env:SPRING_MAIL_PORT="465"
   spring.mail.properties.mail.smtp.ssl.enable=true
   ```

### Issue 3: Email không nhận được

**Checklist:**
- [ ] Kiểm tra spam/junk folder
- [ ] Verify email address đúng
- [ ] Check backend logs có lỗi không
- [ ] Test với email khác
- [ ] Kiểm tra Gmail "All Mail" folder

### Issue 4: Backend không start

**Solutions:**
```powershell
# Clean và rebuild
cd BackEnd
.\mvnw.cmd clean install

# Start lại
.\mvnw.cmd spring-boot:run
```

---

## 📚 Additional Resources

### Documentation Files:
1. **`GMAIL_QUICK_START.md`** - Quick start guide
2. **`BackEnd/GMAIL_SMTP_SETUP.md`** - Chi tiết Gmail setup
3. **`BackEnd/SETUP_COMMANDS.txt`** - Quick commands reference
4. **`EMAIL_SETUP_GUIDE.md`** - Tổng quan email service
5. **`EMAIL_PROVIDER_EXAMPLES.md`** - Các providers khác

### API Endpoints:
- `POST /api/users/forgot-password` - Request reset
- `GET /api/users/validate-reset-token` - Validate token
- `POST /api/users/reset-password` - Reset password
- `PUT /api/users/profile/{userId}` - Update profile
- `PUT /api/users/change-password/{userId}` - Change password

### Swagger API Docs:
- URL: http://localhost:8080/swagger-ui.html
- Test all endpoints directly from browser

---

## 📞 Support & Next Steps

### If Everything Works: ✅
- ✅ Backend đang chạy với SMTP enabled
- ✅ Frontend kết nối được backend
- ✅ Forgot password flow hoạt động
- ✅ Emails được gửi thành công
- ✅ Telegram bot nhận notifications
- 🎉 **You're all set!**

### If You Have Issues: ⚠️
1. Check logs: `backend-local.log`
2. Verify environment variables
3. Test Gmail credentials manually
4. Review documentation files
5. Check firewall/antivirus settings

### For Production Deployment: 🚀
- Consider using dedicated email service (SendGrid, AWS SES)
- Set up proper domain for sender email
- Configure SPF/DKIM records
- Monitor email sending rates
- Set up email logs/analytics

---

## ✅ Final Checklist

Setup Complete:
- [ ] Gmail App Password created
- [ ] Environment variables set
- [ ] Backend started successfully
- [ ] Frontend accessible
- [ ] Forgot password tested
- [ ] Email received and link works
- [ ] Password reset successful
- [ ] Telegram bot receiving orders

---

## 🎉 Congratulations!

Hệ thống của bạn đã được cấu hình để gửi email forgot password qua Gmail SMTP!

**Next Steps:**
1. Test thoroughly với nhiều email addresses
2. Monitor logs khi có lỗi
3. Setup monitoring cho email sending
4. Consider scaling options nếu cần gửi nhiều emails

**Good luck with your project! 🚀**

---

*Last Updated: January 5, 2026*
*Configuration Version: 1.0*


