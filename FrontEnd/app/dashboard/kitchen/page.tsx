'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { KitchenOnly } from '@/lib/components/protected-route';
import { useAuth } from '@/lib/context/auth-context';
import {
  getOrders,
  updateOrderStatus,
  updateOrderItemStatus
} from '@/lib/api';
import type { Order } from '@/lib/types';
import {
  LogOut,
  ChefHat,
  Clock,
  CheckCircle,
  Play,
  AlertTriangle,
  Flame
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function KitchenDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();

  // State for orders
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
    // Set up polling for real-time updates
    const interval = setInterval(loadOrders, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const response = await getOrders();
      // Filter orders that have items in preparation
      const kitchenOrders = response.orders.filter(order =>
        order.items.some(item =>
          ['PENDING', 'PREPARING', 'READY'].includes(item.status)
        )
      );
      setOrders(kitchenOrders);
    } catch (error) {
      console.error('Error loading orders:', error);
      toast.error('Không thể tải đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    toast.success('Đã đăng xuất');
    router.push('/');
  };

  const handleStartCooking = async (orderId: number, itemId: number) => {
    try {
      await updateOrderItemStatus(orderId, itemId, 'PREPARING');
      toast.success('Đã bắt đầu nấu món');
      loadOrders(); // Refresh data
    } catch (error) {
      console.error('Error starting cooking:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const handleFinishCooking = async (orderId: number, itemId: number) => {
    try {
      await updateOrderItemStatus(orderId, itemId, 'READY');
      toast.success('Món đã sẵn sàng phục vụ');
      loadOrders(); // Refresh data
    } catch (error) {
      console.error('Error finishing cooking:', error);
      toast.error('Không thể cập nhật trạng thái');
    }
  };

  const handleOrderReady = async (orderId: number) => {
    try {
      await updateOrderStatus(orderId, 'COMPLETED');
      toast.success('Đơn hàng đã hoàn thành');
      loadOrders(); // Refresh data
    } catch (error) {
      console.error('Error completing order:', error);
      toast.error('Không thể hoàn thành đơn hàng');
    }
  };

  // Categorize orders by status
  const pendingOrders = orders.filter(order =>
    order.items.some(item => item.status === 'PENDING')
  );

  const preparingOrders = orders.filter(order =>
    order.items.some(item => item.status === 'PREPARING')
  );

  const readyOrders = orders.filter(order =>
    order.items.every(item => item.status === 'READY')
  );

  const getItemStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'PREPARING': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'READY': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getItemStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING': return <Clock className="w-4 h-4" />;
      case 'PREPARING': return <Flame className="w-4 h-4" />;
      case 'READY': return <CheckCircle className="w-4 h-4" />;
      default: return <Clock className="w-4 h-4" />;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-emerald-500 rounded-lg flex items-center justify-center text-primary-foreground font-bold">
              👨‍🍳
            </div>
            <div>
              <h1 className="text-xl font-bold">Kitchen Display System</h1>
              <p className="text-sm text-muted-foreground">Chào mừng đầu bếp {user?.username}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary" className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200">
              KITCHEN
            </Badge>
            <Button variant="outline" onClick={handleLogout} className="flex items-center gap-2">
              <LogOut className="w-4 h-4" />
              Đăng xuất
            </Button>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Chờ nấu</CardTitle>
              <Clock className="h-4 w-4 text-yellow-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-yellow-600">{pendingOrders.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Đang nấu</CardTitle>
              <Flame className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">{preparingOrders.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Sẵn sàng</CardTitle>
              <CheckCircle className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">{readyOrders.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Tổng đơn</CardTitle>
              <ChefHat className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{orders.length}</div>
            </CardContent>
          </Card>
        </div>

        {/* Orders Display */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Pending Orders */}
          <div>
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <Clock className="w-5 h-5 text-yellow-600" />
              Chờ nấu ({pendingOrders.length})
            </h2>
            <div className="space-y-4">
              {pendingOrders.map((order) => (
                <Card key={order.id} className="border-l-4 border-l-yellow-500">
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">Đơn #{order.id}</CardTitle>
                      <Badge variant="outline" className="bg-yellow-50 text-yellow-700">
                        Bàn {order.table?.tableNumber || order.tableId}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground">
                      {new Date(order.createdAt).toLocaleTimeString('vi-VN')}
                    </p>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {order.items
                        .filter(item => item.status === 'PENDING')
                        .map((item) => (
                        <div key={item.id} className={`p-3 rounded-lg border ${getItemStatusColor(item.status)}`}>
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">{item.menuItem.name}</span>
                            <span className="text-sm">x{item.quantity}</span>
                          </div>
                          {item.notes && (
                            <p className="text-sm text-muted-foreground mb-2">
                              Ghi chú: {item.notes}
                            </p>
                          )}
                          <Button
                            size="sm"
                            className="w-full"
                            onClick={() => handleStartCooking(order.id, item.id)}
                          >
                            <Play className="w-4 h-4 mr-2" />
                            Bắt đầu nấu
                          </Button>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* Preparing Orders */}
          <div>
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <Flame className="w-5 h-5 text-orange-600" />
              Đang nấu ({preparingOrders.length})
            </h2>
            <div className="space-y-4">
              {preparingOrders.map((order) => (
                <Card key={order.id} className="border-l-4 border-l-orange-500">
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">Đơn #{order.id}</CardTitle>
                      <Badge variant="outline" className="bg-orange-50 text-orange-700">
                        Bàn {order.table?.tableNumber || order.tableId}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {order.items
                        .filter(item => item.status === 'PREPARING')
                        .map((item) => (
                        <div key={item.id} className={`p-3 rounded-lg border ${getItemStatusColor(item.status)}`}>
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">{item.menuItem.name}</span>
                            <span className="text-sm">x{item.quantity}</span>
                          </div>
                          {item.notes && (
                            <p className="text-sm text-muted-foreground mb-2">
                              Ghi chú: {item.notes}
                            </p>
                          )}
                          <Button
                            size="sm"
                            variant="outline"
                            className="w-full border-green-500 text-green-700 hover:bg-green-50"
                            onClick={() => handleFinishCooking(order.id, item.id)}
                          >
                            <CheckCircle className="w-4 h-4 mr-2" />
                            Hoàn thành
                          </Button>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>

          {/* Ready Orders */}
          <div>
            <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <CheckCircle className="w-5 h-5 text-green-600" />
              Sẵn sàng ({readyOrders.length})
            </h2>
            <div className="space-y-4">
              {readyOrders.map((order) => (
                <Card key={order.id} className="border-l-4 border-l-green-500">
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">Đơn #{order.id}</CardTitle>
                      <Badge variant="outline" className="bg-green-50 text-green-700">
                        Bàn {order.table?.tableNumber || order.tableId}
                      </Badge>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {order.items.map((item) => (
                        <div key={item.id} className={`p-3 rounded-lg border ${getItemStatusColor(item.status)}`}>
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-medium">{item.menuItem.name}</span>
                            <span className="text-sm">x{item.quantity}</span>
                          </div>
                          <div className="flex items-center gap-2 text-sm">
                            {getItemStatusIcon(item.status)}
                            <span>Sẵn sàng phục vụ</span>
                          </div>
                        </div>
                      ))}
                      <Button
                        className="w-full bg-green-600 hover:bg-green-700"
                        onClick={() => handleOrderReady(order.id)}
                      >
                        <CheckCircle className="w-4 h-4 mr-2" />
                        Đã giao hết món
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </div>

        {/* Empty State */}
        {orders.length === 0 && !loading && (
          <Card className="mt-8">
            <CardContent className="flex flex-col items-center justify-center py-12">
              <ChefHat className="w-16 h-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">Không có đơn hàng nào</h3>
              <p className="text-muted-foreground text-center">
                Hiện tại không có đơn hàng nào cần xử lý trong bếp.
              </p>
            </CardContent>
          </Card>
        )}

        {/* Kitchen Alerts */}
        {pendingOrders.length > 5 && (
          <Card className="mt-6 border-l-4 border-l-red-500 bg-red-50 dark:bg-red-900/20">
            <CardContent className="flex items-center gap-3 py-4">
              <AlertTriangle className="w-6 h-6 text-red-600" />
              <div>
                <p className="font-semibold text-red-800 dark:text-red-200">
                  Cảnh báo: Quá nhiều đơn hàng chờ xử lý!
                </p>
                <p className="text-sm text-red-700 dark:text-red-300">
                  Có {pendingOrders.length} đơn hàng đang chờ. Hãy ưu tiên xử lý các đơn hàng cũ trước.
                </p>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}

export default function KitchenDashboard() {
  return (
    <KitchenOnly>
      <KitchenDashboardContent />
    </KitchenOnly>
  );
}
