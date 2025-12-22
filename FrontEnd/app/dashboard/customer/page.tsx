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
  getMyOrders,
  getMyBookings,
  createBooking,
  checkTableAvailability
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
  Table
} from 'lucide-react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

function CustomerDashboardContent() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState("menu");

  // State cho dữ liệu dashboard
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tables, setTables] = useState<RestaurantTable[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [selectedTable, setSelectedTable] = useState<number | null>(null);
  const [isBooking, setIsBooking] = useState(false);
  const [availableTablesForBooking, setAvailableTablesForBooking] = useState<RestaurantTable[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  // State cho form đặt bàn
  const [bookingForm, setBookingForm] = useState({
    date: '',
    time: '',
    guests: 2,
    notes: ''
  });

  useEffect(() => {
    loadDashboardData();
  }, [user?.id]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      if (!user?.id) {
        setOrders([]);
        setBookings([]);
        return;
      }

      const [menuRes, categoriesRes, tablesRes, ordersRes, bookingsRes] = await Promise.all([
        getMenuItems(),
        getCategories(),
        getTablesList(),
        getMyOrders(user.id),
        getMyBookings(user.id),
      ]);

      setMenuItems(menuRes.menuItems || []);
      setCategories(categoriesRes.categories || []);

      let tablesData = tablesRes.tables || [];
      if( tablesData.length === 0 ) {
        toast.error('Không có bàn nào trong hệ thống. Vui lòng liên hệ quản trị viên.');
      }
      setTables(tablesData);
      setAvailableTablesForBooking(tablesData.filter(table => table.status === 'AVAILABLE'));

      // Orders và bookings đã được filter ở backend
      setOrders(ordersRes.orders || []);
      setBookings(bookingsRes.bookings || []);
    } catch (error) {
      toast.error('Không thể tải dữ liệu. Vui lòng thử lại sau.');
    
      setOrders([]);
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    toast.success('Đã đăng xuất');
    router.push('/');
  };


  const checkAvailability = async () => {
    if (!bookingForm.date || !bookingForm.time) return;

    try {
      const response = await checkTableAvailability(
        bookingForm.date,
        bookingForm.time,
        bookingForm.guests
      );
      setAvailableTablesForBooking(response.availableTables);
    } catch (error) {
      console.error('Error checking availability:', error);
      // Fallback về tất cả bàn khả dụng nếu API thất bại
      setAvailableTablesForBooking(tables.filter(t => t.status === 'AVAILABLE'));
    }
  };

  // Kiểm tra tính khả dụng khi ngày, giờ, hoặc số khách thay đổi
  useEffect(() => {
    if (bookingForm.date && bookingForm.time) {
      checkAvailability();
    } else {
      // Reset về tất cả bàn khả dụng nếu chưa chọn ngày/giờ
      setAvailableTablesForBooking(tables.filter(t => t.status === 'AVAILABLE'));
    }
  }, [bookingForm.date, bookingForm.time, bookingForm.guests, tables]);

  const handleBookTable = async () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập để đặt bàn');
      return;
    }

    if (!selectedTable) {
      toast.error('Vui lòng chọn bàn');
      return;
    }

    if (!bookingForm.date || !bookingForm.time) {
      toast.error('Vui lòng chọn ngày và giờ');
      return;
    }

    // Xác thực ngày không phải trong quá khứ
    const selectedDateTime = new Date(`${bookingForm.date}T${bookingForm.time}`);
    const now = new Date();
    if (selectedDateTime <= now) {
      toast.error('Bạn không thể đặt bàn cho những ngày đã qua!!');
      return;
    }

    // Xác thực số lượng khách
    if (bookingForm.guests < 1 || bookingForm.guests > 20) {
      toast.error('Số lượng khách phải từ 1 đến 20 người');
      return;
    }

    // Tìm bàn đã chọn để xác thực sức chứa
    const selectedTableData = tables.find(t => t.id === selectedTable);
    if (selectedTableData && bookingForm.guests > selectedTableData.capacity) {
      toast.error(`Bàn này chỉ phục vụ tối đa ${selectedTableData.capacity} khách`);
      return;
    }

    try {
      setIsBooking(true); // Add loading state

      const bookingData = {
        customerId: user.id,
        tableId: selectedTable,
        date: bookingForm.date,
        time: bookingForm.time,
        guests: bookingForm.guests,
        notes: bookingForm.notes || undefined,
        status: 'PENDING' // Default status
      };

      const response = await createBooking(bookingData);

      toast.success('🎉 Đặt bàn thành công!', {
        description: `Bàn ${selectedTableData?.tableName || selectedTable} - ${bookingForm.date} ${bookingForm.time}`
      });

      // Reset form
      setBookingForm({
        date: '',
        time: '',
        guests: 2,
        notes: ''
      });
      setSelectedTable(null);

      // Làm mới dữ liệu
      await loadDashboardData();

    } catch (error) {
      toast.error('Đặt bàn thất bại. Vui lòng thử lại.');
    } finally {
      setIsBooking(false);
    }
  };


  const availableTables = tables.filter(table => table.status === 'AVAILABLE');
  const reservedTables = tables.filter(table => table.status === 'RESERVED');
  const occupiedTables = tables.filter(table => table.status === 'OCCUPIED');

  // Lọc menu items theo category đã chọn
  const filteredMenuItems = selectedCategory
    ? menuItems.filter(item => Number(item.categoryId) === selectedCategory)
    : menuItems;

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
              <h1 className="text-xl font-bold">DEW FOOD</h1>
              <p className="text-sm text-muted-foreground">Chào mừng {user?.fullName || user?.username}</p>
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
            <TabsTrigger value="bookings" className="relative">
              Lịch đặt
              {bookings.length > 0 && (
                <Badge variant="destructive" className="absolute -top-2 -right-2 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs">
                  {bookings.length}
                </Badge>
              )}
            </TabsTrigger>
            <TabsTrigger value="orders">Đơn hàng</TabsTrigger>
          </TabsList>

          {/* Menu Tab */}
          <TabsContent value="menu" className="space-y-6">
            {/* Categories Filter */}
            <div className="flex flex-wrap gap-3 justify-center">
              <Button
                variant={selectedCategory === null ? "default" : "outline"}
                onClick={() => setSelectedCategory(null)}
              >
                Tất cả
              </Button>
              {categories.map((category) => (
                <Button
                  key={category.id}
                  variant={selectedCategory === category.id ? "default" : "outline"}
                  onClick={() => setSelectedCategory(Number(category.id))}
                >
                  {category.name}
                </Button>
              ))}
            </div>

            {/* Menu Items Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredMenuItems
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
                      min={new Date().toISOString().split('T')[0]}
                      value={bookingForm.date}
                      onChange={(e) => setBookingForm(prev => ({ ...prev, date: e.target.value }))}
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Giờ</label>
                    <select
                      value={bookingForm.time}
                      onChange={(e) => setBookingForm(prev => ({ ...prev, time: e.target.value }))}
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    >
                      <option value="">Chọn giờ</option>
                      <option value="11:00">11:00 AM</option>
                      <option value="12:00">12:00 PM</option>
                      <option value="13:00">1:00 PM</option>
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
                      value={bookingForm.guests}
                      onChange={(e) => setBookingForm(prev => ({ ...prev, guests: parseInt(e.target.value) || 2 }))}
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                    {selectedTable && (
                      <p className="text-xs text-muted-foreground mt-1">
                        💡 Gợi ý: Bàn đã chọn phục vụ tối đa {tables.find(t => t.id === selectedTable)?.capacity || 0} khách
                      </p>
                    )}
                  </div>
                  <div>
                    <label className="text-sm font-medium">Ghi chú (tùy chọn)</label>
                    <textarea
                      value={bookingForm.notes}
                      onChange={(e) => setBookingForm(prev => ({ ...prev, notes: e.target.value }))}
                      placeholder="Yêu cầu đặc biệt..."
                      rows={3}
                      className="w-full px-3 py-2 bg-input border border-border rounded-md text-foreground mt-1 focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                  </div>
                  <Button
                    className="w-full"
                    onClick={handleBookTable}
                    disabled={!selectedTable || !bookingForm.date || !bookingForm.time || isBooking}
                  >
                    {isBooking ? (
                      <>
                        <div className="w-4 h-4 mr-2 animate-spin rounded-full border-2 border-current border-t-transparent" />
                        Đang xử lý...
                      </>
                    ) : (
                      <>
                        <Calendar className="w-4 h-4 mr-2" />
                        Đặt bàn
                      </>
                    )}
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Table className="w-5 h-5" />
                    Bàn trống
                    <Badge variant="secondary" className="ml-auto">
                      {availableTablesForBooking.filter(table =>
                        !table.location?.includes('Bar') && !table.location?.includes('Garden')
                      ).length} bàn khả dụng
                    </Badge>
                  </CardTitle>
                  <p className="text-sm text-muted-foreground">
                    Chọn bàn phù hợp với số lượng khách của bạn
                  </p>
                </CardHeader>
                <CardContent>
                  {/* Table Status Overview */}
                  <div className="mb-6 p-4 bg-muted/50 rounded-lg">
                    <h4 className="font-medium text-sm mb-3">Tình trạng bàn ăn</h4>
                    <div className="grid grid-cols-3 gap-4 text-center">
                      <div className="flex flex-col items-center">
                        <div className="text-2xl font-bold text-green-600">{availableTables.length}</div>
                        <div className="text-xs text-muted-foreground">Trống</div>
                      </div>
                      <div className="flex flex-col items-center">
                        <div className="text-2xl font-bold text-blue-600">{reservedTables.length}</div>
                        <div className="text-xs text-muted-foreground">Đã đặt</div>
                      </div>
                      <div className="flex flex-col items-center">
                        <div className="text-2xl font-bold text-red-600">{occupiedTables.length}</div>
                        <div className="text-xs text-muted-foreground">Đang dùng</div>
                      </div>
                    </div>
                  </div>

                  {/* Restaurant Floor Layout */}
                  <div className="space-y-6">
                    {/* Main Dining Area */}
                    <div className="border rounded-lg p-4 bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-950/20 dark:to-indigo-950/20">
                      <h4 className="font-medium text-sm mb-3 flex items-center gap-2">
                        <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                        Khu vực chính
                      </h4>
                      <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                        {availableTablesForBooking
                          .filter(table => !table.location?.includes('Bar') && !table.location?.includes('Garden'))
                          .map((table) => (
                          <Card
                            key={table.id}
                            className={`cursor-pointer transition-all duration-300 hover:shadow-lg hover:scale-105 ${
                              selectedTable === table.id
                                ? 'ring-2 ring-primary bg-primary/5 border-primary shadow-lg'
                                : 'hover:border-primary/50 hover:shadow-md'
                            }`}
                            onClick={() => setSelectedTable(table.id)}
                          >
                            <CardContent className="p-3 text-center">
                              <div className={`w-10 h-10 mx-auto mb-2 rounded-full flex items-center justify-center transition-colors ${
                                selectedTable === table.id
                                  ? 'bg-primary text-primary-foreground'
                                  : 'bg-blue-100 text-blue-600 dark:bg-blue-900 dark:text-blue-300'
                              }`}>
                                <Users className="w-5 h-5" />
                              </div>
                              <h3 className="font-semibold text-sm mb-1">
                                Bàn {table.tableName || table.id}
                              </h3>
                              <p className={`text-xs mb-1 ${
                                bookingForm.guests > table.capacity
                                  ? 'text-destructive font-medium'
                                  : 'text-muted-foreground'
                              }`}>
                                {table.capacity} người
                                {bookingForm.guests > table.capacity && ' ⚠️ Quá tải'}
                              </p>
                              {table.tableType && (
                                <Badge variant="outline" className="text-xs">
                                  {table.tableType === 'VIP' ? '👑 VIP' :
                                   table.tableType === 'WINDOW' ? '🪟 Cửa sổ' : table.tableType}
                                </Badge>
                              )}
                              {selectedTable === table.id && (
                                <div className="mt-2 flex items-center justify-center text-primary">
                                  <div className="w-2 h-2 bg-primary rounded-full animate-pulse mr-1"></div>
                                  <span className="text-xs font-medium">Đã chọn</span>
                                </div>
                              )}
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    </div>

                    {/* Reserved Tables */}
                    {reservedTables.length > 0 && (
                      <div className="border rounded-lg p-4 bg-gradient-to-br from-red-50 to-pink-50 dark:from-red-950/20 dark:to-pink-950/20">
                        <h4 className="font-medium text-sm mb-3 flex items-center gap-2">
                          <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                          Bàn đã đặt ({reservedTables.length})
                        </h4>
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                          {reservedTables.map((table) => (
                            <Card key={table.id} className="opacity-60 cursor-not-allowed">
                              <CardContent className="p-3 text-center">
                                <div className="w-10 h-10 mx-auto mb-2 rounded-full flex items-center justify-center bg-red-100 text-red-600 dark:bg-red-900 dark:text-red-300">
                                  <span className="text-xs font-bold">📅</span>
                                </div>
                                <h3 className="font-semibold text-sm mb-1">
                                  {table.tableName}
                                </h3>
                                <p className="text-xs text-muted-foreground mb-1">
                                  {table.capacity} người
                                </p>
                                <Badge variant="destructive" className="text-xs">
                                  Đã đặt
                                </Badge>
                              </CardContent>
                            </Card>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Special Areas */}
                    {availableTables.some(table => table.location?.includes('Bar') || table.location?.includes('Garden')) && (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {/* Bar Area */}
                        {availableTables.some(table => table.location?.includes('Bar')) && (
                          <div className="border rounded-lg p-4 bg-gradient-to-br from-amber-50 to-orange-50 dark:from-amber-950/20 dark:to-orange-950/20">
                            <h4 className="font-medium text-sm mb-3 flex items-center gap-2">
                              <div className="w-3 h-3 bg-amber-500 rounded-full"></div>
                              Quầy Bar
                            </h4>
                            <div className="flex gap-3">
                              {availableTables
                                .filter(table => table.location?.includes('Bar'))
                                .map((table) => (
                                <Card
                                  key={table.id}
                                  className={`cursor-pointer transition-all duration-300 hover:shadow-lg hover:scale-105 flex-1 ${
                                    selectedTable === table.id
                                      ? 'ring-2 ring-primary bg-primary/5 border-primary'
                                      : 'hover:border-primary/50'
                                  }`}
                                  onClick={() => setSelectedTable(table.id)}
                                >
                                  <CardContent className="p-3 text-center">
                                    <div className={`w-8 h-8 mx-auto mb-2 rounded-full flex items-center justify-center transition-colors ${
                                      selectedTable === table.id
                                        ? 'bg-primary text-primary-foreground'
                                        : 'bg-amber-100 text-amber-600 dark:bg-amber-900 dark:text-amber-300'
                                    }`}>
                                      <Users className="w-4 h-4" />
                                    </div>
                                    <h3 className="font-semibold text-sm mb-1">
                                      Bàn {table.tableName || table.id}
                                    </h3>
                                    <p className="text-xs text-muted-foreground">
                                      {table.capacity} người
                                    </p>
                                    {selectedTable === table.id && (
                                      <div className="mt-2 text-primary">
                                        <span className="text-xs font-medium">✓ Đã chọn</span>
                                      </div>
                                    )}
                                  </CardContent>
                                </Card>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* Outdoor Area */}
                        {availableTables.some(table => table.location?.includes('Garden')) && (
                          <div className="border rounded-lg p-4 bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-950/20 dark:to-emerald-950/20">
                            <h4 className="font-medium text-sm mb-3 flex items-center gap-2">
                              <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                              Sân vườn
                            </h4>
                            <div className="flex gap-3">
                              {availableTables
                                .filter(table => table.location?.includes('Garden'))
                                .map((table) => (
                                <Card
                        key={table.id}
                                  className={`cursor-pointer transition-all duration-300 hover:shadow-lg hover:scale-105 flex-1 ${
                                    selectedTable === table.id
                                      ? 'ring-2 ring-primary bg-primary/5 border-primary'
                                      : 'hover:border-primary/50'
                                  }`}
                        onClick={() => setSelectedTable(table.id)}
                      >
                                  <CardContent className="p-3 text-center">
                                    <div className={`w-8 h-8 mx-auto mb-2 rounded-full flex items-center justify-center transition-colors ${
                                      selectedTable === table.id
                                        ? 'bg-primary text-primary-foreground'
                                        : 'bg-green-100 text-green-600 dark:bg-green-900 dark:text-green-300'
                                    }`}>
                                      <Users className="w-4 h-4" />
                                    </div>
                                    <h3 className="font-semibold text-sm mb-1">
                                      Bàn {table.tableName || table.id}
                                    </h3>
                                    <p className="text-xs text-muted-foreground">
                                      {table.capacity} người
                                    </p>
                                    {selectedTable === table.id && (
                                      <div className="mt-2 text-primary">
                                        <span className="text-xs font-medium">✓ Đã chọn</span>
                                      </div>
                                    )}
                                  </CardContent>
                                </Card>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                  {availableTables.length === 0 && tables.length === 0 && (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground mb-2">Đang tải dữ liệu bàn...</p>
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto"></div>
                    </div>
                  )}
                  {availableTables.length === 0 && tables.length > 0 && (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground">
                        Hiện tại không có bàn trống. Vui lòng quay lại sau.
                      </p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* Bookings History Tab */}
          <TabsContent value="bookings" className="space-y-6">
            <h2 className="text-2xl font-bold">Lịch sử đặt bàn</h2>

            {bookings.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <Calendar className="w-16 h-16 text-muted-foreground mb-4" />
                  <h3 className="text-lg font-semibold mb-2">Chưa có đặt bàn nào</h3>
                  <p className="text-muted-foreground text-center">
                    Bạn chưa có lịch đặt bàn nào. Hãy đặt bàn ngay!
                  </p>
                  <Button className="mt-4" onClick={() => setActiveTab('booking')}>
                    Đặt bàn
                  </Button>
                </CardContent>
              </Card>
            ) : (
              <div className="space-y-4">
                {bookings.map((booking) => (
                  <Card key={booking.id}>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-lg">Đặt bàn #{booking.id}</CardTitle>
                        <Badge variant={
                          booking.status === 'PENDING' ? 'secondary' :
                          booking.status === 'CONFIRMED' ? 'default' :
                          booking.status === 'COMPLETED' ? 'outline' : 'destructive'
                        }>
                          {booking.status === 'PENDING' ? 'Chờ duyệt' :
                           booking.status === 'CONFIRMED' ? 'Đã duyệt' :
                           booking.status === 'COMPLETED' ? 'Hoàn thành' : 'Đã hủy'}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {new Date(booking.date).toLocaleDateString('vi-VN')} • {booking.time} • {booking.guests} người
                      </p>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span>Bàn:</span>
                          <span>{booking.table?.tableName || 'Chưa gán'}</span>
                        </div>
                        {booking.notes && (
                          <div className="text-sm">
                            <span className="font-medium">Ghi chú:</span> {booking.notes}
                          </div>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
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
                        Bàn {order.table?.tableName || order.tableId}
                      </p>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2 mb-4">
                        {order.orderItems?.map((item) => (
                          <div key={item.id} className="flex justify-between text-sm">
                            <span>{item.menuItem.name} x{item.quantity}</span>
                            <span>{(item.price * item.quantity).toLocaleString('vi-VN')}đ</span>
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
