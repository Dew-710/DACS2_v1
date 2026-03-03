# Restaurant Mobile App - Kotlin

Ứng dụng Android thuần Kotlin cho hệ thống quản lý nhà hàng.

## Tính năng

- ✅ Đăng nhập / Đăng ký
- ✅ Xem menu theo danh mục
- ✅ Thêm món vào giỏ hàng
- ✅ Xem đơn hàng
- ✅ Thanh toán

## Công nghệ sử dụng

- **Kotlin** - Ngôn ngữ lập trình
- **MVVM Architecture** - Kiến trúc ứng dụng
- **Retrofit** - HTTP client cho API calls
- **Coroutines** - Xử lý bất đồng bộ
- **Navigation Component** - Điều hướng màn hình
- **Material Design** - UI/UX
- **Glide** - Load và cache hình ảnh

## Cấu trúc Project

```
app/
├── src/main/java/com/restaurant/mobileapp/
│   ├── data/
│   │   ├── api/          # API service và Retrofit client
│   │   ├── model/         # Data models
│   │   └── repository/    # Repository pattern
│   ├── ui/
│   │   ├── login/         # Màn hình đăng nhập
│   │   ├── menu/          # Màn hình menu
│   │   └── viewmodel/     # ViewModels
│   └── MainActivity.kt
└── res/
    ├── layout/            # XML layouts
    ├── navigation/        # Navigation graph
    └── values/           # Strings, colors, themes
```

## Cài đặt

1. Mở project trong Android Studio
2. Sync Gradle files
3. Cấu hình BASE_URL trong `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8080/api/\"")
   ```
   - Nếu chạy trên emulator: dùng `http://10.0.2.2:8080/api/`
   - Nếu chạy trên thiết bị thật: dùng IP máy tính của bạn

4. Build và chạy ứng dụng

## API Endpoints

Ứng dụng kết nối với backend Spring Boot tại:
- Base URL: `http://localhost:8080/api/` (hoặc IP của server)

### Các endpoint chính:
- `POST /users/login` - Đăng nhập
- `POST /users/register` - Đăng ký
- `GET /menu-items/list` - Lấy danh sách món ăn
- `GET /categories/list` - Lấy danh sách danh mục
- `GET /orders/my-orders` - Lấy đơn hàng của khách hàng
- `POST /orders/table/{tableId}/add-items` - Thêm món vào đơn hàng

## Yêu cầu

- Android Studio Hedgehog | 2023.1.1 trở lên
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin: 1.9.20

## License

MIT

