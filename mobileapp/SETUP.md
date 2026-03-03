# Hướng dẫn Setup Mobile App

## Bước 1: Cài đặt Android Studio

1. Tải và cài đặt Android Studio từ [developer.android.com](https://developer.android.com/studio)
2. Mở Android Studio và cài đặt Android SDK

## Bước 2: Mở Project

1. Mở Android Studio
2. Chọn "Open an Existing Project"
3. Chọn thư mục `mobileapp`

## Bước 3: Cấu hình BASE_URL

1. Mở file `app/build.gradle.kts`
2. Tìm dòng:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
   ```
3. Thay đổi URL tùy theo môi trường:
   - **Emulator**: `http://10.0.2.2:8080/api/` (localhost của máy host)
   - **Thiết bị thật**: `http://YOUR_COMPUTER_IP:8080/api/` (IP máy tính của bạn)
   - **Server production**: `https://your-domain.com/api/`

## Bước 4: Sync Gradle

1. Click "Sync Now" khi Android Studio yêu cầu
2. Đợi Gradle sync hoàn tất

## Bước 5: Chạy ứng dụng

1. Kết nối thiết bị Android hoặc khởi động emulator
2. Click nút "Run" (Shift + F10) hoặc chọn Run > Run 'app'
3. Chọn thiết bị/emulator và chờ ứng dụng cài đặt

## Lưu ý

- Đảm bảo backend đang chạy trên port 8080
- Nếu dùng thiết bị thật, đảm bảo thiết bị và máy tính cùng mạng WiFi
- Kiểm tra firewall không chặn port 8080

## Troubleshooting

### Lỗi: "Unable to resolve host"
- Kiểm tra BASE_URL đã đúng chưa
- Kiểm tra kết nối mạng
- Thử ping server từ terminal

### Lỗi: "Connection refused"
- Đảm bảo backend đang chạy
- Kiểm tra port 8080 có bị chiếm không
- Thử truy cập API từ browser

### Lỗi: "SSL handshake failed"
- Nếu dùng HTTPS, kiểm tra certificate
- Có thể cần thêm network security config

