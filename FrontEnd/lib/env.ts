/**
 * Environment configuration utilities
 * Centralized place for all environment variables
 */

/**
 * Get Backend API base URL
 */
export const getApiBaseUrl = (): string => {
  return process.env.NEXT_PUBLIC_API_BASE_URL || 'https://bulk-choosing-circus-inputs.trycloudflare.com';
};

/**
 * Get Frontend App URL (for callbacks, QR codes, etc.)
 */
export const getAppUrl = (): string => {
  // Prefer env variable, fallback to window.location.origin if available
  if (process.env.NEXT_PUBLIC_APP_URL) {
    return process.env.NEXT_PUBLIC_APP_URL;
  }
  
  // Server-side rendering fallback
  if (typeof window === 'undefined') {
    return 'http://localhost:3000';
  }
  
  return window.location.origin;
};

/**
 * Get PayOS Return URL
 */
export const getPayOSReturnUrl = (orderIds?: number[]): string => {
  const baseUrl = process.env.NEXT_PUBLIC_PAYOS_RETURN_URL || `${getAppUrl()}/payment/success`;
  
  if (orderIds && orderIds.length > 0) {
    return `${baseUrl}?orderIds=${orderIds.join(',')}`;
  }
  
  return baseUrl;
};

/**
 * Get PayOS Cancel URL
 */
export const getPayOSCancelUrl = (orderIds?: number[]): string => {
  const baseUrl = process.env.NEXT_PUBLIC_PAYOS_CANCEL_URL || `${getAppUrl()}/payment/cancel`;
  
  if (orderIds && orderIds.length > 0) {
    return `${baseUrl}?orderIds=${orderIds.join(',')}`;
  }
  
  return baseUrl;
};

/**
 * Check if running in development mode
 */
export const isDevelopment = (): boolean => {
  return process.env.NODE_ENV === 'development';
};

/**
 * Check if URL is localhost (not suitable for PayOS)
 */
export const isLocalhostUrl = (url: string): boolean => {
  return url.includes('localhost') || url.includes('127.0.0.1');
};

/**
 * Validate PayOS URLs (must not be localhost)
 */
export const validatePayOSUrls = (): { valid: boolean; warnings: string[] } => {
  const warnings: string[] = [];
  const appUrl = getAppUrl();
  
  if (isLocalhostUrl(appUrl)) {
    warnings.push('⚠️ App URL is localhost - PayOS will reject this!');
    warnings.push('   → Set NEXT_PUBLIC_APP_URL in .env.local to your ngrok URL');
    warnings.push('   → Example: NEXT_PUBLIC_APP_URL=https://abc123.ngrok.io');
  }
  
  const returnUrl = getPayOSReturnUrl();
  const cancelUrl = getPayOSCancelUrl();
  
  if (isLocalhostUrl(returnUrl)) {
    warnings.push('⚠️ PayOS Return URL is localhost');
  }
  
  if (isLocalhostUrl(cancelUrl)) {
    warnings.push('⚠️ PayOS Cancel URL is localhost');
  }
  
  return {
    valid: warnings.length === 0,
    warnings
  };
};

/**
 * Log environment configuration (for debugging)
 */
export const logEnvConfig = () => {
  // Debug logging removed
};
