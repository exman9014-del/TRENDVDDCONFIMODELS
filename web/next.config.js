/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    unoptimized: true,
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'hzoaklaiieydhltmafcj.supabase.co',
      },
    ],
  },
};

module.exports = nextConfig;
