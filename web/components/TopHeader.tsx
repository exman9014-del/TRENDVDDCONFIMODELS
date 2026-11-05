'use client';

import React from 'react';
import { Heart, Users } from 'lucide-react';

interface TopHeaderProps {
  liveUsers: number;
}

export default function TopHeader({ liveUsers }: TopHeaderProps) {
  const formattedCount = liveUsers >= 1000 ? `${(liveUsers / 1000).toFixed(1)}K` : liveUsers;

  return (
    <header className="flex items-center justify-between px-4 py-3 bg-white sticky top-0 z-40 border-b border-gray-100">
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 rounded-lg bg-[#00A86B] flex items-center justify-center text-white shadow-sm">
          <Heart className="w-4 h-4 fill-white" />
        </div>
        <span className="font-extrabold text-lg text-gray-900 tracking-tight">Na Verdade</span>
      </div>

      <div className="flex items-center gap-2 border border-gray-200 rounded-full px-3 py-1 bg-white text-xs font-semibold shadow-xs">
        <div className="flex items-center gap-1.5 text-red-500">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
          </span>
          <span className="text-[10px] font-bold">AO VIVO</span>
        </div>
        <span className="w-px h-3 bg-gray-200" />
        <div className="flex items-center gap-1 text-gray-600">
          <Users className="w-3.5 h-3.5" />
          <span>{formattedCount}</span>
        </div>
      </div>
    </header>
  );
}
