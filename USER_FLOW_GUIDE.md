# User Flow Guide

## 🔄 Quy trình sử dụng các tính năng mới

---

## 1. Cập nhật Thông Tin Cá Nhân

```
┌─────────────────────────────────────────────────────────────┐
│                   PROFILE UPDATE FLOW                        │
└─────────────────────────────────────────────────────────────┘

User đăng nhập
      │
      ↓
Vào trang /profile
      │
      ↓
Click tab "Thông tin cá nhân"
      │
      ↓
Cập nhật:
  • Họ và tên
  • Email
  • Số điện thoại
      │
      ↓
Click "Cập nhật thông tin"
      │
      ↓
Backend validate:
  • Check email có bị trùng không
  • Cập nhật database
  • Gửi email xác nhận (nếu email enabled)
      │
      ↓
Hiển thị thông báo thành công
      │
      ↓
Tự động reload page
      │
      ↓
✓ Thông tin đã được cập nhật
```

---

## 2. Đổi Mật Khẩu

```
┌─────────────────────────────────────────────────────────────┐
│                   CHANGE PASSWORD FLOW                       │
└─────────────────────────────────────────────────────────────┘

User đăng nhập
      │
      ↓
Vào trang /profile
      │
      ↓
Click tab "Đổi mật khẩu"
      │
      ↓
Nhập:
  • Mật khẩu hiện tại
  • Mật khẩu mới (≥6 ký tự)
  • Xác nhận mật khẩu mới
      │
      ↓
Click "Đổi mật khẩu"
      │
      ↓
Backend validate:
  • Check mật khẩu hiện tại đúng không
  • Check mật khẩu mới khớp với xác nhận
  • Check độ dài mật khẩu (≥6)
  • Hash mật khẩu mới với BCrypt
  • Cập nhật database
  • Gửi email xác nhận (nếu email enabled)
      │
      ↓
Hiển thị thông báo thành công
      │
      ↓
Tự động logout sau 2 giây
      │
      ↓
Chuyển đến trang /login
      │
      ↓
✓ Đăng nhập lại với mật khẩu mới
```

---

## 3. Quên Mật Khẩu - Reset Password

```
┌─────────────────────────────────────────────────────────────┐
│                  FORGOT PASSWORD FLOW                        │
└─────────────────────────────────────────────────────────────┘

User ở trang login
      │
      ↓
Click "Quên mật khẩu?"
      │
      ↓
Chuyển đến /forgot-password
      │
      ↓
Nhập email
      │
      ↓
Click "Gửi link đặt lại mật khẩu"
      │
      ↓
Backend:
  • Tìm user theo email
  • Xóa các token cũ của user
  • Tạo token mới (UUID)
  • Lưu token vào database
    - token
    - user_id
    - expiry_date (now + 1 hour)
    - used = false
  • Gửi email với link:
    https://frontend.com/reset-password?token={token}
      │
      ↓
Hiển thị thông báo thành công
      │
      ↓
User check email
      │
      ↓
Click link trong email
      │
      ↓
┌─────────────────────────────────────────────────────────────┐
│                   RESET PASSWORD PAGE                        │
└─────────────────────────────────────────────────────────────┘

Load /reset-password?token={token}
      │
      ↓
Frontend validate token:
  • Call API GET /api/users/validate-reset-token
  • Check token valid và chưa expired
      │
      ├─── Token INVALID ───┐
      │                      │
      ↓                      ↓
Token VALID          Hiển thị lỗi
      │              Link yêu cầu reset mới
      ↓
Hiển thị form nhập mật khẩu mới
      │
      ↓
User nhập:
  • Mật khẩu mới (≥6 ký tự)
  • Xác nhận mật khẩu mới
      │
      ↓
Click "Đặt lại mật khẩu"
      │
      ↓
Backend:
  • Validate token (check used=false, not expired)
  • Validate password match
  • Hash password mới
  • Cập nhật user password
  • Mark token as used=true
  • Gửi email xác nhận
      │
      ↓
Hiển thị thông báo thành công
      │
      ↓
Tự động chuyển đến /login sau 3 giây
      │
      ↓
✓ Đăng nhập với mật khẩu mới
```

---

## 🎭 Role-Based Access

### ADMIN
- ✓ Cập nhật thông tin cá nhân
- ✓ Đổi mật khẩu
- ✓ Quên mật khẩu / Reset
- ✓ Xem/Quản lý user khác (existing feature)

### STAFF
- ✓ Cập nhật thông tin cá nhân
- ✓ Đổi mật khẩu
- ✓ Quên mật khẩu / Reset

### CUSTOMER
- ✓ Cập nhật thông tin cá nhân
- ✓ Đổi mật khẩu
- ✓ Quên mật khẩu / Reset

**→ Tất cả vai trò đều có quyền quản lý thông tin cá nhân của mình**

---

## 📧 Email Flow (When Enabled)

```
┌─────────────────────────────────────────────────────────────┐
│                      EMAIL SERVICE FLOW                      │
└─────────────────────────────────────────────────────────────┘

Action trigger (profile update / password change / reset request)
      │
      ↓
EmailService.sendEmail()
      │
      ├─── EMAIL_ENABLED = false ───┐
      │                              │
      ↓                              ↓
EMAIL_ENABLED = true          Log to console
      │                       (for testing)
      ↓
Check EMAIL_API_URL configured?
      │
      ├─── NO ───┐
      │          │
      ↓          ↓
      YES      Log warning
      │        Return
      ↓
Build HTTP Request:
  • Headers:
    - Content-Type: application/json
    - Authorization: Bearer {EMAIL_API_TOKEN}
  • Body:
    - to: recipient email
    - from: EMAIL_FROM
    - subject: email subject
    - html: HTML content
      │
      ↓
POST to EMAIL_API_URL
      │
      ├─── Success (2xx) ───┐
      │                      │
      ↓                      ↓
   Failure              Log success
      │
      ↓
   Log error
   (but don't fail main operation)
```

---

## 🔒 Security Checks

### Profile Update
```
✓ User phải đăng nhập
✓ User chỉ update được profile của chính mình
✓ Email mới không được trùng với user khác
✓ Validate email format
```

### Password Change
```
✓ User phải đăng nhập
✓ Phải nhập đúng mật khẩu hiện tại
✓ Mật khẩu mới ≥ 6 ký tự
✓ Mật khẩu mới phải khớp với xác nhận
✓ Hash với BCrypt (10 rounds)
```

### Password Reset
```
✓ Token phải unique (UUID)
✓ Token expire sau 1 giờ
✓ Token chỉ dùng được 1 lần
✓ Token bị xóa khi user bị xóa (CASCADE)
✓ Không tiết lộ email có tồn tại hay không
```

---

## 📱 UI/UX Features

### Profile Page (`/profile`)
- **Layout:** Tab-based (Thông tin cá nhân | Đổi mật khẩu)
- **Validation:** Real-time form validation
- **Feedback:** Toast notifications
- **Loading:** Loading spinner khi submit
- **Accessibility:** Proper labels và ARIA attributes

### Forgot Password Page (`/forgot-password`)
- **Simple:** Chỉ nhập email
- **Clear messaging:** Hướng dẫn từng bước
- **Security:** Không tiết lộ email tồn tại hay không
- **Retry:** Có nút gửi lại

### Reset Password Page (`/reset-password`)
- **Token validation:** Kiểm tra trước khi hiển thị form
- **Loading state:** Hiển thị khi đang validate token
- **Error handling:** Clear error messages
- **Auto-redirect:** Tự động chuyển về login sau success

---

## 🧪 Test Scenarios

### Happy Path - Profile Update
```
1. Login as any user
2. Go to /profile
3. Update full name, email, phone
4. Click save
5. ✓ See success message
6. ✓ Page reloads with new data
7. ✓ (If email enabled) Receive confirmation email
```

### Happy Path - Change Password
```
1. Login as any user
2. Go to /profile → "Đổi mật khẩu" tab
3. Enter current password
4. Enter new password (≥6 chars)
5. Confirm new password
6. Click save
7. ✓ See success message
8. ✓ Auto logout after 2s
9. ✓ Login with new password works
10. ✓ (If email enabled) Receive confirmation email
```

### Happy Path - Forgot Password
```
1. Go to /login
2. Click "Quên mật khẩu?"
3. Enter valid email
4. Click send
5. ✓ See success message
6. ✓ (If email enabled) Receive email with link
7. Click link in email
8. ✓ Token validates successfully
9. Enter new password
10. Confirm new password
11. Click reset
12. ✓ See success message
13. ✓ Auto redirect to /login
14. ✓ Login with new password works
```

### Edge Cases
```
❌ Update profile with duplicate email
   → Show error: "Email đã được sử dụng bởi tài khoản khác"

❌ Change password with wrong current password
   → Show error: "Mật khẩu hiện tại không đúng"

❌ Password too short (<6 chars)
   → Show error: "Mật khẩu mới phải có ít nhất 6 ký tự"

❌ Password confirmation doesn't match
   → Show error: "Mật khẩu mới và xác nhận mật khẩu không khớp"

❌ Reset with expired token
   → Show error page: "Token đã hết hạn"

❌ Reset with used token
   → Show error: "Token đã được sử dụng"

❌ Forgot password with non-existent email
   → Show generic success (security: don't reveal if email exists)
```

---

## 📊 Database Impact

### New Records Created:
```sql
-- When user requests password reset
INSERT INTO password_reset_tokens 
  (token, user_id, expiry_date, used, created_at)
VALUES 
  ('uuid-token', 1, NOW() + INTERVAL '1 hour', false, NOW());

-- Token is marked as used after reset
UPDATE password_reset_tokens 
SET used = true 
WHERE token = 'uuid-token';
```

### Cleanup (Manual or Scheduled):
```sql
-- Delete expired tokens (can be run as scheduled job)
DELETE FROM password_reset_tokens 
WHERE expiry_date < NOW();

-- Tokens are automatically deleted when user is deleted (CASCADE)
```

---

## 🎯 Summary

| Feature | URL | Auth Required | All Roles |
|---------|-----|---------------|-----------|
| Profile Update | `/profile` | ✓ Yes | ✓ Yes |
| Change Password | `/profile` | ✓ Yes | ✓ Yes |
| Forgot Password | `/forgot-password` | ✗ No | ✓ Yes |
| Reset Password | `/reset-password?token=xxx` | ✗ No | ✓ Yes |

**All features are fully implemented and ready to use!**

Email service is optional and can be enabled by configuring environment variables.









