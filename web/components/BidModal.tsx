'use client';

import React, { useState } from 'react';
import { Gavel, X, Clock } from 'lucide-react';

interface AuctionItem {
  id: string;
  title: string;
  description: string;
  currentBid: number;
  remainingSeconds: number;
  formattedBid?: string;
  formattedRemainingTime?: string;
}

interface BidModalProps {
  auction: AuctionItem;
  onClose: () => void;
  onSubmit: (auctionId: string, email: string, amount: number) => void;
}

export default function BidModal({ auction, onClose, onSubmit }: BidModalProps) {
  const [email, setEmail] = useState('');
  const [amount, setAmount] = useState<number>(auction.currentBid + 10);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !email.includes('@')) {
      setError('Por favor, informe um e-mail válido para o lance.');
      return;
    }
    if (amount <= auction.currentBid) {
      setError(`O lance deve ser maior que R$ ${auction.currentBid.toFixed(2).replace('.', ',')}.`);
      return;
    }
    onSubmit(auction.id, email, amount);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
      <div className="bg-white w-full max-w-sm rounded-2xl p-5 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-amber-100 flex items-center justify-center text-amber-700">
              <Gavel className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900 text-sm">Dar Lance no Espaço</h3>
              <p className="text-xs text-gray-500">{auction.title}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1 text-gray-400 hover:text-gray-600 rounded-lg">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="my-4 p-3 bg-gray-50 rounded-xl border border-gray-100 flex justify-between items-center">
          <div>
            <span className="text-[11px] text-gray-500">Lance atual</span>
            <div className="text-lg font-extrabold text-[#00A86B]">
              R$ {auction.currentBid.toFixed(2).replace('.', ',')}
            </div>
          </div>
          <div className="text-right">
            <span className="text-[11px] text-gray-500 flex items-center gap-1 justify-end">
              <Clock className="w-3 h-3 text-red-500" /> Tempo
            </span>
            <div className="text-xs font-bold text-red-600">
              {Math.floor(auction.remainingSeconds / 60)}m {auction.remainingSeconds % 60}s
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">
              Seu E-mail (Sem necessidade de login)
            </label>
            <input
              type="email"
              required
              placeholder="seu@email.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setError(null);
              }}
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-hidden focus:border-[#00A86B] focus:ring-1 focus:ring-[#00A86B]"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">
              Valor do seu Lance (R$)
            </label>
            <input
              type="number"
              step="any"
              required
              value={amount}
              onChange={(e) => {
                setAmount(Number(e.target.value));
                setError(null);
              }}
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-hidden focus:border-[#00A86B] focus:ring-1 focus:ring-[#00A86B]"
            />
          </div>

          <div className="flex gap-2">
            {[10, 50, 100].map((inc) => (
              <button
                type="button"
                key={inc}
                onClick={() => setAmount(auction.currentBid + inc)}
                className="flex-1 py-1.5 text-xs font-bold rounded-lg border border-emerald-200 bg-emerald-50 text-[#00A86B] hover:bg-emerald-100 transition"
              >
                + R$ {inc}
              </button>
            ))}
          </div>

          {error && <p className="text-xs text-red-500 font-medium">{error}</p>}

          <button
            type="submit"
            className="w-full py-2.5 mt-2 bg-[#00A86B] hover:bg-[#046A4E] text-white font-bold text-sm rounded-xl shadow-md transition"
          >
            Confirmar Lance no Supabase
          </button>
        </form>
      </div>
    </div>
  );
}
