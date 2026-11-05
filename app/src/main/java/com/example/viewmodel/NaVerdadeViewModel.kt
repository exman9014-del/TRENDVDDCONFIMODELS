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
import com.example.model.PostComment
import com.example.model.PostType
import com.example.model.TraitSuggestion
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
    val feedPosts: List<TrendPost> = emptyList(),
    val userCreatedPosts: List<TrendPost> = emptyList(),
    val userBids: List<BidSubmission> = emptyList(),

    // Post Creation State
    val postType: PostType = PostType.UGLY_TO_BEAUTIFUL,
    val imagesCount: Int = 4, // 4 or more containers
    val selectedThumbnailIndex: Int = 0,
    val userPhotoIndex: Int = 3, // User photo is in the LAST container
    val uploadedPhotoUri: String? = null,
    val uploadedPhotoUrl: String? = null,
    val isUploadingPhoto: Boolean = false,

    // Captions per container (redline)
    val captionsList: List<String> = listOf(
        "nem é magra",
        "na verdade eu não era loiro",
        "desculpa kkkkk",
        "desculpa kkkkk"
    ),

    // Traços por imagem (não mais global)
    // Map of containerIndex -> List of traits
    val imageTraitsMap: Map<Int, List<FeatureTrait>> = emptyMap(),

    // Trait suggestions from other users
    val traitSuggestions: List<TraitSuggestion> = emptyList(),

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

    // Comments & Social interaction
    val selectedPostForComments: TrendPost? = null,
    val commentsForSelectedPost: List<PostComment> = emptyList(),
    val isLoadingComments: Boolean = false,

    // Realtime connection indicator
    val isRealtimeConnected: Boolean = false,

    // Auth & Profile
    val userEmail: String? = null,
    val isLoggedIn: Boolean = false,

    // Feedback
    val notification: UiNotification? = null
) {
    // Current traits for selected thumbnail
    val currentImageTraits: List<FeatureTrait>
        get() = imageTraitsMap[selectedThumbnailIndex] ?: emptyList()
}

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
        val baseTraits = repository.getInitialTraits()
        val initialFeed = repository.getInitialFeedPosts()
        val initialSuggestions = repository.getInitialTraitSuggestions()

        // Initialize traits per image for the 4 initial containers
        val initialImageTraitsMap = mutableMapOf<Int, List<FeatureTrait>>()
        for (i in 0 until 4) {
            // Deep copy traits for each container so toggling is independent
            initialImageTraitsMap[i] = baseTraits.map { it.copy() }
        }

        _uiState.update {
            it.copy(
                auctions = initialAuctions,
                trendCards = initialTrends,
                imageTraitsMap = initialImageTraitsMap,
                traitSuggestions = initialSuggestions,
                feedPosts = initialFeed,
                imagesCount = 4,
                userPhotoIndex = 3, // Last container
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

        // 4. Start live tickers & periodic polling
        startLiveTickers()
    }

    private fun fetchDataFromSupabase() {
        viewModelScope.launch {
            // Fetch posts
            val remotePosts = SupabaseClient.fetchPosts()
            if (remotePosts.isNotEmpty()) {
                _uiState.update { state ->
                    val combined = remotePosts + state.feedPosts.filter { defaultPost ->
                        remotePosts.none { it.id == defaultPost.id }
                    }
                    state.copy(feedPosts = combined)
                }
            }

            // Fetch custom traits suggestions from other users
            val remoteSuggestions = SupabaseClient.fetchCustomTraitSuggestions()
            if (remoteSuggestions.isNotEmpty()) {
                _uiState.update { state ->
                    val merged = remoteSuggestions + state.traitSuggestions.filter { local ->
                        remoteSuggestions.none { it.name.equals(local.name, ignoreCase = true) }
                    }
                    state.copy(traitSuggestions = merged)
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

    // 1. ADD IMAGE CONTAINER (+1)
    fun addImageContainer() {
        _uiState.update { state ->
            val newCount = state.imagesCount + 1
            val newIndex = newCount - 1
            val newCaptions = state.captionsList + "desculpa kkkkk ($newCount)"

            // Copy traits for the new container
            val currentMap = state.imageTraitsMap.toMutableMap()
            val templateTraits = currentMap[0] ?: repository.getInitialTraits()
            currentMap[newIndex] = templateTraits.map { it.copy() }

            // In both Feio->Bonito and Bonito->Feio, the user photo goes in the LAST container!
            val newUserPhotoIndex = newIndex

            state.copy(
                imagesCount = newCount,
                userPhotoIndex = newUserPhotoIndex,
                captionsList = newCaptions,
                imageTraitsMap = currentMap,
                selectedThumbnailIndex = newIndex,
                notification = UiNotification("Container $newCount adicionado! Linha de legenda criada.")
            )
        }
    }

    fun removeImageContainer(index: Int) {
        _uiState.update { state ->
            if (state.imagesCount <= 4) {
                return@update state.copy(notification = UiNotification("Mínimo de 4 imagens para a trend!", isError = true))
            }
            val newCount = state.imagesCount - 1
            val newCaptions = state.captionsList.toMutableList().apply {
                if (index < size) removeAt(index)
            }

            val newMap = mutableMapOf<Int, List<FeatureTrait>>()
            var k = 0
            for (i in 0 until state.imagesCount) {
                if (i != index) {
                    newMap[k] = state.imageTraitsMap[i] ?: repository.getInitialTraits()
                    k++
                }
            }

            val newUserPhotoIndex = newCount - 1
            val newSelected = (state.selectedThumbnailIndex).coerceAtMost(newCount - 1)

            state.copy(
                imagesCount = newCount,
                userPhotoIndex = newUserPhotoIndex,
                captionsList = newCaptions,
                imageTraitsMap = newMap,
                selectedThumbnailIndex = newSelected,
                notification = UiNotification("Container removido.")
            )
        }
    }

    // 2. CAPTIONS PER CONTAINER (REDLINE)
    fun updateCaptionLine(lineIndex: Int, text: String) {
        _uiState.update { state ->
            val list = state.captionsList.toMutableList()
            if (lineIndex in list.indices) {
                list[lineIndex] = text.take(60)
            } else if (lineIndex == list.size) {
                list.add(text.take(60))
            }
            state.copy(captionsList = list)
        }
    }

    // 3. TRAITS PER IMAGE CONTAINER
    fun toggleTraitForImage(imageIndex: Int, traitId: String) {
        _uiState.update { state ->
            val map = state.imageTraitsMap.toMutableMap()
            val currentTraits = map[imageIndex] ?: repository.getInitialTraits()
            val updatedTraits = currentTraits.map { trait ->
                if (trait.id == traitId) trait.copy(isEnabled = !trait.isEnabled) else trait
            }
            map[imageIndex] = updatedTraits
            state.copy(imageTraitsMap = map)
        }
    }

    fun removeTraitForImage(imageIndex: Int, traitId: String) {
        _uiState.update { state ->
            val map = state.imageTraitsMap.toMutableMap()
            val currentTraits = map[imageIndex] ?: repository.getInitialTraits()
            map[imageIndex] = currentTraits.filter { it.id != traitId }
            state.copy(imageTraitsMap = map)
        }
    }

    // 4. ADD FROM SUGGESTION OR CUSTOM TRAIT
    fun addTraitFromSuggestion(suggestion: TraitSuggestion) {
        val selectedIndex = _uiState.value.selectedThumbnailIndex
        val newTrait = FeatureTrait(
            id = "sug_${System.currentTimeMillis()}",
            name = suggestion.name,
            isBeauty = suggestion.isBeauty,
            isEnabled = true,
            isCustom = true,
            creatorName = suggestion.creatorName,
            creatorAvatarUrl = suggestion.creatorAvatarUrl
        )

        _uiState.update { state ->
            val map = state.imageTraitsMap.toMutableMap()
            val currentTraits = (map[selectedIndex] ?: repository.getInitialTraits()).toMutableList()
            if (currentTraits.none { it.name.equals(suggestion.name, ignoreCase = true) }) {
                currentTraits.add(newTrait)
            } else {
                // If already present, enable it for this image
                currentTraits.replaceAll {
                    if (it.name.equals(suggestion.name, ignoreCase = true)) it.copy(isEnabled = true) else it
                }
            }
            map[selectedIndex] = currentTraits
            state.copy(
                imageTraitsMap = map,
                notification = UiNotification("Traço '${suggestion.name}' de ${suggestion.creatorName} adicionado à imagem ${selectedIndex + 1}!")
            )
        }
    }

    fun addCustomTrait(name: String, isBeauty: Boolean, creatorName: String = "Você") {
        if (name.isBlank()) return
        val cleanName = name.trim().lowercase()
        val selectedIndex = _uiState.value.selectedThumbnailIndex

        val newTrait = FeatureTrait(
            id = "custom_${System.currentTimeMillis()}",
            name = cleanName,
            isBeauty = isBeauty,
            isEnabled = true,
            isCustom = true,
            creatorName = creatorName
        )

        val newSuggestion = TraitSuggestion(
            id = "sug_${System.currentTimeMillis()}",
            name = cleanName,
            isBeauty = isBeauty,
            creatorName = creatorName,
            creatorAvatarRes = R.drawable.img_creator_main,
            usageCount = 1
        )

        // 1. Update local state
        _uiState.update { state ->
            val map = state.imageTraitsMap.toMutableMap()
            val currentTraits = (map[selectedIndex] ?: repository.getInitialTraits()).toMutableList()
            currentTraits.add(newTrait)
            map[selectedIndex] = currentTraits

            val updatedSuggestions = listOf(newSuggestion) + state.traitSuggestions

            state.copy(
                imageTraitsMap = map,
                traitSuggestions = updatedSuggestions,
                notification = UiNotification("Traço '$cleanName' adicionado à foto ${selectedIndex + 1} e compartilhado!")
            )
        }

        // 2. Persist to Supabase custom_traits table so others see it as suggestion!
        viewModelScope.launch {
            SupabaseClient.persistCustomTrait(
                name = cleanName,
                isBeauty = isBeauty,
                creatorName = creatorName,
                creatorAvatar = ""
            )
        }
    }

    // AI Generation with Gemini
    fun generateCaptionsWithAI() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiGeneratingCaptions = true) }
            val type = _uiState.value.postType
            val selectedIndex = _uiState.value.selectedThumbnailIndex
            val traits = _uiState.value.imageTraitsMap[selectedIndex] ?: repository.getInitialTraits()
            val linesCount = _uiState.value.imagesCount

            val geminiResult = GeminiAiService.generateTrendCaptions(type, traits, linesCount)

            _uiState.update {
                it.copy(
                    isAiGeneratingCaptions = false,
                    isRealAiPowered = geminiResult.isRealAi,
                    captionsList = geminiResult.captionLines,
                    tiktokTitle = geminiResult.tiktokTitle,
                    notification = UiNotification(
                        if (geminiResult.isRealAi) "✨ $linesCount legendas geradas pelo Gemini com sucesso!"
                        else "✨ $linesCount legendas geradas para a trend!"
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
                            notification = UiNotification("Sua foto foi enviada para o último container com sucesso!")
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadedPhotoUrl = uri.toString(),
                            notification = UiNotification("Foto definida no último container!")
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
                captionsList = listOf(trend.title, "na verdade...", "desculpa aí", "desculpa kkkkk"),
                currentScreen = ScreenDestination.CREATE_POST,
                notification = UiNotification("Trend '${trend.title}' carregada!")
            )
        }
    }

    fun generateWithAI(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationProgress = 0.15f) }

            val currentState = _uiState.value
            val totalImages = currentState.imagesCount

            // 1. Call Gemini to generate customized viral captions and TikTok title
            val geminiResult = GeminiAiService.generateTrendCaptions(
                currentState.postType,
                currentState.currentImageTraits,
                linesCount = totalImages
            )
            _uiState.update { it.copy(generationProgress = 0.35f) }

            // 2. Call Imagen 3 / Gemini to generate trend transformation images with per-container traits
            val generatedAiImages = GeminiAiService.generateTrendImagesPerContainer(
                context = context,
                postType = currentState.postType,
                perContainerTraits = currentState.imageTraitsMap,
                totalCount = totalImages
            )
            _uiState.update { it.copy(generationProgress = 0.85f) }

            val captions = (0 until totalImages).map { i ->
                currentState.captionsList.getOrNull(i)?.takeIf { it.isNotBlank() }
                    ?: geminiResult.captionLines.getOrElse(i) { "desculpa kkkkk" }
            }

            val defaultImagesList = listOf(
                R.drawable.img_result_1,
                R.drawable.img_result_2,
                R.drawable.img_result_3,
                R.drawable.img_result_4
            )

            val finalImageUrls = if (generatedAiImages.isNotEmpty()) {
                generatedAiImages
            } else if (currentState.uploadedPhotoUrl != null) {
                listOf(currentState.uploadedPhotoUrl)
            } else emptyList()

            val result = GenerationResult(
                images = defaultImagesList,
                imageUrls = finalImageUrls,
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
                    imageUrls = finalImageUrls
                )
            }

            val successMsg = if (generatedAiImages.isNotEmpty()) {
                "✨ $totalImages imagens geradas com IA (Imagen 3)!"
            } else if (geminiResult.isRealAi) {
                "✨ Legendas geradas pelo Gemini!"
            } else {
                "Post da trend gerado com sucesso!"
            }

            _uiState.update {
                it.copy(
                    isGenerating = false,
                    generationProgress = 1f,
                    latestResult = result,
                    currentScreen = ScreenDestination.RESULTS,
                    postsCreatedCount = it.postsCreatedCount + 1,
                    notification = UiNotification(successMsg)
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
            isFavorited = false,
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

    // 5. SOCIAL PERSISTENCE (LIKES, FAVORITES, COMMENTS)
    fun toggleLike(postId: String) {
        var isNowLiked = false
        var newLikesCount = 0

        _uiState.update { state ->
            val updated = state.feedPosts.map { post ->
                if (post.id == postId) {
                    val newIsLiked = !post.isLiked
                    isNowLiked = newIsLiked
                    val newCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                    newLikesCount = newCount
                    post.copy(isLiked = newIsLiked, likesCount = newCount)
                } else post
            }
            state.copy(feedPosts = updated)
        }

        // Persist to Supabase
        viewModelScope.launch {
            val email = _uiState.value.userEmail ?: "anonimo@naverdade.app"
            SupabaseClient.persistLike(postId, email, isNowLiked, newLikesCount)
        }
    }

    fun toggleFavorite(postId: String) {
        var isNowFavorited = false

        _uiState.update { state ->
            val updated = state.feedPosts.map { post ->
                if (post.id == postId) {
                    val newFav = !post.isFavorited
                    isNowFavorited = newFav
                    post.copy(isFavorited = newFav)
                } else post
            }
            state.copy(
                feedPosts = updated,
                notification = UiNotification(if (isNowFavorited) "Salvo nos seus favoritos!" else "Removido dos favoritos.")
            )
        }

        // Persist to Supabase
        viewModelScope.launch {
            val email = _uiState.value.userEmail ?: "anonimo@naverdade.app"
            SupabaseClient.persistFavorite(postId, email, isNowFavorited)
        }
    }

    fun openComments(post: TrendPost) {
        _uiState.update { it.copy(selectedPostForComments = post, isLoadingComments = true) }
        viewModelScope.launch {
            val comments = SupabaseClient.fetchComments(post.id)
            _uiState.update {
                it.copy(
                    commentsForSelectedPost = comments.ifEmpty {
                        listOf(
                            PostComment(
                                id = "c1",
                                postId = post.id,
                                authorName = "pedro.builds",
                                content = "Essa trend ficou muito boa kkkkkk",
                                timeAgo = "10m atrás"
                            ),
                            PostComment(
                                id = "c2",
                                postId = post.id,
                                authorName = "larissa.viaja",
                                content = "Gerei o meu ontem, o resultado me chocou!",
                                timeAgo = "5m atrás"
                            )
                        )
                    },
                    isLoadingComments = false
                )
            }
        }
    }

    fun closeComments() {
        _uiState.update { it.copy(selectedPostForComments = null, commentsForSelectedPost = emptyList()) }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val author = _uiState.value.userEmail?.substringBefore("@") ?: "Você"

        val newComment = PostComment(
            id = "c_${System.currentTimeMillis()}",
            postId = postId,
            authorName = author,
            content = text.trim(),
            timeAgo = "Agora"
        )

        _uiState.update { state ->
            val updatedComments = state.commentsForSelectedPost + newComment
            val updatedPosts = state.feedPosts.map { post ->
                if (post.id == postId) post.copy(commentsCount = post.commentsCount + 1) else post
            }
            state.copy(
                commentsForSelectedPost = updatedComments,
                feedPosts = updatedPosts,
                notification = UiNotification("Comentário salvo no Supabase!")
            )
        }

        viewModelScope.launch {
            SupabaseClient.addComment(postId, author, text.trim())
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
