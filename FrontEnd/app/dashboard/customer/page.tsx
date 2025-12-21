'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { CustomerOnly } from '@/lib/components/protected-route';
import { useAuth } from '@/lib/context/auth-context';
import {
  getMenuItems,
  getCategories,
  getTablesList,
  getOrders,
  createBooking
} from '@/lib/api';
import type {
  MenuItem,
  Category,
  RestaurantTable,
  Order,
  Booking
} from '@/lib/types';
import {
  LogOut,
  Users,
  ShoppingCart,
  Calendar,
  QrCode,
  Clock,
  MapPin,
  Phone,
  Star
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function CustomerDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("menu");

  // State for dashboard data
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [cart, setCart] = useState<{ menuItem: MenuItem; quantity: number; notes?: string }[]>([]);
  const [selectedTable, setSelectedTable] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [menuRes, categoriesRes, tablesRes, ordersRes] = await Promise.all([
        getMenuItems(),
        getCategories(),
        getTablesList(),
        getOrders(),
      ]);

      setMenuItems(menuRes.menuItems || []);
      setCategories(categoriesRes.categories || []);
      setTables(tablesRes.tables || []);
      // Filter orders for current user
      setOrders(ordersRes.orders?.filter(order => order.customerId === user?.id) || []);
    } catch (error) {
      console.error('Error loading dashboard data:', error);
      toast.error('Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    toast.success('Đã đăng xuất');
    router.push('/');
  };

  const handleAddToCart = (menuItem: MenuItem) => {
    const existingItem = cart.find(item => item.menuItem.id === menuItem.id);
    if (existingItem) {
      setCart(cart.map(item =>
        item.menuItem.id === menuItem.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setCart([...cart, { menuItem, quantity: 1 }]);
    }
    toast.success(`Đã thêm ${menuItem.name} vào giỏ hàng`);
  };

  const handleUpdateQuantity = (menuItemId: number, quantity: number) => {
    if (quantity <= 0) {
      setCart(cart.filter(item => item.menuItem.id !== menuItemId));
    } else {
      setCart(cart.map(item =>
        item.menuItem.id === menuItemId
          ? { ...item, quantity }
          : item
      ));
    }
  };

  const handleBookTable = async (bookingData: {
    date: string;
    time: string;
    guests: number;
    notes?: string;
  }) => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để đặt bàn');
      return;
    }

    if (!selectedTable) {
      toast.error('Vui lòng chọn bàn');
      return;
    }

    try {
      await createBooking({
        customerId: user.id,
        tableId: selectedTable,
        ...bookingData
      });
      toast.success('Đặt bàn thành công!');
      loadDashboardData(); // Refresh data
    } catch (error) {
      console.error('Error booking table:', error);
      toast.error('Đặt bàn thất bại');
    }
  };

  const getTotalPrice = () => {
    return cart.reduce((total, item) => total + (item.menuItem.price * item.quantity), 0);
  };

  const availableTables = tables.filter(table => table.status === 'AVAILABLE');

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-red-500 rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              👤
            </div>
            <div>
              <h1 className="text-xl font-bold">RestroFlow</h1>
              <p className="text-sm text-muted-foreground">Chào mừng {user?.username}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary" className="bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200">
              CUSTOMER
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
            <TabsTrigger value="menu">Thực đơn</TabsTrigger>
            <TabsTrigger value="booking">Đặt bàn</TabsTrigger>
            <TabsTrigger value="orders">Đơn hàng</TabsTrigger>
            <TabsTrigger value="cart">Giỏ hàng ({cart.length})</TabsTrigger>
          </TabsList>

          {/* Menu Tab */}
          <TabsContent value="menu" className="space-y-6">
            {/* Categories Filter */}
            <div className="flex flex-wrap gap-3 justify-center">
              <Button
                variant="outline"
                onClick={() => setActiveTab('menu')}
                className="bg-primary/10 border-primary text-primary"
              >
                Tất cả
              </Button>
              {categories.map((category) => (
                <Button
                  key={category.id}
                  variant="outline"
                  onClick={() => setActiveTab('menu')}
                >
                  {category.name}
                </Button>
              ))}
            </div>

            {/* Menu Items Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {menuItems
                .filter(item => item.isAvailable)
                .map((item) => (
                <Card key={item.id} className="hover:shadow-lg transition-shadow">
                  <CardContent className="p-4">
                    <div className="text-center mb-4">
                      <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-2">
                        🍽️
                      </div>
                      <h3 className="font-semibold">{item.name}</h3>
                      <p className="text-sm text-muted-foreground">{item.description}</p>
                    </div>
                    <div className="flex items-center justify-between mb-4">
                      <span className="text-lg font-bold text-primary">
                        {item.price.toLocaleString('vi-VN')}đ
                      </span>
                      <Badge variant="outline">{item.category?.name}</Badge>
                    </div>
                    <Button
                      className="w-full"
                      onClick={() => handleAddToCart(item)}
                    >
                      Thêm vào giỏ
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>

          {/* Booking Tab */}
          <TabsContent value="booking" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Đặt bàn</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <label className="text-sm font-medium">Ngày</label>
                    <input
                      type="date"
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Giờ</label>
                    <select className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary">
                      <option value="">Chọn giờ</option>
                      <option value="11:00">11:00 AM</option>
                      <option value="12:00">12:00 PM</option>
                      <option value="18:00">6:00 PM</option>
                      <option value="19:00">7:00 PM</option>
                      <option value="20:00">8:00 PM</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-medium">Số người</label>
                    <input
                      type="number"
                      min="1"
                      max="20"
                      placeholder="4"
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Ghi chú (tùy chọn)</label>
                    <textarea
                      placeholder="Yêu cầu đặc biệt..."
                      rows={3}
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <Button className="w-full">
                    <Calendar className="w-4 h-4 mr-2" />
                    Đặt bàn
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Bàn trống</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-3">
                    {availableTables.map((table) => (
                      <Button
                        key={table.id}
                        variant={selectedTable === table.id ? "default" : "outline"}
                        className="h-16 flex flex-col items-center justify-center"
                        onClick={() => setSelectedTable(table.id)}
                      >
                        <Users className="w-5 h-5 mb-1" />
                        <span className="text-sm">Bàn {table.tableNumber}</span>
                        <span className="text-xs">{table.capacity} người</span>
                      </Button>
                    ))}
                  </div>
                  {availableTables.length === 0 && (
                    <p className="text-muted-foreground text-center py-8">
                      Hiện tại không có bàn trống
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* Orders Tab */}
          <TabsContent value="orders" className="space-y-6">
            <h2 className="text-2xl font-bold">Lịch sử đơn hàng</h2>

            {orders.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <ShoppingCart className="w-16 h-16 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">Chưa có đơn hàng nào</h3>
                  <p className="text-muted-foreground text-center">
                    Bạn chưa có đơn hàng nào. Hãy đặt món ngay!
                  </p>
                  <Button className="mt-4" onClick={() => setActiveTab('menu')}>
                    Xem thực đơn
                  </Button>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-4">
                {orders.map((order) => (
                  <Card key={order.id}>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-lg">Đơn hàng #{order.id}</CardTitle>
                        <Badge variant={
                          order.status === 'ACTIVE' ? 'default' :
                          order.status === 'COMPLETED' ? 'secondary' : 'outline'
                        }>
                          {order.status === 'ACTIVE' ? 'Đang hoạt động' :
                           order.status === 'COMPLETED' ? 'Hoàn thành' : 'Đã hủy'}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {new Date(order.createdAt).toLocaleDateString('vi-VN')} •
                        Bàn {order.table?.tableNumber || order.tableId}
                      </p>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2 mb-4">
                        {order.items.map((item) => (
                          <div key={item.id} className="flex justify-between text-sm">
                            <span>{item.menuItem.name} x{item.quantity}</span>
                            <span>{(item.unitPrice * item.quantity).toLocaleString('vi-VN')}đ</span>
                          </div>
                        ))}
                      </div>
                      <div className="flex justify-between font-semibold text-lg pt-4 border-t">
                        <span>Tổng cộng:</span>
                        <span className="text-primary">{order.totalAmount.toLocaleString('vi-VN')}đ</span>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>

          {/* Cart Tab */}
          <TabsContent value="cart" className="space-y-6">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2">
                <h2 className="text-2xl font-bold mb-4">Giỏ hàng của bạn</h2>

                {cart.length === 0 ? (
                  <Card>
                    <CardContent className="flex flex-col items-center justify-center py-12">
                      <ShoppingCart className="w-16 h-16 text-muted-foreground mb-4" />
                      <h3 className="text-lg font-semibold mb-2">Giỏ hàng trống</h3>
                      <p className="text-muted-foreground text-center mb-4">
                        Hãy thêm món ăn vào giỏ hàng của bạn
                      </p>
                      <Button onClick={() => setActiveTab('menu')}>
                        Xem thực đơn
                      </Button>
                    </CardContent>
                  </Card>
                ) : (
                  <div className="space-y-4">
                    {cart.map((item) => (
                      <Card key={item.menuItem.id}>
                        <CardContent className="p-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-4">
                              <div className="w-12 h-12 bg-muted rounded-lg flex items-center justify-center">
                                🍽️
                              </div>
                              <div>
                                <h3 className="font-semibold">{item.menuItem.name}</h3>
                                <p className="text-sm text-muted-foreground">
                                  {item.menuItem.price.toLocaleString('vi-VN')}đ
                                </p>
                              </div>
                            </div>
                            <div className="flex items-center gap-3">
                              <div className="flex items-center gap-2">
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleUpdateQuantity(item.menuItem.id, item.quantity - 1)}
                                >
                                  -
                                </Button>
                                <span className="w-8 text-center">{item.quantity}</span>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleUpdateQuantity(item.menuItem.id, item.quantity + 1)}
                                >
                                  +
                                </Button>
                              </div>
                              <span className="font-semibold">
                                {(item.menuItem.price * item.quantity).toLocaleString('vi-VN')}đ
                              </span>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                )}
              </div>

              <div>
                <Card className="sticky top-4">
                  <CardHeader>
                    <CardTitle>Tóm tắt đơn hàng</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-2">
                      {cart.map((item) => (
                        <div key={item.menuItem.id} className="flex justify-between text-sm">
                          <span>{item.menuItem.name} x{item.quantity}</span>
                          <span>{(item.menuItem.price * item.quantity).toLocaleString('vi-VN')}đ</span>
                        </div>
                      ))}
                    </div>
                    <div className="border-t pt-4">
                      <div className="flex justify-between font-bold text-lg">
                        <span>Tổng cộng:</span>
                        <span className="text-primary">{getTotalPrice().toLocaleString('vi-VN')}đ</span>
                      </div>
                    </div>
                    <Button
                      className="w-full"
                      disabled={cart.length === 0}
                      onClick={() => toast.info('Tính năng đặt hàng đang được phát triển')}
                    >
                      <QrCode className="w-4 h-4 mr-2" />
                      Đặt hàng
                    </Button>
                  </CardContent>
                </Card>
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

export default function CustomerDashboard() {
  return (
    <CustomerOnly>
      <CustomerDashboardContent />
    </CustomerOnly>
  );
}
