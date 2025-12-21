'use client';

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { XCircle, Loader2, Home, RotateCcw, Receipt } from 'lucide-react';

function PaymentCancelContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [orderIds, setOrderIds] = useState<number[]>([]);

  useEffect(() => {
    // Lấy orderIds từ query params
    const orderIdsParam = searchParams.get('orderIds');
    if (orderIdsParam) {
      const ids = orderIdsParam.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
      setOrderIds(ids);
    }
  }, [searchParams]);

  const handleGoHome = () => {
    router.push('/dashboard');
  };

  const handleRetryPayment = () => {
    if (orderIds.length > 0) {
      // Quay lại trang thanh toán với orderIds
      router.push(`/dashboard/orders?retry=true&ids=${orderIds.join(',')}`);
    } else {
      router.push('/dashboard/orders');
    }
  };

  const handleViewOrders = () => {
    if (orderIds.length > 0) {
      router.push(`/dashboard/orders?ids=${orderIds.join(',')}`);
    } else {
      router.push('/dashboard/orders');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="flex items-center justify-center mb-4">
            <div className="rounded-full bg-yellow-100 p-3">
              <XCircle className="h-12 w-12 text-yellow-600" />
            </div>
          </div>
          <CardTitle className="text-center text-2xl">Thanh toán đã bị hủy</CardTitle>
          <CardDescription className="text-center">
            Bạn đã hủy quá trình thanh toán. Đơn hàng của bạn vẫn đang chờ thanh toán.
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
            Bạn có thể thử thanh toán lại hoặc xem chi tiết đơn hàng.
          </div>
        </CardContent>
        <CardFooter className="flex flex-col gap-2">
          <Button onClick={handleRetryPayment} className="w-full">
            <RotateCcw className="mr-2 h-4 w-4" />
            Thử thanh toán lại
          </Button>
          <Button variant="outline" onClick={handleViewOrders} className="w-full">
            <Receipt className="mr-2 h-4 w-4" />
            Xem đơn hàng
          </Button>
          <Button variant="ghost" onClick={handleGoHome} className="w-full">
            <Home className="mr-2 h-4 w-4" />
            Về trang chủ
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}

export default function PaymentCancelPage() {
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
      <PaymentCancelContent />
    </Suspense>
  );
}
