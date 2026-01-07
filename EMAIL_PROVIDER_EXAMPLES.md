# Email Provider Configuration Examples

Hướng dẫn cấu hình cho các email service provider phổ biến.

---

## 1. SendGrid (Recommended)

### Get API Key:
1. Đăng ký tài khoản tại: https://sendgrid.com
2. Vào Settings → API Keys
3. Create API Key với quyền "Mail Send"

### Configuration:

```bash
# Environment Variables
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.sendgrid.com/v3/mail/send
export EMAIL_API_TOKEN=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
export EMAIL_FROM=noreply@yourdomain.com
```

### API Request Format:
SendGrid cần format đặc biệt. Update `EmailServiceImpl.java`:

```java
// Trong method sendEmail(), thay đổi emailRequest:
Map<String, Object> emailRequest = new HashMap<>();
Map<String, String> from = new HashMap<>();
from.put("email", fromEmail);
emailRequest.put("from", from);

List<Map<String, String>> personalizations = new ArrayList<>();
Map<String, String> personalization = new HashMap<>();
List<Map<String, String>> toList = new ArrayList<>();
Map<String, String> toMap = new HashMap<>();
toMap.put("email", toEmail);
toList.add(toMap);
personalization.put("to", toList);
personalizations.add(personalization);
emailRequest.put("personalizations", personalizations);

emailRequest.put("subject", subject);

List<Map<String, String>> content = new ArrayList<>();
Map<String, String> contentMap = new HashMap<>();
contentMap.put("type", "text/html");
contentMap.put("value", htmlContent);
content.add(contentMap);
emailRequest.put("content", content);
```

**Ưu điểm:**
- ✓ Free tier: 100 emails/day
- ✓ Reliable và phổ biến
- ✓ Good deliverability
- ✓ Dashboard để track emails

---

## 2. Mailgun

### Get API Key:
1. Đăng ký tại: https://mailgun.com
2. Lấy API Key từ dashboard
3. Verify domain của bạn

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.mailgun.net/v3/YOUR_DOMAIN/messages
export EMAIL_API_TOKEN=YOUR_MAILGUN_API_KEY
export EMAIL_FROM=noreply@yourdomain.com
```

### API Format:
Mailgun sử dụng form-data. Cần update code để support:

```java
// Sử dụng MultiValueMap cho form data
MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
formData.add("from", fromEmail);
formData.add("to", toEmail);
formData.add("subject", subject);
formData.add("html", htmlContent);

HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
headers.setBasicAuth("api", emailApiToken);

HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
```

**Ưu điểm:**
- ✓ Free tier: 5,000 emails/month (first 3 months)
- ✓ Simple API
- ✓ Good documentation

---

## 3. AWS SES (Amazon Simple Email Service)

### Setup:
1. Đăng ký AWS account
2. Vào AWS SES console
3. Verify email addresses hoặc domain
4. Create SMTP credentials hoặc use AWS SDK

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://email.YOUR_REGION.amazonaws.com/v2/email/outbound-emails
export EMAIL_API_TOKEN=YOUR_AWS_ACCESS_KEY
export EMAIL_FROM=noreply@yourdomain.com
```

### Using AWS SDK (Recommended):
Thêm dependency vào `pom.xml`:
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-ses</artifactId>
    <version>1.12.x</version>
</dependency>
```

**Ưu điểm:**
- ✓ Rất rẻ: $0.10 per 1,000 emails
- ✓ Free tier: 62,000 emails/month (nếu send từ EC2)
- ✓ High scalability
- ✓ Tích hợp tốt với AWS

---

## 4. Gmail SMTP (Simple but Limited)

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=smtp://smtp.gmail.com:587
export EMAIL_API_TOKEN=YOUR_GMAIL_APP_PASSWORD
export EMAIL_FROM=youremail@gmail.com
```

### Setup Gmail:
1. Enable 2-factor authentication
2. Tạo App Password tại: https://myaccount.google.com/apppasswords
3. Use app password thay vì password thật

**Hạn chế:**
- ✗ Limit: 500 emails/day
- ✗ Không professional (dùng @gmail.com)
- ✓ Free và dễ setup
- ✓ Tốt cho development/testing

---

## 5. Resend (Modern & Developer-Friendly)

### Get API Key:
1. Đăng ký tại: https://resend.com
2. Verify domain
3. Get API key

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.resend.com/emails
export EMAIL_API_TOKEN=re_xxxxxxxxxxxxxxxxxxxxxxxx
export EMAIL_FROM=noreply@yourdomain.com
```

### API Format:
```json
{
  "from": "noreply@yourdomain.com",
  "to": "recipient@example.com",
  "subject": "Email Subject",
  "html": "<html>...</html>"
}
```

**Ưu điểm:**
- ✓ Free tier: 3,000 emails/month
- ✓ Modern API
- ✓ Simple integration
- ✓ Good for developers

---

## 6. Brevo (formerly Sendinblue)

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.brevo.com/v3/smtp/email
export EMAIL_API_TOKEN=YOUR_BREVO_API_KEY
export EMAIL_FROM=noreply@yourdomain.com
```

**Ưu điểm:**
- ✓ Free tier: 300 emails/day
- ✓ Marketing features included
- ✓ Good UI

---

## 7. Custom SMTP Server

Nếu bạn có SMTP server riêng:

### Configuration:

```bash
export EMAIL_ENABLED=true
export EMAIL_SMTP_HOST=smtp.yourdomain.com
export EMAIL_SMTP_PORT=587
export EMAIL_SMTP_USERNAME=your_username
export EMAIL_SMTP_PASSWORD=your_password
export EMAIL_FROM=noreply@yourdomain.com
```

### Update EmailServiceImpl.java:
```java
@Value("${email.smtp.host:}")
private String smtpHost;

@Value("${email.smtp.port:587}")
private int smtpPort;

@Value("${email.smtp.username:}")
private String smtpUsername;

@Value("${email.smtp.password:}")
private String smtpPassword;

// Use JavaMailSender
private void sendEmail(String toEmail, String subject, String htmlContent) {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.smtp.port", smtpPort);

    Session session = Session.getInstance(props, new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(smtpUsername, smtpPassword);
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");
        
        Transport.send(message);
    } catch (MessagingException e) {
        log.error("Failed to send email", e);
    }
}
```

---

## Comparison Table

| Provider | Free Tier | Price | Complexity | Recommended For |
|----------|-----------|-------|------------|-----------------|
| **SendGrid** | 100/day | $19.95/mo (40k) | Medium | Production |
| **Mailgun** | 5k/mo (3 months) | $35/mo (50k) | Medium | Production |
| **AWS SES** | 62k/mo (from EC2) | $0.10/1k | High | Large scale |
| **Gmail** | 500/day | Free | Low | Development |
| **Resend** | 3k/mo | $20/mo (50k) | Low | Startups |
| **Brevo** | 300/day | €25/mo (20k) | Medium | Marketing |

---

## Recommendation

### For Development/Testing:
```bash
# Sử dụng Gmail hoặc disable email
export EMAIL_ENABLED=false
```

### For Small Production:
```bash
# Resend hoặc SendGrid
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.resend.com/emails
export EMAIL_API_TOKEN=your_token
```

### For Large Scale:
```bash
# AWS SES
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://email.us-east-1.amazonaws.com/v2/email/outbound-emails
export EMAIL_API_TOKEN=your_aws_key
```

---

## Testing Email Integration

### 1. Test with Mailtrap (Development)
```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://mailtrap.io/api/send
export EMAIL_API_TOKEN=your_mailtrap_token
```

Mailtrap bắt tất cả emails để test mà không gửi thật.

### 2. Test Locally Without Email
```bash
export EMAIL_ENABLED=false
```

Xem email content trong application logs.

### 3. Test Email Content
```bash
# Request forgot password
curl -X POST http://localhost:8080/api/users/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'

# Check logs for email content
```

---

## Troubleshooting

### Email không gửi được:

1. **Check configuration:**
   ```bash
   echo $EMAIL_ENABLED
   echo $EMAIL_API_URL
   echo $EMAIL_API_TOKEN
   ```

2. **Check logs:**
   ```bash
   tail -f backend-local.log | grep -i email
   ```

3. **Test API manually:**
   ```bash
   curl -X POST $EMAIL_API_URL \
     -H "Authorization: Bearer $EMAIL_API_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "from": "test@example.com",
       "to": "recipient@example.com",
       "subject": "Test",
       "html": "<p>Test email</p>"
     }'
   ```

4. **Common issues:**
   - ✗ API token expired → Generate new token
   - ✗ Domain not verified → Verify domain in provider dashboard
   - ✗ Rate limit exceeded → Upgrade plan or wait
   - ✗ Blocked by spam filter → Check SPF/DKIM records

---

## Best Practices

1. **Use environment variables**, không hardcode trong code
2. **Verify domain** để tránh bị spam filter
3. **Monitor email sending** qua provider dashboard
4. **Set up SPF/DKIM/DMARC** records cho domain
5. **Use templates** để maintain email content
6. **Test thoroughly** trước khi deploy production
7. **Handle failures gracefully**, không để email error làm crash app
8. **Log email activity** để debug khi cần

---

## Quick Start (Recommended for Vietnam)

### Option 1: SendGrid (International)
```bash
export EMAIL_ENABLED=true
export EMAIL_API_URL=https://api.sendgrid.com/v3/mail/send
export EMAIL_API_TOKEN=SG.your_key_here
export EMAIL_FROM=noreply@yourdomain.com
```

### Option 2: Use Vietnamese SMTP
Nhiều hosting VN cung cấp SMTP service:
- INET
- Viettel IDC
- FPT Cloud

Cấu hình tương tự custom SMTP server ở trên.

---

**Chọn provider phù hợp với budget và quy mô dự án của bạn!**





