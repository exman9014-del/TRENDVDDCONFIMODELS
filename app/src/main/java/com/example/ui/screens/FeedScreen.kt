package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AuctionItem
import com.example.model.TrendPost
import com.example.ui.components.BidDialog
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.LivePulsingDot
import com.example.ui.theme.CardBorder
import com.example.ui.theme.LiveIndicatorRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NaVerdadeUiState

data class StoryItem(
    val username: String,
    val avatarRes: Int,
    val isLive: Boolean = false,
    val badgeIconText: String? = null
)

@Composable
fun FeedScreen(
    state: NaVerdadeUiState,
    onToggleLike: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenComments: (TrendPost) -> Unit,
    onCloseComments: () -> Unit,
    onAddComment: (postId: String, text: String) -> Unit,
    onSubmitBid: (auctionId: String, email: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAuctionForBid by remember { mutableStateOf<AuctionItem?>(null) }
    val feedAuction = state.auctions.find { it.id == "banner_feed" }
        ?: AuctionItem(
            id = "banner_feed",
            title = "Banner Feed",
            description = "No feed principal.",
            currentBid = 32.0,
            remainingSeconds = 267
        )

    val stories = listOf(
        StoryItem("pedro.builds", R.drawable.img_creator_main, isLive = false, badgeIconText = "🔨"),
        StoryItem("lari.adventures", R.drawable.img_hero_woman, isLive = true, badgeIconText = "AO VIVO"),
        StoryItem("caio.explore", R.drawable.img_result_4, isLive = false, badgeIconText = "⚡"),
        StoryItem("trend.queen", R.drawable.img_result_1, isLive = false),
        StoryItem("lucas.glow", R.drawable.img_result_2, isLive = false)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("feed_lazy_column")
    ) {
        // 1. TOP HEADER: AO VIVO + LOGO TIKTOK STYLE
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ao vivo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiveIndicatorRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    LivePulsingDot()
                }

                // TikTok Style Logo with Na Verdade
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Na Verdade",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryGreen)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Feed",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Mensagens",
                        tint = TextPrimary
                    )
                }
            }
        }

        // 2. STORIES ROW
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                // Add your story
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .border(1.dp, CardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Novo",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ao vivo",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                items(stories) { story ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (story.isLive) LiveIndicatorRed else PrimaryGreen,
                                    shape = CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            Image(
                                painter = painterResource(id = story.avatarRes),
                                contentDescription = story.username,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )

                            if (story.isLive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LiveIndicatorRed)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "AO VIVO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = story.username,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF3F4F6))
            )
        }

        // 3. FEED POSTS WITH LIVE AUCTION BANNER INTERSPERSED
        itemsIndexed(state.feedPosts) { index, post ->
            FeedPostItem(
                post = post,
                onLikeClick = { onToggleLike(post.id) },
                onFavoriteClick = { onToggleFavorite(post.id) },
                onCommentClick = { onOpenComments(post) },
                onShareClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Olha esse post da trend Na Verdade por ${post.authorName}: ${post.caption} #NaVerdade")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Post Na Verdade"))
                }
            )

            // Insert Live Auction Banner between post 0 and post 1 (matching reference design!)
            if (index == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                            )
                        )
                        .padding(14.dp)
                        .testTag("feed_auction_banner")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6366F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Alugue este espaço no feed – Leilão ao vivo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Seu conteúdo aqui. Para o público certo, no momento certo.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Lance atual",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = feedAuction.formattedBid,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "por ${feedAuction.highestBidder}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Tempo restante",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = feedAuction.formattedRemainingTime,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC)
                                )
                            }

                            Button(
                                onClick = { selectedAuctionForBid = feedAuction },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("feed_dar_lance_button")
                            ) {
                                Text(
                                    text = "Dar Lance",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (selectedAuctionForBid != null) {
        BidDialog(
            auction = selectedAuctionForBid!!,
            onDismiss = { selectedAuctionForBid = null },
            onSubmitBid = onSubmitBid
        )
    }

    // Comments Sheet
    state.selectedPostForComments?.let { post ->
        CommentsBottomSheet(
            post = post,
            comments = state.commentsForSelectedPost,
            isLoading = state.isLoadingComments,
            onAddComment = { text -> onAddComment(post.id, text) },
            onDismiss = onCloseComments
        )
    }
}

@Composable
fun FeedPostItem(
    post: TrendPost,
    onLikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // Author Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = post.authorAvatarRes ?: R.drawable.img_hero_woman),
                        contentDescription = post.authorName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (post.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verificado",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (post.location.isNotBlank()) {
                        Text(
                            text = post.location,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Opções",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post Images (4-image grid or single banner)
        if (post.images.size >= 4 || post.imageUrls.size >= 4) {
            // 4-Image Grid for "Na Verdade" trend!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PostGridImage(
                        res = post.images.getOrElse(0) { R.drawable.img_result_1 },
                        url = post.imageUrls.getOrNull(0),
                        caption = post.stepCaptions.getOrNull(0),
                        modifier = Modifier.weight(1f)
                    )
                    PostGridImage(
                        res = post.images.getOrElse(1) { R.drawable.img_result_2 },
                        url = post.imageUrls.getOrNull(1),
                        caption = post.stepCaptions.getOrNull(1),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PostGridImage(
                        res = post.images.getOrElse(2) { R.drawable.img_result_3 },
                        url = post.imageUrls.getOrNull(2),
                        caption = post.stepCaptions.getOrNull(2),
                        modifier = Modifier.weight(1f)
                    )
                    PostGridImage(
                        res = post.images.getOrElse(3) { R.drawable.img_result_4 },
                        url = post.imageUrls.getOrNull(3),
                        caption = post.stepCaptions.getOrNull(3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Single image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (post.imageUrls.isNotEmpty()) {
                    Image(
                        painter = coil.compose.rememberAsyncImagePainter(model = post.imageUrls.first()),
                        contentDescription = "Post photo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Image(
                        painter = painterResource(id = post.images.firstOrNull() ?: R.drawable.img_feed_post_1),
                        contentDescription = "Post photo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interaction bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLikeClick() }
                        .testTag("like_post_${post.id}")
                ) {
                    val heartColor by animateColorAsState(
                        targetValue = if (post.isLiked) Color(0xFFEF4444) else TextSecondary,
                        label = "heartColor"
                    )
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Curtir",
                        tint = heartColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Comment button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .testTag("comment_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comentar",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Share button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onShareClick() }
                        .testTag("share_post_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Compartilhar",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Favorite (bookmark) button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onFavoriteClick() }
                    .testTag("favorite_post_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Favoritar",
                    tint = if (post.isFavorited) PrimaryGreen else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Caption & Likes summary
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                text = "Curtido por pedro.builds e outras ${post.likesCount} pessoas",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                Text(
                    text = post.authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = post.caption,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            }
            if (post.commentsCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ver todos os ${post.commentsCount} comentários",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.clickable { onCommentClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFF3F4F6))
        )
    }
}

@Composable
fun PostGridImage(
    res: Int,
    url: String? = null,
    caption: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        if (!url.isNullOrBlank()) {
            Image(
                painter = coil.compose.rememberAsyncImagePainter(model = url),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = res),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (!caption.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
