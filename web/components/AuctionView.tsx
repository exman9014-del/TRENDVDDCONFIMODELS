'use client';

import React, { useState } from 'react';
import {
  Gavel,
  Users,
  Eye,
  Bell,
  Home,
  PlusCircle,
  CheckCircle,
  Camera,
  ArrowRight,
  Award,
} from 'lucide-react';

interface AuctionItem {
  id: string;
  title: string;
  description: string;
  currentBid: number;
  remainingSeconds: number;
  iconType?: string;
}

interface AuctionViewProps {
  auctions: AuctionItem[];
  onOpenBid: (auctionId: string) => void;
  activeUsers: number;
  liveViewers: number;
  postsCreated: number;
}

export default function AuctionView({
  auctions,
  onOpenBid,
  activeUsers,
  liveViewers,
  postsCreated,
}: AuctionViewProps) {
  const [activeTab, setActiveTab] = useState<'auctions' | 'myBids' | 'ranking'>('auctions');

  const formatSeconds = (sec: number) => {
    const h = Math.floor(sec / 3600);
    const m = Math.floor((sec % 3600) / 60);
    const s = sec % 60;
    if (h > 0) {
      return `${h.toString().padStart(2, '0')}h ${m.toString().padStart(2, '0')}m ${s.toString().padStart(2, '0')}s`;
    }
    return `${m.toString().padStart(2, '0')}m ${s.toString().padStart(2, '0')}s`;
  };

  const getIcon = (type?: string) => {
    switch (type) {
      case 'home':
        return <Home className="w-5 h-5 text-white" />;
      case 'create':
        return <PlusCircle className="w-5 h-5 text-white" />;
      case 'results':
        return <CheckCircle className="w-5 h-5 text-white" />;
      default:
        return <Camera className="w-5 h-5 text-white" />;
    }
  };

  return (
    <div className="p-4 space-y-4 pb-20">
      {/* Top Indicators Bar */}
      <div className="flex items-center justify-between text-xs text-gray-600 pb-2 border-b border-gray-100">
        <span className="font-semibold">{activeUsers} usuários usando</span>
        <div className="flex items-center gap-1.5 text-red-500 font-bold">
          <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
          {liveViewers} visualizando
        </div>
        <button className="text-gray-400 hover:text-gray-600">
          <Bell className="w-4 h-4" />
        </button>
      </div>

      {/* Title */}
      <div className="text-center space-y-1">
        <h1 className="text-2xl font-black text-[#046A4E]">Leilão de Espaço</h1>
        <p className="text-xs text-gray-500 leading-tight">
          Espaços publicitários exclusivos. Dê seu lance e ganhe visibilidade!
        </p>
      </div>

      {/* Metrics Row: 12.4k visitas | LANCES HOJE laurel | 3.2k posts */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex-1 p-2 bg-white rounded-xl border border-gray-200 text-center shadow-xs">
          <span className="text-xs font-black text-gray-800 block">12.4k</span>
          <span className="text-[9px] text-gray-500">visitas no site</span>
        </div>

        <div className="px-3 py-1.5 bg-amber-50 rounded-xl border border-amber-200 text-center shadow-xs">
          <span className="text-[10px] font-extrabold text-amber-700 block">LANCES</span>
          <span className="text-[11px] font-black text-amber-700">HOJE!</span>
        </div>

        <div className="flex-1 p-2 bg-white rounded-xl border border-gray-200 text-center shadow-xs">
          <span className="text-xs font-black text-gray-800 block">{postsCreated}</span>
          <span className="text-[9px] text-gray-500">posts criados</span>
        </div>
      </div>

      {/* 87 anunciantes stack */}
      <div className="flex items-center justify-between px-1">
        <span className="text-xs font-semibold text-gray-600">
          87 anunciantes já estão participando
        </span>
        <div className="flex -space-x-1.5">
          {['/img_hero_woman.png', '/img_creator_main.png', '/img_result_1.png', '/img_result_2.png'].map(
            (src, i) => (
              <img
                key={i}
                src={src}
                alt="Anunciante"
                className="w-6 h-6 rounded-full border border-white object-cover shadow-xs"
              />
            )
          )}
        </div>
      </div>

      {/* Sub Tabs */}
      <div className="flex gap-2">
        {(['auctions', 'myBids', 'ranking'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`flex-1 py-1.5 rounded-xl text-xs font-bold transition ${
              activeTab === tab
                ? 'bg-[#00A86B] text-white shadow-xs'
                : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
            }`}
          >
            {tab === 'auctions' ? 'Leilões' : tab === 'myBids' ? 'Meus Lances' : 'Ranking'}
          </button>
        ))}
      </div>

      {/* CONTENT: BANNERS REAIS */}
      {activeTab === 'auctions' && (
        <div className="space-y-3">
          {auctions.map((auc) => (
            <div
              key={auc.id}
              className="p-4 bg-white rounded-2xl border border-gray-200 shadow-xs space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-10 h-10 rounded-xl bg-[#046A4E] flex items-center justify-center">
                    {getIcon(auc.iconType)}
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900 text-sm">{auc.title}</h3>
                    <p className="text-[11px] text-gray-500">{auc.description}</p>
                  </div>
                </div>
                <span className="text-[10px] font-bold bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full flex items-center gap-1">
                  👑 Maior lance
                </span>
              </div>

              <div className="flex items-end justify-between pt-1">
                <div>
                  <span className="text-[10px] text-gray-500 block">Lance atual</span>
                  <div className="text-xl font-black text-[#046A4E]">
                    R$ {auc.currentBid.toFixed(2).replace('.', ',')}
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-[10px] text-gray-500 block">Tempo restante</span>
                  <span className="text-xs font-bold text-gray-800">
                    {formatSeconds(auc.remainingSeconds)}
                  </span>
                </div>
              </div>

              <button
                onClick={() => onOpenBid(auc.id)}
                className="w-full py-2.5 bg-[#046A4E] hover:bg-[#034d38] text-white text-xs font-bold rounded-xl flex items-center justify-center gap-1.5 shadow-xs transition"
              >
                <span>Dar Lance</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          ))}
        </div>
      )}

      {activeTab === 'myBids' && (
        <div className="p-6 bg-white rounded-2xl border border-gray-200 text-center space-y-2">
          <Gavel className="w-8 h-8 text-gray-300 mx-auto" />
          <h3 className="font-bold text-xs text-gray-800">Nenhum lance nesta sessão</h3>
          <p className="text-[11px] text-gray-500">
            Dê um lance nos banners usando apenas o seu e-mail!
          </p>
        </div>
      )}

      {activeTab === 'ranking' && (
        <div className="bg-white rounded-2xl border border-gray-200 divide-y divide-gray-100">
          {[
            { rank: '1º', name: 'growth_agency_br', val: 'R$ 2.450,00' },
            { rank: '2º', name: 'viral_tools_saas', val: 'R$ 1.780,00' },
            { rank: '3º', name: 'midia_ninja', val: 'R$ 1.250,00' },
            { rank: '4º', name: 'creators_hub', val: 'R$ 980,00' },
          ].map((item, idx) => (
            <div key={idx} className="flex items-center justify-between p-3 text-xs">
              <div className="flex items-center gap-2">
                <span className="font-bold text-amber-600 w-5">{item.rank}</span>
                <span className="font-medium text-gray-800">{item.name}</span>
              </div>
              <span className="font-bold text-[#00A86B]">{item.val}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
