"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/lib/context/auth-context"
import { QrCode, Users, ChefHat, BarChart3, Star, Clock, MapPin, Phone, Utensils, Zap, ArrowRight } from "lucide-react"

export default function Home() {
  const { isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const [isClient, setIsClient] = useState(false)

  useEffect(() => {
    setIsClient(true)
  }, [])

  useEffect(() => {
    // Only redirect on client side and when not loading
    if (isClient && !isLoading && isAuthenticated) {
      router.push('/dashboard')
    }
  }, [isAuthenticated, isLoading, router, isClient])

  // Show loading state during hydration to prevent flash
  if (!isClient || isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    )
  }

  const handleGetStarted = () => {
    router.push('/login')
  }

  return (
    <main className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Navigation Header */}
      <header className="border-b border-border bg-card/50 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-secondary rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              RF
            </div>
            <h1 className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
              RestroFlow
            </h1>
          </div>
          <Button onClick={handleGetStarted} className="bg-gradient-to-r from-primary to-secondary hover:opacity-90 text-primary-foreground font-semibold">
            Đăng nhập
            <ArrowRight className="w-4 h-4 ml-2" />
          </Button>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative py-16 md:py-24 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-5xl md:text-6xl font-bold text-foreground mb-6 text-balance">
            Chào mừng đến với{" "}
            <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">RestroFlow</span>
          </h2>
          <p className="text-xl text-muted-foreground mb-8 text-pretty">
            Hệ thống quản lý nhà hàng hiện đại với đặt bàn online, đặt món qua QR code, và trải nghiệm dịch vụ liền mạch.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button
              onClick={handleGetStarted}
              size="lg"
              className="bg-gradient-to-r from-primary to-secondary hover:opacity-90 text-primary-foreground font-semibold text-lg"
            >
              Bắt đầu ngay
            </Button>
            <Button
              onClick={() => document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })}
              size="lg"
              variant="outline"
              className="border-border text-foreground hover:bg-muted font-semibold text-lg bg-transparent"
            >
              Tìm hiểu thêm
            </Button>
          </div>
        </div>
      </section>

      {/* Restaurant Info */}
      <section className="py-12 px-4 bg-card/30">
        <div className="max-w-6xl mx-auto grid md:grid-cols-3 gap-6">
          <div className="flex items-start gap-4 text-center md:text-left">
            <MapPin className="w-6 h-6 text-primary flex-shrink-0 mt-1" />
            <div>
              <h3 className="font-semibold text-foreground mb-1">Địa chỉ</h3>
              <p className="text-muted-foreground">123 Đường ẩm thực, Thành phố đồ ăn, TP 12345</p>
            </div>
          </div>
          <div className="flex items-start gap-4 text-center md:text-left">
            <Clock className="w-6 h-6 text-primary flex-shrink-0 mt-1" />
            <div>
              <h3 className="font-semibold text-foreground mb-1">Giờ mở cửa</h3>
              <p className="text-muted-foreground">Thứ Hai-Chủ Nhật: 11:00 Sáng - 10:00 Tối</p>
            </div>
          </div>
          <div className="flex items-start gap-4 text-center md:text-left">
            <Phone className="w-6 h-6 text-primary flex-shrink-0 mt-1" />
            <div>
              <h3 className="font-semibold text-foreground mb-1">Liên hệ</h3>
              <p className="text-muted-foreground">(555) 123-4567</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-16 px-4">
        <div className="max-w-6xl mx-auto">
          <h3 className="text-3xl font-bold text-foreground text-center mb-12">Tại sao chọn RestroFlow?</h3>
          <div className="grid md:grid-cols-4 gap-6">
            {[
              { icon: QrCode, title: "Đặt món QR Code", description: "Quét mã QR để đặt món trực tiếp từ bàn" },
              { icon: Clock, title: "Phục vụ nhanh", description: "Không chờ nhân viên, tự đặt bàn và gọi món" },
              { icon: Utensils, title: "Menu cao cấp", description: "Bộ sưu tập món ăn được tuyển chọn từ đầu bếp hàng đầu" },
              { icon: Zap, title: "Cập nhật thời gian thực", description: "Theo dõi trạng thái đơn hàng theo thời gian thực" },
            ].map((feature, idx) => {
              const Icon = feature.icon
              return (
                <Card
                  key={idx}
                  className="border-border bg-card/50 hover:border-primary/50 hover:bg-card transition-all"
                >
                  <CardContent className="pt-6">
                    <div className="w-12 h-12 bg-gradient-to-br from-primary to-secondary rounded-lg flex items-center justify-center mb-4 text-primary-foreground">
                      <Icon className="w-6 h-6" />
                    </div>
                    <h4 className="font-semibold text-foreground mb-2">{feature.title}</h4>
                    <p className="text-sm text-muted-foreground">{feature.description}</p>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        </div>
      </section>

      {/* User Roles Section */}
      <section className="py-16 px-4 bg-card/30">
        <div className="max-w-6xl mx-auto">
          <h3 className="text-3xl font-bold text-foreground text-center mb-12">Đa dạng vai trò người dùng</h3>
          <div className="grid md:grid-cols-4 gap-6">
            {[
              { label: "Khách hàng", icon: Users, description: "Đặt bàn, đặt món, thanh toán", color: "from-orange-500 to-red-500" },
              { label: "Nhân viên", icon: Users, description: "Quản lý bàn, phục vụ khách", color: "from-blue-500 to-cyan-500" },
              { label: "Đầu bếp", icon: ChefHat, description: "Xem đơn hàng, cập nhật trạng thái", color: "from-green-500 to-emerald-500" },
              { label: "Quản trị", icon: BarChart3, description: "Thống kê, quản lý hệ thống", color: "from-purple-500 to-pink-500" },
            ].map((role, idx) => {
              const Icon = role.icon
              return (
                <Card key={idx} className="border-border bg-card/50 hover:border-primary/50 hover:bg-card transition-all">
                  <CardContent className="pt-6 text-center">
                    <div className={`w-16 h-16 bg-gradient-to-br ${role.color} rounded-lg flex items-center justify-center mb-4 text-primary-foreground mx-auto`}>
                      <Icon className="w-8 h-8" />
                    </div>
                    <h4 className="font-semibold text-foreground mb-2">{role.label}</h4>
                    <p className="text-sm text-muted-foreground">{role.description}</p>
                </CardContent>
              </Card>
              )
            })}
          </div>
        </div>
      </section>

      {/* Reviews Section */}
      <section className="py-16 px-4">
        <div className="max-w-6xl mx-auto">
          <h3 className="text-3xl font-bold text-foreground text-center mb-12">Khách hàng nói gì về chúng tôi</h3>
          <div className="grid md:grid-cols-3 gap-6">
            {[
              { name: "Nguyễn Văn A", rating: 5, text: "Trải nghiệm tuyệt vời! Hệ thống rất tiện lợi." },
              { name: "Trần Thị B", rating: 5, text: "Dịch vụ tốt nhất tôi từng dùng. Đặt hàng rất dễ dàng!" },
              { name: "Lê Văn C", rating: 5, text: "Thức ăn ngon và quy trình đặt hàng liền mạch." },
            ].map((review, idx) => (
              <Card key={idx} className="border-border bg-card/50">
                <CardContent className="pt-6">
                  <div className="flex gap-1 mb-4 justify-center">
                    {[...Array(review.rating)].map((_, i) => (
                      <Star key={i} className="w-5 h-5 fill-primary text-primary" />
                    ))}
                  </div>
                  <p className="text-foreground mb-4 italic text-center">"{review.text}"</p>
                  <p className="font-semibold text-muted-foreground text-sm text-center">— {review.name}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 px-4 bg-gradient-to-r from-primary/20 via-transparent to-secondary/20">
        <div className="max-w-4xl mx-auto text-center">
          <h3 className="text-3xl font-bold text-foreground mb-4">Sẵn sàng trải nghiệm RestroFlow?</h3>
          <p className="text-muted-foreground mb-8">
            Đặt bàn ngay hôm nay và tận hưởng dịch vụ tuyệt vời cùng ẩm thực của chúng tôi.
          </p>
          <Button
            onClick={handleGetStarted}
            size="lg"
            className="bg-gradient-to-r from-primary to-secondary hover:opacity-90 text-primary-foreground font-semibold text-lg"
          >
            Bắt đầu ngay
          </Button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border bg-card/50 py-8 px-4 mt-12">
        <div className="max-w-6xl mx-auto text-center text-muted-foreground text-sm">
          <p>© 2025 RestroFlow. Bảo lưu mọi quyền.</p>
          <p className="mt-2">Biến đổi trải nghiệm nhà hàng với công nghệ hiện đại.</p>
        </div>
      </footer>
    </main>
  )
}

