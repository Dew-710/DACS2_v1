// User types
export interface User {
  id: number;
  username: string;
  email: string;
  role?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Auth types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  message: string;
  user: User;
  token?: string;
}

// Menu types
export interface Category {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  category?: Category;
  imageUrl?: string;
  isAvailable: boolean;
  preparationTime?: number;
  calories?: number;
  allergens?: string[];
  createdAt?: string;
  updatedAt?: string;
}

// Table types
export interface RestaurantTable {
  id: number;
  tableName: string;
  capacity: number;
  status: string; // 'VACANT', 'OCCUPIED', 'RESERVED', 'CLEANING', 'MAINTENANCE'
  qrCode?: string;
  tableType?: string;
  location?: string;
  lastUpdated?: string;
  createdAt?: string;
}

// Order types
export interface OrderItem {
  id: number;
  menuItem: MenuItem;
  menuItemId: number;
  quantity: number;
  price: number;
  status: string; // 'PENDING', 'PREPARING', 'READY', 'SERVED', 'CANCELLED'
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Order {
  id: number;
  tableId: number;
  table?: RestaurantTable;
  customerId?: number;
  customer?: User;
  orderItems?: OrderItem[];
  status: string; // 'ACTIVE', 'COMPLETED', 'CANCELLED', 'PAID'
  totalAmount: number;
  subtotal?: number;
  tax?: number;
  discount?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  tableId: number;
  customerId?: number;
  items?: CreateOrderItemRequest[];
}

export interface CreateOrderItemRequest {
  menuItemId: number;
  quantity: number;
  notes?: string;
}

// Booking types
export interface Booking {
  id: number;
  customerId: number;
  customer?: User;
  tableId: number;
  table?: RestaurantTable;
  date: string;
  time: string;
  guests: number;
  status: string; // 'PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateBookingRequest {
  customerId: number;
  tableId: number;
  date: string;
  time: string;
  guests: number;
  notes?: string;
}

export interface BookingAvailabilityRequest {
  date: string;
  time: string;
  guests: number;
}

// Payment types
export interface Payment {
  id: number;
  orderId: number;
  order?: Order;
  amount: number;
  paymentMethod: string; // 'CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'DIGITAL_WALLET'
  status: string; // 'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'
  transactionId?: string;
  paymentDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreatePaymentRequest {
  orderId: number;
  amount: number;
  paymentMethod: string;
}

// API Response types
export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface PaginatedResponse<T> {
  message: string;
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// Form types
export interface LoginFormData {
  username: string;
  password: string;
}

export interface RegisterFormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface MenuItemFormData {
  name: string;
  description: string;
  price: number;
  categoryId: number;
  imageUrl?: string;
  available: boolean;
}

export interface CategoryFormData {
  name: string;
  description?: string;
}

export interface BookingFormData {
  customerId: number;
  tableId: number;
  date: string;
  time: string;
  guests: number;
  notes?: string;
}

// WebSocket types for real-time updates
export interface KitchenNotification {
  type: 'NEW_ORDER' | 'ORDER_UPDATE' | 'ITEM_READY';
  orderId: number;
  tableNumber: string;
  message: string;
  timestamp: string;
}

export interface IoTData {
  type: 'IMAGE' | 'SENSOR' | 'STATUS';
  deviceId: string;
  data: any;
  timestamp: string;
}

// Dashboard stats
export interface DashboardStats {
  totalOrders: number;
  activeOrders: number;
  totalRevenue: number;
  availableTables: number;
  occupiedTables: number;
  pendingBookings: number;
}

// Error types
export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}
