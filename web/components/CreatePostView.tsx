'use client';

import React, { useState, useRef } from 'react';
import {
  Sparkles,
  ArrowRight,
  Eye,
  EyeOff,
  X,
  Plus,
  Upload,
  Gavel,
  RefreshCw,
} from 'lucide-react';

interface Trait {
  id: string;
  name: string;
  isBeauty: boolean;
  enabled: boolean;
}

interface CreatePostViewProps {
  onGenerate: (data: {
    postType: 'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY';
    captions: string[];
    userPhotoUrl?: string;
  }) => void;
  onOpenBid: (auctionId: string) => void;
  onOpenAddTrait: () => void;
  traits: Trait[];
  onToggleTrait: (id: string) => void;
  onRemoveTrait: (id: string) => void;
}

export default function CreatePostView({
  onGenerate,
  onOpenBid,
  onOpenAddTrait,
  traits,
  onToggleTrait,
  onRemoveTrait,
}: CreatePostViewProps) {
  const [postType, setPostType] = useState<'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY'>(
    'UGLY_TO_BEAUTIFUL'
  );
  const [selectedThumb, setSelectedThumb] = useState(0);
  const [userPhoto, setUserPhoto] = useState<string | null>(null);

  const [captions, setCaptions] = useState([
    'nem é magra',
    'na verdade eu não era loiro',
    'desculpa kkkkk',
    'desculpa kkkkk',
  ]);

  const [isAiGeneratingCaptions, setIsAiGeneratingCaptions] = useState(false);
  const [isGeneratingPost, setIsGeneratingPost] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const thumbnails = [
    userPhoto || '/img_creator_main.png',
    '/img_result_1.png',
    '/img_result_2.png',
    '/img_result_3.png',
  ];

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setUserPhoto(url);
    }
  };

  const handleGenerateCaptions = () => {
    setIsAiGeneratingCaptions(true);
    setTimeout(() => {
      if (postType === 'UGLY_TO_BEAUTIFUL') {
        const presets = [
          ['nem é magra', 'na verdade eu não era loiro', 'desculpa kkkkk', 'desculpa kkkkk'],
          ['achei que era feio', 'mas na verdade esse é o meu auge', 'desculpaaaaa', 'desculpa kkkkk'],
          ['antes da academia', 'fase do desleixo', 'começou o glow up', 'na verdade é foco kkkk'],
        ];
        setCaptions(presets[Math.floor(Math.random() * presets.length)]);
      } else {
        const presets = [
          ['achou que era modelo?', 'na verdade era só ângulo e filtro', 'desculpa aí', 'voltei ao normal kkkk'],
          ['parecia perfeito', 'mas na verdade...', 'barba mal feita total', 'desculpa kkkkk'],
        ];
        setCaptions(presets[Math.floor(Math.random() * presets.length)]);
      }
      setIsAiGeneratingCaptions(false);
    }, 600);
  };

  const handleSubmit = () => {
    setIsGeneratingPost(true);
    setTimeout(() => {
      onGenerate({
        postType,
        captions,
        userPhotoUrl: userPhoto || undefined,
      });
      setIsGeneratingPost(false);
    }, 1200);
  };

  return (
    <div className="p-4 space-y-4 pb-20">
      <h1 className="text-lg font-black text-gray-900">Criar Post</h1>

      {/* 1. SELETOR TIPO DE POST */}
      <div className="space-y-1.5">
        <label className="text-xs font-bold text-gray-700">Tipo de post</label>
        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={() => setPostType('UGLY_TO_BEAUTIFUL')}
            className={`py-2.5 px-3 rounded-xl font-bold text-xs border transition ${
              postType === 'UGLY_TO_BEAUTIFUL'
                ? 'border-[#00A86B] bg-emerald-50 text-[#00A86B] shadow-xs'
                : 'border-gray-200 text-gray-500 bg-white'
            }`}
          >
            Feio → Bonito
          </button>
          <button
            type="button"
            onClick={() => setPostType('BEAUTIFUL_TO_UGLY')}
            className={`py-2.5 px-3 rounded-xl font-bold text-xs border transition ${
              postType === 'BEAUTIFUL_TO_UGLY'
                ? 'border-red-500 bg-red-50 text-red-500 shadow-xs'
                : 'border-gray-200 text-gray-500 bg-white'
            }`}
          >
            Bonito → Feio
          </button>
        </div>
      </div>

      {/* 2. PREVIEW + CARROSSEL DE 4 THUMBNAILS */}
      <div className="space-y-2">
        <div className="relative h-64 rounded-2xl overflow-hidden bg-gray-100 border border-gray-200 shadow-xs">
          <img
            src={thumbnails[selectedThumb]}
            alt="Preview"
            className="w-full h-full object-cover"
          />

          {/* Current caption pill */}
          <div className="absolute inset-x-0 bottom-3 flex justify-center px-4">
            <span className="bg-black/75 text-white font-bold text-xs px-3 py-1.5 rounded-lg text-center backdrop-blur-xs">
              {captions[selectedThumb] || 'Legenda da etapa'}
            </span>
          </div>

          {/* Upload Button overlay */}
          <button
            onClick={() => fileInputRef.current?.click()}
            className="absolute top-3 right-3 bg-black/60 hover:bg-black/80 text-white text-[10px] font-bold px-2.5 py-1.5 rounded-full flex items-center gap-1 backdrop-blur-xs transition"
          >
            <Upload className="w-3 h-3" />
            Sua Foto
          </button>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            accept="image/*"
            className="hidden"
          />
        </div>

        {/* 4 Thumbnails */}
        <div className="flex items-center justify-between gap-2 pt-1">
          {thumbnails.map((thumb, idx) => (
            <div
              key={idx}
              onClick={() => setSelectedThumb(idx)}
              className="flex-1 flex flex-col items-center gap-1 cursor-pointer"
            >
              <span
                className={`w-5 h-5 rounded-full text-[10px] font-bold flex items-center justify-center transition ${
                  selectedThumb === idx ? 'bg-[#00A86B] text-white' : 'bg-gray-200 text-gray-600'
                }`}
              >
                {idx + 1}
              </span>
              <div
                className={`w-full aspect-square rounded-xl overflow-hidden border-2 bg-gray-100 transition ${
                  selectedThumb === idx ? 'border-[#00A86B]' : 'border-transparent'
                }`}
              >
                <img src={thumb} alt={`Etapa ${idx + 1}`} className="w-full h-full object-cover" />
              </div>
            </div>
          ))}
          <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center text-gray-500">
            <ArrowRight className="w-4 h-4" />
          </div>
        </div>
      </div>

      {/* 3. SELETOR DE TRAÇOS */}
      <div className="p-3.5 bg-white rounded-2xl border border-gray-200 space-y-3 shadow-xs">
        <div>
          <h3 className="font-bold text-sm text-gray-900">Seletor de traços</h3>
          <p className="text-[11px] text-gray-500">Toque para adicionar ou remover</p>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={onOpenAddTrait}
            className="py-2 px-3 rounded-xl bg-[#00A86B] hover:bg-[#046A4E] text-white text-xs font-bold flex items-center justify-center gap-1 shadow-xs transition"
          >
            <Plus className="w-3.5 h-3.5" />
            Adicionar beleza
          </button>
          <button
            type="button"
            onClick={onOpenAddTrait}
            className="py-2 px-3 rounded-xl bg-red-500 hover:bg-red-600 text-white text-xs font-bold flex items-center justify-center gap-1 shadow-xs transition"
          >
            <Plus className="w-3.5 h-3.5" />
            Adicionar feiura
          </button>
        </div>

        {/* Beleza List */}
        <div className="space-y-1">
          <span className="text-xs font-bold text-[#00A86B] block">Beleza</span>
          <div className="space-y-1">
            {traits
              .filter((t) => t.isBeauty)
              .map((t) => (
                <div
                  key={t.id}
                  className="flex items-center justify-between py-1 text-xs text-gray-700"
                >
                  <div
                    onClick={() => onToggleTrait(t.id)}
                    className="flex items-center gap-2 cursor-pointer flex-1"
                  >
                    {t.enabled ? (
                      <Eye className="w-3.5 h-3.5 text-[#00A86B]" />
                    ) : (
                      <EyeOff className="w-3.5 h-3.5 text-gray-400" />
                    )}
                    <span className={t.enabled ? 'font-medium' : 'text-gray-400 line-through'}>
                      {t.name}
                    </span>
                  </div>
                  <button
                    onClick={() => onRemoveTrait(t.id)}
                    className="text-red-400 hover:text-red-600 p-1"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
          </div>
        </div>

        {/* Feiura List */}
        <div className="space-y-1 pt-1 border-t border-gray-100">
          <span className="text-xs font-bold text-red-500 block">Feiura</span>
          <div className="space-y-1">
            {traits
              .filter((t) => !t.isBeauty)
              .map((t) => (
                <div
                  key={t.id}
                  className="flex items-center justify-between py-1 text-xs text-gray-700"
                >
                  <div
                    onClick={() => onToggleTrait(t.id)}
                    className="flex items-center gap-2 cursor-pointer flex-1"
                  >
                    {t.enabled ? (
                      <Eye className="w-3.5 h-3.5 text-red-500" />
                    ) : (
                      <EyeOff className="w-3.5 h-3.5 text-gray-400" />
                    )}
                    <span className={t.enabled ? 'font-medium' : 'text-gray-400 line-through'}>
                      {t.name}
                    </span>
                  </div>
                  <button
                    onClick={() => onRemoveTrait(t.id)}
                    className="text-red-400 hover:text-red-600 p-1"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
          </div>
        </div>

        <button
          type="button"
          onClick={onOpenAddTrait}
          className="w-full py-2 bg-gray-50 hover:bg-gray-100 border border-gray-200 text-gray-700 font-semibold text-xs rounded-xl transition"
        >
          + Criar traço personalizado
        </button>
      </div>

      {/* 4. LEGENDA DO POST COM BOTÃO GERAR */}
      <div className="p-3.5 bg-white rounded-2xl border border-gray-200 space-y-2.5 shadow-xs">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-sm text-gray-900">Legenda do post</h3>
          <button
            type="button"
            onClick={handleGenerateCaptions}
            className="flex items-center gap-1 px-2.5 py-1 bg-emerald-50 text-[#00A86B] font-bold text-xs rounded-lg hover:bg-emerald-100 transition"
          >
            <Sparkles className="w-3 h-3" />
            {isAiGeneratingCaptions ? 'Gerando...' : 'Gerar'}
          </button>
        </div>

        {captions.map((cap, idx) => (
          <div key={idx} className="flex items-center gap-2">
            <span className="text-[11px] font-semibold text-gray-500 w-12">Linha {idx + 1}</span>
            <div className="flex-1 relative">
              <input
                type="text"
                maxLength={60}
                value={cap}
                onChange={(e) => {
                  const updated = [...captions];
                  updated[idx] = e.target.value;
                  setCaptions(updated);
                }}
                className="w-full px-3 py-1.5 text-xs border border-gray-200 rounded-lg pr-10 focus:outline-hidden focus:border-[#00A86B]"
              />
              <span className="absolute right-2 top-1.5 text-[9px] text-gray-400">
                {cap.length}/60
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* 5. BANNER PEQUENO DE LEILÃO */}
      <div className="p-3 bg-gray-50 rounded-xl border border-gray-200 flex items-center justify-between shadow-xs">
        <div>
          <span className="text-xs font-bold text-gray-900 block">
            Espaço disponível – Leilão ao vivo
          </span>
          <span className="text-[11px] text-gray-500">Lance atual: R$ 15,00</span>
        </div>
        <button
          onClick={() => onOpenBid('banner_create')}
          className="text-xs font-bold text-[#00A86B] hover:underline"
        >
          Dar Lance
        </button>
      </div>

      {/* 6. BOTÃO GERAR COM IA */}
      <button
        onClick={handleSubmit}
        disabled={isGeneratingPost}
        className="w-full py-3.5 bg-[#00A86B] hover:bg-[#046A4E] text-white font-extrabold text-sm rounded-xl shadow-lg transition flex items-center justify-center gap-2 active:scale-98"
      >
        {isGeneratingPost ? (
          <>
            <RefreshCw className="w-4 h-4 animate-spin" />
            <span>Gerando 4 imagens com IA...</span>
          </>
        ) : (
          <>
            <Sparkles className="w-4 h-4" />
            <span>Gerar com IA</span>
          </>
        )}
      </button>
    </div>
  );
}
