'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { StaffOnly } from '@/lib/components/protected-route';
import { useAuth } from '@/lib/context/auth-context';
import { SepayPayment } from '@/components/sepay-payment';
import { PayOSPayment } from '@/components/payos-payment';
import type { PayOSPaymentItem } from '@/lib/types';
import { getAppUrl, getApiBaseUrl } from '@/lib/env';
import {
  getTablesList,
  getOrders,
  getActiveOrdersByTable,
  getBookings,
  updateTableStatus,
  checkOutTable,
  getTableCurrentOrder,
  createOrderWithCustomer,
  sendQRCodeToESP32,
  getQRCodeImageUrl,
  confirmBooking,
  cancelBooking,
  checkInBooking,
  register,
  checkTableAvailability
} from '@/lib/api';
import type {
  RestaurantTable,
  Order,
  Booking
} from '@/lib/types';
import {
  LogOut,
  Users,
  ShoppingCart,
  Table,
  Calendar,
  Clock,
  CheckCircle,
  XCircle,
  UserPlus,
  Phone,
  QrCode,
  Printer,
  Download,
  MapPin,
  Tag,
  Wallet,
  CreditCard
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function StaffDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("tables");

  // State cho dữ liệu dashboard
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [pendingTables, setPendingTables] = useState<RestaurantTable[]>([]);
  const [loading, setLoading] = useState(true);
  const [availableTablesForStaff, setAvailableTablesForStaff] = useState<RestaurantTable[]>([]);
  const [tableFilter, setTableFilter] = useState<'all' | 'available' | 'occupied' | 'maintenance' | 'reserved' | 'pending'>('all');

  // State cho payment dialog
  const [paymentDialogOpen, setPaymentDialogOpen] = useState(false);
  const [selectedOrderForPayment, setSelectedOrderForPayment] = useState<Order | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'SEPAY' | 'PAYOS' | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [tablesRes, ordersRes, bookingsRes] = await Promise.all([
        getTablesList(),
        getOrders(),
        getBookings(),
      ]);

      setTables(tablesRes.tables || []);
      setOrders(ordersRes.orders || []);
      setBookings(bookingsRes.bookings || []);

      // Lọc bàn có trạng thái PENDING_CHECKIN để check-in
      const pendingTables = (tablesRes.tables || []).filter(table =>
        table && table.status === 'PENDING_CHECKIN'
      );
      setPendingTables(pendingTables);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      toast.error('Không thể tải dữ liệu dashboard');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    toast.success('Đã đăng xuất');
    router.push('/');
  };

  const handleCheckIn = async (tableId: number) => {
    try {
      // Tìm bàn
      const table = pendingTables.find(t => t.id === tableId);
      if (!table) {
        toast.error('Không tìm thấy bàn để check-in');
        return;
      }

      // Tìm booking tương ứng cho bàn này
      const booking = bookings.find(b => b.tableId === tableId && b.status === 'CONFIRMED');
      if (!booking) {
        toast.error('Không tìm thấy booking tương ứng');
        return;
      }

      // Check-in booking (đặt bàn thành OCCUPIED)
      await checkInBooking(booking.id);

      toast.success(`Check-in thành công cho bàn ${table.tableName}`);

      // Xóa bàn này khỏi danh sách pending ngay lập tức
      setPendingTables(prev => prev.filter(t => t.id !== tableId));

      // Làm mới dữ liệu sau một khoảng delay ngắn để đảm bảo UI cập nhật trước
      setTimeout(() => loadDashboardData(), 100);
    } catch (error) {
      console.error('Error during check-in:', error);
      toast.error('Check-in thất bại');
    }
  };

  const handleWalkInCheckIn = async (tableId: number) => {
    try {
      // Tạo khách vãng lai
      const timestamp = Date.now();
      const randomSuffix = Math.random().toString(36).substring(2, 8);
      const customerData = {
        username: `walkin_${timestamp}_${randomSuffix}`,
        email: `walkin_${timestamp}_${randomSuffix}@restaurant.com`,
        password: 'password123',
        fullName: 'Khách vãng lai',
        phone: '',
        role: 'CUSTOMER'
      };

      const customerResponse = await register(customerData);
      const customerId = customerResponse.user.id;

      // Tạo order mới cho bàn
      const newOrder = {
        tableId: tableId,  // ✅ Thêm tableId vào object
        status: 'PLACED',
        totalAmount: 0,
        notes: 'Khách vãng lai - Check-in trực tiếp'
      };

      await createOrderWithCustomer(customerId, tableId, newOrder);

      // Cập nhật trạng thái bàn thành OCCUPIED
      await updateTableStatus(tableId, 'OCCUPIED');

      toast.success(`Check-in khách vãng lai thành công cho bàn ${tableId}`);

      // Làm mới dữ liệu
      loadDashboardData();
    } catch (error) {
      console.error('Error during walk-in check-in:', error);
      toast.error('Check-in thất bại. Vui lòng thử lại.');
    }
  };


  const handleCheckOut = async (tableId: number) => {
    try {
      await checkOutTable(tableId);
      toast.success('Check-out bàn thành công');
      loadDashboardData(); // Refresh data
    } catch (error) {
      console.error('Error during check-out:', error);
      toast.error('Check-out thất bại');
    }
  };

  const handleTableStatusChange = async (tableId: number, newStatus: string) => {
    try {
      await updateTableStatus(tableId, newStatus);
      toast.success(`Trạng thái bàn đã được cập nhật thành ${newStatus}`);
      loadDashboardData();
    } catch (error) {
      console.error('Error updating table status:', error);
      toast.error('Cập nhật trạng thái bàn thất bại');
    }
  };

  const handleBookingAction = async (bookingId: number, action: 'approve' | 'reject') => {
    try {
      if (action === 'approve') {
        await confirmBooking(bookingId);
        toast.success('Đặt bàn đã được duyệt - khách có thể check-in từ trang Check-in');
      } else {
        await cancelBooking(bookingId);
        toast.success('Đặt bàn đã bị từ chối');
      }
      loadDashboardData();
    } catch (error) {
      console.error('Error handling booking:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      toast.error(`Thao tác thất bại: ${errorMessage}`);
    }
  };

  const handleSendQRToESP32 = async (tableId: number) => {
    try {
      // TODO: Implement ESP32 WebSocket integration
      console.log('Sending QR to ESP32 for table:', tableId);
      toast.info('Chức năng đang được phát triển');
    } catch (error) {
      console.error('Error sending QR to ESP32:', error);
      toast.error('Không thể gửi mã QR');
    }
  };

  const handleOrderStatusUpdate = async (orderId: number, newStatus: string) => {
    try {
      const apiUrl = getApiBaseUrl();
      await fetch(`${apiUrl}/api/orders/${orderId}/status/${newStatus}`, { method: 'PUT' });
      toast.success('Trạng thái đơn hàng đã được cập nhật');
      loadDashboardData();
    } catch (error) {
      console.error('Error updating order status:', error);
      toast.error('Cập nhật trạng thái thất bại');
    }
  };

  const handleProcessPayment = (order: Order) => {
    console.log('[handleProcessPayment] Order:', order);
    console.log('[handleProcessPayment] Order Items:', order.orderItems);
    
    // Validate order has items
    if (!order.orderItems || order.orderItems.length === 0) {
      toast.error('Đơn hàng không có món ăn nào. Không thể thanh toán!');
      console.error('[handleProcessPayment] Order has no items');
      return;
    }
    
    setSelectedOrderForPayment(order);
    setPaymentMethod(null);
    setPaymentDialogOpen(true);
  };

  const handleCashPayment = async () => {
    if (!selectedOrderForPayment) return;

    try {
      const apiUrl = getApiBaseUrl();
      await fetch(`${apiUrl}/api/payments/process`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          orderId: selectedOrderForPayment.id,
          amount: selectedOrderForPayment.totalAmount || 0,
          paymentMethod: 'CASH'
        })
      });
      toast.success('Thanh toán tiền mặt thành công');
      setPaymentDialogOpen(false);
      setSelectedOrderForPayment(null);
      loadDashboardData();
    } catch (error) {
      console.error('Error processing payment:', error);
      toast.error('Thanh toán thất bại');
    }
  };

  const handleSepayPaymentSuccess = () => {
    toast.success('Thanh toán SePay thành công!');
    setPaymentDialogOpen(false);
    setSelectedOrderForPayment(null);
    setPaymentMethod(null);
    loadDashboardData();
  };

  const handleSepayPaymentFailed = () => {
    toast.error('Thanh toán SePay thất bại');
  };

  const handlePayOSPaymentSuccess = () => {
    toast.success('Đang chuyển đến trang thanh toán PayOS...');
    // PayOS sẽ redirect, không cần làm gì thêm
  };

  const handlePayOSPaymentFailed = () => {
    toast.error('Không thể tạo link thanh toán PayOS');
  };

  // Helper function để chuyển đổi order items sang PayOS format
  const preparePayOSItems = (order: Order): PayOSPaymentItem[] => {
    console.log('[preparePayOSItems] Order:', order);
    console.log('[preparePayOSItems] Order Items:', order.orderItems);
    
    if (!order.orderItems || order.orderItems.length === 0) {
      console.warn('[preparePayOSItems] WARNING: Order has no items!');
      return [];
    }
    
    // ✅ Map sang format mới của PayOS: { name, quantity, price }
    const items = order.orderItems.map(item => ({
      name: item.menuItem?.name || 'Món ăn',
      quantity: item.quantity,
      price: item.price  // Đơn giá (không phải tổng)
    }));
    
    console.log('[preparePayOSItems] Prepared items:', items);
    return items;
  };

  const handleClosePaymentDialog = () => {
    setPaymentDialogOpen(false);
    setSelectedOrderForPayment(null);
    setPaymentMethod(null);
  };

  // Tính toán thống kê
  const availableTables = tables.filter(table => table.status === 'AVAILABLE');
  const occupiedTables = tables.filter(table => table.status === 'OCCUPIED');
  const maintenanceTables = tables.filter(table => table.status === 'MAINTENANCE');
  const reservedTables = tables.filter(table => table.status === 'RESERVED');
  const pendingCheckInTables = tables.filter(table => table.status === 'PENDING_CHECKIN');
  const activeOrders = orders.filter(order => order.status === 'ACTIVE' || order.status === 'PLACED');

  // Lọc bàn dựa trên bộ lọc đã chọn
  const filteredTables = tableFilter === 'all' ? tables :
    tableFilter === 'available' ? availableTables :
    tableFilter === 'occupied' ? occupiedTables :
    tableFilter === 'maintenance' ? maintenanceTables :
    tableFilter === 'reserved' ? reservedTables :
    tableFilter === 'pending' ? pendingCheckInTables : tables;

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              👨‍💼
            </div>
            <div>
              <h1 className="text-xl font-bold">Staff Dashboard</h1>
              <p className="text-sm text-muted-foreground">Chào mừng nhân viên {user?.username}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary" className="bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
              STAFF
            </Badge>
            <Button variant="outline" onClick={handleLogout} className="flex items-center gap-2">
              <LogOut className="w-4 h-4" />
              Đăng xuất
            </Button>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="grid w-full grid-cols-5 mb-8">
            <TabsTrigger value="tables">Quản lý bàn</TabsTrigger>
            <TabsTrigger value="orders">Đơn hàng</TabsTrigger>
            <TabsTrigger value="bookings">Đặt bàn</TabsTrigger>
            <TabsTrigger value="checkin">Check-in</TabsTrigger>
            <TabsTrigger value="qrcodes">QR Codes</TabsTrigger>
          </TabsList>

          {/* Tables Management Tab */}
          <TabsContent value="tables" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Quản lý bàn ăn</h2>
              <div className="flex gap-2 flex-wrap">
                <Badge variant="outline" className="cursor-pointer hover:bg-green-100"
                       onClick={() => setTableFilter('available')}>
                  {availableTables.length} bàn trống
                </Badge>
                <Badge variant="destructive" className="cursor-pointer hover:bg-red-100"
                       onClick={() => setTableFilter('occupied')}>
                  {occupiedTables.length} bàn đang dùng
                </Badge>
                {maintenanceTables.length > 0 && (
                  <Badge variant="secondary" className="cursor-pointer hover:bg-yellow-100"
                         onClick={() => setTableFilter('maintenance')}>
                    {maintenanceTables.length} bàn bảo trì
                  </Badge>
                )}
                {reservedTables.length > 0 && (
                  <Badge variant="secondary" className="cursor-pointer hover:bg-blue-100"
                         onClick={() => setTableFilter('reserved')}>
                    {reservedTables.length} bàn đã đặt
                  </Badge>
                )}
                {pendingCheckInTables.length > 0 && (
                  <Badge variant="outline" className="cursor-pointer hover:bg-orange-100"
                         onClick={() => setTableFilter('pending')}>
                    {pendingCheckInTables.length} bàn chờ check-in
                  </Badge>
                )}
                <Button variant="outline" size="sm" onClick={() => setTableFilter('all')}>
                  Xem tất cả
                </Button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {filteredTables.map((table) => (
                <Card key={table.id} className={`cursor-pointer transition-all hover:shadow-lg ${
                  table.status === 'AVAILABLE' ? 'border-green-200 bg-green-50 dark:bg-green-900/20' :
                  table.status === 'OCCUPIED' ? 'border-red-200 bg-red-50 dark:bg-red-900/20' :
                  table.status === 'RESERVED' ? 'border-blue-200 bg-blue-50 dark:bg-blue-900/20' :
                  table.status === 'MAINTENANCE' ? 'border-yellow-200 bg-yellow-50 dark:bg-yellow-900/20' :
                  'border-gray-200'
                }`}>
                  <CardContent className="p-4 text-center">
                    <div className="w-12 h-12 mx-auto mb-2 bg-muted rounded-full flex items-center justify-center">
                      <Table className="w-6 h-6" />
                    </div>
                    <p className="font-semibold">Bàn {table.tableName}</p>
                    <p className="text-sm text-muted-foreground">{table.capacity} người</p>
                    <Badge variant={
                      table.status === 'AVAILABLE' ? 'default' :
                      table.status === 'OCCUPIED' ? 'destructive' :
                      table.status === 'RESERVED' ? 'secondary' :
                      table.status === 'PENDING_CHECKIN' ? 'outline' :
                      table.status === 'MAINTENANCE' ? 'outline' : 'outline'
                    } className="mt-2">
                      {table.status === 'AVAILABLE' ? 'Trống' :
                       table.status === 'OCCUPIED' ? 'Đang dùng' :
                       table.status === 'RESERVED' ? 'Đã đặt' :
                       table.status === 'PENDING_CHECKIN' ? 'Chờ check-in' :
                       table.status === 'MAINTENANCE' ? 'Bảo trì' : table.status}
                    </Badge>
                    <div className="flex gap-2 mt-3">
                      {table.status === 'AVAILABLE' && (
                        <Button
                          size="sm"
                          className="flex-1"
                          onClick={() => handleWalkInCheckIn(table.id)}
                        >
                          <UserPlus className="w-4 h-4 mr-2" />
                          Check-in
                        </Button>
                      )}
                      {table.status === 'OCCUPIED' && (
                        <Button
                          size="sm"
                          variant="destructive"
                          className="flex-1"
                          onClick={() => handleCheckOut(table.id)}
                        >
                          Check-out
                        </Button>
                      )}
                      <Button
                        size="sm"
                        variant="outline"
                        className="flex-1"
                        onClick={() => handleTableStatusChange(table.id, table.status === 'MAINTENANCE' ? 'AVAILABLE' : 'MAINTENANCE')}
                      >
                        {table.status === 'MAINTENANCE' ? 'Hoạt động' : 'Bảo trì'}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>

          {/* Orders Tab */}
          <TabsContent value="orders" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Đơn hàng đang phục vụ</h2>
              <Badge variant="outline">
                {activeOrders.length} đơn hàng hoạt động
              </Badge>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {activeOrders.map((order) => (
                <Card key={order.id}>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-lg">Đơn hàng #{order.id}</CardTitle>
                        <p className="text-sm text-muted-foreground">
                          Bàn {order.table?.tableName || 'N/A'}
                        </p>
                      </div>
                      <Badge variant="secondary">
                        {order.status}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div>
                        <p className="font-medium mb-2">Món đã gọi:</p>
                        <div className="space-y-1">
                          {order.orderItems?.map((item, idx) => (
                            <div key={idx} className="flex justify-between text-sm">
                              <span>{item.menuItem.name} x{item.quantity}</span>
                              <span>{(item.price * item.quantity).toLocaleString('vi-VN')}đ</span>
                            </div>
                          ))}
                        </div>
                      </div>
                      <div className="border-t pt-3 flex justify-between font-semibold">
                        <span>Tổng cộng:</span>
                        <span>{(order.totalAmount || 0).toLocaleString('vi-VN')}đ</span>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          className="flex-1"
                          onClick={() => handleOrderStatusUpdate(order.id, 'COMPLETED')}
                        >
                          Hoàn thành
                        </Button>
                        <Button
                          size="sm"
                          className="flex-1"
                          onClick={() => handleProcessPayment(order)}
                        >
                          Thanh toán
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {activeOrders.length === 0 && (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <ShoppingCart className="w-16 h-16 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">Không có đơn hàng nào</h3>
                  <p className="text-muted-foreground text-center">
                    Hiện tại không có đơn hàng nào đang hoạt động.
                  </p>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          {/* Bookings Tab */}
          <TabsContent value="bookings" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Đặt bàn hôm nay</h2>
              <Badge variant="outline">
                {bookings.filter(b => b.status === 'PENDING').length} đặt bàn chờ duyệt
              </Badge>
            </div>

            <Card>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                  </div>
                ) : bookings.filter(b => b.status === 'PENDING').length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12">
                    <Calendar className="w-16 h-16 text-muted-foreground mb-4" />
                    <h3 className="text-lg font-semibold mb-2">Không có đặt bàn nào</h3>
                    <p className="text-muted-foreground text-center">
                      Hiện tại không có yêu cầu đặt bàn nào chờ xử lý.
                    </p>
                  </div>
                ) : (
                  <div className="divide-y">
                    {bookings.filter(b => b.status === 'PENDING').map((booking) => (
                      <div key={booking.id} className="flex items-center justify-between p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                            <Users className="w-5 h-5" />
                          </div>
                          <div>
                            <p className="font-medium">{booking?.customer?.username || booking?.customer?.fullName || 'Khách'}</p>
                            <p className="text-sm text-muted-foreground">
                              {booking.date} - {booking.time} • {booking.guests} người
                            </p>
                            {booking.notes && (
                              <p className="text-xs text-muted-foreground mt-1">
                                Ghi chú: {booking.notes}
                              </p>
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <Button size="sm" variant="outline">
                            <Phone className="w-4 h-4 mr-1" />
                            Liên hệ
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleBookingAction(booking.id, 'reject')}
                          >
                            <XCircle className="w-4 h-4 mr-1" />
                            Từ chối
                          </Button>
                          <Button
                            size="sm"
                            onClick={() => handleBookingAction(booking.id, 'approve')}
                          >
                            <CheckCircle className="w-4 h-4 mr-1" />
                            Duyệt
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Available Tables Overview */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Table className="w-5 h-5" />
                  Tổng quan bàn ăn
                  <Badge variant="secondary" className="ml-auto">
                    {availableTables.length} bàn trống
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-600">{availableTables.length}</div>
                    <div className="text-sm text-muted-foreground">Trống</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-red-600">{occupiedTables.length}</div>
                    <div className="text-sm text-muted-foreground">Đang dùng</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-600">{reservedTables.length}</div>
                    <div className="text-sm text-muted-foreground">Đã đặt</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-orange-600">{pendingCheckInTables.length}</div>
                    <div className="text-sm text-muted-foreground">Chờ check-in</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Check-in Tab */}
          <TabsContent value="checkin" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Check-in khách hàng đã được duyệt</h2>
              <Badge variant="outline">{pendingCheckInTables.length} bàn chờ check-in</Badge>
            </div>

            {pendingCheckInTables.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <Calendar className="w-12 h-12 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">Không có booking nào đã duyệt chờ check-in</h3>
                  <p className="text-muted-foreground text-center">
                    Tất cả booking đã duyệt hôm nay đã được check-in hoặc chưa có booking nào được duyệt.
                  </p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {pendingCheckInTables.map((table) => {
                  // Tìm booking tương ứng cho bàn này
                  const booking = bookings.find(b => b.tableId === table.id && b.status === 'CONFIRMED');
                  return (
                    <Card key={table.id} className="hover:shadow-md transition-shadow">
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <CardTitle className="text-lg">{table.tableName}</CardTitle>
                          <Badge variant="secondary">Chờ check-in</Badge>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="flex items-center gap-2 text-sm">
                          <Users className="w-4 h-4 text-muted-foreground" />
                          <span>Khách hàng: {booking?.customer?.fullName || 'N/A'}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                          <Clock className="w-4 h-4 text-muted-foreground" />
                          <span>Thời gian: {booking?.time || 'N/A'}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                          <UserPlus className="w-4 h-4 text-muted-foreground" />
                          <span>{booking?.guests || table.capacity} người</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                          <MapPin className="w-4 h-4 text-muted-foreground" />
                          <span>{table.location || 'Chưa xác định'}</span>
                        </div>
                        {booking?.notes && (
                          <div className="text-sm text-muted-foreground bg-muted/50 p-2 rounded">
                            {booking.notes}
                          </div>
                        )}
                        <Button
                          className="w-full"
                          onClick={() => handleCheckIn(table.id)}
                        >
                          <CheckCircle className="w-4 h-4 mr-2" />
                          Check-in bàn này
                        </Button>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </TabsContent>

          {/* QR Codes Tab */}
          <TabsContent value="qrcodes" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">QR Codes bàn ăn</h2>
              <Button onClick={() => window.print()}>
                <Printer className="w-4 h-4 mr-2" />
                In QR Codes
              </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {tables.map((table) => (
                <Card key={table.id} className="p-6 text-center">
                  {table.qrCode ? (
                    <>
                      <div className="w-48 h-48 mx-auto mb-4 bg-white border-2 border-gray-200 rounded-lg flex items-center justify-center p-2">
                        <img
                          src={getQRCodeImageUrl(table.id)}
                          alt={`QR Code for ${table.tableName}`}
                          className="w-full h-full object-contain"
                        />
                      </div>

                      <h3 className="font-semibold text-lg mb-2">{table.tableName}</h3>
                      <p className="text-sm text-muted-foreground mb-2">
                        {table.capacity} người • {table.tableType}
                      </p>
                      <p className="text-xs text-muted-foreground mb-4 font-mono">
                        {table.qrCode}
                      </p>

                      <div className="space-y-2">
                        <Button
                          size="sm"
                          className="w-full"
                          onClick={() => handleSendQRToESP32(table.id)}
                        >
                          <QrCode className="w-4 h-4 mr-2" />
                          Gửi tới ESP32
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          className="w-full"
                          onClick={() => navigator.clipboard.writeText(table.qrCode || '')}
                        >
                          <Download className="w-4 h-4 mr-2" />
                          Copy Code
                        </Button>
                        <p className="text-xs text-muted-foreground">
                          URL: {getAppUrl()}/menu/{table.qrCode}
                        </p>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="w-32 h-32 mx-auto mb-4 bg-gray-100 border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center">
                        <div className="text-center">
                          <QrCode className="w-12 h-12 mx-auto mb-2 text-gray-400" />
                          <p className="text-xs text-gray-500">Chưa có QR</p>
                        </div>
                      </div>
                      <h3 className="font-semibold text-lg mb-2">{table.tableName}</h3>
                      <Button
                        size="sm"
                        className="w-full"
                        onClick={async () => {
                          try {
                            const apiUrl = getApiBaseUrl();
                            const response = await fetch(`${apiUrl}/api/tables/${table.id}/generate-qr`, {
                              method: 'POST',
                            });
                            if (response.ok) {
                              toast.success('Đã tạo QR code');
                              loadDashboardData();
                            }
                          } catch (error) {
                            toast.error('Tạo QR code thất bại');
                          }
                        }}
                      >
                        Tạo QR Code
                      </Button>
                    </>
                  )}
                </Card>
              ))}
            </div>

            <Card className="p-6">
              <h3 className="font-semibold mb-4">Hướng dẫn sử dụng QR Codes</h3>
              <div className="space-y-3 text-sm text-muted-foreground">
                <p>
                  <strong>1. In QR Codes:</strong> Sử dụng nút "In QR Codes" để in tất cả mã QR cho các bàn.
                </p>
                <p>
                  <strong>2. Dán lên bàn:</strong> Dán mã QR lên mỗi bàn tương ứng.
                </p>
                <p>
                  <strong>3. Khách hàng quét:</strong> Khách quét QR để truy cập menu và gọi món trực tiếp.
                </p>
                <p>
                  <strong>4. URL format:</strong> {getAppUrl()}/menu/[QR_CODE]
                </p>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      {/* Payment Dialog */}
      <Dialog open={paymentDialogOpen} onOpenChange={setPaymentDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Thanh toán đơn hàng #{selectedOrderForPayment?.id}</DialogTitle>
            <DialogDescription>
              Tổng tiền: {selectedOrderForPayment?.totalAmount?.toLocaleString('vi-VN')}đ
            </DialogDescription>
          </DialogHeader>

          {!paymentMethod ? (
            // Chọn phương thức thanh toán
            <div className="grid grid-cols-3 gap-4 py-4">
              <Button
                variant="outline"
                className="h-32 flex flex-col gap-3"
                onClick={() => setPaymentMethod('CASH')}
              >
                <Wallet className="w-8 h-8" />
                <span className="text-lg font-semibold">Tiền mặt</span>
              </Button>
              <Button
                variant="outline"
                className="h-32 flex flex-col gap-3"
                onClick={() => setPaymentMethod('SEPAY')}
              >
                <QrCode className="w-8 h-8" />
                <span className="text-lg font-semibold">QR Code (SePay)</span>
              </Button>
              <Button
                variant="outline"
                className="h-32 flex flex-col gap-3"
                onClick={() => setPaymentMethod('PAYOS')}
              >
                <CreditCard className="w-8 h-8" />
                <span className="text-lg font-semibold">PayOS</span>
              </Button>
            </div>
          ) : paymentMethod === 'CASH' ? (
            // Thanh toán tiền mặt
            <div className="space-y-4 py-4">
              <div className="text-center">
                <Wallet className="w-16 h-16 mx-auto mb-4 text-green-600" />
                <p className="text-lg mb-2">Xác nhận thanh toán tiền mặt</p>
                <p className="text-3xl font-bold text-green-600">
                  {selectedOrderForPayment?.totalAmount?.toLocaleString('vi-VN')}đ
                </p>
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  className="flex-1"
                  onClick={() => setPaymentMethod(null)}
                >
                  Quay lại
                </Button>
                <Button
                  className="flex-1"
                  onClick={handleCashPayment}
                >
                  Xác nhận thanh toán
                </Button>
              </div>
            </div>
          ) : paymentMethod === 'SEPAY' ? (
            // Thanh toán SePay
            <div>
              <SepayPayment
                orderId={selectedOrderForPayment?.id || 0}
                amount={selectedOrderForPayment?.totalAmount || 0}
                description={`Thanh toán đơn hàng #${selectedOrderForPayment?.id}`}
                onPaymentSuccess={handleSepayPaymentSuccess}
                onPaymentFailed={handleSepayPaymentFailed}
                onCancel={() => setPaymentMethod(null)}
              />
            </div>
          ) : (
            // Thanh toán PayOS
            <div>
              <PayOSPayment
                orderIds={[selectedOrderForPayment?.id || 0]}
                amount={selectedOrderForPayment?.totalAmount || 0}
                description={`Thanh toán đơn hàng #${selectedOrderForPayment?.id}`}
                items={preparePayOSItems(selectedOrderForPayment!)}
                mode="redirect"
                onPaymentSuccess={handlePayOSPaymentSuccess}
                onPaymentFailed={handlePayOSPaymentFailed}
                onCancel={() => setPaymentMethod(null)}
              />
            </div>
          )}
        </DialogContent>
      </Dialog>

    </div>
  );
}

export default function StaffDashboard() {
  return (
    <StaffOnly>
      <StaffDashboardContent />
    </StaffOnly>
  );
}
