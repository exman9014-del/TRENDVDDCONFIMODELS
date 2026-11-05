'use client';

import React, { useState } from 'react';
import {
  Heart,
  MessageCircle,
  Share2,
  Bookmark,
  MoreHorizontal,
  Plus,
  BadgeCheck,
  Megaphone,
  Gavel,
  Radio,
  Sparkles,
  Send,
  X,
  Check,
} from 'lucide-react';
import { supabase } from '../lib/supabase';

export interface FeedPost {
  id: string;
  author: string;
  avatar: string;
  location: string;
  verified: boolean;
  images: string[];
  captions: string[];
  description: string;
  likes: number;
  comments: number;
  timeAgo: string;
  isLiked?: boolean;
  isFavorited?: boolean;
}

export interface PostComment {
  id: string;
  postId: string;
  authorName: string;
  content: string;
  timeAgo: string;
}

interface FeedViewProps {
  onOpenBid: (auctionId: string) => void;
  onCreateClick: () => void;
}

export const FeedView: React.FC<FeedViewProps> = ({ onOpenBid, onCreateClick }) => {
  const [posts, setPosts] = useState<FeedPost[]>([
    {
      id: 'p1',
      author: 'larissa.viaja',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop&crop=faces',
      location: 'Florianópolis, SC',
      verified: true,
      images: [
        '/img_feed_post_1.png',
        '/img_feed_post_2.png',
        '/img_result_1.png',
        '/img_result_3.png',
      ],
      captions: [
        'Desculpaaaaa',
        'Esse sou eu no meu auge',
        'Esse é o meu marido',
        'Na verdade era tudo trend',
      ],
      description: 'Trend do "Na Verdade" com o pessoal! Não acreditem em nada que vcs veem aqui kkkk 🌊✨',
      likes: 1248,
      comments: 89,
      timeAgo: '2h atrás',
      isLiked: false,
      isFavorited: false,
    },
    {
      id: 'p2',
      author: 'caio.explore',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=faces',
      location: 'Chapada Diamantina, BA',
      verified: true,
      images: [
        '/img_creator_main.png',
        '/img_result_2.png',
        '/img_result_4.png',
        '/img_hero_woman.png',
      ],
      captions: [
        'eu achando que tava lindo',
        'na verdade era só filtro',
        'depois do treino',
        'desculpa kkkk',
      ],
      description: 'Natureza que recarrega e trend que não perdoa. Arrasta pro lado pra ver a transformação ⛰️🔥',
      likes: 873,
      comments: 42,
      timeAgo: '4h atrás',
      isLiked: false,
      isFavorited: false,
    },
    {
      id: 'p3',
      author: 'mari.lifestyle',
      avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&h=150&fit=crop&crop=faces',
      location: 'São Paulo, SP',
      verified: true,
      images: [
        '/img_result_1.png',
        '/img_result_3.png',
        '/img_hero_woman.png',
        '/img_result_2.png',
      ],
      captions: [
        'nem é magra',
        'na verdade eu não era loiro',
        'desculpa kkkk',
        'socorro',
      ],
      description: 'Gerei com a IA do Na Verdade e o resultado me chocou! Testem agora no botão + ✨💚',
      likes: 2150,
      comments: 134,
      timeAgo: '6h atrás',
      isLiked: true,
      isFavorited: true,
    },
  ]);

  // Comments modal state
  const [selectedPostForComments, setSelectedPostForComments] = useState<FeedPost | null>(null);
  const [commentsList, setCommentsList] = useState<PostComment[]>([]);
  const [newCommentText, setNewCommentText] = useState('');
  const [shareToast, setShareToast] = useState(false);

  const stories = [
    { id: 's0', name: 'Você', avatar: '', isAdd: true },
    {
      id: 's1',
      name: 'Ao vivo',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop&crop=faces',
      isLive: true,
    },
    {
      id: 's2',
      name: 'pedro.builds',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop&crop=faces',
      badge: '🔨',
    },
    {
      id: 's3',
      name: 'lari.adventures',
      avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop&crop=faces',
      isLive: true,
    },
    {
      id: 's4',
      name: 'caio.explore',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=faces',
      badge: '⚡',
    },
  ];

  // 1. LIKE WITH SUPABASE PERSISTENCE
  const handleLike = async (id: string) => {
    let isNowLiked = false;
    let newLikes = 0;

    setPosts((prev) =>
      prev.map((p) => {
        if (p.id === id) {
          isNowLiked = !p.isLiked;
          newLikes = isNowLiked ? p.likes + 1 : Math.max(0, p.likes - 1);
          return {
            ...p,
            isLiked: isNowLiked,
            likes: newLikes,
          };
        }
        return p;
      })
    );

    try {
      if (isNowLiked) {
        await supabase.from('likes').insert({
          post_id: id,
          user_email: 'visitante@naverdade.app',
          created_at: new Date().toISOString(),
        });
      } else {
        await supabase
          .from('likes')
          .delete()
          .match({ post_id: id, user_email: 'visitante@naverdade.app' });
      }
      await supabase.from('posts').update({ likes_count: newLikes }).eq('id', id);
    } catch (err) {
      console.warn('Supabase like sync fallback:', err);
    }
  };

  // 2. FAVORITE WITH SUPABASE PERSISTENCE
  const handleFavorite = async (id: string) => {
    let isNowFav = false;

    setPosts((prev) =>
      prev.map((p) => {
        if (p.id === id) {
          isNowFav = !p.isFavorited;
          return {
            ...p,
            isFavorited: isNowFav,
          };
        }
        return p;
      })
    );

    try {
      if (isNowFav) {
        await supabase.from('favorites').insert({
          post_id: id,
          user_email: 'visitante@naverdade.app',
          created_at: new Date().toISOString(),
        });
      } else {
        await supabase
          .from('favorites')
          .delete()
          .match({ post_id: id, user_email: 'visitante@naverdade.app' });
      }
    } catch (err) {
      console.warn('Supabase favorite sync fallback:', err);
    }
  };

  // 3. SHARE BUTTON
  const handleShare = async (post: FeedPost) => {
    const text = `Olha o post da trend "Na Verdade" por ${post.author}: ${post.description} #NaVerdade`;
    if (navigator.share) {
      try {
        await navigator.share({
          title: 'Na Verdade - Trend Viral',
          text,
          url: window.location.href,
        });
      } catch (e) {
        // Ignored
      }
    } else {
      navigator.clipboard.writeText(`${text} - ${window.location.href}`);
      setShareToast(true);
      setTimeout(() => setShareToast(false), 2500);
    }
  };

  // 4. COMMENTS MODAL & SUPABASE PERSISTENCE
  const openComments = async (post: FeedPost) => {
    setSelectedPostForComments(post);
    try {
      const { data } = await supabase
        .from('comments')
        .select('*')
        .eq('post_id', post.id)
        .order('created_at', { ascending: true });

      if (data && data.length > 0) {
        setCommentsList(
          data.map((c: any) => ({
            id: c.id,
            postId: c.post_id,
            authorName: c.author_name || 'Usuário',
            content: c.content,
            timeAgo: 'Recente',
          }))
        );
      } else {
        setCommentsList([
          {
            id: 'c1',
            postId: post.id,
            authorName: 'pedro.builds',
            content: 'Essa trend ficou muito boa kkkkkk',
            timeAgo: '10m atrás',
          },
          {
            id: 'c2',
            postId: post.id,
            authorName: 'larissa.viaja',
            content: 'Gerei o meu ontem, o resultado me chocou!',
            timeAgo: '5m atrás',
          },
        ]);
      }
    } catch (e) {
      setCommentsList([
        {
          id: 'c1',
          postId: post.id,
          authorName: 'pedro.builds',
          content: 'Essa trend ficou incrível!',
          timeAgo: '10m atrás',
        },
      ]);
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCommentText.trim() || !selectedPostForComments) return;

    const newComment: PostComment = {
      id: `c_${Date.now()}`,
      postId: selectedPostForComments.id,
      authorName: 'Você',
      content: newCommentText.trim(),
      timeAgo: 'Agora',
    };

    setCommentsList((prev) => [...prev, newComment]);
    setPosts((prev) =>
      prev.map((p) =>
        p.id === selectedPostForComments.id ? { ...p, comments: p.comments + 1 } : p
      )
    );

    const textToSave = newCommentText.trim();
    setNewCommentText('');

    try {
      await supabase.from('comments').insert({
        post_id: selectedPostForComments.id,
        author_name: 'Você',
        content: textToSave,
        created_at: new Date().toISOString(),
      });
    } catch (err) {
      console.warn('Supabase comment sync fallback:', err);
    }
  };

  return (
    <div className="pb-16 max-w-xl mx-auto">
      {/* Top Bar inside Feed view */}
      <div className="px-4 py-3 flex items-center justify-between border-b border-gray-100 bg-white sticky top-14 z-20">
        <div className="flex items-center gap-2">
          <span className="flex h-2.5 w-2.5 rounded-full bg-red-500 animate-ping"></span>
          <span className="font-bold text-gray-900 text-sm tracking-wide">FEED DA TREND</span>
        </div>
        <button
          onClick={onCreateClick}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-emerald-600 text-white text-xs font-semibold hover:bg-emerald-700 transition"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Criar meu post</span>
        </button>
      </div>

      {/* Stories Row */}
      <div className="px-4 py-3 flex gap-3 overflow-x-auto no-scrollbar border-b border-gray-100 bg-white">
        {stories.map((story) => (
          <div key={story.id} className="flex flex-col items-center flex-shrink-0 cursor-pointer">
            {story.isAdd ? (
              <button
                onClick={onCreateClick}
                className="w-16 h-16 rounded-full border-2 border-dashed border-emerald-500 flex items-center justify-center bg-emerald-50 hover:bg-emerald-100 transition relative"
              >
                <Plus className="w-6 h-6 text-emerald-600" />
              </button>
            ) : (
              <div className="relative">
                <div
                  className={`w-16 h-16 rounded-full p-0.5 ${
                    story.isLive
                      ? 'bg-gradient-to-tr from-pink-500 via-red-500 to-yellow-400'
                      : 'bg-gradient-to-tr from-emerald-500 to-teal-400'
                  }`}
                >
                  <img
                    src={story.avatar}
                    alt={story.name}
                    className="w-full h-full rounded-full object-cover border-2 border-white"
                  />
                </div>
                {story.isLive ? (
                  <span className="absolute -bottom-1 left-1/2 -translate-x-1/2 bg-red-600 text-white text-[9px] font-black px-1.5 py-0.2 rounded uppercase tracking-wider">
                    AO VIVO
                  </span>
                ) : story.badge ? (
                  <span className="absolute -bottom-1 right-0 bg-white border border-gray-200 text-xs rounded-full w-5 h-5 flex items-center justify-center shadow-sm">
                    {story.badge}
                  </span>
                ) : null}
              </div>
            )}
            <span className="text-[11px] font-medium text-gray-700 mt-1 max-w-[68px] truncate text-center">
              {story.name}
            </span>
          </div>
        ))}
      </div>

      {/* Feed Posts */}
      <div className="divide-y divide-gray-100">
        {posts.map((post, idx) => (
          <React.Fragment key={post.id}>
            <article className="bg-white pt-4 pb-5">
              {/* Post Header */}
              <div className="px-4 flex items-center justify-between mb-3">
                <div className="flex items-center gap-2.5">
                  <img
                    src={post.avatar}
                    alt={post.author}
                    className="w-10 h-10 rounded-full object-cover border border-emerald-200"
                  />
                  <div>
                    <div className="flex items-center gap-1">
                      <span className="font-bold text-sm text-gray-900">{post.author}</span>
                      {post.verified && (
                        <BadgeCheck className="w-4 h-4 text-emerald-600 fill-emerald-100" />
                      )}
                    </div>
                    <span className="text-xs text-gray-500">{post.location}</span>
                  </div>
                </div>
                <button className="text-gray-400 hover:text-gray-600 p-1">
                  <MoreHorizontal className="w-5 h-5" />
                </button>
              </div>

              {/* 4-Image Grid (Na Verdade layout) */}
              <div className="px-4 mb-3">
                <div className="grid grid-cols-2 gap-1.5 rounded-2xl overflow-hidden bg-gray-100 shadow-sm aspect-square">
                  {post.images.map((imgSrc, imgIdx) => (
                    <div key={imgIdx} className="relative group overflow-hidden bg-black/5">
                      <img
                        src={imgSrc}
                        alt={`Parte ${imgIdx + 1}`}
                        className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                      />
                      {post.captions[imgIdx] && (
                        <div className="absolute inset-x-0 bottom-0 p-2 bg-gradient-to-t from-black/85 via-black/40 to-transparent">
                          <span className="text-white text-[11px] font-bold drop-shadow leading-tight line-clamp-2">
                            {post.captions[imgIdx]}
                          </span>
                        </div>
                      )}
                      <div className="absolute top-1.5 left-1.5 bg-black/60 backdrop-blur-sm text-white text-[10px] font-extrabold w-5 h-5 rounded-full flex items-center justify-center">
                        {imgIdx + 1}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Actions Row */}
              <div className="px-4 flex items-center justify-between mb-2">
                <div className="flex items-center gap-4">
                  <button
                    onClick={() => handleLike(post.id)}
                    className="flex items-center gap-1 text-gray-700 hover:text-red-500 transition"
                  >
                    <Heart
                      className={`w-6 h-6 ${
                        post.isLiked ? 'fill-red-500 text-red-500' : 'text-gray-700'
                      }`}
                    />
                  </button>
                  <button
                    onClick={() => openComments(post)}
                    className="flex items-center gap-1 text-gray-700 hover:text-emerald-600 transition"
                  >
                    <MessageCircle className="w-6 h-6" />
                  </button>
                  <button
                    onClick={() => handleShare(post)}
                    className="text-gray-700 hover:text-emerald-600 transition"
                  >
                    <Share2 className="w-6 h-6" />
                  </button>
                </div>
                <button
                  onClick={() => handleFavorite(post.id)}
                  className="text-gray-700 hover:text-emerald-600 transition"
                >
                  <Bookmark
                    className={`w-6 h-6 ${
                      post.isFavorited ? 'fill-[#00A86B] text-[#00A86B]' : 'text-gray-700'
                    }`}
                  />
                </button>
              </div>

              {/* Likes & Caption */}
              <div className="px-4 space-y-1">
                <p className="text-xs font-semibold text-gray-900">
                  Curtido por <span className="font-bold">pedro.builds</span> e outras{' '}
                  <span className="font-bold">{post.likes.toLocaleString()} pessoas</span>
                </p>
                <p className="text-sm text-gray-800 leading-snug">
                  <span className="font-bold mr-1.5">{post.author}</span>
                  {post.description}
                </p>
                <div className="flex items-center gap-2 pt-1 text-xs text-gray-400">
                  <button
                    onClick={() => openComments(post)}
                    className="hover:text-gray-600 transition"
                  >
                    Ver todos os {post.comments} comentários
                  </button>
                  <span>•</span>
                  <span>{post.timeAgo}</span>
                </div>
              </div>
            </article>

            {/* Interspersed Auction Banner */}
            {idx === 0 && (
              <div className="p-4 bg-gray-50">
                <div className="rounded-2xl p-4 bg-gradient-to-r from-gray-950 via-purple-950 to-gray-900 text-white shadow-md border border-purple-800/40">
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <div className="flex items-center gap-2.5">
                      <div className="w-9 h-9 rounded-xl bg-purple-600/30 border border-purple-400/40 flex items-center justify-center text-purple-300">
                        <Megaphone className="w-5 h-5" />
                      </div>
                      <div>
                        <h4 className="font-bold text-sm text-white leading-tight">
                          Alugue este espaço no feed
                        </h4>
                        <span className="text-[11px] text-purple-300 font-medium">
                          — Leilão ao vivo
                        </span>
                      </div>
                    </div>
                    <span className="bg-red-600/90 text-white text-[10px] font-black px-2 py-0.5 rounded-full uppercase tracking-wider animate-pulse">
                      AO VIVO
                    </span>
                  </div>

                  <p className="text-xs text-gray-300 mb-3 leading-relaxed">
                    Seu anúncio ou post exibido entre as publicações para milhares de criadores em tempo real.
                  </p>

                  <div className="flex items-center justify-between pt-2 border-t border-white/10">
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold">Lance atual</div>
                      <div className="text-base font-black text-emerald-400">R$ 32,00</div>
                    </div>
                    <div className="text-center">
                      <div className="text-[10px] text-gray-400 uppercase font-semibold">Tempo restante</div>
                      <div className="text-xs font-mono font-bold text-purple-200">04:27</div>
                    </div>
                    <button
                      onClick={() => onOpenBid('banner_feed')}
                      className="px-4 py-2 rounded-xl bg-gradient-to-r from-pink-600 to-rose-600 hover:from-pink-500 hover:to-rose-500 text-white text-xs font-bold shadow-md transition flex items-center gap-1.5"
                    >
                      <Gavel className="w-3.5 h-3.5" />
                      <span>Dar Lance</span>
                    </button>
                  </div>
                </div>
              </div>
            )}
          </React.Fragment>
        ))}
      </div>

      {/* COMMENTS MODAL */}
      {selectedPostForComments && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-xs">
          <div className="bg-white w-full max-w-sm rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <h3 className="font-bold text-sm text-gray-900">
                Comentários ({commentsList.length})
              </h3>
              <button
                onClick={() => setSelectedPostForComments(null)}
                className="p-1 text-gray-400 hover:text-gray-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* List */}
            <div className="flex-1 overflow-y-auto py-3 space-y-3">
              {commentsList.map((comm) => (
                <div key={comm.id} className="text-xs">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-gray-900">{comm.authorName}</span>
                    <span className="text-[10px] text-gray-400">{comm.timeAgo}</span>
                  </div>
                  <p className="text-gray-700 mt-0.5">{comm.content}</p>
                </div>
              ))}
            </div>

            {/* Form */}
            <form onSubmit={handleAddComment} className="pt-2 border-t border-gray-100 flex gap-2">
              <input
                type="text"
                placeholder="Adicione um comentário..."
                value={newCommentText}
                onChange={(e) => setNewCommentText(e.target.value)}
                className="flex-1 py-2 px-3 text-xs border border-gray-200 rounded-xl focus:outline-none focus:border-emerald-600"
              />
              <button
                type="submit"
                className="w-9 h-9 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl flex items-center justify-center flex-shrink-0 transition"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Share Toast */}
      {shareToast && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-xs font-bold px-4 py-2 rounded-full shadow-lg flex items-center gap-1.5 z-50 animate-bounce">
          <Check className="w-4 h-4 text-emerald-400" />
          <span>Link copiado para a área de transferência!</span>
        </div>
      )}
    </div>
  );
};
