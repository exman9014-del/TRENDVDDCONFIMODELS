package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.GeminiAiService
import com.example.data.NaVerdadeRepository
import com.example.data.SupabaseClient
import com.example.data.SupabaseRealtimeClient
import com.example.model.AuctionItem
import com.example.model.BidSubmission
import com.example.model.FeatureTrait
import com.example.model.GenerationResult
import com.example.model.PostType
import com.example.model.TrendCardItem
import com.example.model.TrendPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

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
    val uploadedPhotoUri: String? = null,
    val uploadedPhotoUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val captionLine1: String = "nem é magra",
    val captionLine2: String = "na verdade eu não era loiro",
    val captionLine3: String = "desculpa kkkkk",
    val captionLine4: String = "desculpa kkkkk",
    val isGenerating: Boolean = false,
    val isAiGeneratingCaptions: Boolean = false,
    val isRealAiPowered: Boolean = false,
    val generationProgress: Float = 0f,

    // Results State
    val latestResult: GenerationResult? = null,
    val tiktokTitle: String = "POV: Quando você acha que conhece alguém, mas na verdade... 😳 #NaVerdade #TrendViral #GlowUp",
    val autoPostEmail: String = "",

    // Newsletter State
    val newsletterEmail: String = "",

    // Realtime connection indicator
    val isRealtimeConnected: Boolean = false,

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

    private val realtimeClient = SupabaseRealtimeClient(viewModelScope)

    init {
        // 1. Load initial seed data
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

        // 2. Fetch remote data from Supabase
        fetchDataFromSupabase()

        // 3. Setup Supabase Realtime listeners
        setupRealtime()

        // 4. Start live timers & periodic polling
        startLiveTickers()
    }

    private fun fetchDataFromSupabase() {
        viewModelScope.launch {
            // Fetch posts
            val remotePosts = SupabaseClient.fetchPosts()
            if (remotePosts.isNotEmpty()) {
                _uiState.update { state ->
                    // Merge remote posts on top of default seed posts
                    val combined = remotePosts + state.feedPosts.filter { defaultPost ->
                        remotePosts.none { it.id == defaultPost.id }
                    }
                    state.copy(feedPosts = combined)
                }
            }

            // Fetch recent bids to update auctions
            val remoteBids = SupabaseClient.fetchRecentBids()
            if (remoteBids.isNotEmpty()) {
                _uiState.update { state ->
                    var updatedAuctions = state.auctions
                    remoteBids.forEach { (auctionId, amount) ->
                        updatedAuctions = updatedAuctions.map { auc ->
                            if (auc.id == auctionId && amount > auc.currentBid) {
                                auc.copy(currentBid = amount, totalBids = auc.totalBids + 1)
                            } else auc
                        }
                    }
                    val topHomeBid = updatedAuctions.find { it.id == "banner_home" }?.currentBid ?: state.homeAuctionHighestBid
                    state.copy(auctions = updatedAuctions, homeAuctionHighestBid = topHomeBid)
                }
            }
        }
    }

    private fun setupRealtime() {
        realtimeClient.onConnectionStateChanged = { connected ->
            _uiState.update { it.copy(isRealtimeConnected = connected) }
        }

        realtimeClient.onBidInserted = { auctionId, email, amount ->
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
                val newHomeBid = if (auctionId == "banner_home") amount else state.homeAuctionHighestBid
                val newHomeBidsCount = if (auctionId == "banner_home") state.homeAuctionTotalBids + 1 else state.homeAuctionTotalBids

                state.copy(
                    auctions = updatedAuctions,
                    homeAuctionHighestBid = newHomeBid,
                    homeAuctionTotalBids = newHomeBidsCount,
                    notification = UiNotification("Novo lance em tempo real: R$ ${String.format("%.2f", amount)}")
                )
            }
        }

        realtimeClient.onPostInserted = { newPost ->
            _uiState.update { state ->
                if (state.feedPosts.none { it.id == newPost.id }) {
                    state.copy(
                        feedPosts = listOf(newPost) + state.feedPosts,
                        postsCreatedCount = state.postsCreatedCount + 1,
                        notification = UiNotification("Novo post criado na trend!")
                    )
                } else state
            }
        }

        realtimeClient.connect()
    }

    private fun startLiveTickers() {
        viewModelScope.launch {
            var counter = 0
            while (isActive) {
                delay(1000)
                counter++

                // Every 15 seconds, poll Supabase in case WebSocket misses an event
                if (counter % 15 == 0) {
                    fetchDataFromSupabase()
                }

                _uiState.update { state ->
                    val updatedAuctions = state.auctions.map { auction ->
                        if (auction.remainingSeconds > 0) {
                            auction.copy(remainingSeconds = auction.remainingSeconds - 1)
                        } else {
                            auction.copy(remainingSeconds = 3600)
                        }
                    }

                    val newCountdown = if (state.homeAuctionCountdownSeconds > 0) {
                        state.homeAuctionCountdownSeconds - 1
                    } else 60

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

    // AI Generation with Gemini
    fun generateCaptionsWithAI() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGeneratingCaptions = true) }
            val type = _uiState.value.postType
            val traits = _uiState.value.traits

            val geminiResult = GeminiAiService.generateTrendCaptions(type, traits)

            _uiState.update {
                it.copy(
                    isAiGeneratingCaptions = false,
                    isRealAiPowered = geminiResult.isRealAi,
                    captionLine1 = geminiResult.captionLines.getOrElse(0) { "nem é magra" },
                    captionLine2 = geminiResult.captionLines.getOrElse(1) { "na verdade eu não era loiro" },
                    captionLine3 = geminiResult.captionLines.getOrElse(2) { "desculpa kkkkk" },
                    captionLine4 = geminiResult.captionLines.getOrElse(3) { "desculpa kkkkk" },
                    tiktokTitle = geminiResult.tiktokTitle,
                    notification = UiNotification(
                        if (geminiResult.isRealAi) "✨ Legendas geradas pelo Gemini com sucesso!"
                        else "✨ Legendas geradas para a trend!"
                    )
                )
            }
        }
    }

    // Upload user photo to Supabase Storage
    fun uploadUserPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, uploadedPhotoUri = uri.toString()) }
            try {
                val bytes = withContext(Dispatchers.IO) {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val out = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    out.toByteArray()
                }

                val fileName = "photo_${System.currentTimeMillis()}.jpg"
                val uploadedUrl = SupabaseClient.uploadImage(bytes, fileName)

                if (uploadedUrl != null) {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadedPhotoUrl = uploadedUrl,
                            notification = UiNotification("Foto enviada com sucesso para o Supabase Storage!")
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadedPhotoUrl = uri.toString(), // local fallback
                            notification = UiNotification("Foto pronta para criação de post!")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("NaVerdadeVM", "Error uploading photo: ${e.message}")
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        uploadedPhotoUrl = uri.toString(),
                        notification = UiNotification("Foto pronta!")
                    )
                }
            }
        }
    }

    fun selectTrendTemplate(trend: TrendCardItem) {
        _uiState.update {
            it.copy(
                captionLine1 = trend.title,
                captionLine2 = "na verdade...",
                captionLine3 = "desculpa aí",
                captionLine4 = "desculpa kkkkk",
                currentScreen = ScreenDestination.CREATE_POST,
                notification = UiNotification("Trend '${trend.title}' carregada!")
            )
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
            _uiState.update { it.copy(isGenerating = true, generationProgress = 0.15f) }

            // Call Gemini to refine title and captions
            val geminiResult = GeminiAiService.generateTrendCaptions(
                _uiState.value.postType,
                _uiState.value.traits
            )

            delay(400)
            _uiState.update { it.copy(generationProgress = 0.55f) }
            delay(400)
            _uiState.update { it.copy(generationProgress = 0.9f) }
            delay(200)

            val currentState = _uiState.value
            val captions = listOf(
                currentState.captionLine1.ifBlank { geminiResult.captionLines[0] },
                currentState.captionLine2.ifBlank { geminiResult.captionLines[1] },
                currentState.captionLine3.ifBlank { geminiResult.captionLines[2] },
                currentState.captionLine4.ifBlank { geminiResult.captionLines[3] }
            )

            val imagesList = listOf(
                R.drawable.img_result_1,
                R.drawable.img_result_2,
                R.drawable.img_result_3,
                R.drawable.img_result_4
            )

            val imageUrls = if (currentState.uploadedPhotoUrl != null) {
                listOf(currentState.uploadedPhotoUrl)
            } else emptyList()

            val result = GenerationResult(
                images = imagesList,
                imageUrls = imageUrls,
                captions = captions,
                tiktokTitle = currentState.tiktokTitle.ifBlank { geminiResult.tiktokTitle },
                postType = currentState.postType
            )

            // Save to Supabase PostgREST
            viewModelScope.launch {
                SupabaseClient.createPost(
                    author = currentState.userEmail ?: "criador.naverdade",
                    caption = result.captions.joinToString(" | "),
                    tiktokTitle = result.tiktokTitle,
                    stepCaptions = result.captions,
                    imageUrls = imageUrls
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
                notification = UiNotification("Lance de R$ ${String.format("%.2f", amount)} enviado ao Supabase!")
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
                notification = UiNotification("Inscrição confirmada no Supabase!")
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
                notification = UiNotification("Email salvo no Supabase!")
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
            imageUrls = result.imageUrls,
            stepCaptions = result.captions,
            likesCount = 1,
            isLiked = true,
            commentsCount = 0,
            timeAgo = "Agora"
        )

        // Save to Supabase
        viewModelScope.launch {
            SupabaseClient.createPost(
                author = newPost.authorName,
                caption = newPost.caption,
                tiktokTitle = result.tiktokTitle,
                stepCaptions = result.captions,
                imageUrls = result.imageUrls
            )
        }

        _uiState.update {
            it.copy(
                feedPosts = listOf(newPost) + it.feedPosts,
                userCreatedPosts = listOf(newPost) + it.userCreatedPosts,
                currentScreen = ScreenDestination.FEED,
                notification = UiNotification("Publicado no Feed do Supabase!")
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

    override fun onCleared() {
        super.onCleared()
        realtimeClient.disconnect()
    }
}
