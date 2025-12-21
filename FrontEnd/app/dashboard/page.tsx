'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/context/auth-context';
import { Card, CardContent } from '@/components/ui/card';
import { Loader2 } from 'lucide-react';

export default function DashboardPage() {
  const { isAuthenticated, role, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;

    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    // Redirect to appropriate dashboard based on role
    switch (role) {
      case 'ADMIN':
        router.push('/dashboard/admin');
        break;
      case 'STAFF':
        router.push('/dashboard/staff');
        break;
      case 'KITCHEN':
        router.push('/dashboard/kitchen');
        break;
      case 'CUSTOMER':
        router.push('/dashboard/customer');
        break;
      default:
        router.push('/unauthorized');
        break;
    }
  }, [isAuthenticated, role, isLoading, router]);

  // Show loading state while determining where to redirect
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted flex items-center justify-center">
      <Card className="w-full max-w-md">
        <CardContent className="flex flex-col items-center justify-center py-8">
          <Loader2 className="w-8 h-8 animate-spin text-primary mb-4" />
          <p className="text-muted-foreground">Đang chuyển hướng...</p>
        </CardContent>
      </Card>
    </div>
  );
}
