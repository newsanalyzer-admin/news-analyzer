/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  output: 'standalone',

  // Image optimization configuration
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'bioguide.congress.gov',
        pathname: '/bioguide/photo/**',
      },
    ],
  },

  // Redirects from old factbase routes to new knowledge-base routes
  async redirects() {
    return [
      {
        source: '/factbase',
        destination: '/knowledge-base',
        permanent: true,
      },
      {
        source: '/factbase/organizations/:path*',
        destination: '/knowledge-base/organizations',
        permanent: true,
      },
      {
        source: '/factbase/government-orgs',
        destination: '/knowledge-base/organizations',
        permanent: true,
      },
      {
        source: '/factbase/people/federal-judges',
        destination: '/knowledge-base/people?type=judges',
        permanent: true,
      },
      {
        source: '/factbase/people/congressional-members',
        destination: '/knowledge-base/people?type=members',
        permanent: true,
      },
      {
        source: '/factbase/people/executive-appointees',
        destination: '/knowledge-base/people?type=appointees',
        permanent: true,
      },
      {
        source: '/factbase/people/:path*',
        destination: '/knowledge-base/people',
        permanent: true,
      },
    ]
  },

  // API proxy configuration for development
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: process.env.NEXT_PUBLIC_API_URL
          ? `${process.env.NEXT_PUBLIC_API_URL}/api/:path*`
          : 'http://localhost:8080/api/:path*',
      },
    ]
  },

  // Security headers
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
        ],
      },
    ]
  },

  // Optimization
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },
}

module.exports = nextConfig
