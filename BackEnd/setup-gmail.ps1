# Gmail SMTP Setup Script for Restaurant Backend
# Run this in PowerShell before starting the backend

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  Gmail SMTP Configuration Setup" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# Prompt for Gmail credentials
Write-Host "Please enter your Gmail configuration:" -ForegroundColor Yellow
Write-Host ""

$gmailAddress = Read-Host "Enter your Gmail address (e.g., yourname@gmail.com)"
$appPassword = Read-Host "Enter your Gmail App Password (16 characters)" -AsSecureString
$appPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($appPassword)
)

Write-Host ""
Write-Host "Setting environment variables..." -ForegroundColor Green

# Database Configuration (existing)
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="restaurant"
$env:DB_USERNAME="dew_x_phatdev"
$env:DB_PASSWORD="123456789"

# Spring Boot Configuration
$env:SPRING_PROFILES_ACTIVE="postgres"
$env:SERVER_PORT="8080"

# JWT Configuration
$env:JWT_SECRET="restaurant_jwt_secret_key_change_this_in_production"
$env:JWT_EXPIRATION="86400000"

# Frontend URL (Cloudflare Tunnel)
$env:FRONTEND_URL="https://tcp-iowa-favorite-exams.trycloudflare.com"

# Email Configuration - Gmail SMTP
$env:EMAIL_ENABLED="true"
$env:EMAIL_SMTP_ENABLED="true"
$env:SPRING_MAIL_HOST="smtp.gmail.com"
$env:SPRING_MAIL_PORT="587"
$env:SPRING_MAIL_USERNAME=$gmailAddress
$env:SPRING_MAIL_PASSWORD=$appPasswordPlain
$env:EMAIL_FROM=$gmailAddress

# Telegram Bot Configuration
$env:TELEGRAM_BOT_ENABLED="true"
$env:TELEGRAM_BOT_TOKEN="8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ"
$env:TELEGRAM_BOT_USERNAME="RestaurantKitchenBot"
$env:TELEGRAM_BOT_CHAT_ID="6284223765"

# PayOS Configuration
$env:PAYOS_CLIENT_ID="2dae7da2-c098-42d8-a46a-0bf93b078f17"
$env:PAYOS_API_KEY="24cfa45c-177e-4360-84a7-3e432442b1c4"
$env:PAYOS_CHECKSUM_KEY="3100e7a11ab456e10c6f711dc0cf063f0814f0ab159662e4191d7a3d30a66d4b"

Write-Host ""
Write-Host "✓ Environment variables set successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Configuration Summary:" -ForegroundColor Cyan
Write-Host "  - Email: $gmailAddress" -ForegroundColor White
Write-Host "  - SMTP: smtp.gmail.com:587" -ForegroundColor White
Write-Host "  - Frontend: https://tcp-iowa-favorite-exams.trycloudflare.com" -ForegroundColor White
Write-Host "  - Telegram Chat ID: 6284223765" -ForegroundColor White
Write-Host ""
Write-Host "Starting backend server..." -ForegroundColor Green
Write-Host ""

# Start the backend
.\mvnw.cmd spring-boot:run


