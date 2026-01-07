# Email Configuration Guide

This guide explains how to configure the email service for password reset and profile update notifications.

## Features Implemented

### 1. **Profile Management** (All Users: Admin, Staff, Customer)
- Update personal information (Full Name, Email, Phone)
- Change password with current password verification
- Email confirmation for updates

### 2. **Password Reset Flow**
- Request password reset via email
- Secure token generation (expires in 1 hour)
- Reset password using email token
- Token validation and expiration handling

---

## Backend Configuration

### Email Service Properties

Add these properties to `application.properties` or set as environment variables:

```properties
# Email Configuration
email.enabled=false              # Set to 'true' to enable email sending
email.api.url=                   # Your email service API endpoint
email.api.token=                 # Your email service API token
email.from=noreply@restaurant.com # Sender email address
```

### Environment Variables (Recommended for Production)

```bash
# Enable email service
export EMAIL_ENABLED=true

# Email API Configuration
export EMAIL_API_URL=https://your-email-api.com/send
export EMAIL_API_TOKEN=your_api_token_here

# Optional: Customize sender email
export EMAIL_FROM=noreply@yourrestaurant.com
```

---

## Supported Email Service Providers

The implementation uses a generic REST API approach. You can integrate with any email service provider that accepts HTTP POST requests.

### Example: Using SendGrid

```bash
export EMAIL_API_URL=https://api.sendgrid.com/v3/mail/send
export EMAIL_API_TOKEN=your_sendgrid_api_key
```

**Request Format for SendGrid:**
The service sends JSON with:
```json
{
  "to": "recipient@example.com",
  "from": "noreply@restaurant.com",
  "subject": "Email Subject",
  "html": "<html>...</html>"
}
```

### Example: Using Mailgun

```bash
export EMAIL_API_URL=https://api.mailgun.net/v3/your-domain.com/messages
export EMAIL_API_TOKEN=your_mailgun_api_key
```

### Example: Using Custom SMTP Service

If using a custom service, ensure your API endpoint accepts:
- Authorization: Bearer {token}
- Content-Type: application/json
- Body: { to, from, subject, html }

---

## API Endpoints

### Profile Management

#### 1. Update Profile
```
PUT /api/users/profile/{userId}
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+84123456789"
}

Response:
{
  "message": "Cập nhật thông tin cá nhân thành công",
  "user": { ... }
}
```

#### 2. Change Password
```
PUT /api/users/change-password/{userId}
Content-Type: application/json

{
  "currentPassword": "old_password",
  "newPassword": "new_password",
  "confirmPassword": "new_password"
}

Response:
{
  "message": "Đổi mật khẩu thành công"
}
```

### Password Reset

#### 3. Request Password Reset
```
POST /api/users/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response:
{
  "message": "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn."
}
```

#### 4. Validate Reset Token
```
GET /api/users/validate-reset-token?token={token}

Response:
{
  "valid": true,
  "message": "Token hợp lệ"
}
```

#### 5. Reset Password
```
POST /api/users/reset-password
Content-Type: application/json

{
  "token": "reset_token_from_email",
  "newPassword": "new_password",
  "confirmPassword": "new_password"
}

Response:
{
  "message": "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới."
}
```

---

## Frontend Pages

### 1. Profile Page
**URL:** `/profile`
**Access:** All authenticated users (Admin, Staff, Customer)

Features:
- View and update personal information
- Change password
- Tab-based interface for better UX

### 2. Forgot Password Page
**URL:** `/forgot-password`
**Access:** Public (no authentication required)

Features:
- Email input form
- Success feedback
- Link to login page

### 3. Reset Password Page
**URL:** `/reset-password?token={token}`
**Access:** Public (requires valid token)

Features:
- Token validation
- New password form
- Automatic redirect to login after success

---

## Database Migration

A Flyway migration has been created to add the `password_reset_tokens` table:

**File:** `V6__create_password_reset_tokens_table.sql`

```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

The migration will run automatically on application startup.

---

## Email Templates

The service includes three pre-designed HTML email templates:

### 1. Password Reset Email
- Includes reset link button
- Shows expiration time (1 hour)
- Fallback plain text link

### 2. Profile Update Confirmation
- Notifies user of profile changes
- Security warning if not authorized

### 3. Password Change Confirmation
- Notifies user of password change
- Strong security warning

---

## Testing Without Email Service

When `email.enabled=false` (default), the service logs email content instead of sending:

```
INFO: Email service is disabled. Would send email to: user@example.com
DEBUG: Email content: <html>...</html>
```

This allows testing the full flow without configuring an email provider.

---

## Security Features

1. **Token Expiration:** Reset tokens expire after 1 hour
2. **Single Use:** Tokens can only be used once
3. **Secure Generation:** Uses UUID for token generation
4. **Password Strength:** Minimum 6 characters required
5. **Current Password Verification:** Required for password changes
6. **Email Validation:** Prevents revealing if email exists (security best practice)

---

## Usage Examples

### For Users (Frontend)

1. **Update Profile:**
   - Go to `/profile`
   - Click "Thông tin cá nhân" tab
   - Update fields and click "Cập nhật thông tin"

2. **Change Password:**
   - Go to `/profile`
   - Click "Đổi mật khẩu" tab
   - Enter current and new passwords
   - Click "Đổi mật khẩu"

3. **Reset Forgotten Password:**
   - Click "Quên mật khẩu?" on login page
   - Enter email address
   - Check email for reset link
   - Click link and enter new password

### For Administrators

To enable email sending in production:

1. Choose an email service provider (SendGrid, Mailgun, etc.)
2. Get API credentials
3. Set environment variables:
   ```bash
   export EMAIL_ENABLED=true
   export EMAIL_API_URL=your_api_url
   export EMAIL_API_TOKEN=your_api_token
   ```
4. Restart the application
5. Test the password reset flow

---

## Troubleshooting

### Email Not Sending

1. Check `email.enabled` is set to `true`
2. Verify `EMAIL_API_URL` is correct
3. Verify `EMAIL_API_TOKEN` is valid
4. Check application logs for errors
5. Test API endpoint manually with curl

### Token Invalid or Expired

- Tokens expire after 1 hour
- Request a new password reset
- Check system time is synchronized

### Email Not Received

- Check spam/junk folder
- Verify email address is correct
- Check email service provider logs
- Verify FROM email is whitelisted

---

## Future Enhancements

Potential improvements:
- Email verification on registration
- Two-factor authentication
- Email templates customization UI
- Multiple language support for emails
- Rate limiting for password reset requests
- Admin dashboard for email logs

---

## Support

For issues or questions:
1. Check application logs
2. Verify configuration
3. Test with `email.enabled=false` first
4. Contact system administrator





