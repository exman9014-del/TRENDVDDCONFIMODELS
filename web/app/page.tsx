'use client';

import React, { useState, useEffect } from 'react';
import TopHeader from '../components/TopHeader';
import Navbar, { TabType } from '../components/Navbar';
import LandingView from '../components/LandingView';
import CreatePostView from '../components/CreatePostView';
import ResultsView from '../components/ResultsView';
import AuctionView from '../components/AuctionView';
import { FeedView } from '../components/FeedView';
import { ProfileView } from '../components/ProfileView';
import BidModal from '../components/BidModal';
import AddTraitModal from '../components/AddTraitModal';
import { supabase } from '../lib/supabase';

interface Trait {
  id: string;
  name: string;
  isBeauty: boolean;
  enabled: boolean;
}

interface AuctionData {
  id: string;
  title: string;
  description: string;
  currentBid: number;
  remainingSeconds: number;
  iconType?: string;
}

export default function HomePage() {
  const [currentTab, setCurrentTab] = useState<TabType>('home');
  const [previousTab, setPreviousTab] = useState<TabType>('home');
  const [showResults, setShowResults] = useState(false);

  // Live indicators
  const [liveUsers, setLiveUsers] = useState(1247);
  const [liveViewers, setLiveViewers] = useState(892);
  const [postsCreated, setPostsCreated] = useState(3240);

  // Auctions data
  const [auctions, setAuctions] = useState<AuctionData[]>([
    {
      id: 'banner_home',
      title: 'Banner Home',
      description: 'Aparece na página inicial.',
      currentBid: 2450,
      remainingSeconds: 9258,
      iconType: 'home',
    },
    {
      id: 'banner_create',
      title: 'Banner Criar Post',
      description: 'Durante a criação do post.',
      currentBid: 1780,
      remainingSeconds: 4725,
      iconType: 'plus',
    },
    {
      id: 'banner_results',
      title: 'Banner Resultados',
      description: 'Exibido após gerar as imagens.',
      currentBid: 980,
      remainingSeconds: 12127,
      iconType: 'check',
    },
    {
      id: 'banner_feed',
      title: 'Banner Feed',
      description: 'No feed principal.',
      currentBid: 1250,
      remainingSeconds: 3393,
      iconType: 'camera',
    },
  ]);

  // Selected auction for bidding
  const [activeBidAuction, setActiveBidAuction] = useState<AuctionData | null>(null);

  // Trait modal
  const [isAddTraitOpen, setIsAddTraitOpen] = useState(false);

  // Initial traits list
  const [traits, setTraits] = useState<Trait[]>([
    // Beleza
    { id: 'b1', name: 'olhos verdes', isBeauty: true, enabled: true },
    { id: 'b2', name: 'cabelo loiro', isBeauty: true, enabled: true },
    { id: 'b3', name: 'barba', isBeauty: true, enabled: true },
    { id: 'b4', name: 'tatuagem braço', isBeauty: true, enabled: false },
    { id: 'b5', name: 'tatuagem pescoço', isBeauty: true, enabled: false },
    { id: 'b6', name: 'musculoso', isBeauty: true, enabled: false },
    // Feiura
    { id: 'u1', name: 'barba mal feita', isBeauty: false, enabled: true },
    { id: 'u2', name: 'sem barba', isBeauty: false, enabled: false },
    { id: 'u3', name: 'pálido', isBeauty: false, enabled: false },
    { id: 'u4', name: 'sujo', isBeauty: false, enabled: false },
    { id: 'u5', name: 'roupa rasgada', isBeauty: false, enabled: false },
    { id: 'u6', name: 'postura desleixada', isBeauty: false, enabled: false },
  ]);

  // Results State
  const [resultData, setResultData] = useState<{
    images: string[];
    captions: string[];
    tiktokTitle: string;
  }>({
    images: [
      '/img_result_1.png',
      '/img_result_2.png',
      '/img_result_3.png',
      '/img_result_4.png',
    ],
    captions: [
      'nem é magra',
      'na verdade eu não era loiro',
      'desculpa kkkk',
      'desculpa kkkk',
    ],
    tiktokTitle: 'POV: Você caiu na trend do "Na Verdade" 😂 #naverdade #trend #glowup',
  });

  // Tab navigation
  const handleSelectTab = (tab: TabType) => {
    setPreviousTab(currentTab);
    setCurrentTab(tab);
    setShowResults(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Open Bid Modal
  const handleOpenBid = (auctionId: string) => {
    const found = auctions.find((a) => a.id === auctionId);
    if (found) {
      setActiveBidAuction(found);
    }
  };

  // Submit Bid
  const handleSubmitBid = async (auctionId: string, email: string, amount: number) => {
    setAuctions((prev) =>
      prev.map((item) => (item.id === auctionId ? { ...item, currentBid: amount } : item))
    );
    setActiveBidAuction(null);

    // Sync to Supabase
    try {
      await supabase.from('bids').insert({
        banner_id: auctionId,
        amount,
        bidder_email: email,
        created_at: new Date().toISOString(),
      });
    } catch (err) {
      console.warn('Supabase bid sync fallback:', err);
    }
  };

  // Trait actions
  const handleToggleTrait = (id: string) => {
    setTraits((prev) =>
      prev.map((t) => (t.id === id ? { ...t, enabled: !t.enabled } : t))
    );
  };

  const handleRemoveTrait = (id: string) => {
    setTraits((prev) => prev.filter((t) => t.id !== id));
  };

  const handleAddCustomTrait = (name: String, isBeauty: boolean) => {
    const newTrait: Trait = {
      id: `custom_${Date.now()}`,
      name: String(name),
      isBeauty,
      enabled: true,
    };
    setTraits((prev) => [...prev, newTrait]);
  };

  // Generate with AI handler
  const handleGeneratePost = (data: {
    postType: 'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY';
    captions: string[];
    selectedImage: string;
  }) => {
    // Generate results
    setResultData({
      images: [
        '/img_result_1.png',
        '/img_result_2.png',
        '/img_result_3.png',
        '/img_result_4.png',
      ],
      captions: data.captions.some((c) => c.trim().length > 0)
        ? data.captions
        : [
            'nem é magra',
            'na verdade eu não era loiro',
            'desculpa kkkk',
            'desculpa kkkk',
          ],
      tiktokTitle:
        data.postType === 'UGLY_TO_BEAUTIFUL'
          ? 'POV: O glow up da trend "Na Verdade" chocou todo mundo 😱 #naverdade #glowup'
          : 'POV: A ilusão da internet quando você descobre a verdade 😂 #naverdade #trend',
    });
    setShowResults(true);
    setPostsCreated((prev) => prev + 1);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Email subscribe
  const handleSubscribeEmail = async (email: string) => {
    try {
      await supabase.from('subscribers').insert({
        email,
        created_at: new Date().toISOString(),
      });
    } catch (err) {
      console.warn('Supabase subscriber sync fallback:', err);
    }
  };

  // Countdown timer effect
  useEffect(() => {
    const interval = setInterval(() => {
      setAuctions((prev) =>
        prev.map((a) => ({
          ...a,
          remainingSeconds: Math.max(0, a.remainingSeconds - 1),
        }))
      );
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  // Live viewer pulse effect
  useEffect(() => {
    const timer = setInterval(() => {
      setLiveUsers((prev) => Math.max(1100, prev + Math.floor(Math.random() * 5) - 2));
      setLiveViewers((prev) => Math.max(800, prev + Math.floor(Math.random() * 7) - 3));
    }, 4500);
    return () => clearInterval(timer);
  }, []);

  const homeAuction = auctions.find((a) => a.id === 'banner_home') || auctions[0];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans">
      {/* Top Header */}
      <TopHeader liveUsers={liveUsers} />

      {/* Main Content Area */}
      <main className="flex-1 max-w-md w-full mx-auto bg-white shadow-xs">
        {showResults ? (
          <ResultsView
            resultData={resultData}
            onBack={() => setShowResults(false)}
            onPublish={() => {
              setShowResults(false);
              setCurrentTab('feed');
            }}
            onOpenBid={handleOpenBid}
          />
        ) : (
          <>
            {currentTab === 'home' && (
              <LandingView
                onCreateClick={() => handleSelectTab('create')}
                onOpenBid={handleOpenBid}
                homeBid={homeAuction.currentBid}
                homeCountdown={homeAuction.remainingSeconds}
                onSubscribe={handleSubscribeEmail}
              />
            )}

            {currentTab === 'create' && (
              <CreatePostView
                traits={traits}
                onToggleTrait={handleToggleTrait}
                onRemoveTrait={handleRemoveTrait}
                onOpenAddTrait={() => setIsAddTraitOpen(true)}
                onOpenBid={handleOpenBid}
                onGenerate={handleGeneratePost}
              />
            )}

            {currentTab === 'auctions' && (
              <AuctionView
                auctions={auctions}
                onOpenBid={handleOpenBid}
                activeUsers={liveUsers}
                liveViewers={liveViewers}
                postsCreated={postsCreated}
              />
            )}

            {currentTab === 'feed' && (
              <FeedView
                onOpenBid={handleOpenBid}
                onCreateClick={() => handleSelectTab('create')}
              />
            )}

            {currentTab === 'profile' && (
              <ProfileView
                onCreateClick={() => handleSelectTab('create')}
                onAuctionClick={() => handleSelectTab('auctions')}
              />
            )}
          </>
        )}
      </main>

      {/* Navigation Bar */}
      <Navbar currentTab={currentTab} onSelectTab={handleSelectTab} />

      {/* Bid Modal */}
      {activeBidAuction && (
        <BidModal
          auction={activeBidAuction}
          onClose={() => setActiveBidAuction(null)}
          onSubmit={handleSubmitBid}
        />
      )}

      {/* Add Trait Modal */}
      {isAddTraitOpen && (
        <AddTraitModal
          onClose={() => setIsAddTraitOpen(false)}
          onAdd={handleAddCustomTrait}
        />
      )}
    </div>
  );
}
