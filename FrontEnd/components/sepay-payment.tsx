'use client';

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { CheckCircle2, XCircle, Clock, QrCode, X } from 'lucide-react';
import { createSepayPayment, checkSepayPaymentStatus, cancelSepayPayment, type SepayPaymentRequest } from '@/lib/api';
import { toast } from 'sonner';

interface SepayPaymentProps {
  orderId: number;
  amount: number;
  description?: string;
  onPaymentSuccess?: () => void;
  onPaymentFailed?: () => void;
  onCancel?: () => void;
}

export function SepayPayment({
  orderId,
  amount,
  description,
  onPaymentSuccess,
  onPaymentFailed,
  onCancel
}: SepayPaymentProps) {
  const [loading, setLoading] = useState(false);
  const [paymentData, setPaymentData] = useState<any>(null);
  const [status, setStatus] = useState<'idle' | 'pending' | 'completed' | 'failed' | 'cancelled'>('idle');
  const [timeRemaining, setTimeRemaining] = useState<number>(300); // 5 phút
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(null);

  // Tạo payment và hiển thị QR
  const initiatePayment = async () => {
    try {
      setLoading(true);
      const request: SepayPaymentRequest = {
        orderId,
        amount,
        description: description || `Thanh toán đơn hàng #${orderId}`
      };

      const response = await createSepayPayment(request);

      if (response.success) {
        setPaymentData(response.data);
        setStatus('pending');
        toast.success('QR code đã được tạo. Vui lòng quét mã để thanh toán.');
        
        // Bắt đầu polling status
        startPolling(response.data.transactionId);
        
        // Bắt đầu đếm ngược thời gian
        startCountdown();
      } else {
        throw new Error(response.message || 'Tạo thanh toán thất bại');
      }
    } catch (error: any) {
      console.error('Error creating payment:', error);
      toast.error(error.message || 'Không thể tạo thanh toán. Vui lòng thử lại.');
      setStatus('failed');
      onPaymentFailed?.();
    } finally {
      setLoading(false);
    }
  };

  // Polling payment status mỗi 3 giây
  const startPolling = (transactionId: string) => {
    const interval = setInterval(async () => {
      try {
        const statusResponse = await checkSepayPaymentStatus(transactionId);
        
        if (statusResponse.success) {
          const currentStatus = statusResponse.data.status;
          
          if (currentStatus === 'COMPLETED') {
            setStatus('completed');
            stopPolling();
            toast.success('🎉 Thanh toán thành công!', {
              description: `Đơn hàng #${orderId} đã được thanh toán.`
            });
            onPaymentSuccess?.();
          } else if (currentStatus === 'FAILED') {
            setStatus('failed');
            stopPolling();
            toast.error('Thanh toán thất bại');
            onPaymentFailed?.();
          } else if (currentStatus === 'CANCELLED') {
            setStatus('cancelled');
            stopPolling();
            toast.info('Thanh toán đã bị hủy');
          }
        }
      } catch (error) {
        console.error('Error checking payment status:', error);
      }
    }, 3000); // Poll mỗi 3 giây

    setPollingInterval(interval);
  };

  const stopPolling = () => {
    if (pollingInterval) {
      clearInterval(pollingInterval);
      setPollingInterval(null);
    }
  };

  // Đếm ngược thời gian hết hạn
  const startCountdown = () => {
    const countdownInterval = setInterval(() => {
      setTimeRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(countdownInterval);
          handleTimeout();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const handleTimeout = () => {
    if (status === 'pending') {
      setStatus('failed');
      stopPolling();
      toast.error('Hết thời gian thanh toán');
      onPaymentFailed?.();
    }
  };

  const handleCancel = async () => {
    if (!paymentData?.transactionId) return;

    try {
      await cancelSepayPayment(paymentData.transactionId);
      setStatus('cancelled');
      stopPolling();
      toast.info('Đã hủy thanh toán');
      onCancel?.();
    } catch (error: any) {
      console.error('Error cancelling payment:', error);
      toast.error('Không thể hủy thanh toán');
    }
  };

  // Cleanup khi unmount
  useEffect(() => {
    return () => {
      stopPolling();
    };
  }, [pollingInterval]);

  // Format thời gian còn lại
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  // Format số tiền
  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <QrCode className="w-5 h-5" />
          Thanh toán SePay
        </CardTitle>
        <CardDescription>
          Quét mã QR bằng ứng dụng ngân hàng để thanh toán
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Trạng thái idle - chưa tạo payment */}
        {status === 'idle' && (
          <div className="text-center space-y-4">
            <div className="p-4 bg-muted rounded-lg space-y-2">
              <p className="text-sm text-muted-foreground">Đơn hàng</p>
              <p className="text-2xl font-bold">#{orderId}</p>
              <p className="text-lg font-semibold text-primary">
                {formatAmount(amount)}
              </p>
            </div>
            <Button 
              onClick={initiatePayment} 
              disabled={loading}
              className="w-full"
              size="lg"
            >
              {loading ? (
                <>
                  <Spinner className="w-4 h-4 mr-2" />
                  Đang tạo QR...
                </>
              ) : (
                'Tạo mã QR thanh toán'
              )}
            </Button>
          </div>
        )}

        {/* Trạng thái pending - hiển thị QR */}
        {status === 'pending' && paymentData && (
          <div className="space-y-4">
            {/* QR Code */}
            <div className="flex flex-col items-center space-y-4">
              <div className="relative border-4 border-primary rounded-lg p-2 bg-white">
                <img 
                  src={paymentData.paymentUrl} 
                  alt="QR Code thanh toán" 
                  className="w-64 h-64 object-contain"
                />
              </div>

              {/* Thông tin thanh toán */}
              <div className="w-full space-y-2 text-sm">
                <div className="flex justify-between p-2 bg-muted rounded">
                  <span className="text-muted-foreground">Số tiền:</span>
                  <span className="font-semibold">{formatAmount(paymentData.amount)}</span>
                </div>
                <div className="flex justify-between p-2 bg-muted rounded">
                  <span className="text-muted-foreground">Ngân hàng:</span>
                  <span className="font-semibold">{paymentData.bankCode}</span>
                </div>
                <div className="flex justify-between p-2 bg-muted rounded">
                  <span className="text-muted-foreground">Số tài khoản:</span>
                  <span className="font-semibold">{paymentData.accountNumber}</span>
                </div>
                <div className="flex justify-between p-2 bg-muted rounded">
                  <span className="text-muted-foreground">Tên tài khoản:</span>
                  <span className="font-semibold">{paymentData.accountName}</span>
                </div>
                <div className="flex justify-between p-2 bg-muted rounded">
                  <span className="text-muted-foreground">Nội dung:</span>
                  <span className="font-semibold break-all">{paymentData.content}</span>
                </div>
              </div>

              {/* Countdown timer */}
              <div className="flex items-center gap-2 text-orange-600">
                <Clock className="w-4 h-4" />
                <span className="font-mono font-bold">{formatTime(timeRemaining)}</span>
              </div>

              {/* Status badge */}
              <Badge variant="outline" className="flex items-center gap-2">
                <Spinner className="w-3 h-3" />
                Đang chờ thanh toán...
              </Badge>
            </div>

            <Alert>
              <AlertDescription className="text-sm">
                Mở ứng dụng ngân hàng, quét mã QR và xác nhận thanh toán. Hệ thống sẽ tự động cập nhật khi thanh toán thành công.
              </AlertDescription>
            </Alert>
          </div>
        )}

        {/* Trạng thái completed - thành công */}
        {status === 'completed' && (
          <div className="text-center space-y-4 py-8">
            <CheckCircle2 className="w-16 h-16 text-green-500 mx-auto" />
            <div>
              <p className="text-xl font-bold text-green-600">Thanh toán thành công!</p>
              <p className="text-sm text-muted-foreground mt-2">
                Đơn hàng #{orderId} đã được thanh toán
              </p>
            </div>
          </div>
        )}

        {/* Trạng thái failed - thất bại */}
        {status === 'failed' && (
          <div className="text-center space-y-4 py-8">
            <XCircle className="w-16 h-16 text-red-500 mx-auto" />
            <div>
              <p className="text-xl font-bold text-red-600">Thanh toán thất bại</p>
              <p className="text-sm text-muted-foreground mt-2">
                Vui lòng thử lại hoặc chọn phương thức thanh toán khác
              </p>
            </div>
            <Button onClick={initiatePayment} variant="outline">
              Thử lại
            </Button>
          </div>
        )}

        {/* Trạng thái cancelled - đã hủy */}
        {status === 'cancelled' && (
          <div className="text-center space-y-4 py-8">
            <X className="w-16 h-16 text-muted-foreground mx-auto" />
            <div>
              <p className="text-xl font-bold text-muted-foreground">Đã hủy thanh toán</p>
            </div>
          </div>
        )}
      </CardContent>

      <CardFooter className="flex justify-between">
        {status === 'pending' && (
          <Button 
            onClick={handleCancel} 
            variant="outline"
            className="w-full"
          >
            Hủy thanh toán
          </Button>
        )}
        
        {(status === 'completed' || status === 'failed' || status === 'cancelled') && onCancel && (
          <Button 
            onClick={onCancel} 
            variant="outline"
            className="w-full"
          >
            Đóng
          </Button>
        )}
      </CardFooter>
    </Card>
  );
}
