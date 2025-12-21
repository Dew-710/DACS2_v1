'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth, UserRole } from '../context/auth-context';
import { Card, CardContent } from '@/components/ui/card';
import { AlertTriangle, Loader2 } from 'lucide-react';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: UserRole | UserRole[];
  fallbackPath?: string;
  requireAuth?: boolean;
}

export function ProtectedRoute({
  children,
  requiredRoles,
  fallbackPath = '/login',
  requireAuth = true
}: ProtectedRouteProps) {
  const { isAuthenticated, hasRole, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;

    // Check authentication
    if (requireAuth && !isAuthenticated) {
      router.push(fallbackPath);
      return;
    }

    // Check role authorization
    if (requiredRoles && isAuthenticated) {
      if (!hasRole(requiredRoles)) {
        router.push('/unauthorized');
        return;
      }
    }
  }, [isAuthenticated, hasRole, requiredRoles, requireAuth, isLoading, router, fallbackPath]);

  // Show loading state
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardContent className="flex flex-col items-center justify-center py-8">
            <Loader2 className="w-8 h-8 animate-spin text-primary mb-4" />
            <p className="text-muted-foreground">Đang tải...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show unauthorized message if user doesn't have required role
  if (requireAuth && isAuthenticated && requiredRoles && !hasRole(requiredRoles)) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center px-4">
        <Card className="w-full max-w-md">
          <CardContent className="flex flex-col items-center justify-center py-8 text-center">
            <AlertTriangle className="w-16 h-16 text-destructive mb-4" />
            <h2 className="text-2xl font-bold text-foreground mb-2">Không có quyền truy cập</h2>
            <p className="text-muted-foreground mb-4">
              Bạn không có quyền truy cập vào trang này.
            </p>
            <p className="text-sm text-muted-foreground">
              Vui lòng liên hệ quản trị viên nếu bạn nghĩ đây là lỗi.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Show login prompt if authentication is required but user is not authenticated
  if (requireAuth && !isAuthenticated) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center px-4">
        <Card className="w-full max-w-md">
          <CardContent className="flex flex-col items-center justify-center py-8 text-center">
            <AlertTriangle className="w-16 h-16 text-primary mb-4" />
            <h2 className="text-2xl font-bold text-foreground mb-2">Yêu cầu đăng nhập</h2>
            <p className="text-muted-foreground mb-4">
              Vui lòng đăng nhập để tiếp tục.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return <>{children}</>;
}

// Convenience components for specific roles
export function AdminOnly({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles="ADMIN">
      {children}
    </ProtectedRoute>
  );
}

export function StaffOnly({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles="STAFF">
      {children}
    </ProtectedRoute>
  );
}

export function KitchenOnly({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles="KITCHEN">
      {children}
    </ProtectedRoute>
  );
}

export function CustomerOnly({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles={["CUSTOMER", "ADMIN"]}>
      {children}
    </ProtectedRoute>
  );
}

export function StaffOrAdmin({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles={['STAFF', 'ADMIN']}>
      {children}
    </ProtectedRoute>
  );
}

export function KitchenOrAdmin({ children }: { children: React.ReactNode }) {
  return (
    <ProtectedRoute requiredRoles={['KITCHEN', 'ADMIN']}>
      {children}
    </ProtectedRoute>
  );
}
