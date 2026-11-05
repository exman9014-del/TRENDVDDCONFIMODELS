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
  Camera,
  Trash2,
  ListFilter,
  Search,
} from 'lucide-react';

export interface Trait {
  id: string;
  name: string;
  isBeauty: boolean;
  enabled: boolean;
  creatorName?: string;
}

export interface TraitSuggestion {
  id: string;
  name: string;
  isBeauty: boolean;
  creatorName: string;
  creatorAvatar?: string;
  usageCount?: number;
}

interface CreatePostViewProps {
  onGenerate: (data: {
    postType: 'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY';
    captions: string[];
    userPhotoUrl?: string;
  }) => void;
  onOpenBid: (auctionId: string) => void;
  onOpenAddTrait: () => void;
  imageTraitsMap: Record<number, Trait[]>;
  onToggleTraitForImage: (imageIndex: number, traitId: string) => void;
  onRemoveTraitForImage: (imageIndex: number, traitId: string) => void;
  onAddTraitFromSuggestion: (suggestion: TraitSuggestion) => void;
  traitSuggestions: TraitSuggestion[];
  containersCount: number;
  onAddContainer: () => void;
  onRemoveContainer: (index: number) => void;
  userPhoto: string | null;
  onUploadUserPhoto: (url: string) => void;
}

export default function CreatePostView({
  onGenerate,
  onOpenBid,
  onOpenAddTrait,
  imageTraitsMap,
  onToggleTraitForImage,
  onRemoveTraitForImage,
  onAddTraitFromSuggestion,
  traitSuggestions,
  containersCount,
  onAddContainer,
  onRemoveContainer,
  userPhoto,
  onUploadUserPhoto,
}: CreatePostViewProps) {
  const [postType, setPostType] = useState<'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY'>(
    'UGLY_TO_BEAUTIFUL'
  );
  const [selectedThumb, setSelectedThumb] = useState(0);
  const [showAllSuggestions, setShowAllSuggestions] = useState(false);
  const [suggestionsSearch, setSuggestionsSearch] = useState('');
  const [suggestionFilter, setSuggestionFilter] = useState<'all' | 'beauty' | 'ugly'>('all');

  // Captions list (redline for each image container)
  const [captions, setCaptions] = useState<string[]>([
    'nem é magra',
    'na verdade eu não era loiro',
    'desculpa kkkkk',
    'desculpa kkkkk',
  ]);

  const [isAiGeneratingCaptions, setIsAiGeneratingCaptions] = useState(false);
  const [isGeneratingPost, setIsGeneratingPost] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // The user photo slot is ALWAYS the last container
  const userPhotoIndex = containersCount - 1;

  // Make sure captions has enough lines for all containers
  React.useEffect(() => {
    if (captions.length < containersCount) {
      setCaptions((prev) => {
        const next = [...prev];
        while (next.length < containersCount) {
          next.push(`desculpa kkkkk (${next.length + 1})`);
        }
        return next;
      });
    }
  }, [containersCount, captions.length]);

  const defaultPhotos = [
    '/img_creator_main.png',
    '/img_result_1.png',
    '/img_result_2.png',
    '/img_result_3.png',
    '/img_result_4.png',
  ];

  const getContainerPhoto = (idx: number) => {
    if (idx === userPhotoIndex && userPhoto) {
      return userPhoto;
    }
    return defaultPhotos[idx % defaultPhotos.length];
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      onUploadUserPhoto(url);
    }
  };

  const handleCaptionChange = (index: number, val: string) => {
    setCaptions((prev) => {
      const next = [...prev];
      next[index] = val;
      return next;
    });
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
        const selected = presets[Math.floor(Math.random() * presets.length)];
        const expanded = [...selected];
        while (expanded.length < containersCount) {
          expanded.push(`desculpa kkkkk (${expanded.length + 1})`);
        }
        setCaptions(expanded.slice(0, containersCount));
      } else {
        const presets = [
          ['achou que era modelo?', 'na verdade era só ângulo e filtro', 'desculpa aí', 'voltei ao normal kkkk'],
          ['parecia perfeito', 'mas na verdade...', 'barba mal feita total', 'desculpa kkkkk'],
        ];
        const selected = presets[Math.floor(Math.random() * presets.length)];
        const expanded = [...selected];
        while (expanded.length < containersCount) {
          expanded.push(`desculpa kkkkk (${expanded.length + 1})`);
        }
        setCaptions(expanded.slice(0, containersCount));
      }
      setIsAiGeneratingCaptions(false);
    }, 600);
  };

  const handleSubmit = () => {
    setIsGeneratingPost(true);
    setTimeout(() => {
      onGenerate({
        postType,
        captions: captions.slice(0, containersCount),
        userPhotoUrl: userPhoto || undefined,
      });
      setIsGeneratingPost(false);
    }, 1200);
  };

  const currentTraits = imageTraitsMap[selectedThumb] || [];
  const isSelectedUserPhoto = selectedThumb === userPhotoIndex;

  const filteredSuggestions = traitSuggestions.filter((s) => {
    const matchesSearch =
      s.name.toLowerCase().includes(suggestionsSearch.toLowerCase()) ||
      s.creatorName.toLowerCase().includes(suggestionsSearch.toLowerCase());
    const matchesType =
      suggestionFilter === 'all' ||
      (suggestionFilter === 'beauty' && s.isBeauty) ||
      (suggestionFilter === 'ugly' && !s.isBeauty);
    return matchesSearch && matchesType;
  });

  return (
    <div className="p-4 space-y-4 pb-20">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-black text-gray-900">Criar Post</h1>
        <span className="text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
          {containersCount} Fotos
        </span>
      </div>

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
                ? 'border-red-500 bg-red-50 text-red-600 shadow-xs'
                : 'border-gray-200 text-gray-500 bg-white'
            }`}
          >
            Bonito → Feio
          </button>
        </div>
      </div>

      {/* 2. SUGESTÕES DE TRAÇOS DE OUTROS USUÁRIOS (TICKER ESTILO LETREIRO) */}
      <div className="p-3 rounded-2xl bg-gradient-to-r from-emerald-50 via-teal-50 to-emerald-50 border border-emerald-200/80 space-y-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-800">
            <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
            <span>Sugestões de outros usuários</span>
          </div>
          <button
            onClick={() => setShowAllSuggestions(true)}
            className="text-[11px] font-bold text-emerald-700 hover:text-emerald-900 flex items-center gap-1"
          >
            <span>Ver todas</span>
            <ListFilter className="w-3 h-3" />
          </button>
        </div>

        {/* Marquee ticker row */}
        <div className="flex gap-2 overflow-x-auto no-scrollbar py-1">
          {traitSuggestions.map((sug) => (
            <button
              key={sug.id}
              onClick={() => onAddTraitFromSuggestion(sug)}
              className="flex items-center gap-1.5 bg-white border border-emerald-200 hover:border-emerald-400 px-2.5 py-1.5 rounded-full text-xs flex-shrink-0 shadow-2xs transition group"
            >
              <div className="w-4 h-4 rounded-full overflow-hidden bg-emerald-100 flex-shrink-0">
                <img
                  src={
                    sug.creatorAvatar ||
                    'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=50&h=50&fit=crop'
                  }
                  alt={sug.creatorName}
                  className="w-full h-full object-cover"
                />
              </div>
              <span className="font-semibold text-gray-800 text-[11px]">
                {sug.name} <span className="text-gray-400 font-normal">por {sug.creatorName}</span>
              </span>
              <Plus
                className={`w-3.5 h-3.5 ${
                  sug.isBeauty ? 'text-[#00A86B]' : 'text-red-500'
                } group-hover:scale-110 transition`}
              />
            </button>
          ))}
        </div>
      </div>

      {/* 3. VISUALIZADOR PRINCIPAL DA FOTO SELECIONADA */}
      <div className="relative rounded-2xl overflow-hidden aspect-[4/5] bg-gray-100 border border-gray-200 shadow-inner group">
        <img
          src={getContainerPhoto(selectedThumb)}
          alt={`Foto ${selectedThumb + 1}`}
          className="w-full h-full object-cover"
        />

        {/* Highlight if waiting for user photo upload in the last container */}
        {isSelectedUserPhoto && (
          <div className="absolute top-3 left-3 bg-[#00A86B] text-white text-[10px] font-black uppercase px-2.5 py-1 rounded-full shadow-md flex items-center gap-1">
            <Camera className="w-3 h-3" />
            <span>Sua Foto (Último Container)</span>
          </div>
        )}

        {/* Upload Button */}
        <div className="absolute top-3 right-3 flex gap-2">
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            accept="image/*"
            className="hidden"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            className="bg-black/60 backdrop-blur-md text-white text-xs font-semibold px-3 py-1.5 rounded-full flex items-center gap-1.5 hover:bg-black/80 transition"
          >
            <Upload className="w-3.5 h-3.5" />
            <span>{userPhoto && isSelectedUserPhoto ? 'Trocar Foto' : 'Sua Foto'}</span>
          </button>
        </div>

        {/* Caption overlay */}
        {captions[selectedThumb] && (
          <div className="absolute bottom-3 inset-x-3 text-center">
            <span className="inline-block bg-black/70 backdrop-blur-xs text-white text-xs font-bold px-3 py-1.5 rounded-md drop-shadow-md">
              {captions[selectedThumb]}
            </span>
          </div>
        )}
      </div>

      {/* 4. CONTAINERS ROW COM THUMBNAILS + BOTÃO "+1" */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-2">
        {Array.from({ length: containersCount }).map((_, idx) => {
          const isSelected = selectedThumb === idx;
          const isUserSlot = idx === userPhotoIndex;

          return (
            <div
              key={idx}
              onClick={() => setSelectedThumb(idx)}
              className="flex flex-col items-center flex-shrink-0 cursor-pointer"
            >
              <div
                className={`w-5 h-5 rounded-full text-[10px] font-bold flex items-center justify-center mb-1 ${
                  isSelected
                    ? 'bg-[#00A86B] text-white'
                    : isUserSlot
                    ? 'bg-amber-500 text-white'
                    : 'bg-gray-200 text-gray-600'
                }`}
              >
                {idx + 1}
              </div>
              <div
                className={`w-16 h-20 rounded-xl overflow-hidden border-2 relative ${
                  isSelected
                    ? 'border-[#00A86B] ring-2 ring-emerald-100'
                    : isUserSlot
                    ? 'border-amber-400'
                    : 'border-transparent'
                }`}
              >
                <img
                  src={getContainerPhoto(idx)}
                  alt={`Thumbnail ${idx + 1}`}
                  className="w-full h-full object-cover"
                />
                {isUserSlot && (
                  <div className="absolute bottom-0 inset-x-0 bg-amber-500 text-white text-[8px] font-bold text-center py-0.5">
                    SUA FOTO
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* BOTÃO "+1" PARA ADICIONAR CONTAINER */}
        <button
          onClick={onAddContainer}
          className="flex flex-col items-center justify-center w-16 h-20 mt-6 rounded-xl border-2 border-dashed border-emerald-500 bg-emerald-50/60 hover:bg-emerald-100/60 transition flex-shrink-0"
        >
          <Plus className="w-5 h-5 text-emerald-600 mb-0.5" />
          <span className="text-[11px] font-black text-emerald-700">+1 Foto</span>
        </button>
      </div>

      {/* 5. SELETOR DE TRAÇOS POR IMAGEM (NÃO MAIS GLOBAL) */}
      <div className="bg-gray-50 border border-gray-200 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-bold text-sm text-gray-900">Seletor de traços</h3>
              <span className="bg-[#00A86B] text-white text-[10px] font-bold px-2 py-0.5 rounded-full">
                Foto {selectedThumb + 1} de {containersCount}
              </span>
            </div>
            <p className="text-[11px] text-gray-500">
              Personalize o que aparece nesta imagem individualmente
            </p>
          </div>

          {containersCount > 4 && (
            <button
              onClick={() => onRemoveContainer(selectedThumb)}
              className="text-red-500 hover:text-red-700 p-1 rounded-lg"
              title="Excluir este container"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Quick Add Trait Buttons */}
        <div className="grid grid-cols-2 gap-2">
          <button
            onClick={onOpenAddTrait}
            className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white border border-emerald-300 text-[#00A86B] font-bold text-xs hover:bg-emerald-50 transition"
          >
            <Plus className="w-4 h-4 text-[#00A86B]" />
            <span>Adicionar beleza</span>
          </button>
          <button
            onClick={onOpenAddTrait}
            className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white border border-red-300 text-red-500 font-bold text-xs hover:bg-red-50 transition"
          >
            <Plus className="w-4 h-4 text-red-500" />
            <span>Adicionar feiura</span>
          </button>
        </div>

        {/* Beleza List for this image */}
        <div className="space-y-1.5 pt-2">
          <span className="text-xs font-bold text-[#00A86B] block">
            Beleza (Foto {selectedThumb + 1})
          </span>
          {currentTraits
            .filter((t) => t.isBeauty)
            .map((trait) => (
              <div
                key={trait.id}
                className="flex items-center justify-between py-1 px-2 rounded-lg bg-white border border-gray-100"
              >
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onToggleTraitForImage(selectedThumb, trait.id)}
                    className="text-emerald-600"
                  >
                    {trait.enabled ? (
                      <Eye className="w-4 h-4 text-[#00A86B]" />
                    ) : (
                      <EyeOff className="w-4 h-4 text-gray-400" />
                    )}
                  </button>
                  <span
                    className={`text-xs ${
                      trait.enabled ? 'text-gray-900 font-medium' : 'text-gray-400 line-through'
                    }`}
                  >
                    {trait.name}
                  </span>
                </div>
                <button
                  onClick={() => onRemoveTraitForImage(selectedThumb, trait.id)}
                  className="text-gray-300 hover:text-red-500"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
        </div>

        {/* Feiura List for this image */}
        <div className="space-y-1.5 pt-2">
          <span className="text-xs font-bold text-red-500 block">
            Feiura (Foto {selectedThumb + 1})
          </span>
          {currentTraits
            .filter((t) => !t.isBeauty)
            .map((trait) => (
              <div
                key={trait.id}
                className="flex items-center justify-between py-1 px-2 rounded-lg bg-white border border-gray-100"
              >
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => onToggleTraitForImage(selectedThumb, trait.id)}
                    className="text-red-500"
                  >
                    {trait.enabled ? (
                      <Eye className="w-4 h-4 text-red-500" />
                    ) : (
                      <EyeOff className="w-4 h-4 text-gray-400" />
                    )}
                  </button>
                  <span
                    className={`text-xs ${
                      trait.enabled ? 'text-gray-900 font-medium' : 'text-gray-400 line-through'
                    }`}
                  >
                    {trait.name}
                  </span>
                </div>
                <button
                  onClick={() => onRemoveTraitForImage(selectedThumb, trait.id)}
                  className="text-gray-300 hover:text-red-500"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
        </div>

        <button
          onClick={onOpenAddTrait}
          className="w-full py-2 bg-white border border-dashed border-gray-300 rounded-xl text-xs font-semibold text-gray-600 hover:text-gray-900 hover:border-gray-400 transition"
        >
          + Criar traço personalizado (Compartilhar)
        </button>
      </div>

      {/* 6. LEGENDAS DO POST COM AUTO-GERAÇÃO (REDLINE DINÂMICO) */}
      <div className="bg-white border border-gray-200 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="font-bold text-sm text-gray-900">Legenda do post</h3>
            <span className="text-[11px] text-gray-500">
              Gere com IA ou personalize para as {containersCount} fotos
            </span>
          </div>
          <button
            onClick={handleGenerateCaptions}
            disabled={isAiGeneratingCaptions}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-emerald-50 text-[#00A86B] font-bold text-xs hover:bg-emerald-100 transition"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>{isAiGeneratingCaptions ? 'Gerando...' : 'Gerar com IA'}</span>
          </button>
        </div>

        <div className="space-y-2">
          {Array.from({ length: containersCount }).map((_, i) => (
            <div key={i} className="flex items-center gap-2">
              <span className="text-[11px] font-bold text-gray-500 w-12 flex-shrink-0">
                Linha {i + 1}
              </span>
              <div className="relative flex-1">
                <input
                  type="text"
                  maxLength={60}
                  value={captions[i] || ''}
                  onChange={(e) => handleCaptionChange(i, e.target.value)}
                  placeholder={`Legenda da foto ${i + 1}...`}
                  className="w-full py-2 px-3 pr-12 text-xs border border-gray-200 rounded-xl focus:outline-none focus:border-[#00A86B]"
                />
                <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-gray-400">
                  {(captions[i] || '').length}/60
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 7. BANNER PEQUENO DE LEILÃO */}
      <div className="p-3 bg-gray-50 border border-gray-200 rounded-2xl flex items-center justify-between">
        <div>
          <span className="text-xs font-bold text-gray-800 block">
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

      {/* 8. BOTÃO FINAL GERAR COM IA */}
      <button
        onClick={handleSubmit}
        disabled={isGeneratingPost}
        className="w-full py-3.5 bg-[#00A86B] hover:bg-[#046A4E] text-white font-extrabold text-sm rounded-2xl shadow-md flex items-center justify-center gap-2 transition active:scale-98 disabled:opacity-50"
      >
        {isGeneratingPost ? (
          <>
            <RefreshCw className="w-4 h-4 animate-spin" />
            <span>Processando {containersCount} imagens com IA...</span>
          </>
        ) : (
          <>
            <Sparkles className="w-4 h-4" />
            <span>Gerar com IA ({containersCount} Imagens)</span>
          </>
        )}
      </button>

      {/* MODAL "VER TODAS AS SUGESTÕES" */}
      {showAllSuggestions && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-white w-full max-w-sm rounded-3xl p-5 shadow-2xl flex flex-col max-h-[85vh]">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div>
                <h3 className="font-bold text-sm text-gray-900">Sugestões da Comunidade</h3>
                <p className="text-[11px] text-gray-500">Toque para adicionar à foto selecionada</p>
              </div>
              <button
                onClick={() => setShowAllSuggestions(false)}
                className="p-1 text-gray-400 hover:text-gray-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Search */}
            <div className="my-3 relative">
              <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Buscar traço ou criador..."
                value={suggestionsSearch}
                onChange={(e) => setSuggestionsSearch(e.target.value)}
                className="w-full pl-9 pr-3 py-2 text-xs border border-gray-200 rounded-xl focus:outline-none focus:border-emerald-600"
              />
            </div>

            {/* Filter tags */}
            <div className="flex gap-2 mb-3">
              <button
                onClick={() => setSuggestionFilter('all')}
                className={`px-3 py-1 rounded-full text-xs font-bold ${
                  suggestionFilter === 'all'
                    ? 'bg-emerald-600 text-white'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                Todos ({traitSuggestions.length})
              </button>
              <button
                onClick={() => setSuggestionFilter('beauty')}
                className={`px-3 py-1 rounded-full text-xs font-bold ${
                  suggestionFilter === 'beauty'
                    ? 'bg-emerald-600 text-white'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                Beleza
              </button>
              <button
                onClick={() => setSuggestionFilter('ugly')}
                className={`px-3 py-1 rounded-full text-xs font-bold ${
                  suggestionFilter === 'ugly'
                    ? 'bg-red-500 text-white'
                    : 'bg-gray-100 text-gray-600'
                }`}
              >
                Feiura
              </button>
            </div>

            {/* Suggestions list */}
            <div className="flex-1 overflow-y-auto space-y-2 pr-1">
              {filteredSuggestions.map((sug) => (
                <div
                  key={sug.id}
                  onClick={() => {
                    onAddTraitFromSuggestion(sug);
                    setShowAllSuggestions(false);
                  }}
                  className="flex items-center justify-between p-2.5 rounded-xl border border-gray-100 hover:border-emerald-300 hover:bg-emerald-50/40 cursor-pointer transition"
                >
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-full overflow-hidden bg-gray-100">
                      <img
                        src={
                          sug.creatorAvatar ||
                          'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=60&h=60&fit=crop'
                        }
                        alt={sug.creatorName}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-bold text-xs text-gray-900">{sug.name}</span>
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.2 rounded ${
                            sug.isBeauty
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {sug.isBeauty ? 'Beleza' : 'Feiura'}
                        </span>
                      </div>
                      <span className="text-[10px] text-gray-500">por {sug.creatorName}</span>
                    </div>
                  </div>
                  <Plus className="w-4 h-4 text-emerald-600" />
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
