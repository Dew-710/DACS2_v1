'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { AdminOnly } from '@/lib/components/protected-route';
import { useAuth } from '@/lib/context/auth-context';
import {
  getTablesList,
  getMenuItems,
  getOrders,
  getCategories,
  getUsersList,
  getBookings
} from '@/lib/api';
import type {
  RestaurantTable,
  MenuItem,
  Order,
  Category,
  User,
  Booking
} from '@/lib/types';
import {
  LogOut,
  Users,
  ChefHat,
  ShoppingCart,
  Table,
  Package,
  Calendar,
  BarChart3,
  Settings,
  TrendingUp,
  DollarSign,
  Clock,
  CheckCircle,
  XCircle
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function AdminDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("overview");

  // State for dashboard data
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [
        tablesRes,
        menuRes,
        ordersRes,
        categoriesRes,
        usersRes,
        bookingsRes
      ] = await Promise.all([
        getTablesList(),
        getMenuItems(),
        getOrders(),
        getCategories(),
        getUsersList(),
        getBookings(),
      ]);

      setTables(tablesRes.tables || []);
      setMenuItems(menuRes.menuItems || []);
      setOrders(ordersRes.orders || []);
      setCategories(categoriesRes.categories || []);
      setUsers(usersRes.users || []);
      setBookings(bookingsRes.bookings || []);
    } catch (error) {
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

  // Calculate statistics
  const totalRevenue = orders.reduce((sum, order) => sum + order.totalAmount, 0);
  const activeOrders = orders.filter(order => order.status === 'ACTIVE');
  const pendingBookings = bookings.filter(booking => booking.status === 'PENDING');
  const availableTables = tables.filter(table => table.status === 'VACANT');
  const occupiedTables = tables.filter(table => table.status === 'OCCUPIED');

  const stats = [
    {
      title: "Tổng doanh thu",
      value: `${totalRevenue.toLocaleString('vi-VN')}đ`,
      icon: DollarSign,
      color: "text-green-600",
      bgColor: "bg-green-50 dark:bg-green-900/20"
    },
    {
      title: "Đơn hàng hoạt động",
      value: activeOrders.length.toString(),
      icon: ShoppingCart,
      color: "text-blue-600",
      bgColor: "bg-blue-50 dark:bg-blue-900/20"
    },
    {
      title: "Bàn trống",
      value: `${availableTables.length}/${tables.length}`,
      icon: Table,
      color: "text-emerald-600",
      bgColor: "bg-emerald-50 dark:bg-emerald-900/20"
    },
    {
      title: "Đặt bàn chờ duyệt",
      value: pendingBookings.length.toString(),
      icon: Calendar,
      color: "text-orange-600",
      bgColor: "bg-orange-50 dark:bg-orange-900/20"
    }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              👑
            </div>
            <div>
              <h1 className="text-xl font-bold">Admin Dashboard</h1>
              <p className="text-sm text-muted-foreground">Chào mừng Admin {user?.username}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary" className="bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200">
              ADMIN
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
            <TabsTrigger value="overview">Tổng quan</TabsTrigger>
            <TabsTrigger value="users">Người dùng</TabsTrigger>
            <TabsTrigger value="orders">Đơn hàng</TabsTrigger>
            <TabsTrigger value="menu">Thực đơn</TabsTrigger>
            <TabsTrigger value="tables">Bàn ăn</TabsTrigger>
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {stats.map((stat, idx) => (
                <Card key={idx}>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
                    <div className={`p-2 rounded-lg ${stat.bgColor}`}>
                      <stat.icon className={`h-4 w-4 ${stat.color}`} />
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">{stat.value}</div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Recent Activity */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Đơn hàng gần đây</CardTitle>
                </CardHeader>
                <CardContent>
                  {loading ? (
                    <div className="flex items-center justify-center py-8">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                    </div>
                  ) : orders.length === 0 ? (
                    <p className="text-muted-foreground text-center py-8">Chưa có đơn hàng</p>
                  ) : (
                    <div className="space-y-3">
                      {orders.slice(0, 5).map((order) => (
                        <div key={order.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                          <div>
                            <p className="font-medium">Đơn #{order.id}</p>
                            <p className="text-sm text-muted-foreground">
                              {new Date(order.createdAt).toLocaleDateString('vi-VN')}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-medium">{order.totalAmount.toLocaleString('vi-VN')}đ</p>
                            <Badge variant={order.status === 'ACTIVE' ? 'default' : 'secondary'}>
                              {order.status === 'ACTIVE' ? 'Đang hoạt động' :
                               order.status === 'COMPLETED' ? 'Hoàn thành' : 'Đã hủy'}
                            </Badge>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Đặt bàn chờ duyệt</CardTitle>
                </CardHeader>
                <CardContent>
                  {loading ? (
                    <div className="flex items-center justify-center py-8">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                    </div>
                  ) : pendingBookings.length === 0 ? (
                    <p className="text-muted-foreground text-center py-8">Không có đặt bàn nào chờ duyệt</p>
                  ) : (
                    <div className="space-y-3">
                      {pendingBookings.slice(0, 5).map((booking) => (
                        <div key={booking.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                          <div>
                            <p className="font-medium">{booking.customer?.username || 'Khách'}</p>
                            <p className="text-sm text-muted-foreground">
                              {booking.date} - {booking.time}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="text-sm">{booking.guests} người</p>
                            <div className="flex gap-2 mt-1">
                              <Button size="sm" variant="outline" className="h-6 px-2">
                                <CheckCircle className="w-3 h-3" />
                              </Button>
                              <Button size="sm" variant="outline" className="h-6 px-2">
                                <XCircle className="w-3 h-3" />
                              </Button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* Users Tab */}
          <TabsContent value="users" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Quản lý người dùng</h2>
              <Button>Thêm người dùng</Button>
            </div>

            <Card>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                  </div>
                ) : users.length === 0 ? (
                  <p className="text-muted-foreground text-center py-8">Chưa có người dùng</p>
                ) : (
                  <div className="divide-y">
                    {users.map((userItem) => (
                      <div key={userItem.id} className="flex items-center justify-between p-4">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-primary/10 rounded-full flex items-center justify-center">
                            <Users className="w-5 h-5" />
                          </div>
                          <div>
                            <p className="font-medium">{userItem.username}</p>
                            <p className="text-sm text-muted-foreground">{userItem.email}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-3">
                          <Badge variant={
                            userItem.role === 'ADMIN' ? 'default' :
                            userItem.role === 'STAFF' ? 'secondary' :
                            userItem.role === 'KITCHEN' ? 'outline' : 'outline'
                          }>
                            {userItem.role}
                          </Badge>
                          <Button variant="outline" size="sm">Chỉnh sửa</Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Orders Tab */}
          <TabsContent value="orders" className="space-y-6">
            <h2 className="text-2xl font-bold">Quản lý đơn hàng</h2>
            <Card>
              <CardContent>
                <p className="text-muted-foreground">Chức năng quản lý đơn hàng sẽ được phát triển</p>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Menu Tab */}
          <TabsContent value="menu" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Quản lý thực đơn</h2>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  onClick={async () => {
                    try {
                      await fetch('/api/categories/cleanup-duplicates', { method: 'POST' });
                      toast.success('Cleanup initiated. Please run the SQL script.');
                      loadDashboardData();
                    } catch (error) {
                      toast.error('Cleanup failed');
                    }
                  }}
                >
                  Cleanup Categories
                </Button>
                <Button>Thêm món ăn</Button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle>Danh mục ({categories.length})</CardTitle>
                </CardHeader>
                <CardContent>
                  {categories.map((category) => (
                    <div key={category.id} className="flex items-center justify-between py-2">
                      <span>{category.name}</span>
                      <Button variant="outline" size="sm">Sửa</Button>
                    </div>
                  ))}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Món ăn ({menuItems.length})</CardTitle>
                </CardHeader>
                <CardContent>
                  {menuItems.slice(0, 5).map((item) => (
                    <div key={item.id} className="flex items-center justify-between py-2">
                      <div>
                        <p className="font-medium">{item.name}</p>
                        <p className="text-sm text-muted-foreground">{item.price}đ</p>
                      </div>
                      <Badge variant={item.isAvailable ? 'default' : 'secondary'}>
                        {item.isAvailable ? 'Có sẵn' : 'Hết'}
                      </Badge>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* Tables Tab */}
          <TabsContent value="tables" className="space-y-6">
            <div className="flex items-center justify-between">
              <h2 className="text-2xl font-bold">Quản lý bàn ăn</h2>
              <Button>Thêm bàn</Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {tables.map((table) => (
                <Card key={table.id} className={`cursor-pointer transition-all hover:shadow-lg ${
                  table.status === 'AVAILABLE' ? 'border-green-200 bg-green-50 dark:bg-green-900/20' :
                  table.status === 'OCCUPIED' ? 'border-red-200 bg-red-50 dark:bg-red-900/20' :
                  'border-gray-200'
                }`}>
                  <CardContent className="p-4 text-center">
                    <div className="w-12 h-12 mx-auto mb-2 bg-muted rounded-full flex items-center justify-center">
                      <Table className="w-6 h-6" />
                    </div>
                    <p className="font-semibold">{table.tableName}</p>
                    <p className="text-sm text-muted-foreground">{table.capacity} người</p>
                    <Badge variant={
                      table.status === 'VACANT' ? 'default' :
                      table.status === 'OCCUPIED' ? 'destructive' : 'secondary'
                    } className="mt-2">
                      {table.status === 'VACANT' ? 'Trống' :
                       table.status === 'OCCUPIED' ? 'Đang dùng' :
                       table.status === 'RESERVED' ? 'Đã đặt' :
                       table.status === 'CLEANING' ? 'Đang dọn' : 'Bảo trì'}
                    </Badge>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

export default function AdminDashboard() {
  return (
    <AdminOnly>
      <AdminDashboardContent />
    </AdminOnly>
  );
}
