# Features Update Summary

## ✨ New Features Added

### 1. Profile Management (Tất cả người dùng: Admin, Staff, Customer)

#### Cập nhật thông tin cá nhân
- **Trang:** `/profile`
- **Chức năng:**
  - Xem và cập nhật họ tên (Full Name)
  - Xem và cập nhật email
  - Xem và cập nhật số điện thoại
  - Hiển thị username (không thể thay đổi)
  - Hiển thị role (vai trò)
- **Thông báo:** Email xác nhận khi cập nhật thành công

#### Đổi mật khẩu
- **Trang:** `/profile` (tab "Đổi mật khẩu")
- **Chức năng:**
  - Yêu cầu nhập mật khẩu hiện tại
  - Nhập mật khẩu mới (tối thiểu 6 ký tự)
  - Xác nhận mật khẩu mới
  - Kiểm tra mật khẩu hiện tại trước khi thay đổi
- **Thông báo:** Email xác nhận và tự động đăng xuất sau khi đổi thành công

### 2. Quên Mật Khẩu (Password Reset)

#### Request đặt lại mật khẩu
- **Trang:** `/forgot-password`
- **Chức năng:**
  - Nhập email để nhận link đặt lại mật khẩu
  - Gửi email với link reset (có token)
  - Link hết hạn sau 1 giờ
  - Token chỉ sử dụng được 1 lần

#### Đặt lại mật khẩu
- **Trang:** `/reset-password?token={token}`
- **Chức năng:**
  - Kiểm tra token hợp lệ
  - Nhập mật khẩu mới
  - Xác nhận mật khẩu mới
  - Tự động chuyển đến trang đăng nhập sau khi thành công

---

## 🔧 Backend Changes

### New Files Created:

1. **DTOs (Request Objects):**
   - `UpdateProfileRequest.java` - Cập nhật thông tin cá nhân
   - `ChangePasswordRequest.java` - Đổi mật khẩu
   - `ForgotPasswordRequest.java` - Yêu cầu quên mật khẩu
   - `ResetPasswordRequest.java` - Đặt lại mật khẩu

2. **Entity:**
   - `PasswordResetToken.java` - Lưu token reset mật khẩu

3. **Repository:**
   - `PasswordResetTokenRepository.java` - Quản lý token

4. **Service:**
   - `EmailService.java` (Interface)
   - `EmailServiceImpl.java` - Gửi email (có thể cấu hình API)

5. **Database Migration:**
   - `V6__create_password_reset_tokens_table.sql` - Tạo bảng token

### Updated Files:

1. **UserService.java** - Thêm các method:
   - `updateProfile()`
   - `changePassword()`
   - `requestPasswordReset()`
   - `resetPassword()`
   - `validateResetToken()`
   - `findByEmail()`

2. **UserServiceImpl.java** - Implementation của các method mới

3. **UserController.java** - Thêm endpoints:
   - `PUT /api/users/profile/{id}`
   - `PUT /api/users/change-password/{id}`
   - `POST /api/users/forgot-password`
   - `POST /api/users/reset-password`
   - `GET /api/users/validate-reset-token`

4. **UserRepository.java** - Thêm method `findByEmail()`

5. **application.properties** - Thêm email configuration

---

## 🎨 Frontend Changes

### New Pages:

1. **Profile Page** - `/profile`
   - Tab-based interface
   - Profile update form
   - Password change form
   - Toast notifications

2. **Forgot Password Page** - `/forgot-password`
   - Email input form
   - Success message
   - Link back to login

3. **Reset Password Page** - `/reset-password`
   - Token validation
   - New password form
   - Auto-redirect to login

### Updated Files:

1. **types.ts** - Thêm interfaces:
   - `UpdateProfileRequest`
   - `ChangePasswordRequest`
   - `ForgotPasswordRequest`
   - `ResetPasswordRequest`

2. **api.ts** - Thêm API functions:
   - `updateProfile()`
   - `changePassword()`
   - `forgotPassword()`
   - `resetPassword()`
   - `validateResetToken()`

3. **login-form.tsx** - Thêm link "Quên mật khẩu?"

---

## 📧 Email Configuration

### Để sử dụng tính năng email:

1. **Cấu hình environment variables:**
   ```bash
   export EMAIL_ENABLED=true
   export EMAIL_API_URL=https://your-email-api.com/send
   export EMAIL_API_TOKEN=your_email_api_token
   export EMAIL_FROM=noreply@restaurant.com
   ```

2. **Hoặc cập nhật application.properties:**
   ```properties
   email.enabled=true
   email.api.url=https://your-email-api.com/send
   email.api.token=your_email_api_token
   email.from=noreply@restaurant.com
   ```

### Email Templates Có Sẵn:

1. **Password Reset Email:**
   - Tiêu đề: "Đặt lại mật khẩu - Restaurant Management System"
   - Nội dung: Link reset với button, hướng dẫn sử dụng
   - Thời hạn: 1 giờ

2. **Profile Update Confirmation:**
   - Tiêu đề: "Thông tin cá nhân đã được cập nhật"
   - Nội dung: Thông báo cập nhật thành công

3. **Password Change Confirmation:**
   - Tiêu đề: "Mật khẩu đã được thay đổi"
   - Nội dung: Cảnh báo bảo mật

### Testing Without Email (Default):

- Khi `email.enabled=false`, hệ thống sẽ log nội dung email thay vì gửi
- Cho phép test toàn bộ flow mà không cần cấu hình email service

---

## 🔐 Security Features

1. **Password Requirements:**
   - Tối thiểu 6 ký tự
   - Cần nhập mật khẩu hiện tại để đổi

2. **Token Security:**
   - UUID random generation
   - Hết hạn sau 1 giờ
   - Chỉ sử dụng được 1 lần
   - Tự động xóa khi user bị xóa (CASCADE)

3. **Email Validation:**
   - Không tiết lộ email có tồn tại hay không (security best practice)

4. **Password Hashing:**
   - Sử dụng BCrypt
   - Salt tự động

---

## 📊 Database Schema

### New Table: `password_reset_tokens`

```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);
```

---

## 🧪 Testing Guide

### 1. Test Profile Update:
```bash
# Login first and get user ID
# Then update profile:
curl -X PUT http://localhost:8080/api/users/profile/1 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyen Van A",
    "email": "nguyenvana@example.com",
    "phone": "0123456789"
  }'
```

### 2. Test Change Password:
```bash
curl -X PUT http://localhost:8080/api/users/change-password/1 \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "old_password",
    "newPassword": "new_password",
    "confirmPassword": "new_password"
  }'
```

### 3. Test Forgot Password:
```bash
curl -X POST http://localhost:8080/api/users/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

### 4. Test Reset Password:
```bash
curl -X POST http://localhost:8080/api/users/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "token_from_email",
    "newPassword": "new_password",
    "confirmPassword": "new_password"
  }'
```

---

## 🚀 Deployment Notes

1. **Database Migration:**
   - Flyway sẽ tự động chạy migration V6
   - Không cần tạo table thủ công

2. **Email Service:**
   - Mặc định disabled (`email.enabled=false`)
   - Cấp token và API sau để enable

3. **Frontend:**
   - Đã thêm các trang mới vào routing
   - Không cần cấu hình thêm

4. **Backward Compatible:**
   - Không ảnh hưởng đến chức năng cũ
   - User cũ có thể tiếp tục sử dụng bình thường

---

## 📝 Next Steps (Để enable email)

1. **Chọn Email Service Provider:**
   - SendGrid
   - Mailgun
   - AWS SES
   - Hoặc custom SMTP service

2. **Lấy API Credentials:**
   - API URL
   - API Token/Key

3. **Cấu hình Environment:**
   ```bash
   export EMAIL_ENABLED=true
   export EMAIL_API_URL=your_api_url
   export EMAIL_API_TOKEN=your_token
   ```

4. **Test Email Flow:**
   - Thử forgot password
   - Check email inbox
   - Verify reset link works

---

## 📚 Documentation

- **Chi tiết cấu hình email:** Xem `EMAIL_SETUP_GUIDE.md`
- **API Documentation:** Swagger UI tại `/swagger-ui.html`

---

## ✅ Checklist

- [x] Backend DTOs created
- [x] Email service implemented
- [x] Password reset token entity
- [x] User service updated
- [x] Controller endpoints added
- [x] Database migration created
- [x] Frontend types added
- [x] API functions added
- [x] Profile page created
- [x] Forgot password page created
- [x] Reset password page created
- [x] Login form updated
- [x] Documentation created

---

**Tất cả chức năng đã hoàn thành và sẵn sàng sử dụng!**

Bạn chỉ cần cấp **EMAIL_API_URL** và **EMAIL_API_TOKEN** để enable email service.





