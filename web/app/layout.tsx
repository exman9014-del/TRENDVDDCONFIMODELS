import './globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Na Verdade - Trend Viral com IA',
  description: 'Crie seu post da trend "Na Verdade" com IA, leilão de anúncios em tempo real e feed estilo TikTok.',
  icons: {
    icon: '/logo.jpg',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body className="bg-gray-50 min-h-screen text-gray-900">
        <div className="max-w-md mx-auto min-h-screen bg-white shadow-xl relative pb-20 border-x border-gray-100">
          {children}
        </div>
      </body>
    </html>
  );
}
