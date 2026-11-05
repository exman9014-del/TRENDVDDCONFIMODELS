package com.example.model

data class FeatureTrait(
    val id: String,
    val name: String,
    val isBeauty: Boolean,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false
)

enum class PostType {
    UGLY_TO_BEAUTIFUL, // Feio -> Bonito (Verde)
    BEAUTIFUL_TO_UGLY  // Bonito -> Feio (Vermelho)
}

data class GenerationResult(
    val images: List<Int>,
    val captions: List<String>,
    val tiktokTitle: String,
    val postType: PostType
)
