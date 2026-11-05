package com.example.data

import com.example.R
import com.example.model.AuctionItem
import com.example.model.FeatureTrait
import com.example.model.PostType
import com.example.model.TrendCardItem
import com.example.model.TrendPost

class NaVerdadeRepository {

    fun getInitialAuctions(): List<AuctionItem> {
        return listOf(
            AuctionItem(
                id = "banner_home",
                title = "Banner Home",
                description = "Aparece na página inicial.",
                currentBid = 2450.0,
                remainingSeconds = 2 * 3600 + 34 * 60 + 18,
                highestBidder = "growth_agency_br",
                totalBids = 94,
                iconType = "home"
            ),
            AuctionItem(
                id = "banner_create",
                title = "Banner Criar Post",
                description = "Durante a criação do post.",
                currentBid = 1780.0,
                remainingSeconds = 1 * 3600 + 18 * 60 + 45,
                highestBidder = "viral_tools_saas",
                totalBids = 68,
                iconType = "create"
            ),
            AuctionItem(
                id = "banner_results",
                title = "Banner Resultados",
                description = "Exibido após gerar as imagens.",
                currentBid = 980.0,
                remainingSeconds = 3 * 3600 + 22 * 60 + 7,
                highestBidder = "creators_hub",
                totalBids = 53,
                iconType = "results"
            ),
            AuctionItem(
                id = "banner_feed",
                title = "Banner Feed",
                description = "No feed principal.",
                currentBid = 1250.0,
                remainingSeconds = 56 * 60 + 33,
                highestBidder = "midia_ninja",
                totalBids = 81,
                iconType = "feed"
            )
        )
    }

    fun getInitialTrendCards(): List<TrendCardItem> {
        return listOf(
            TrendCardItem(
                id = "trend_1",
                title = "Desculpaaaa",
                caption = "Trend do momento que tá dominando o TikTok e Reels",
                imageRes = R.drawable.img_result_3,
                postsPerHour = "15.8K posts/h"
            ),
            TrendCardItem(
                id = "trend_2",
                title = "Esse sou eu no meu auge",
                caption = "A transformação que chocou a internet inteira",
                imageRes = R.drawable.img_creator_main,
                postsPerHour = "12.4K posts/h"
            ),
            TrendCardItem(
                id = "trend_3",
                title = "Esse é o meu marido",
                caption = "Antes e depois surpreendente de glow up",
                imageRes = R.drawable.img_result_4,
                postsPerHour = "9.6K posts/h"
            ),
            TrendCardItem(
                id = "trend_4",
                title = "Na verdade eu não era assim",
                caption = "A trend #NaVerdade oficial com 4 fotos",
                imageRes = R.drawable.img_result_2,
                postsPerHour = "18.2K posts/h"
            ),
            TrendCardItem(
                id = "trend_5",
                title = "Nem é magra",
                caption = "Ironia e estética autêntica para o feed",
                imageRes = R.drawable.img_result_1,
                postsPerHour = "8.1K posts/h"
            )
        )
    }

    fun getInitialTraits(): List<FeatureTrait> {
        return listOf(
            // Beleza
            FeatureTrait(id = "b_olhos", name = "olhos verdes", isBeauty = true, isEnabled = true),
            FeatureTrait(id = "b_cabelo", name = "cabelo loiro", isBeauty = true, isEnabled = true),
            FeatureTrait(id = "b_barba", name = "barba", isBeauty = true, isEnabled = true),
            FeatureTrait(id = "b_tat_braco", name = "tatuagem braço", isBeauty = true, isEnabled = true),
            FeatureTrait(id = "b_tat_pescoco", name = "tatuagem pescoço", isBeauty = true, isEnabled = true),
            FeatureTrait(id = "b_musculoso", name = "musculoso", isBeauty = true, isEnabled = true),

            // Feiura
            FeatureTrait(id = "f_barba_ruim", name = "barba mal feita", isBeauty = false, isEnabled = true),
            FeatureTrait(id = "f_sem_barba", name = "sem barba", isBeauty = false, isEnabled = true),
            FeatureTrait(id = "f_palido", name = "pálido", isBeauty = false, isEnabled = true),
            FeatureTrait(id = "f_sujo", name = "sujo", isBeauty = false, isEnabled = true),
            FeatureTrait(id = "f_roupa_rasgada", name = "roupa rasgada", isBeauty = false, isEnabled = true),
            FeatureTrait(id = "f_postura", name = "postura desleixada", isBeauty = false, isEnabled = true)
        )
    }

    fun getInitialFeedPosts(): List<TrendPost> {
        return listOf(
            TrendPost(
                id = "post_larissa",
                authorName = "larissa.viaja",
                authorHandle = "@larissa.viaja",
                authorAvatarRes = R.drawable.img_hero_woman,
                isVerified = true,
                location = "Florianópolis, SC",
                caption = "Momentos que viram memórias 🌊☀️",
                images = listOf(R.drawable.img_feed_post_1),
                stepCaptions = listOf("Chegada", "Praia Secreta", "Centro Histórico", "Açaí Perfeito"),
                likesCount = 1248,
                isLiked = false,
                commentsCount = 89,
                timeAgo = "2h atrás"
            ),
            TrendPost(
                id = "post_trend_official",
                authorName = "pedro.builds",
                authorHandle = "@pedro.builds",
                authorAvatarRes = R.drawable.img_creator_main,
                isVerified = true,
                location = "São Paulo, SP",
                caption = "Vocês pediram a trend Na Verdade... aqui está! 😂🔥",
                images = listOf(
                    R.drawable.img_result_1,
                    R.drawable.img_result_2,
                    R.drawable.img_result_3,
                    R.drawable.img_result_4
                ),
                stepCaptions = listOf(
                    "nem é magra",
                    "na verdade eu não era loiro",
                    "desculpa kkkkk",
                    "desculpa kkkkk"
                ),
                likesCount = 4892,
                isLiked = true,
                commentsCount = 312,
                timeAgo = "35m atrás"
            ),
            TrendPost(
                id = "post_caio",
                authorName = "caio.explore",
                authorHandle = "@caio.explore",
                authorAvatarRes = R.drawable.img_creator_main,
                isVerified = true,
                location = "Chapada Diamantina, BA",
                caption = "Natureza que recarrega. Vale cada segundo de trilha! 🌿",
                images = listOf(R.drawable.img_feed_post_2),
                stepCaptions = listOf("Mirante", "Cachoeira", "Trilha", "Pôr do Sol"),
                likesCount = 873,
                isLiked = false,
                commentsCount = 44,
                timeAgo = "4h atrás"
            )
        )
    }
}
