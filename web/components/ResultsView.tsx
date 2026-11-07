'use client';

import React, { useState } from 'react';
import { ArrowLeft, Copy, Check, Download, Share2, HelpCircle, Clock } from 'lucide-react';

interface ResultsViewProps {
  onBack: () => void;
  onPublish: () => void;
  onOpenBid: (auctionId: string) => void;
  resultData: {
    images: string[];
    captions: string[];
    sources: Array<'fooocus' | 'comfyui' | 'original'>;
    tiktokTitle: string;
  };
}

export default function ResultsView({
  onBack,
  onPublish,
  onOpenBid,
  resultData,
}: ResultsViewProps) {
  const [copied, setCopied] = useState(false);
  const [autoEmail, setAutoEmail] = useState('');
  const [emailSaved, setEmailSaved] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadSaved, setDownloadSaved] = useState(false);
  const generatedSources = resultData.sources.filter((source) => source !== 'original');
  const generatedBackend = generatedSources[0];
  const generatedLabel =
    generatedBackend === 'comfyui' ? 'ComfyUI' : generatedBackend === 'fooocus' ? 'Fooocus' : 'IA';

  const handleCopyTitle = () => {
    navigator.clipboard.writeText(resultData.tiktokTitle);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleSaveAutoPost = (e: React.FormEvent) => {
    e.preventDefault();
    if (autoEmail) {
      setEmailSaved(true);
      setTimeout(() => setEmailSaved(false), 3000);
    }
  };

  // REAL BROWSER DOWNLOAD FUNCTION (FIXED)
  const downloadSingleImage = async (imgSrc: string, index: number) => {
    try {
      if (imgSrc.startsWith('data:') || imgSrc.startsWith('blob:')) {
        const a = document.createElement('a');
        a.href = imgSrc;
        a.download = `na_verdade_foto_${index + 1}.png`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        return;
      }

      const res = await fetch(imgSrc);
      const blob = await res.blob();
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `na_verdade_foto_${index + 1}.png`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(blobUrl);
      document.body.removeChild(a);
    } catch (err) {
      const a = document.createElement('a');
      a.href = imgSrc;
      a.download = `na_verdade_foto_${index + 1}.png`;
      a.target = '_blank';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }
  };

  const handleDownloadAll = async () => {
    setIsDownloading(true);
    try {
      for (let i = 0; i < resultData.images.length; i++) {
        await downloadSingleImage(resultData.images[i], i);
        await new Promise((r) => setTimeout(r, 250));
      }
      setDownloadSaved(true);
    } catch (e) {
      console.warn('Download error:', e);
      setDownloadSaved(true);
    } finally {
      setIsDownloading(false);
      setTimeout(() => setDownloadSaved(false), 3500);
    }
  };

  return (
    <div className="p-4 space-y-4 pb-20">
      {/* Top action header */}
      <div className="flex items-center justify-between">
        <button onClick={onBack} className="p-1 text-gray-700 hover:text-black">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <button className="text-gray-400 hover:text-gray-600">
          <HelpCircle className="w-5 h-5" />
        </button>
      </div>

      <div className="text-center space-y-1">
        <h1 className="text-lg font-black text-gray-900">
          {generatedSources.length} imagens geradas por {generatedLabel}
        </h1>
        <p className="text-xs text-gray-500">
          A última é sua foto original. Deslize para ver e baixar.
        </p>
      </div>

      {/* 1. CARROSSEL DAS IMAGENS GERADAS COM LEGENDAS */}
      <div className="flex gap-2.5 overflow-x-auto no-scrollbar py-2 px-1">
        {resultData.images.map((img, idx) => (
          <div
            key={idx}
            className="shrink-0 w-44 h-72 rounded-2xl overflow-hidden relative shadow-md bg-gray-100 border border-gray-200 group"
          >
            <img
              src={img}
              alt={`Imagem ${idx + 1}`}
              className="w-full h-full object-cover"
            />
            <span className="absolute top-2 left-2 bg-black/60 text-white font-bold text-[10px] px-2 py-0.5 rounded-full">
              {idx + 1}/{resultData.images.length}
            </span>
            <span
              className={`absolute top-9 left-2 text-white font-bold text-[9px] px-2 py-1 rounded-full ${
                resultData.sources[idx] === 'original' ? 'bg-gray-700/80' : 'bg-emerald-700/90'
              }`}
            >
              {resultData.sources[idx] === 'original'
                ? 'Foto original'
                : `Gerada pelo ${resultData.sources[idx] === 'comfyui' ? 'ComfyUI' : 'Fooocus'}`}
            </span>

            {/* Single image download button */}
            <button
              onClick={() => downloadSingleImage(img, idx)}
              className="absolute top-2 right-2 bg-black/60 hover:bg-black/80 text-white p-1.5 rounded-full backdrop-blur-xs transition shadow-sm"
              title="Baixar esta imagem"
            >
              <Download className="w-3.5 h-3.5" />
            </button>

            <div className="absolute inset-x-0 bottom-0 bg-linear-to-t from-black/90 via-black/50 to-transparent p-3 pt-6">
              <p className="text-white font-extrabold text-xs leading-snug drop-shadow-sm">
                {resultData.captions[idx] || 'desculpa kkkkk'}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Dots indicator */}
      <div className="flex justify-center gap-1.5 py-1">
        {resultData.images.map((_, i) => (
          <span
            key={i}
            className={`w-2 h-2 rounded-full ${i === 0 ? 'bg-[#00A86B]' : 'bg-gray-300'}`}
          />
        ))}
      </div>

      {/* 2. TÍTULO DO TIKTOK + BOTÃO COPIAR */}
      <div className="space-y-1.5">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold text-gray-800">Título do TikTok</span>
          <button
            onClick={handleCopyTitle}
            className="text-xs font-bold text-[#00A86B] flex items-center gap-1 hover:underline"
          >
            {copied ? (
              <>
                <Check className="w-3.5 h-3.5" />
                Copiado!
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5" />
                Copiar título
              </>
            )}
          </button>
        </div>
        <div className="p-3 bg-gray-50 rounded-xl border border-gray-200 text-xs text-gray-700 leading-relaxed font-medium">
          {resultData.tiktokTitle}
        </div>
      </div>

      {/* 3. BANNER DE LEILÃO AO VIVO (GRADIENTE AZUL / ROXO) */}
      <div
        onClick={() => onOpenBid('banner_results')}
        className="p-4 rounded-2xl bg-linear-to-r from-indigo-600 to-blue-600 text-white shadow-md cursor-pointer space-y-2 hover:opacity-95 transition"
      >
        <span className="font-bold text-xs block">Seu anúncio aqui – Leilão ao vivo</span>
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[10px] text-white/80">Lance atual</span>
            <div className="text-lg font-black text-amber-300">R$ 150,00</div>
          </div>
          <div className="flex items-center gap-1 bg-white/20 backdrop-blur-xs px-2.5 py-1 rounded-full text-xs font-bold">
            <Clock className="w-3.5 h-3.5" />
            00:15:30
          </div>
        </div>
      </div>

      {/* 4. POSTAGEM AUTOMÁTICA EM BREVE - DEIXE SEU EMAIL */}
      <div className="space-y-1.5">
        <div className="py-1 px-3 bg-gray-100 rounded-lg text-center text-xs font-semibold text-gray-600">
          Postagem automática em breve – deixe seu email
        </div>
        <form onSubmit={handleSaveAutoPost} className="flex gap-2">
          <input
            type="email"
            required
            placeholder="seu@email.com"
            value={autoEmail}
            onChange={(e) => setAutoEmail(e.target.value)}
            className="flex-1 px-3 py-2 text-xs border border-gray-200 rounded-xl focus:outline-hidden focus:border-[#00A86B]"
          />
          <button
            type="submit"
            className="px-4 py-2 bg-[#00A86B] hover:bg-[#046A4E] text-white font-bold text-xs rounded-xl shadow-xs transition"
          >
            {emailSaved ? 'Salvo! ✓' : 'Salvar'}
          </button>
        </form>
      </div>

      {/* 5. BOTÕES: BAIXAR IMAGENS E PUBLICAR NO FEED */}
      <div className="space-y-2 pt-1">
        <button
          onClick={handleDownloadAll}
          disabled={isDownloading}
          className="w-full py-3 bg-white border border-gray-200 hover:bg-gray-50 text-gray-800 font-bold text-xs rounded-xl shadow-xs flex items-center justify-center gap-1.5 transition disabled:opacity-60"
        >
          <Download className="w-4 h-4 text-emerald-600" />
          {isDownloading
            ? 'Baixando Imagens para o seu Dispositivo...'
            : downloadSaved
            ? 'Imagens Baixadas com Sucesso! ✓'
            : `Baixar Todas (${resultData.images.length} Imagens)`}
        </button>

        <button
          onClick={onPublish}
          className="w-full py-3 bg-[#00A86B] hover:bg-[#046A4E] text-white font-bold text-xs rounded-xl shadow-md flex items-center justify-center gap-1.5 transition"
        >
          <Share2 className="w-4 h-4" />
          Publicar no Feed do App (Sem Login)
        </button>
      </div>
    </div>
  );
}
