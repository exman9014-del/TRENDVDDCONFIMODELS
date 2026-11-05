package com.example.model

data class FeatureTrait(
    val id: String,
    val name: String,
    val isBeauty: Boolean,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val creatorName: String? = null,
    val creatorAvatarUrl: String? = null
)

data class TraitSuggestion(
    val id: String,
    val name: String,
    val isBeauty: Boolean,
    val creatorName: String,
    val creatorAvatarRes: Int? = null,
    val creatorAvatarUrl: String? = null,
    val usageCount: Int = 1
)

enum class PostType {
    UGLY_TO_BEAUTIFUL, // Feio -> Bonito (Verde)
    BEAUTIFUL_TO_UGLY  // Bonito -> Feio (Vermelho)
}

data class GenerationResult(
    val images: List<Int> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val captions: List<String>,
    val tiktokTitle: String,
    val postType: PostType
)
