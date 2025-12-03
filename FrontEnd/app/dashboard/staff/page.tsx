'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { StaffOnly } from '@/lib/components/protected-route';
import { useAuth } from '@/lib/context/auth-context';
import {
  getTablesList,
  getOrders,
  getActiveOrdersByTable,
  getBookings,
  updateTableStatus,
  checkOutTable,
  getTableCurrentOrder,
  createOrderWithCustomer
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
  UserPlus,
  Phone
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function StaffDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("tables");

  // State for dashboard data
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCheckInForm, setShowCheckInForm] = useState(false);
  const [selectedTableForCheckIn, setSelectedTableForCheckIn] = useState<number | null>(null);
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');

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

  const handleCheckIn = (tableId: number) => {
    setSelectedTableForCheckIn(tableId);
    setShowCheckInForm(true);
  };

  const handleSubmitCheckIn = async () => {
    if (!selectedTableForCheckIn || !customerName.trim()) {
      toast.error('Vui lòng nhập tên khách hàng');
      return;
    }

    try {
      // Create a new order for the table
      const newOrder = {
        items: [], // Start with empty items
        status: 'PLACED', // Initial status
        totalAmount: 0, // Initial amount
        notes: `Khách hàng: ${customerName}${customerPhone ? ` - ${customerPhone}` : ''}`
      };

      await createOrderWithCustomer(3, selectedTableForCheckIn, newOrder); // Use customer ID 3 (Alice Customer) as default

      // Update table status to OCCUPIED
      await updateTableStatus(selectedTableForCheckIn, 'OCCUPIED');

      toast.success(`Check-in thành công cho khách hàng: ${customerName}`);

      // Reset form
      setShowCheckInForm(false);
      setSelectedTableForCheckIn(null);
      setCustomerName('');
      setCustomerPhone('');

      loadDashboardData(); // Refresh data
    } catch (error) {
      console.error('Error during check-in:', error);
      toast.error('Check-in thất bại');
    }
  };

  const handleTableStatusChange = async (tableId: number, newStatus: string) => {
    try {
      await updateTableStatus(tableId, newStatus);
      toast.success(`Trạng thái bàn đã được cập nhật thành ${newStatus}`);
      loadDashboardData(); // Refresh data
    } catch (error) {
      console.error('Error updating table status:', error);
      toast.error('Cập nhật trạng thái bàn thất bại');
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

  // Calculate statistics
  const availableTables = tables.filter(table => table.status === 'AVAILABLE');
  const occupiedTables = tables.filter(table => table.status === 'OCCUPIED');
  const maintenanceTables = tables.filter(table => table.status === 'MAINTENANCE');
  const reservedTables = tables.filter(table => table.status === 'RESERVED');
  const activeOrders = orders.filter(order => order.status === 'ACTIVE');
  const pendingBookings = bookings.filter(booking => booking.status === 'PENDING');

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
          <TabsList className="grid w-full grid-cols-4 mb-8">
            <TabsTrigger value="tables">Quản lý bàn</TabsTrigger>
            <TabsTrigger value="orders">Đơn hàng</TabsTrigger>
            <TabsTrigger value="bookings">Đặt bàn</TabsTrigger>
            <TabsTrigger value="checkin">Check-in</TabsTrigger>
          </TabsList>

          {/* Tables Management Tab */}
          <TabsContent value="tables" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Quản lý bàn ăn</h2>
              <div className="flex gap-2 flex-wrap">
                <Badge variant="outline">
                  {availableTables.length} bàn trống
                </Badge>
                <Badge variant="destructive">
                  {occupiedTables.length} bàn đang dùng
                </Badge>
                {maintenanceTables.length > 0 && (
                  <Badge variant="secondary">
                    {maintenanceTables.length} bàn bảo trì
                  </Badge>
                )}
                {reservedTables.length > 0 && (
                  <Badge variant="secondary">
                    {reservedTables.length} bàn đã đặt
                  </Badge>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {tables.map((table) => (
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
                    <p className="font-semibold">Bàn {table.tableNumber}</p>
                    <p className="text-sm text-muted-foreground">{table.capacity} người</p>
                    <Badge variant={
                      table.status === 'AVAILABLE' ? 'default' :
                      table.status === 'OCCUPIED' ? 'destructive' :
                      table.status === 'RESERVED' ? 'secondary' :
                      table.status === 'MAINTENANCE' ? 'outline' : 'outline'
                    } className="mt-2">
                      {table.status === 'AVAILABLE' ? 'Trống' :
                       table.status === 'OCCUPIED' ? 'Đang dùng' :
                       table.status === 'RESERVED' ? 'Đã đặt' :
                       table.status === 'MAINTENANCE' ? 'Bảo trì' : table.status}
                    </Badge>
                    <div className="flex gap-2 mt-3">
                      {table.status === 'AVAILABLE' && (
                        <Button
                          size="sm"
                          className="flex-1"
                          onClick={() => handleCheckIn(table.id)}
                        >
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
                          Bàn {order.table?.tableNumber || order.tableId}
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
                          {order.items.map((item, idx) => (
                            <div key={idx} className="flex justify-between text-sm">
                              <span>{item.menuItem.name} x{item.quantity}</span>
                              <span>{(item.unitPrice * item.quantity).toLocaleString('vi-VN')}đ</span>
                            </div>
                          ))}
                        </div>
                      </div>
                      <div className="border-t pt-3 flex justify-between font-semibold">
                        <span>Tổng cộng:</span>
                        <span>{order.totalAmount.toLocaleString('vi-VN')}đ</span>
                      </div>
                      <div className="flex gap-2">
                        <Button size="sm" variant="outline" className="flex-1">
                          Cập nhật trạng thái
                        </Button>
                        <Button size="sm" className="flex-1">
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
                {pendingBookings.length} đặt bàn chờ duyệt
              </Badge>
            </div>

            <Card>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                  </div>
                ) : pendingBookings.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12">
                    <Calendar className="w-16 h-16 text-muted-foreground mb-4" />
                    <h3 className="text-lg font-semibold mb-2">Không có đặt bàn nào</h3>
                    <p className="text-muted-foreground text-center">
                      Hiện tại không có yêu cầu đặt bàn nào chờ xử lý.
                    </p>
                  </div>
                ) : (
                  <div className="divide-y">
                    {pendingBookings.map((booking) => (
                      <div key={booking.id} className="flex items-center justify-between p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                            <Users className="w-5 h-5" />
                          </div>
                          <div>
                            <p className="font-medium">{booking.customer?.username || 'Khách'}</p>
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
                          <Button size="sm">
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
          </TabsContent>

          {/* Check-in Tab */}
          <TabsContent value="checkin" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Check-in khách hàng</h2>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Check-in nhanh</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <label className="text-sm font-medium">Tên khách hàng</label>
                    <input
                      type="text"
                      placeholder="Nhập tên khách hàng"
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Số người</label>
                    <input
                      type="number"
                      placeholder="4"
                      min="1"
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <Button className="w-full">
                    <UserPlus className="w-4 h-4 mr-2" />
                    Check-in khách
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Hướng dẫn check-in</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3 text-sm text-muted-foreground">
                    <div className="flex items-start gap-3">
                      <div className="w-6 h-6 bg-primary/10 rounded-full flex items-center justify-center text-xs font-bold">1</div>
                      <p>Xác nhận thông tin khách hàng và số lượng người</p>
                    </div>
                    <div className="flex items-start gap-3">
                      <div className="w-6 h-6 bg-primary/10 rounded-full flex items-center justify-center text-xs font-bold">2</div>
                      <p>Chọn bàn trống phù hợp với số lượng khách</p>
                    </div>
                    <div className="flex items-start gap-3">
                      <div className="w-6 h-6 bg-primary/10 rounded-full flex items-center justify-center text-xs font-bold">3</div>
                      <p>Cập nhật trạng thái bàn thành "Đang sử dụng"</p>
                    </div>
                    <div className="flex items-start gap-3">
                      <div className="w-6 h-6 bg-primary/10 rounded-full flex items-center justify-center text-xs font-bold">4</div>
                      <p>Hướng dẫn khách đến bàn và bắt đầu phục vụ</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>
        </Tabs>
      </div>

      {/* Check-in Dialog */}
      <Dialog open={showCheckInForm} onOpenChange={setShowCheckInForm}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Check-in khách hàng</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="customerName">Tên khách hàng</Label>
              <Input
                id="customerName"
                value={customerName}
                onChange={(e) => setCustomerName(e.target.value)}
                placeholder="Nhập tên khách hàng"
              />
            </div>
            <div>
              <Label htmlFor="customerPhone">Số điện thoại (tùy chọn)</Label>
              <Input
                id="customerPhone"
                value={customerPhone}
                onChange={(e) => setCustomerPhone(e.target.value)}
                placeholder="Nhập số điện thoại"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCheckInForm(false)}>
              Hủy
            </Button>
            <Button onClick={handleSubmitCheckIn}>
              Check-in
            </Button>
          </DialogFooter>
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
