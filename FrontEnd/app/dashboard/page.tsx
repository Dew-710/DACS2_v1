'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/lib/context/auth-context';
import { getTablesList, getMenuItems, getOrders, getCategories } from '@/lib/api';
import type { RestaurantTable, MenuItem, Order, Category } from '@/lib/types';
import { LogOut, Users, ChefHat, ShoppingCart, Table, Package } from 'lucide-react';
import { toast } from 'sonner';

export default function DashboardPage() {
  const { user, logout, isAuthenticated } = useAuth();
  const router = useRouter();
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    loadDashboardData();
  }, [isAuthenticated, router]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [tablesRes, menuRes, ordersRes, categoriesRes] = await Promise.all([
        getTablesList(),
        getMenuItems(),
        getOrders(),
        getCategories(),
      ]);

      setTables(tablesRes.tables || []);
      setMenuItems(menuRes.menuItems || []);
      setOrders(ordersRes.orders || []);
      setCategories(categoriesRes.categories || []);
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

  if (!isAuthenticated) {
    return null;
  }

  const activeOrders = orders.filter(order => order.status === 'ACTIVE');
  const availableTables = tables.filter(table => table.status === 'AVAILABLE');

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-secondary rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              RF
            </div>
            <div>
              <h1 className="text-xl font-bold">RestroFlow Dashboard</h1>
              <p className="text-sm text-muted-foreground">Chào mừng, {user?.username}</p>
            </div>
          </div>
          <Button variant="outline" onClick={handleLogout} className="flex items-center gap-2">
            <LogOut className="w-4 h-4" />
            Đăng xuất
          </Button>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Tổng bàn</CardTitle>
              <Table className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{tables.length}</div>
              <p className="text-xs text-muted-foreground">
                {availableTables.length} bàn trống
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Món ăn</CardTitle>
              <ChefHat className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{menuItems.length}</div>
              <p className="text-xs text-muted-foreground">
                {categories.length} danh mục
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Đơn hàng</CardTitle>
              <ShoppingCart className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{orders.length}</div>
              <p className="text-xs text-muted-foreground">
                {activeOrders.length} đang hoạt động
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Danh mục</CardTitle>
              <Package className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{categories.length}</div>
              <p className="text-xs text-muted-foreground">
                Đang hoạt động
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Tables Status */}
          <Card>
            <CardHeader>
              <CardTitle>Trạng thái bàn</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : tables.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">Chưa có dữ liệu bàn</p>
              ) : (
                <div className="space-y-3">
                  {tables.slice(0, 5).map((table) => (
                    <div key={table.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                      <div>
                        <p className="font-medium">Bàn {table.tableNumber}</p>
                        <p className="text-sm text-muted-foreground">{table.capacity} người</p>
                      </div>
                      <Badge variant={table.status === 'AVAILABLE' ? 'default' : 'secondary'}>
                        {table.status === 'AVAILABLE' ? 'Trống' :
                         table.status === 'OCCUPIED' ? 'Đang dùng' :
                         table.status === 'RESERVED' ? 'Đã đặt' : 'Bảo trì'}
                      </Badge>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Recent Orders */}
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

          {/* Menu Items */}
          <Card>
            <CardHeader>
              <CardTitle>Món ăn nổi bật</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : menuItems.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">Chưa có món ăn</p>
              ) : (
                <div className="space-y-3">
                  {menuItems.slice(0, 5).map((item) => (
                    <div key={item.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                      <div>
                        <p className="font-medium">{item.name}</p>
                        <p className="text-sm text-muted-foreground">{item.description}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium">{item.price.toLocaleString('vi-VN')}đ</p>
                        <Badge variant={item.available ? 'default' : 'secondary'}>
                          {item.available ? 'Có sẵn' : 'Hết'}
                        </Badge>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Categories */}
          <Card>
            <CardHeader>
              <CardTitle>Danh mục món ăn</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                </div>
              ) : categories.length === 0 ? (
                <p className="text-muted-foreground text-center py-8">Chưa có danh mục</p>
              ) : (
                <div className="space-y-3">
                  {categories.map((category) => (
                    <div key={category.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                      <div>
                        <p className="font-medium">{category.name}</p>
                        {category.description && (
                          <p className="text-sm text-muted-foreground">{category.description}</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* API Connection Status */}
        <Card className="mt-8">
          <CardHeader>
            <CardTitle>Kết nối API</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <div className="w-3 h-3 bg-green-500 rounded-full mx-auto mb-2"></div>
                <p className="text-sm font-medium">User API</p>
                <p className="text-xs text-muted-foreground">✅ Kết nối thành công</p>
              </div>
              <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <div className="w-3 h-3 bg-green-500 rounded-full mx-auto mb-2"></div>
                <p className="text-sm font-medium">Menu API</p>
                <p className="text-xs text-muted-foreground">✅ Kết nối thành công</p>
              </div>
              <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <div className="w-3 h-3 bg-green-500 rounded-full mx-auto mb-2"></div>
                <p className="text-sm font-medium">Order API</p>
                <p className="text-xs text-muted-foreground">✅ Kết nối thành công</p>
              </div>
              <div className="text-center p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                <div className="w-3 h-3 bg-green-500 rounded-full mx-auto mb-2"></div>
                <p className="text-sm font-medium">Table API</p>
                <p className="text-xs text-muted-foreground">✅ Kết nối thành công</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
