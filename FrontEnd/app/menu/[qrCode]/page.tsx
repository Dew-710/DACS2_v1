'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { getTableByQr, getMenuItems, getCategories, createOrderWithCustomer, addItemsToOrder } from '@/lib/api';
import type { RestaurantTable, MenuItem, Category } from '@/lib/types';
import { ShoppingCart, Loader2, Utensils } from 'lucide-react';
import { toast } from 'sonner';

export default function CustomerMenuPage() {
  const params = useParams();
  const qrCode = params?.qrCode as string;
  
  const [table, setTable] = useState<RestaurantTable | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [cart, setCart] = useState<{ menuItem: MenuItem; quantity: number }[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (qrCode) {
      loadData();
    }
  }, [qrCode]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [tableRes, menuRes, categoriesRes] = await Promise.all([
        getTableByQr(qrCode),
        getMenuItems(),
        getCategories(),
      ]);

      setTable(tableRes.table);
      setMenuItems(menuRes.menuItems || []);
      setCategories(categoriesRes.categories || []);
    } catch (error) {
      console.error('Error loading data:', error);
      toast.error('Không thể tải dữ liệu');
    } finally {
      setLoading(false);
    }
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

  const handleSubmitOrder = async () => {
    if (!table || cart.length === 0) {
      toast.error('Vui lòng chọn món ăn');
      return;
    }

    try {
      setSubmitting(true);
      
      // Create order with default customer (ID 3)
      const newOrder = {
        items: [],
        status: 'PLACED',
        totalAmount: 0,
        notes: `Đơn hàng từ QR code - Bàn ${table.tableName}`
      };

      const orderResult = await createOrderWithCustomer(3, table.id, newOrder);
      const orderId = orderResult.order.id;

      // Add items to order - need to send full menuItem objects
      const orderItems = cart.map(item => ({
        menuItem: {
          id: item.menuItem.id,
          name: item.menuItem.name,
          price: item.menuItem.price,
          description: item.menuItem.description,
          imageUrl: item.menuItem.imageUrl,
          category: item.menuItem.category,
          isAvailable: item.menuItem.isAvailable,
          preparationTime: item.menuItem.preparationTime,
          calories: item.menuItem.calories,
          allergens: item.menuItem.allergens
        },
        quantity: item.quantity,
        price: item.menuItem.price
      }));

      await addItemsToOrder(orderId, orderItems);
      
      toast.success('Đặt món thành công! Nhân viên sẽ phục vụ bạn sớm nhất có thể.');
      setCart([]);
    } catch (error) {
      console.error('Error submitting order:', error);
      toast.error('Đặt món thất bại. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  };

  const getTotalPrice = () => {
    return cart.reduce((total, item) => total + (item.menuItem.price * item.quantity), 0);
  };

  const filteredMenuItems = selectedCategory
    ? menuItems.filter(item => item.category?.id === selectedCategory)
    : menuItems;

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="w-8 h-8 animate-spin text-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Đang tải menu...</p>
        </div>
      </div>
    );
  }

  if (!table) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="p-8 text-center">
            <Utensils className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
            <h1 className="text-2xl font-bold mb-2">Không tìm thấy bàn</h1>
            <p className="text-muted-foreground">
              Mã QR không hợp lệ hoặc bàn không tồn tại.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted">
      {/* Header */}
      <header className="border-b bg-card/80 backdrop-blur-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold">RestroFlow</h1>
              <p className="text-sm text-muted-foreground">Bàn {table.tableName}</p>
            </div>
            <Badge variant="secondary">
              {cart.length} món
            </Badge>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        {/* Categories Filter */}
        <div className="flex flex-wrap gap-2 mb-6">
          <Button
            variant={selectedCategory === null ? "default" : "outline"}
            size="sm"
            onClick={() => setSelectedCategory(null)}
          >
            Tất cả
          </Button>
          {categories.map((category) => (
            <Button
              key={category.id}
              variant={selectedCategory === category.id ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedCategory(category.id)}
            >
              {category.name}
            </Button>
          ))}
        </div>

        {/* Menu Items Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          {filteredMenuItems
            .filter(item => item.isAvailable)
            .map((item) => (
              <Card key={item.id} className="hover:shadow-lg transition-shadow">
                <CardContent className="p-4">
                  <div className="text-center mb-3">
                    <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-2">
                      🍽️
                    </div>
                    <h3 className="font-semibold">{item.name}</h3>
                    <p className="text-sm text-muted-foreground line-clamp-2">{item.description}</p>
                  </div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-lg font-bold text-primary">
                      {item.price.toLocaleString('vi-VN')}đ
                    </span>
                    {item.category && (
                      <Badge variant="outline" className="text-xs">
                        {item.category.name}
                      </Badge>
                    )}
                  </div>
                  <Button
                    className="w-full"
                    size="sm"
                    onClick={() => handleAddToCart(item)}
                  >
                    Thêm vào giỏ
                  </Button>
                </CardContent>
              </Card>
            ))}
        </div>

        {/* Cart Sidebar */}
        {cart.length > 0 && (
          <div className="fixed bottom-0 left-0 right-0 bg-card border-t shadow-lg p-4">
            <div className="container mx-auto">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <ShoppingCart className="w-5 h-5" />
                    <span className="font-semibold">Giỏ hàng ({cart.length} món)</span>
                  </div>
                  <div className="text-sm text-muted-foreground">
                    {cart.map((item, idx) => (
                      <div key={idx} className="flex items-center justify-between">
                        <span>{item.menuItem.name} x{item.quantity}</span>
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-6 w-6 p-0"
                            onClick={() => handleUpdateQuantity(item.menuItem.id, item.quantity - 1)}
                          >
                            -
                          </Button>
                          <span className="w-8 text-center">{item.quantity}</span>
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-6 w-6 p-0"
                            onClick={() => handleUpdateQuantity(item.menuItem.id, item.quantity + 1)}
                          >
                            +
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
                <div className="ml-6 text-right">
                  <p className="text-sm text-muted-foreground mb-1">Tổng cộng</p>
                  <p className="text-2xl font-bold text-primary mb-2">
                    {getTotalPrice().toLocaleString('vi-VN')}đ
                  </p>
                  <Button
                    onClick={handleSubmitOrder}
                    disabled={submitting}
                    className="w-full"
                  >
                    {submitting ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Đang xử lý...
                      </>
                    ) : (
                      'Đặt món'
                    )}
                  </Button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
