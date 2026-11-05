package com.example.model

data class TrendPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarRes: Int? = null,
    val authorAvatarUrl: String? = null,
    val isVerified: Boolean = true,
    val location: String = "",
    val caption: String = "",
    val images: List<Int> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val stepCaptions: List<String> = emptyList(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val timeAgo: String = "Agora"
)

data class TrendCardItem(
    val id: String,
    val title: String,
    val caption: String,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val postsPerHour: String
)
