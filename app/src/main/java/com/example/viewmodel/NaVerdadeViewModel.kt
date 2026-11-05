package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.NaVerdadeRepository
import com.example.data.SupabaseClient
import com.example.model.AuctionItem
import com.example.model.BidSubmission
import com.example.model.FeatureTrait
import com.example.model.GenerationResult
import com.example.model.PostType
import com.example.model.TrendCardItem
import com.example.model.TrendPost
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenDestination {
    LANDING,
    CREATE_POST,
    RESULTS,
    AUCTIONS,
    FEED,
    PROFILE
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false
)

data class NaVerdadeUiState(
    val currentScreen: ScreenDestination = ScreenDestination.LANDING,
    val activeUsers: Int = 1247,
    val liveViewers: Int = 892,
    val peopleEnteringRecent: Int = 14,
    val siteVisits: String = "12.4k",
    val postsCreatedCount: Int = 3280,
    val advertisersCount: Int = 87,
    val homeAuctionHighestBid: Double = 152.0,
    val homeAuctionTotalBids: Int = 47,
    val homeAuctionCountdownSeconds: Int = 23,
    val auctions: List<AuctionItem> = emptyList(),
    val trendCards: List<TrendCardItem> = emptyList(),
    val traits: List<FeatureTrait> = emptyList(),
    val feedPosts: List<TrendPost> = emptyList(),
    val userCreatedPosts: List<TrendPost> = emptyList(),
    val userBids: List<BidSubmission> = emptyList(),

    // Post Creation State
    val postType: PostType = PostType.UGLY_TO_BEAUTIFUL,
    val selectedThumbnailIndex: Int = 0,
    val captionLine1: String = "nem é magra",
    val captionLine2: String = "na verdade eu não era loiro",
    val captionLine3: String = "desculpa kkkkk",
    val captionLine4: String = "desculpa kkkkk",
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,

    // Results State
    val latestResult: GenerationResult? = null,
    val tiktokTitle: String = "POV: Quando você acha que conhece alguém, mas na verdade... 😳 #NaVerdade #TrendViral #GlowUp",
    val autoPostEmail: String = "",

    // Newsletter State
    val newsletterEmail: String = "",

    // Auth & Profile
    val userEmail: String? = null,
    val isLoggedIn: Boolean = false,

    // Feedback
    val notification: UiNotification? = null
)

class NaVerdadeViewModel(
    private val repository: NaVerdadeRepository = NaVerdadeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NaVerdadeUiState())
    val uiState: StateFlow<NaVerdadeUiState> = _uiState.asStateFlow()

    init {
        // Load initial seed data
        val initialAuctions = repository.getInitialAuctions()
        val initialTrends = repository.getInitialTrendCards()
        val initialTraits = repository.getInitialTraits()
        val initialFeed = repository.getInitialFeedPosts()

        _uiState.update {
            it.copy(
                auctions = initialAuctions,
                trendCards = initialTrends,
                traits = initialTraits,
                feedPosts = initialFeed,
                latestResult = GenerationResult(
                    images = listOf(
                        R.drawable.img_result_1,
                        R.drawable.img_result_2,
                        R.drawable.img_result_3,
                        R.drawable.img_result_4
                    ),
                    captions = listOf(
                        "nem é magra",
                        "na verdade eu não era loiro",
                        "desculpa kkkkk",
                        "desculpa kkkkk"
                    ),
                    tiktokTitle = "POV: na verdade eu nunca fui assim... kkkkk #NaVerdade #Trend",
                    postType = PostType.UGLY_TO_BEAUTIFUL
                )
            )
        }

        // Start live ticker coroutine
        startLiveTickers()
    }

    private fun startLiveTickers() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { state ->
                    // Decrement auction seconds
                    val updatedAuctions = state.auctions.map { auction ->
                        if (auction.remainingSeconds > 0) {
                            auction.copy(remainingSeconds = auction.remainingSeconds - 1)
                        } else {
                            auction.copy(remainingSeconds = 3600) // Reset loop
                        }
                    }

                    val newCountdown = if (state.homeAuctionCountdownSeconds > 0) {
                        state.homeAuctionCountdownSeconds - 1
                    } else 60

                    // Slight natural fluctuation in live viewers
                    val randomViewerDelta = (-2..3).random()
                    val newViewers = (state.liveViewers + randomViewerDelta).coerceIn(800, 1500)

                    state.copy(
                        auctions = updatedAuctions,
                        homeAuctionCountdownSeconds = newCountdown,
                        liveViewers = newViewers
                    )
                }
            }
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setPostType(type: PostType) {
        _uiState.update { it.copy(postType = type) }
    }

    fun selectThumbnail(index: Int) {
        _uiState.update { it.copy(selectedThumbnailIndex = index) }
    }

    fun updateCaptionLine(lineIndex: Int, text: String) {
        _uiState.update {
            when (lineIndex) {
                1 -> it.copy(captionLine1 = text.take(60))
                2 -> it.copy(captionLine2 = text.take(60))
                3 -> it.copy(captionLine3 = text.take(60))
                4 -> it.copy(captionLine4 = text.take(60))
                else -> it
            }
        }
    }

    fun generateCaptionsWithAI() {
        val type = _uiState.value.postType
        if (type == PostType.UGLY_TO_BEAUTIFUL) {
            val suggestions = listOf(
                listOf("nem é magra", "na verdade eu não era loiro", "desculpa kkkkk", "desculpa kkkkk"),
                listOf("achei que era feio", "mas na verdade...", "esse sou eu no meu auge", "desculpaaaaa"),
                listOf("antes da academia", "fase do desleixo", "começou o glow up", "na verdade é foco kkkk")
            ).random()
            _uiState.update {
                it.copy(
                    captionLine1 = suggestions[0],
                    captionLine2 = suggestions[1],
                    captionLine3 = suggestions[2],
                    captionLine4 = suggestions[3],
                    notification = UiNotification("Legendas geradas com sucesso!")
                )
            }
        } else {
            val suggestions = listOf(
                listOf("achou que era o auge?", "na verdade era filtro", "desculpa aí", "voltei ao normal kkkk"),
                listOf("parecia modelo", "mas na verdade...", "barba mal feita total", "desculpa kkkkk")
            ).random()
            _uiState.update {
                it.copy(
                    captionLine1 = suggestions[0],
                    captionLine2 = suggestions[1],
                    captionLine3 = suggestions[2],
                    captionLine4 = suggestions[3],
                    notification = UiNotification("Legendas geradas para Bonito → Feio!")
                )
            }
        }
    }

    fun toggleTrait(id: String) {
        _uiState.update { state ->
            val updated = state.traits.map { trait ->
                if (trait.id == id) trait.copy(isEnabled = !trait.isEnabled) else trait
            }
            state.copy(traits = updated)
        }
    }

    fun removeTrait(id: String) {
        _uiState.update { state ->
            state.copy(traits = state.traits.filter { it.id != id })
        }
    }

    fun addCustomTrait(name: String, isBeauty: Boolean) {
        if (name.isBlank()) return
        val newTrait = FeatureTrait(
            id = "custom_${System.currentTimeMillis()}",
            name = name.trim().lowercase(),
            isBeauty = isBeauty,
            isEnabled = true,
            isCustom = true
        )
        _uiState.update {
            it.copy(
                traits = it.traits + newTrait,
                notification = UiNotification("Traço '$name' adicionado!")
            )
        }
    }

    fun generateWithAI() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationProgress = 0.1f) }

            delay(600)
            _uiState.update { it.copy(generationProgress = 0.45f) }
            delay(600)
            _uiState.update { it.copy(generationProgress = 0.85f) }
            delay(400)

            val currentState = _uiState.value
            val result = GenerationResult(
                images = listOf(
                    R.drawable.img_result_1,
                    R.drawable.img_result_2,
                    R.drawable.img_result_3,
                    R.drawable.img_result_4
                ),
                captions = listOf(
                    currentState.captionLine1.ifBlank { "nem é magra" },
                    currentState.captionLine2.ifBlank { "na verdade eu não era loiro" },
                    currentState.captionLine3.ifBlank { "desculpa kkkkk" },
                    currentState.captionLine4.ifBlank { "desculpa kkkkk" }
                ),
                tiktokTitle = if (currentState.postType == PostType.UGLY_TO_BEAUTIFUL) {
                    "POV: na verdade eu não era loiro... kkkkk #NaVerdade #TrendViral #fyp"
                } else {
                    "POV: Bonito pra Feio na verdade kkkkk #Trend #NaVerdade"
                },
                postType = currentState.postType
            )

            // Save post optionally to Supabase in background
            viewModelScope.launch {
                SupabaseClient.createPost(
                    author = currentState.userEmail ?: "Anônimo",
                    caption = result.captions.joinToString(" | "),
                    tiktokTitle = result.tiktokTitle,
                    stepCaptions = result.captions
                )
            }

            _uiState.update {
                it.copy(
                    isGenerating = false,
                    generationProgress = 1f,
                    latestResult = result,
                    currentScreen = ScreenDestination.RESULTS,
                    postsCreatedCount = it.postsCreatedCount + 1,
                    notification = UiNotification("Post gerado com sucesso!")
                )
            }
        }
    }

    fun submitBid(auctionId: String, email: String, amount: Double) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(notification = UiNotification("Informe um e-mail válido!", isError = true)) }
            return
        }

        viewModelScope.launch {
            SupabaseClient.submitBid(auctionId, email, amount)
        }

        _uiState.update { state ->
            val updatedAuctions = state.auctions.map { auction ->
                if (auction.id == auctionId) {
                    auction.copy(
                        currentBid = amount,
                        highestBidder = email.substringBefore("@"),
                        totalBids = auction.totalBids + 1
                    )
                } else auction
            }

            val newBid = BidSubmission(
                auctionId = auctionId,
                email = email,
                amount = amount
            )

            val updatedHomeBid = if (auctionId == "banner_home") amount else state.homeAuctionHighestBid
            val updatedHomeBidsCount = if (auctionId == "banner_home") state.homeAuctionTotalBids + 1 else state.homeAuctionTotalBids

            state.copy(
                auctions = updatedAuctions,
                homeAuctionHighestBid = updatedHomeBid,
                homeAuctionTotalBids = updatedHomeBidsCount,
                userBids = state.userBids + newBid,
                notification = UiNotification("Lance de R$ ${String.format("%.2f", amount)} confirmado!")
            )
        }
    }

    fun subscribeNewsletter(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(notification = UiNotification("Informe um e-mail válido!", isError = true)) }
            return
        }
        viewModelScope.launch {
            SupabaseClient.subscribeEmail(email)
        }
        _uiState.update {
            it.copy(
                newsletterEmail = "",
                notification = UiNotification("Inscrição confirmada! Você receberá as trends.")
            )
        }
    }

    fun saveAutoPostEmail(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(notification = UiNotification("Informe um e-mail válido!", isError = true)) }
            return
        }
        viewModelScope.launch {
            SupabaseClient.subscribeEmail(email)
        }
        _uiState.update {
            it.copy(
                autoPostEmail = email,
                notification = UiNotification("Email salvo! Avisaremos quando o autopost ativar.")
            )
        }
    }

    fun publishToFeed() {
        val result = _uiState.value.latestResult ?: return
        val newPost = TrendPost(
            id = "post_${System.currentTimeMillis()}",
            authorName = _uiState.value.userEmail?.substringBefore("@") ?: "voce.criador",
            authorHandle = "@" + (_uiState.value.userEmail?.substringBefore("@") ?: "voce"),
            authorAvatarRes = R.drawable.img_creator_main,
            isVerified = true,
            location = "Brasil",
            caption = result.tiktokTitle,
            images = result.images,
            stepCaptions = result.captions,
            likesCount = 1,
            isLiked = true,
            commentsCount = 0,
            timeAgo = "Agora"
        )
        _uiState.update {
            it.copy(
                feedPosts = listOf(newPost) + it.feedPosts,
                userCreatedPosts = listOf(newPost) + it.userCreatedPosts,
                currentScreen = ScreenDestination.FEED,
                notification = UiNotification("Publicado no Feed com sucesso!")
            )
        }
    }

    fun toggleLike(postId: String) {
        _uiState.update { state ->
            val updated = state.feedPosts.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLiked
                    val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                    post.copy(isLiked = newIsLiked, likesCount = newCount)
                } else post
            }
            state.copy(feedPosts = updated)
        }
    }

    fun loginOrRegister(email: String, pass: String) {
        if (email.isBlank() || pass.length < 6) {
            _uiState.update { it.copy(notification = UiNotification("Preencha email e senha (mínimo 6 dígitos)", isError = true)) }
            return
        }
        viewModelScope.launch {
            val res = SupabaseClient.authSignUp(email, pass)
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    userEmail = email,
                    notification = UiNotification(res.second)
                )
            }
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userEmail = null,
                notification = UiNotification("Sessão encerrada.")
            )
        }
    }

    fun dismissNotification() {
        _uiState.update { it.copy(notification = null) }
    }
}
