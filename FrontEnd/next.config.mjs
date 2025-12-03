/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  experimental: {
    // Disable console error interception to prevent hydration warnings from being intercepted
    disableConsoleErrorInterception: true,
  },
}

export default nextConfig
