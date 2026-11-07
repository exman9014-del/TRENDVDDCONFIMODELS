-- ==============================================================================
-- SCHEMA COMPLETO DO SUPABASE - APLICATIVO "NA VERDADE" (MOBILE & WEB)
-- Cole este script no SQL Editor do seu painel Supabase e clique em "RUN".
-- ==============================================================================

-- 1. HABILITAR EXTENSÃO UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 2. TABELA: posts (Publicações da Trend com fotos e legendas)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.posts (
    id TEXT PRIMARY KEY DEFAULT ('post_' || extract(epoch from now())::bigint || '_' || substr(md5(random()::text), 1, 6)),
    author_name TEXT NOT NULL DEFAULT 'criador_viral',
    author_handle TEXT NOT NULL DEFAULT '@criador',
    caption TEXT DEFAULT '',
    tiktok_title TEXT DEFAULT '',
    step_captions JSONB DEFAULT '[]'::jsonb,
    images_urls JSONB DEFAULT '[]'::jsonb,
    likes_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 3. TABELA: likes (Curtidas com prevenção de duplicatas)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id TEXT NOT NULL,
    user_email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    CONSTRAINT unique_post_user_like UNIQUE (post_id, user_email)
);

-- ------------------------------------------------------------------------------
-- 4. TABELA: favorites (Salvar posts favoritos)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id TEXT NOT NULL,
    user_email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    CONSTRAINT unique_post_user_favorite UNIQUE (post_id, user_email)
);

-- ------------------------------------------------------------------------------
-- 5. TABELA: comments (Comentários dos posts)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id TEXT NOT NULL,
    author_name TEXT NOT NULL DEFAULT 'Usuário',
    author_avatar TEXT DEFAULT '',
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 6. TABELA: custom_traits (Traços personalizados e sugestões da comunidade)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.custom_traits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    is_beauty BOOLEAN NOT NULL DEFAULT true,
    creator_name TEXT NOT NULL DEFAULT 'Comunidade',
    creator_avatar TEXT DEFAULT '',
    usage_count INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 7. TABELA: bids (Lances dos leilões de espaço publicitário)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.bids (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id TEXT,
    banner_id TEXT,
    email TEXT,
    bidder_email TEXT,
    amount NUMERIC(10, 2) NOT NULL,
    timestamp BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 8. TABELA: subscribers (Inscrições na newsletter de trends)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.subscribers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL UNIQUE,
    subscribed_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 9. TABELA: auctions (Dados dos 4 Banners de leilão)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.auctions (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    current_bid NUMERIC(10, 2) NOT NULL DEFAULT 15.00,
    remaining_seconds BIGINT DEFAULT 3600,
    highest_bidder TEXT DEFAULT 'anunciante',
    total_bids INTEGER DEFAULT 0,
    icon_type TEXT DEFAULT 'home'
);

-- SEED DOS 4 BANNERS PADRÃO
INSERT INTO public.auctions (id, title, description, current_bid, remaining_seconds, highest_bidder, total_bids, icon_type)
VALUES
    ('banner_home', 'Banner Home', 'Aparece na página inicial.', 2450.00, 9258, 'growth_agency_br', 94, 'home'),
    ('banner_create', 'Banner Criar Post', 'Durante a criação do post.', 1780.00, 4725, 'viral_tools_saas', 68, 'create'),
    ('banner_results', 'Banner Resultados', 'Exibido após gerar as imagens.', 980.00, 12127, 'creators_hub', 53, 'results'),
    ('banner_feed', 'Banner Feed', 'No feed principal entre publicações.', 1250.00, 3393, 'midia_ninja', 81, 'feed')
ON CONFLICT (id) DO UPDATE SET
    current_bid = EXCLUDED.current_bid,
    highest_bidder = EXCLUDED.highest_bidder;

-- SEED INICIAL DE SUGESTÕES DE TRAÇOS DA COMUNIDADE
INSERT INTO public.custom_traits (name, is_beauty, creator_name, usage_count)
VALUES
    ('olhos verdes', true, 'Leonardo', 1420),
    ('tatuagem dragão', true, 'Larissa', 980),
    ('barba lenhador', true, 'Pedro', 840),
    ('cabelo platinado', true, 'Mariana', 760),
    ('sorriso perfeito', true, 'Beatriz', 590),
    ('olheiras pesadas', false, 'Caio', 610),
    ('cabelo bagunçado', false, 'Lucas', 450)
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------------------------
-- 10. SEGURANÇA: HABILITAR RLS COM POLÍTICAS PÚBLICAS (Login é opcional)
-- ------------------------------------------------------------------------------
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.custom_traits ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bids ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscribers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.auctions ENABLE ROW LEVEL SECURITY;

-- Políticas para POSTS
CREATE POLICY "Public Read Posts" ON public.posts FOR SELECT USING (true);
CREATE POLICY "Public Insert Posts" ON public.posts FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Update Posts" ON public.posts FOR UPDATE USING (true);

-- Políticas para LIKES
CREATE POLICY "Public Read Likes" ON public.likes FOR SELECT USING (true);
CREATE POLICY "Public Insert Likes" ON public.likes FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Delete Likes" ON public.likes FOR DELETE USING (true);

-- Políticas para FAVORITES
CREATE POLICY "Public Read Favorites" ON public.favorites FOR SELECT USING (true);
CREATE POLICY "Public Insert Favorites" ON public.favorites FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Delete Favorites" ON public.favorites FOR DELETE USING (true);

-- Políticas para COMMENTS
CREATE POLICY "Public Read Comments" ON public.comments FOR SELECT USING (true);
CREATE POLICY "Public Insert Comments" ON public.comments FOR INSERT WITH CHECK (true);

-- Políticas para CUSTOM_TRAITS
CREATE POLICY "Public Read Traits" ON public.custom_traits FOR SELECT USING (true);
CREATE POLICY "Public Insert Traits" ON public.custom_traits FOR INSERT WITH CHECK (true);

-- Políticas para BIDS
CREATE POLICY "Public Read Bids" ON public.bids FOR SELECT USING (true);
CREATE POLICY "Public Insert Bids" ON public.bids FOR INSERT WITH CHECK (true);

-- Políticas para SUBSCRIBERS
CREATE POLICY "Public Read Subscribers" ON public.subscribers FOR SELECT USING (true);
CREATE POLICY "Public Insert Subscribers" ON public.subscribers FOR INSERT WITH CHECK (true);

-- Políticas para AUCTIONS
CREATE POLICY "Public Read Auctions" ON public.auctions FOR SELECT USING (true);
CREATE POLICY "Public Update Auctions" ON public.auctions FOR UPDATE USING (true);

-- ------------------------------------------------------------------------------
-- 11. BUCKET DO STORAGE: naverdade-images (Armazenamento de fotos)
-- ------------------------------------------------------------------------------
INSERT INTO storage.buckets (id, name, public)
VALUES ('naverdade-images', 'naverdade-images', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Políticas do Storage
CREATE POLICY "Public Storage Read" ON storage.objects
FOR SELECT USING (bucket_id = 'naverdade-images');

CREATE POLICY "Public Storage Insert" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'naverdade-images');

CREATE POLICY "Public Storage Update" ON storage.objects
FOR UPDATE USING (bucket_id = 'naverdade-images');

-- ------------------------------------------------------------------------------
-- 12. ATIVAR REALTIME (Para contadores e lances ao vivo)
-- ------------------------------------------------------------------------------
DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.posts;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;

DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE public.bids;
EXCEPTION WHEN OTHERS THEN NULL;
END $$;
