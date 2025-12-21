'use client';

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { CheckCircle2, Loader2, AlertCircle, Home, Receipt } from 'lucide-react';
import { getPaymentByOrderId } from '@/lib/api';
import { toast } from 'sonner';

function PaymentSuccessContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [verified, setVerified] = useState(false);
  const [error, setError] = useState<string>('');
  const [orderIds, setOrderIds] = useState<number[]>([]);

  useEffect(() => {
    const verifyPayment = async () => {
      try {
        // Lấy orderIds từ query params
        const orderIdsParam = searchParams.get('orderIds');
        if (!orderIdsParam) {
          throw new Error('Không tìm thấy thông tin đơn hàng');
        }

        const ids = orderIdsParam.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        setOrderIds(ids);

        if (ids.length === 0) {
          throw new Error('Thông tin đơn hàng không hợp lệ');
        }

        // Lấy JWT token
        const token = localStorage.getItem('jwt') || localStorage.getItem('token') || '';
        
        if (!token) {
          throw new Error('Vui lòng đăng nhập để xác nhận thanh toán');
        }

        // Kiểm tra trạng thái thanh toán của order đầu tiên
        // (Backend sẽ xử lý webhook và cập nhật trạng thái)
        const paymentStatus = await getPaymentByOrderId(ids[0], token);

        if (paymentStatus && (paymentStatus.status === 'COMPLETED' || paymentStatus.paidAt)) {
          setVerified(true);
          toast.success('🎉 Thanh toán thành công!', {
            description: `Đơn hàng ${ids.join(', ')} đã được thanh toán.`
          });
        } else {
          // Nếu chưa có webhook, polling thêm vài lần
          await new Promise(resolve => setTimeout(resolve, 2000));
          const retryStatus = await getPaymentByOrderId(ids[0], token);
          
          if (retryStatus && (retryStatus.status === 'COMPLETED' || retryStatus.paidAt)) {
            setVerified(true);
            toast.success('🎉 Thanh toán thành công!');
          } else {
            // Vẫn chưa có thông tin, cho phép user tiếp tục nhưng cảnh báo
            setVerified(true);
            toast.warning('Đang xác nhận thanh toán...', {
              description: 'Vui lòng kiểm tra lại trạng thái đơn hàng sau ít phút.'
            });
          }
        }
      } catch (err: any) {
        console.error('Error verifying payment:', err);
        setError(err.message || 'Không thể xác nhận thanh toán');
        toast.error('Lỗi xác nhận thanh toán', {
          description: err.message
        });
      } finally {
        setLoading(false);
      }
    };

    verifyPayment();
  }, [searchParams]);

  const handleGoHome = () => {
    router.push('/dashboard');
  };

  const handleViewOrders = () => {
    if (orderIds.length > 0) {
      router.push(`/dashboard/orders?ids=${orderIds.join(',')}`);
    } else {
      router.push('/dashboard/orders');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Card className="w-full max-w-md">
          <CardContent className="pt-6 flex flex-col items-center space-y-4">
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
            <p className="text-muted-foreground">Đang xác nhận thanh toán...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error && !verified) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <Card className="w-full max-w-md">
          <CardHeader>
            <div className="flex items-center justify-center mb-4">
              <AlertCircle className="h-12 w-12 text-destructive" />
            </div>
            <CardTitle className="text-center">Lỗi xác nhận</CardTitle>
            <CardDescription className="text-center">{error}</CardDescription>
          </CardHeader>
          <CardFooter className="flex flex-col gap-2">
            <Button onClick={handleGoHome} className="w-full">
              <Home className="mr-2 h-4 w-4" />
              Về trang chủ
            </Button>
            <Button variant="outline" onClick={handleViewOrders} className="w-full">
              <Receipt className="mr-2 h-4 w-4" />
              Xem đơn hàng
            </Button>
          </CardFooter>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="flex items-center justify-center mb-4">
            <div className="rounded-full bg-green-100 p-3">
              <CheckCircle2 className="h-12 w-12 text-green-600" />
            </div>
          </div>
          <CardTitle className="text-center text-2xl">Thanh toán thành công!</CardTitle>
          <CardDescription className="text-center">
            Cảm ơn bạn đã thanh toán. Đơn hàng của bạn đang được xử lý.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {orderIds.length > 0 && (
            <Alert>
              <Receipt className="h-4 w-4" />
              <AlertDescription>
                <strong>Mã đơn hàng:</strong> {orderIds.map(id => `#${id}`).join(', ')}
              </AlertDescription>
            </Alert>
          )}
          <div className="text-sm text-muted-foreground text-center">
            Bạn có thể xem chi tiết đơn hàng hoặc quay về trang chủ.
          </div>
        </CardContent>
        <CardFooter className="flex flex-col gap-2">
          <Button onClick={handleViewOrders} className="w-full">
            <Receipt className="mr-2 h-4 w-4" />
            Xem đơn hàng
          </Button>
          <Button variant="outline" onClick={handleGoHome} className="w-full">
            <Home className="mr-2 h-4 w-4" />
            Về trang chủ
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}

export default function PaymentSuccessPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Card className="w-full max-w-md">
          <CardContent className="pt-6 flex flex-col items-center space-y-4">
            <Loader2 className="h-12 w-12 animate-spin text-primary" />
            <p className="text-muted-foreground">Đang tải...</p>
          </CardContent>
        </Card>
      </div>
    }>
      <PaymentSuccessContent />
    </Suspense>
  );
}
