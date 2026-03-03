/** @type {import('next').NextConfig} */
const nextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },experimental: {
    allowedOrigins: [
      'https://app.dewjunior.id.vn',
      'http://localhost:3000'
    ]
  }
}

export default nextConfig
