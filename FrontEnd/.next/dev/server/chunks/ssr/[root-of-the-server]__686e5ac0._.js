module.exports = [
"[externals]/next/dist/compiled/next-server/app-page-turbo.runtime.dev.js [external] (next/dist/compiled/next-server/app-page-turbo.runtime.dev.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/compiled/next-server/app-page-turbo.runtime.dev.js", () => require("next/dist/compiled/next-server/app-page-turbo.runtime.dev.js"));

module.exports = mod;
}),
"[project]/DACS2/FrontEnd/components/theme-provider.tsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "ThemeProvider",
    ()=>ThemeProvider
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react-jsx-dev-runtime.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2d$themes$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next-themes/dist/index.mjs [app-ssr] (ecmascript)");
'use client';
;
;
;
function ThemeProvider({ children, ...props }) {
    const [mounted, setMounted] = __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"](false);
    __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useEffect"](()=>{
        setMounted(true);
    }, []);
    if (!mounted) {
        // Return children without theme provider during SSR to prevent hydration mismatch
        return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])(__TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["Fragment"], {
            children: children
        }, void 0, false);
    }
    return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])(__TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2d$themes$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["ThemeProvider"], {
        ...props,
        children: children
    }, void 0, false, {
        fileName: "[project]/DACS2/FrontEnd/components/theme-provider.tsx",
        lineNumber: 21,
        columnNumber: 10
    }, this);
}
}),
"[project]/DACS2/FrontEnd/lib/api.ts [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "addItemsToOrder",
    ()=>addItemsToOrder,
    "cancelBooking",
    ()=>cancelBooking,
    "cancelSepayPayment",
    ()=>cancelSepayPayment,
    "checkInBooking",
    ()=>checkInBooking,
    "checkInTable",
    ()=>checkInTable,
    "checkOutTable",
    ()=>checkOutTable,
    "checkSepayPaymentStatus",
    ()=>checkSepayPaymentStatus,
    "checkTableAvailability",
    ()=>checkTableAvailability,
    "checkoutOrder",
    ()=>checkoutOrder,
    "confirmBooking",
    ()=>confirmBooking,
    "createBooking",
    ()=>createBooking,
    "createCategory",
    ()=>createCategory,
    "createMenuItem",
    ()=>createMenuItem,
    "createOrder",
    ()=>createOrder,
    "createOrderFromRequest",
    ()=>createOrderFromRequest,
    "createOrderWithCustomer",
    ()=>createOrderWithCustomer,
    "createSepayPayment",
    ()=>createSepayPayment,
    "deleteCategory",
    ()=>deleteCategory,
    "deleteMenuItem",
    ()=>deleteMenuItem,
    "deleteOrder",
    ()=>deleteOrder,
    "getActiveBookingByTable",
    ()=>getActiveBookingByTable,
    "getActiveOrdersByTable",
    ()=>getActiveOrdersByTable,
    "getAllTables",
    ()=>getAllTables,
    "getAvailableTables",
    ()=>getAvailableTables,
    "getBookings",
    ()=>getBookings,
    "getCategories",
    ()=>getCategories,
    "getMenuItemById",
    ()=>getMenuItemById,
    "getMenuItems",
    ()=>getMenuItems,
    "getMenuItemsByCategory",
    ()=>getMenuItemsByCategory,
    "getMyBookings",
    ()=>getMyBookings,
    "getMyOrders",
    ()=>getMyOrders,
    "getOrderById",
    ()=>getOrderById,
    "getOrders",
    ()=>getOrders,
    "getOrdersByTable",
    ()=>getOrdersByTable,
    "getQRCodeImageUrl",
    ()=>getQRCodeImageUrl,
    "getTableByQr",
    ()=>getTableByQr,
    "getTableCurrentOrder",
    ()=>getTableCurrentOrder,
    "getTablesList",
    ()=>getTablesList,
    "getUsersList",
    ()=>getUsersList,
    "login",
    ()=>login,
    "processPayment",
    ()=>processPayment,
    "register",
    ()=>register,
    "removeItemFromOrder",
    ()=>removeItemFromOrder,
    "sendQRCodeToESP32",
    ()=>sendQRCodeToESP32,
    "updateCategory",
    ()=>updateCategory,
    "updateMenuItem",
    ()=>updateMenuItem,
    "updateOrderItemStatus",
    ()=>updateOrderItemStatus,
    "updateOrderStatus",
    ()=>updateOrderStatus,
    "updateTableStatus",
    ()=>updateTableStatus
]);
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"; // Backend URL
async function fetchData(endpoint, options = {}) {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...options.headers || {}
        }
    });
    // Xử lý lỗi trước
    if (!response.ok) {
        let errMsg = response.statusText;
        try {
            const errorBody = await response.json();
            errMsg = errorBody.message || errorBody.error || errMsg;
        } catch (_) {}
        throw new Error(errMsg);
    }
    // Server có thể không trả JSON (ví dụ DELETE 204)
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }
    // @ts-ignore
    return null; // hoặc return undefined
}
async function login(loginRequest) {
    return fetchData("/api/users/login", {
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}
async function register(registerRequest) {
    return fetchData("/api/users/register", {
        method: 'POST',
        body: JSON.stringify(registerRequest)
    });
}
async function getUsersList() {
    return fetchData("/api/users/list");
}
async function getMenuItems() {
    return fetchData("/api/menu-items/list");
}
async function getMenuItemById(id) {
    return fetchData(`/api/menu-items/${id}`);
}
async function getMenuItemsByCategory(categoryId) {
    return fetchData(`/api/menu-items/category/${categoryId}`);
}
async function createMenuItem(menuItem) {
    return fetchData("/api/menu-items/create", {
        method: 'POST',
        body: JSON.stringify(menuItem)
    });
}
async function updateMenuItem(id, menuItem) {
    return fetchData(`/api/menu-items/update/${id}`, {
        method: 'PUT',
        body: JSON.stringify(menuItem)
    });
}
async function deleteMenuItem(id) {
    return fetchData(`/api/menu-items/delete/${id}`, {
        method: 'DELETE'
    });
}
async function getCategories() {
    return fetchData("/api/categories/list");
}
async function createCategory(category) {
    return fetchData("/api/categories/create", {
        method: 'POST',
        body: JSON.stringify(category)
    });
}
async function updateCategory(id, category) {
    return fetchData(`/api/categories/update/${id}`, {
        method: 'PUT',
        body: JSON.stringify(category)
    });
}
async function deleteCategory(id) {
    return fetchData(`/api/categories/delete/${id}`, {
        method: 'DELETE'
    });
}
async function getTablesList() {
    return fetchData("/api/tables/list");
}
async function getAllTables() {
    return fetchData("/api/tables/all");
}
async function getAvailableTables() {
    return fetchData("/api/tables/available");
}
async function updateTableStatus(tableId, status) {
    return fetchData(`/api/tables/${tableId}/status-update/${status}`, {
        method: 'PUT'
    });
}
async function checkInTable(qrCode, customerId) {
    return fetchData(`/api/tables/checkin/${qrCode}?customerId=${customerId}`, {
        method: 'POST'
    });
}
async function checkOutTable(tableId) {
    return fetchData(`/api/tables/${tableId}/checkout`, {
        method: 'POST'
    });
}
async function getTableCurrentOrder(tableId) {
    return fetchData(`/api/tables/${tableId}/current-order`);
}
async function sendQRCodeToESP32(tableId) {
    return fetchData(`/api/send-qr-code/${tableId}`, {
        method: 'POST'
    });
}
function getQRCodeImageUrl(tableId) {
    return `${process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080"}/api/qr-code/${tableId}/image`;
}
async function getTableByQr(qrCode) {
    return fetchData(`/api/tables/qr/${qrCode}`);
}
async function getOrders() {
    return fetchData("/api/orders/list");
}
async function getMyOrders(customerId) {
    const url = customerId ? `/api/orders/my-orders?customerId=${customerId}` : "/api/orders/my-orders";
    return fetchData(url);
}
async function getOrderById(id) {
    return fetchData(`/api/orders/${id}`);
}
async function getOrdersByTable(tableId) {
    return fetchData(`/api/orders/table/${tableId}`);
}
async function getActiveOrdersByTable(tableId) {
    return fetchData(`/api/orders/table/${tableId}/active`);
}
async function createOrder(order) {
    return fetchData("/api/orders/create", {
        method: 'POST',
        body: JSON.stringify(order)
    });
}
async function createOrderWithCustomer(customerId, tableId, order) {
    return fetchData(`/api/orders/create-with-customer/${customerId}/table/${tableId}`, {
        method: 'POST',
        body: JSON.stringify(order)
    });
}
async function createOrderFromRequest(request) {
    return fetchData(`/api/orders/create-from-request`, {
        method: 'POST',
        body: JSON.stringify(request)
    });
}
async function addItemsToOrder(orderId, items) {
    return fetchData(`/api/orders/${orderId}/add-items`, {
        method: 'POST',
        body: JSON.stringify(items)
    });
}
async function removeItemFromOrder(orderId, itemId) {
    return fetchData(`/api/orders/${orderId}/remove-item/${itemId}`, {
        method: 'DELETE'
    });
}
async function updateOrderStatus(id, status) {
    return fetchData(`/api/orders/${id}/status/${status}`, {
        method: 'PUT'
    });
}
async function checkoutOrder(orderId) {
    return fetchData(`/api/orders/${orderId}/checkout`, {
        method: 'PUT'
    });
}
async function updateOrderItemStatus(orderId, itemId, status) {
    return fetchData(`/api/orders/${orderId}/item/${itemId}/status/${status}`, {
        method: 'PUT'
    });
}
async function deleteOrder(id) {
    return fetchData(`/api/orders/${id}`, {
        method: 'DELETE'
    });
}
async function getBookings() {
    return fetchData("/api/bookings/list");
}
async function getMyBookings(customerId) {
    const url = customerId ? `/api/bookings/my-bookings?customerId=${customerId}` : "/api/bookings/my-bookings";
    return fetchData(url);
}
async function getActiveBookingByTable(tableId) {
    return fetchData(`/api/bookings/table/${tableId}/active`);
}
async function createBooking(booking) {
    return fetchData("/api/bookings/create", {
        method: 'POST',
        body: JSON.stringify(booking)
    });
}
async function confirmBooking(bookingId) {
    return fetchData(`/api/bookings/${bookingId}/confirm`, {
        method: 'PUT'
    });
}
async function checkInBooking(bookingId) {
    return fetchData(`/api/bookings/${bookingId}/checkin`, {
        method: 'PUT'
    });
}
async function checkTableAvailability(date, time, guests) {
    const params = new URLSearchParams({
        date,
        time,
        guests: guests.toString()
    });
    return fetchData(`/api/bookings/availability?${params}`);
}
async function cancelBooking(bookingId) {
    return fetchData(`/api/bookings/${bookingId}/cancel`, {
        method: 'PUT'
    });
}
async function processPayment(payment) {
    return fetchData("/api/payments/process", {
        method: 'POST',
        body: JSON.stringify(payment)
    });
}
async function createSepayPayment(request) {
    return fetchData("/api/payments/sepay/create", {
        method: 'POST',
        body: JSON.stringify(request)
    });
}
async function checkSepayPaymentStatus(transactionId) {
    return fetchData(`/api/payments/sepay/status/${transactionId}`);
}
async function cancelSepayPayment(transactionId) {
    return fetchData(`/api/payments/sepay/cancel/${transactionId}`, {
        method: 'POST'
    });
}
}),
"[project]/DACS2/FrontEnd/lib/context/auth-context.tsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "AuthProvider",
    ()=>AuthProvider,
    "useAuth",
    ()=>useAuth
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react-jsx-dev-runtime.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$lib$2f$api$2e$ts__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/lib/api.ts [app-ssr] (ecmascript)");
'use client';
;
;
;
const AuthContext = /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["createContext"])(undefined);
function AuthProvider({ children }) {
    const [user, setUser] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(null);
    const [isLoading, setIsLoading] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(true);
    const [isHydrated, setIsHydrated] = (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useState"])(false);
    (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useEffect"])(()=>{
        // Mark as hydrated after component mounts
        setIsHydrated(true);
        // Check if user is logged in on app start
        if ("TURBOPACK compile-time falsy", 0) //TURBOPACK unreachable
        ;
        setIsLoading(false);
    }, []);
    const login = async (username, password)=>{
        try {
            setIsLoading(true);
            const response = await (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$lib$2f$api$2e$ts__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["login"])({
                username,
                password
            });
            const userData = response.user;
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
        } catch (error) {
            throw error;
        } finally{
            setIsLoading(false);
        }
    };
    const register = async (username, email, password, fullName, phone)=>{
        try {
            setIsLoading(true);
            const response = await (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$lib$2f$api$2e$ts__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["register"])({
                username,
                email,
                password,
                fullName: fullName || '',
                phone: phone || ''
            });
            const userData = response.user;
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
        } catch (error) {
            throw error;
        } finally{
            setIsLoading(false);
        }
    };
    const logout = ()=>{
        setUser(null);
        localStorage.removeItem('user');
    };
    const role = user?.role || null;
    const hasRole = (roles)=>{
        if (!role) return false;
        const roleArray = Array.isArray(roles) ? roles : [
            roles
        ];
        return roleArray.includes(role);
    };
    const hasAnyRole = (roles)=>{
        if (!role) return false;
        return roles.includes(role);
    };
    const value = {
        user,
        login,
        register,
        logout,
        isLoading,
        isAuthenticated: !!user,
        role,
        hasRole,
        hasAnyRole,
        isAdmin: role === 'ADMIN',
        isStaff: role === 'STAFF',
        isKitchen: role === 'KITCHEN',
        isCustomer: role === 'CUSTOMER'
    };
    return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])(AuthContext.Provider, {
        value: value,
        children: children
    }, void 0, false, {
        fileName: "[project]/DACS2/FrontEnd/lib/context/auth-context.tsx",
        lineNumber: 119,
        columnNumber: 10
    }, this);
}
function useAuth() {
    const context = (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useContext"])(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
}),
"[project]/DACS2/FrontEnd/components/ui/sonner.tsx [app-ssr] (ecmascript)", ((__turbopack_context__) => {
"use strict";

__turbopack_context__.s([
    "Toaster",
    ()=>Toaster
]);
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next/dist/server/route-modules/app-page/vendored/ssr/react-jsx-dev-runtime.js [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2d$themes$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/next-themes/dist/index.mjs [app-ssr] (ecmascript)");
var __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$sonner$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__ = __turbopack_context__.i("[project]/DACS2/FrontEnd/node_modules/sonner/dist/index.mjs [app-ssr] (ecmascript)");
'use client';
;
;
;
const Toaster = ({ ...props })=>{
    const { theme = 'system' } = (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2d$themes$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["useTheme"])();
    return /*#__PURE__*/ (0, __TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$next$2f$dist$2f$server$2f$route$2d$modules$2f$app$2d$page$2f$vendored$2f$ssr$2f$react$2d$jsx$2d$dev$2d$runtime$2e$js__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["jsxDEV"])(__TURBOPACK__imported__module__$5b$project$5d2f$DACS2$2f$FrontEnd$2f$node_modules$2f$sonner$2f$dist$2f$index$2e$mjs__$5b$app$2d$ssr$5d$__$28$ecmascript$29$__["Toaster"], {
        theme: theme,
        className: "toaster group",
        style: {
            '--normal-bg': 'var(--popover)',
            '--normal-text': 'var(--popover-foreground)',
            '--normal-border': 'var(--border)'
        },
        ...props
    }, void 0, false, {
        fileName: "[project]/DACS2/FrontEnd/components/ui/sonner.tsx",
        lineNumber: 10,
        columnNumber: 5
    }, ("TURBOPACK compile-time value", void 0));
};
;
}),
"[externals]/next/dist/server/app-render/work-unit-async-storage.external.js [external] (next/dist/server/app-render/work-unit-async-storage.external.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/server/app-render/work-unit-async-storage.external.js", () => require("next/dist/server/app-render/work-unit-async-storage.external.js"));

module.exports = mod;
}),
"[externals]/next/dist/server/app-render/work-async-storage.external.js [external] (next/dist/server/app-render/work-async-storage.external.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/server/app-render/work-async-storage.external.js", () => require("next/dist/server/app-render/work-async-storage.external.js"));

module.exports = mod;
}),
"[externals]/next/dist/server/app-render/action-async-storage.external.js [external] (next/dist/server/app-render/action-async-storage.external.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/server/app-render/action-async-storage.external.js", () => require("next/dist/server/app-render/action-async-storage.external.js"));

module.exports = mod;
}),
"[externals]/next/dist/server/app-render/after-task-async-storage.external.js [external] (next/dist/server/app-render/after-task-async-storage.external.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/server/app-render/after-task-async-storage.external.js", () => require("next/dist/server/app-render/after-task-async-storage.external.js"));

module.exports = mod;
}),
"[externals]/next/dist/server/app-render/dynamic-access-async-storage.external.js [external] (next/dist/server/app-render/dynamic-access-async-storage.external.js, cjs)", ((__turbopack_context__, module, exports) => {

const mod = __turbopack_context__.x("next/dist/server/app-render/dynamic-access-async-storage.external.js", () => require("next/dist/server/app-render/dynamic-access-async-storage.external.js"));

module.exports = mod;
}),
];

//# sourceMappingURL=%5Broot-of-the-server%5D__686e5ac0._.js.map