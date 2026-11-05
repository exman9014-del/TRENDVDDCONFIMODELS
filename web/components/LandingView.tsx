'use client';

import React, { useState, useEffect, useRef } from 'react';
import { Sparkles, TrendingUp, Flame, Music, Heart, Gavel, Send, Clock, RefreshCw } from 'lucide-react';

interface LandingViewProps {
  onCreateClick: () => void;
  onOpenBid: (auctionId: string) => void;
  homeBid: number;
  homeCountdown: number;
  onSubscribe: (email: string) => void;
}

const TREND_CARDS = [
  { id: '1', title: 'Desculpaaaa', sub: '15.8K posts/h', img: '/img_result_3.png' },
  { id: '2', title: 'Esse sou eu no meu auge', sub: '12.4K posts/h', img: '/img_creator_main.png' },
  { id: '3', title: 'Esse é o meu marido', sub: '9.6K posts/h', img: '/img_result_4.png' },
  { id: '4', title: 'Na verdade não era loiro', sub: '18.2K posts/h', img: '/img_result_2.png' },
  { id: '5', title: 'Nem é magra', sub: '8.1K posts/h', img: '/img_result_1.png' },
];

export default function LandingView({
  onCreateClick,
  onOpenBid,
  homeBid,
  homeCountdown,
  onSubscribe,
}: LandingViewProps) {
  const [email, setEmail] = useState('');
  const [carouselIndex, setCarouselIndex] = useState(0);
  const carouselRef = useRef<HTMLDivElement>(null);

  // Automatic Sliding Carousel that auto-scrolls
  useEffect(() => {
    const timer = setInterval(() => {
      setCarouselIndex((prev) => (prev + 1) % TREND_CARDS.length);
    }, 2800);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    if (carouselRef.current) {
      const scrollAmount = carouselIndex * 150;
      carouselRef.current.scrollTo({ left: scrollAmount, behavior: 'smooth' });
    }
  }, [carouselIndex]);

  const handleSubscribe = (e: React.FormEvent) => {
    e.preventDefault();
    if (email) {
      onSubscribe(email);
      setEmail('');
    }
  };

  return (
    <div className="space-y-5 pb-6">
      {/* 1. HERO SECTION */}
      <div className="p-4 bg-linear-to-b from-[#F0FDF4] to-white rounded-3xl mx-4 mt-2 border border-emerald-100 shadow-xs">
        <div className="flex items-center justify-between gap-3">
          <div className="flex-1 space-y-2">
            <h1 className="text-2xl font-black leading-tight text-gray-900">
              Crie seu <br />
              post da <span className="text-[#00A86B]">trend</span>
            </h1>
            <p className="text-xs text-gray-600 leading-relaxed">
              Transforme trends em posts incríveis que geram{' '}
              <span className="font-semibold text-[#046A4E]">engajamento e resultados.</span>
            </p>
            <button
              onClick={onCreateClick}
              className="mt-2 inline-flex items-center gap-1.5 px-4 py-2 bg-[#00A86B] hover:bg-[#046A4E] text-white text-xs font-bold rounded-xl shadow-md transition transform active:scale-95"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Criar Post
            </button>
          </div>

          <div className="w-32 h-44 rounded-t-full rounded-b-2xl bg-emerald-100 overflow-hidden relative shadow-inner">
            <img
              src="/img_hero_woman.png"
              alt="Criadora Na Verdade"
              className="w-full h-full object-cover"
            />
          </div>
        </div>
      </div>

      {/* 2. CARROSSEL AUTOMÁTICO DAS FOTOS DA TREND (Desliza Sozinho) */}
      <div className="space-y-2">
        <div className="flex items-center justify-between px-4">
          <div className="flex items-center gap-2">
            <h2 className="text-sm font-extrabold text-gray-900">Trend Na Verdade</h2>
            <span className="text-[10px] bg-emerald-100 text-[#00A86B] px-1.5 py-0.5 rounded-md font-bold">
              Automático
            </span>
          </div>
          <button onClick={onCreateClick} className="text-xs font-bold text-[#00A86B]">
            Ver todas &gt;
          </button>
        </div>

        <div
          ref={carouselRef}
          className="flex gap-3 overflow-x-auto no-scrollbar px-4 scroll-smooth"
        >
          {TREND_CARDS.map((card, idx) => (
            <div
              key={card.id}
              onClick={onCreateClick}
              className={`shrink-0 w-32 rounded-2xl overflow-hidden bg-white border cursor-pointer transition transform hover:scale-102 ${
                carouselIndex === idx ? 'border-[#00A86B] ring-2 ring-emerald-200' : 'border-gray-200'
              }`}
            >
              <div className="h-36 relative overflow-hidden bg-gray-100">
                <img src={card.img} alt={card.title} className="w-full h-full object-cover" />
                <div className="absolute inset-x-0 bottom-0 bg-linear-to-t from-black/80 to-transparent p-2">
                  <span className="text-[10px] font-bold text-white block truncate">{card.title}</span>
                </div>
              </div>
              <div className="p-2 text-center">
                <span className="text-[10px] font-bold text-[#00A86B]">{card.sub}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 3. O QUE ESTÁ BOMBANDO NA INTERNET AGORA */}
      <div className="px-4 space-y-2">
        <div className="flex items-center gap-1.5">
          <h2 className="text-sm font-extrabold text-gray-900">O que está bombando na internet agora</h2>
          <TrendingUp className="w-4 h-4 text-[#00A86B]" />
        </div>

        <div className="grid grid-cols-3 gap-2">
          <div className="p-2.5 bg-white rounded-xl border border-gray-200 space-y-1.5 shadow-xs">
            <div className="w-6 h-6 rounded-md bg-emerald-100 flex items-center justify-center text-[#00A86B]">
              <Flame className="w-3.5 h-3.5" />
            </div>
            <span className="text-[10px] font-bold text-gray-800 block truncate">Trend do Momento</span>
            <span className="text-[9px] text-gray-500 block truncate">Desafio do espelho</span>
            <span className="inline-block bg-[#00A86B] text-white text-[9px] font-bold px-1.5 py-0.5 rounded">
              12.4K posts/h
            </span>
          </div>

          <div className="p-2.5 bg-white rounded-xl border border-gray-200 space-y-1.5 shadow-xs">
            <div className="w-6 h-6 rounded-md bg-emerald-100 flex items-center justify-center text-[#00A86B]">
              <Music className="w-3.5 h-3.5" />
            </div>
            <span className="text-[10px] font-bold text-gray-800 block truncate">Áudio Viral</span>
            <span className="text-[9px] text-gray-500 block truncate">&ldquo;Na verdade, né...&rdquo;</span>
            <span className="inline-block bg-[#00A86B] text-white text-[9px] font-bold px-1.5 py-0.5 rounded">
              8.7K posts/h
            </span>
          </div>

          <div className="p-2.5 bg-white rounded-xl border border-gray-200 space-y-1.5 shadow-xs">
            <div className="w-6 h-6 rounded-md bg-emerald-100 flex items-center justify-center text-[#00A86B]">
              <Heart className="w-3.5 h-3.5" />
            </div>
            <span className="text-[10px] font-bold text-gray-800 block truncate">Nicho em Alta</span>
            <span className="text-[9px] text-gray-500 block truncate">Finanças para jovens</span>
            <span className="inline-block bg-[#00A86B] text-white text-[9px] font-bold px-1.5 py-0.5 rounded">
              5.1K posts/h
            </span>
          </div>
        </div>
      </div>

      {/* 4. BANNER DE LEILÃO AO VIVO DO ESPAÇO DA HOME */}
      <div className="px-4">
        <div className="p-4 rounded-2xl bg-[#F0FDF4] border border-emerald-200 shadow-xs space-y-3">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-full bg-[#046A4E] text-white flex items-center justify-center">
                <Gavel className="w-5 h-5" />
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <span className="font-bold text-gray-900 text-sm">Leilão ao vivo</span>
                  <span className="text-[9px] font-bold bg-[#00A86B] text-white px-1.5 py-0.5 rounded-full">
                    AO VIVO
                  </span>
                </div>
                <p className="text-[11px] text-gray-500">
                  O post mais curtido em 60s leva o destaque!
                </p>
              </div>
            </div>

            <div className="text-right">
              <span className="text-[10px] text-gray-500">Melhor lance</span>
              <div className="text-base font-black text-[#046A4E]">
                R$ {homeBid.toFixed(2).replace('.', ',')}
              </div>
            </div>
          </div>

          <div className="flex gap-2">
            <div className="flex-1 px-3 py-2 bg-white rounded-xl border border-gray-200 text-xs font-semibold text-gray-700 flex items-center">
              R$ {(homeBid + 8).toFixed(2).replace('.', ',')}
            </div>
            <button
              onClick={() => onOpenBid('banner_home')}
              className="px-4 py-2 bg-[#00A86B] hover:bg-[#046A4E] text-white font-bold text-xs rounded-xl shadow-xs transition"
            >
              Dar Lance
            </button>
          </div>

          <div className="space-y-1">
            <div className="w-full bg-emerald-200 h-1.5 rounded-full overflow-hidden">
              <div
                className="bg-[#00A86B] h-full rounded-full transition-all duration-1000"
                style={{ width: `${(homeCountdown / 60) * 100}%` }}
              />
            </div>
            <div className="text-center text-[10px] text-gray-500 font-semibold flex items-center justify-center gap-1">
              <Clock className="w-3 h-3 text-red-500" />
              Finaliza em 00:{homeCountdown.toString().padStart(2, '0')}
            </div>
          </div>
        </div>
      </div>

      {/* 5. RECEBA AS TRENDS PRIMEIRO (NEWSLETTER) */}
      <div className="px-4">
        <form
          onSubmit={handleSubscribe}
          className="p-4 bg-white rounded-2xl border border-gray-200 shadow-xs space-y-3"
        >
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-[#00A86B] text-white flex items-center justify-center">
              <Send className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-bold text-xs text-gray-900">Receba as trends primeiro</h3>
              <p className="text-[11px] text-gray-500">Inscreva-se e seja o primeiro a saber.</p>
            </div>
          </div>

          <div className="flex gap-2">
            <input
              type="email"
              required
              placeholder="Seu melhor e-mail"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="flex-1 px-3 py-2 text-xs border border-gray-200 rounded-xl focus:outline-hidden focus:border-[#00A86B]"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-[#00A86B] hover:bg-[#046A4E] text-white font-bold text-xs rounded-xl flex items-center gap-1 shadow-xs transition"
            >
              Quero receber
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
