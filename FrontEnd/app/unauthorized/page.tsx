'use client';

import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { AlertTriangle, ArrowLeft, Home } from 'lucide-react';
import { useAuth } from '@/lib/context/auth-context';

export default function UnauthorizedPage() {
  const router = useRouter();
  const { user, logout } = useAuth();

  const handleGoBack = () => {
    router.back();
  };

  const handleGoHome = () => {
    router.push('/dashboard');
  };

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center px-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-destructive/5 rounded-full blur-3xl"></div>
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-secondary/5 rounded-full blur-3xl"></div>
      </div>

      <div className="w-full max-w-md relative z-10">
        {/* Logo and Title */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-destructive to-red-600 rounded-lg flex items-center justify-center text-white font-bold text-2xl mx-auto mb-4">
            RF
          </div>
          <h1 className="text-3xl font-bold text-foreground mb-2">RestroFlow</h1>
          <p className="text-muted-foreground">Bảo mật hệ thống</p>
        </div>

        {/* Unauthorized Card */}
        <Card className="border border-border bg-card/80 backdrop-blur-sm shadow-xl">
          <CardHeader>
            <CardTitle className="text-2xl text-center flex items-center justify-center gap-2">
              <AlertTriangle className="w-6 h-6 text-destructive" />
              Không có quyền truy cập
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="text-center">
              <p className="text-muted-foreground mb-4">
                Xin chào <span className="font-semibold text-foreground">{user?.username}</span>!
              </p>
              <p className="text-muted-foreground mb-4">
                Bạn không có quyền truy cập vào trang này. Vui lòng liên hệ quản trị viên
                hoặc đăng nhập với tài khoản có quyền phù hợp.
              </p>

              {user?.role && (
                <div className="bg-muted/50 rounded-lg p-3 mb-4">
                  <p className="text-sm text-muted-foreground">
                    Vai trò hiện tại của bạn: <span className="font-semibold text-foreground">{user.role}</span>
                  </p>
                </div>
              )}
            </div>

            {/* Action Buttons */}
            <div className="space-y-3">
              <Button
                onClick={handleGoHome}
                className="w-full bg-gradient-to-r from-primary to-secondary hover:opacity-90 text-primary-foreground font-semibold"
              >
                <Home className="w-4 h-4 mr-2" />
                Về trang chủ
              </Button>

              <Button
                onClick={handleGoBack}
                variant="outline"
                className="w-full"
              >
                <ArrowLeft className="w-4 h-4 mr-2" />
                Quay lại
              </Button>

              <Button
                onClick={handleLogout}
                variant="destructive"
                className="w-full"
              >
                Đăng xuất
              </Button>
            </div>

            {/* Role Information */}
            <div className="pt-4 border-t border-border">
              <h4 className="font-semibold text-foreground mb-3">Các vai trò trong hệ thống:</h4>
              <div className="space-y-2 text-sm text-muted-foreground">
                <div className="flex justify-between">
                  <span>👑 Admin:</span>
                  <span>Quản lý toàn bộ hệ thống</span>
                </div>
                <div className="flex justify-between">
                  <span>👨‍💼 Staff:</span>
                  <span>Quản lý bàn và phục vụ</span>
                </div>
                <div className="flex justify-between">
                  <span>👨‍🍳 Kitchen:</span>
                  <span>Quản lý bếp và đơn hàng</span>
                </div>
                <div className="flex justify-between">
                  <span>👤 Customer:</span>
                  <span>Đặt bàn và đặt món</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="text-center mt-6 text-sm text-muted-foreground">
          <p>Nếu bạn nghĩ đây là lỗi, vui lòng liên hệ quản trị viên</p>
        </div>
      </div>
    </div>
  );
}
