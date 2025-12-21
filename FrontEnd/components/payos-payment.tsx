'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { ExternalLink, QrCode, Loader2, AlertCircle } from 'lucide-react';
import { createPayOSPaymentLink } from '@/lib/api';
import type { CreatePaymentLinkRequest, PayOSPaymentItem } from '@/lib/types';
import { getPayOSReturnUrl, getPayOSCancelUrl, validatePayOSUrls } from '@/lib/env';
import { toast } from 'sonner';

interface PayOSPaymentProps {
  orderIds: number[]; // Mảng các order IDs để thanh toán
  amount: number;
  description?: string;
  items: PayOSPaymentItem[]; // Chi tiết các items để hiển thị
  mode?: 'redirect' | 'qrcode'; // redirect (mặc định) hoặc hiển thị QR code
  onPaymentSuccess?: () => void;
  onPaymentFailed?: () => void;
  onCancel?: () => void;
}

export function PayOSPayment({
  orderIds,
  amount,
  description,
  items,
  mode = 'redirect',
  onPaymentSuccess,
  onPaymentFailed,
  onCancel
}: PayOSPaymentProps) {
  const [loading, setLoading] = useState(false);
  const [paymentUrl, setPaymentUrl] = useState<string>('');
  const [error, setError] = useState<string>('');

  const initiatePayment = async () => {
    try {
      setLoading(true);
      setError('');

      // Lấy JWT token từ localStorage (hoặc context/cookie)
      const token = localStorage.getItem('jwt') || localStorage.getItem('token') || '';
      
      if (!token) {
        throw new Error('Vui lòng đăng nhập để thanh toán');
      }

      // ✅ VALIDATION: Kiểm tra amount > 0
      if (!amount || amount <= 0) {
        throw new Error('Số tiền thanh toán phải lớn hơn 0');
      }

      // ✅ Validate PayOS URLs (must not be localhost)
      const urlValidation = validatePayOSUrls();
      if (!urlValidation.valid) {
        urlValidation.warnings.forEach(warning => console.warn(warning));
      }
      
      // ✅ Get URLs from environment configuration
      const returnUrl = getPayOSReturnUrl(orderIds);
      const cancelUrl = getPayOSCancelUrl(orderIds);
      
      console.log('PayOS URLs:', { returnUrl, cancelUrl });

      // ✅ Map items sang đúng format PayOS yêu cầu
      const payosItems: PayOSPaymentItem[] = items.map(item => ({
        name: item.name,
        quantity: item.quantity,
        price: item.price // Đơn giá
      }));

      const request: CreatePaymentLinkRequest = {
        amount,                 // ✅ REQUIRED
        description: description || `Thanh toán đơn hàng ${orderIds.join(', ')}`, // ✅ REQUIRED
        returnUrl,              // ✅ REQUIRED
        cancelUrl,              // ✅ REQUIRED
        currency: 'VND',
        items: payosItems,
        metadata: {
          orderIds: orderIds.join(',')
        }
      };

      // Debug log
      console.log('=== PayOS Payment Request ===');
      console.log('Amount:', amount);
      console.log('Description:', request.description);
      console.log('Return URL:', returnUrl);
      console.log('Cancel URL:', cancelUrl);
      console.log('Items:', JSON.stringify(payosItems, null, 2));
      console.log('JWT Token:', token ? `${token.substring(0, 20)}...` : 'MISSING');
      console.log('============================');

      const response = await createPayOSPaymentLink(request, token);

      if (response && response.paymentUrl) {
        setPaymentUrl(response.paymentUrl);
        
        if (mode === 'redirect') {
          // Redirect người dùng tới trang thanh toán PayOS
          toast.success('Đang chuyển tới trang thanh toán...');
          setTimeout(() => {
            window.location.href = response.paymentUrl;
          }, 500);
        } else {
          // Mode QR code - hiển thị link để scan
          toast.success('Link thanh toán đã được tạo. Vui lòng quét QR hoặc mở link.');
        }
      } else {
        throw new Error('Không thể tạo link thanh toán');
      }
    } catch (err: any) {
      console.error('Error creating PayOS payment:', err);
      const errorMsg = err.message || 'Không thể tạo thanh toán. Vui lòng thử lại.';
      setError(errorMsg);
      toast.error(errorMsg);
      onPaymentFailed?.();
    } finally {
      setLoading(false);
    }
  };

  const handleOpenPaymentLink = () => {
    if (paymentUrl) {
      window.open(paymentUrl, '_blank');
    }
  };

  const handleCancel = () => {
    setPaymentUrl('');
    setError('');
    onCancel?.();
  };

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <QrCode className="h-5 w-5" />
          Thanh toán PayOS
        </CardTitle>
        <CardDescription>
          {description || `Thanh toán cho ${orderIds.length} đơn hàng`}
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Hiển thị thông tin đơn hàng */}
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Đơn hàng:</span>
            <span className="font-medium">#{orderIds.join(', #')}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Số lượng món:</span>
            <span className="font-medium">{items.length} món</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Tổng tiền:</span>
            <span className="text-xl font-bold text-primary">
              {amount.toLocaleString('vi-VN')} ₫
            </span>
          </div>
        </div>

        {/* Chi tiết items */}
        <div className="border-t pt-3 space-y-2">
          <p className="text-sm font-medium">Chi tiết:</p>
          <div className="max-h-32 overflow-y-auto space-y-1">
            {items.map((item, index) => (
              <div key={index} className="text-xs flex justify-between text-muted-foreground">
                <span>{item.name} x{item.quantity}</span>
                <span>{(item.price * item.quantity).toLocaleString('vi-VN')} ₫</span>
              </div>
            ))}
          </div>
        </div>

        {/* Error message */}
        {error && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Payment URL mode (QR code mode) */}
        {mode === 'qrcode' && paymentUrl && !error && (
          <div className="space-y-3">
            <Alert>
              <QrCode className="h-4 w-4" />
              <AlertDescription>
                Link thanh toán đã sẵn sàng. Nhấn nút bên dưới để mở trang thanh toán.
              </AlertDescription>
            </Alert>
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-xs text-muted-foreground break-all">
                {paymentUrl}
              </p>
            </div>
          </div>
        )}
      </CardContent>

      <CardFooter className="flex gap-2">
        {!paymentUrl ? (
          <>
            <Button 
              onClick={initiatePayment} 
              disabled={loading}
              className="flex-1"
            >
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {mode === 'redirect' ? 'Thanh toán ngay' : 'Tạo link thanh toán'}
            </Button>
            {onCancel && (
              <Button variant="outline" onClick={onCancel}>
                Hủy
              </Button>
            )}
          </>
        ) : mode === 'qrcode' ? (
          <>
            <Button onClick={handleOpenPaymentLink} className="flex-1">
              <ExternalLink className="mr-2 h-4 w-4" />
              Mở trang thanh toán
            </Button>
            <Button variant="outline" onClick={handleCancel}>
              Đóng
            </Button>
          </>
        ) : null}
      </CardFooter>
    </Card>
  );
}
