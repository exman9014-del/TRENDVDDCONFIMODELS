'use client';

import React, { useState } from 'react';
import { X, Sparkles } from 'lucide-react';

interface AddTraitModalProps {
  onClose: () => void;
  onAdd: (name: String, isBeauty: boolean) => void;
}

export default function AddTraitModal({ onClose, onAdd }: AddTraitModalProps) {
  const [name, setName] = useState('');
  const [isBeauty, setIsBeauty] = useState(true);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onAdd(name.trim(), isBeauty);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
      <div className="bg-white w-full max-w-sm rounded-2xl p-5 shadow-2xl">
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <h3 className="font-bold text-gray-900 text-sm">Criar Traço Personalizado</h3>
          <button onClick={onClose} className="p-1 text-gray-400 hover:text-gray-600 rounded-lg">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-gray-600 mb-1.5">
              Categoria do Traço
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setIsBeauty(true)}
                className={`py-2 text-xs font-bold rounded-lg border transition ${
                  isBeauty
                    ? 'border-[#00A86B] bg-emerald-50 text-[#00A86B]'
                    : 'border-gray-200 text-gray-500'
                }`}
              >
                + Beleza
              </button>
              <button
                type="button"
                onClick={() => setIsBeauty(false)}
                className={`py-2 text-xs font-bold rounded-lg border transition ${
                  !isBeauty
                    ? 'border-red-500 bg-red-50 text-red-500'
                    : 'border-gray-200 text-gray-500'
                }`}
              >
                + Feiura
              </button>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">
              Nome do Traço
            </label>
            <input
              type="text"
              required
              placeholder="Ex: maxilar marcado, sardas"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-hidden focus:border-[#00A86B]"
            />
          </div>

          <button
            type="submit"
            disabled={!name.trim()}
            className="w-full py-2.5 bg-[#00A86B] disabled:opacity-50 text-white font-bold text-sm rounded-xl transition"
          >
            Adicionar ao Seletor
          </button>
        </form>
      </div>
    </div>
  );
}
