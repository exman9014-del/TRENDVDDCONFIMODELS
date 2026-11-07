package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.model.AuctionItem
import com.example.model.FeatureTrait
import com.example.model.PostType
import com.example.model.TraitSuggestion
import com.example.ui.components.AddTraitDialog
import com.example.ui.components.AllSuggestionsDialog
import com.example.ui.components.BidDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralRed
import com.example.ui.theme.MintBorder
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NaVerdadeUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    state: NaVerdadeUiState,
    onBackClick: () -> Unit,
    onSetPostType: (PostType) -> Unit,
    onSelectThumbnail: (Int) -> Unit,
    onAddImageContainer: () -> Unit,
    onRemoveImageContainer: (Int) -> Unit,
    onUpdateCaption: (Int, String) -> Unit,
    onGenerateCaptions: () -> Unit,
    onToggleTraitForImage: (imageIndex: Int, traitId: String) -> Unit,
    onRemoveTraitForImage: (imageIndex: Int, traitId: String) -> Unit,
    onAddCustomTrait: (name: String, isBeauty: Boolean) -> Unit,
    onAddTraitFromSuggestion: (TraitSuggestion) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onGenerateWithAI: () -> Unit,
    onSubmitBid: (auctionId: String, email: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAddTraitDialog by remember { mutableStateOf(false) }
    var showAllSuggestionsDialog by remember { mutableStateOf(false) }
    var showBidDialog by remember { mutableStateOf(false) }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadPhoto(uri)
        }
    }

    val createAuction = state.auctions.find { it.id == "banner_create" }
        ?: AuctionItem(
            id = "banner_create",
            title = "Banner Criar Post",
            description = "Durante a criação do post.",
            currentBid = 15.0,
            remainingSeconds = 3600
        )

    val defaultThumbnails = listOf(
        R.drawable.img_creator_main,
        R.drawable.img_result_1,
        R.drawable.img_result_2,
        R.drawable.img_result_3,
        R.drawable.img_result_4
    )

    // Current traits for selected image container
    val currentImageTraits = state.imageTraitsMap[state.selectedThumbnailIndex] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Criar Post",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("create_post_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    // Container counter badge
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${state.imagesCount} Fotos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky CTA: "✨ Gerar com IA"
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onGenerateWithAI,
                        enabled = !state.isGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_with_ai_button")
                    ) {
                        if (state.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val progressText = when {
                                state.generationProgress < 0.4f -> "Criando legendas com Gemini..."
                                state.generationProgress < 0.8f -> "Gerando ${state.imagesCount} imagens com IA..."
                                else -> "Finalizando post no Supabase..."
                            }
                            Text(
                                text = progressText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gerar com IA (${state.imagesCount} Imagens)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. TIPO DE POST SELECTOR
            Text(
                text = "Tipo de post",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Feio -> Bonito (Verde)
                val isUglyToBeauty = state.postType == PostType.UGLY_TO_BEAUTIFUL
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isUglyToBeauty) Color(0xFFF0FDF4) else Color.White)
                        .border(
                            width = if (isUglyToBeauty) 2.dp else 1.dp,
                            color = if (isUglyToBeauty) PrimaryGreen else CardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSetPostType(PostType.UGLY_TO_BEAUTIFUL) }
                        .padding(vertical = 12.dp)
                        .testTag("type_ugly_to_beauty"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Feio → Bonito",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUglyToBeauty) PrimaryGreen else TextSecondary
                    )
                }

                // Bonito -> Feio (Vermelho)
                val isBeautyToUgly = state.postType == PostType.BEAUTIFUL_TO_UGLY
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isBeautyToUgly) Color(0xFFFEE2E2) else Color.White)
                        .border(
                            width = if (isBeautyToUgly) 2.dp else 1.dp,
                            color = if (isBeautyToUgly) CoralRed else CardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSetPostType(PostType.BEAUTIFUL_TO_UGLY) }
                        .padding(vertical = 12.dp)
                        .testTag("type_beauty_to_ugly"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bonito → Feio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBeautyToUgly) CoralRed else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. SUGESTÕES DE TRAÇOS DE OUTROS USUÁRIOS (TICKER / MARQUEE)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0FDF4))
                    .border(1.dp, MintBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sugestões de outros usuários",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showAllSuggestionsDialog = true }
                            .padding(4.dp)
                            .testTag("btn_see_all_suggestions")
                    ) {
                        Text(
                            text = "Ver todas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Ver todas",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal ticker of trait suggestions
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.traitSuggestions) { suggestion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, if (suggestion.isBeauty) Color(0xFFBBF7D0) else Color(0xFFFECACA), RoundedCornerShape(20.dp))
                                .clickable { onAddTraitFromSuggestion(suggestion) }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("sug_chip_${suggestion.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            ) {
                                if (!suggestion.creatorAvatarUrl.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = suggestion.creatorAvatarUrl),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = suggestion.creatorAvatarRes ?: R.drawable.img_creator_main),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${suggestion.name} por ${suggestion.creatorName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar",
                                tint = if (suggestion.isBeauty) PrimaryGreen else CoralRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. MAIN PREVIEW PHOTO
            val isUserPhotoSlot = state.selectedThumbnailIndex == state.userPhotoIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3F4F6))
                    .border(
                        width = if (isUserPhotoSlot) 2.dp else 1.dp,
                        color = if (isUserPhotoSlot) PrimaryGreen else CardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                if (isUserPhotoSlot && state.uploadedPhotoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = state.uploadedPhotoUrl),
                        contentDescription = "Sua Foto Carregada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val currentImageRes = defaultThumbnails.getOrElse(state.selectedThumbnailIndex) {
                        R.drawable.img_creator_main
                    }
                    Image(
                        painter = painterResource(id = currentImageRes),
                        contentDescription = "Foto Principal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // If this is the user photo slot, show prominent badge
                if (isUserPhotoSlot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryGreen)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FOTO DO USUÁRIO (ÚLTIMO)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Upload button on top right of preview
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Enviar Foto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.uploadedPhotoUrl != null) "Trocar Foto" else "Sua Foto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Upload progress indicator
                if (state.isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enviando para Supabase Storage...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Current caption overlay badge over preview
                val currentCaption = state.captionsList.getOrElse(state.selectedThumbnailIndex) { "" }
                if (currentCaption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentCaption,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. CONTAINERS CAROUSEL (1, 2, 3, 4... + BOTÃO "+1")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.imagesCount) { index ->
                        val isSelected = state.selectedThumbnailIndex == index
                        val isUserSlot = index == state.userPhotoIndex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onSelectThumbnail(index) }
                                .testTag("thumb_step_${index + 1}")
                        ) {
                            // Step circle badge
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) PrimaryGreen
                                        else if (isUserSlot) Color(0xFFF59E0B)
                                        else Color(0xFFE5E7EB)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || isUserSlot) Color.White else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Thumbnail Box
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.5.dp else if (isUserSlot) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryGreen else if (isUserSlot) Color(0xFFF59E0B) else CardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                if (isUserSlot && state.uploadedPhotoUrl != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = state.uploadedPhotoUrl),
                                        contentDescription = "Sua foto",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val imgRes = defaultThumbnails.getOrElse(index) { R.drawable.img_creator_main }
                                    Image(
                                        painter = painterResource(id = imgRes),
                                        contentDescription = "Foto ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Visual highlight tag if this is the user's photo slot
                                if (isUserSlot) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.9f))
                                            .padding(vertical = 1.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "SUA FOTO",
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // BOTÃO "+1" (Adicionar container extra de imagem)
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onAddImageContainer() }
                                .testTag("btn_add_image_container")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .border(
                                        width = 1.5.dp,
                                        color = PrimaryGreen,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "+1",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryGreen
                                    )
                                    Text(
                                        text = "Foto",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. SELETOR DE TRAÇOS POR IMAGEM (NÃO MAIS GLOBAL)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Seletor de traços",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryGreen)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Foto ${state.selectedThumbnailIndex + 1} de ${state.imagesCount}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "Defina os traços individuais para esta foto",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Remove container option if > 4
                        if (state.imagesCount > 4) {
                            IconButton(
                                onClick = { onRemoveImageContainer(state.selectedThumbnailIndex) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir container",
                                    tint = CoralRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddTraitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_add_beauty_trait")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Adicionar beleza", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddTraitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_add_ugly_trait")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Adicionar feiura", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Beleza List for this specific image
                    Text(
                        text = "Beleza (Foto ${state.selectedThumbnailIndex + 1})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    currentImageTraits.filter { it.isBeauty }.forEach { trait ->
                        TraitRowItem(
                            trait = trait,
                            onToggle = { onToggleTraitForImage(state.selectedThumbnailIndex, trait.id) },
                            onRemove = { onRemoveTraitForImage(state.selectedThumbnailIndex, trait.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feiura List for this specific image
                    Text(
                        text = "Feiura (Foto ${state.selectedThumbnailIndex + 1})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    currentImageTraits.filter { !it.isBeauty }.forEach { trait ->
                        TraitRowItem(
                            trait = trait,
                            onToggle = { onToggleTraitForImage(state.selectedThumbnailIndex, trait.id) },
                            onRemove = { onRemoveTraitForImage(state.selectedThumbnailIndex, trait.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .clickable { showAddTraitDialog = true }
                            .padding(vertical = 10.dp)
                            .testTag("btn_custom_trait"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Criar traço personalizado (Compartilhar)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. LEGENDAS DO POST (REDLINE DINÂMICO PARA CADA CONTAINER)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Legenda do post",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Gere com IA Gemini ou digite para as ${state.imagesCount} fotos",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .clickable { onGenerateCaptions() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("btn_generate_captions")
                        ) {
                            if (state.isAiGeneratingCaptions) {
                                CircularProgressIndicator(
                                    color = PrimaryGreen,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.isAiGeneratingCaptions) "Gerando..." else "Gerar com IA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamically render inputs for all containers
                    state.captionsList.forEachIndexed { index, captionText ->
                        CaptionInputField(
                            label = "Linha ${index + 1}",
                            value = captionText,
                            onValueChange = { onUpdateCaption(index, it) },
                            testTag = "input_caption_${index + 1}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. BANNER PEQUENO DE LEILÃO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("create_post_auction_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Espaço disponível – Leilão ao vivo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Lance atual: ${createAuction.formattedBid}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Dar Lance",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier
                            .clickable { showBidDialog = true }
                            .padding(4.dp)
                            .testTag("create_post_dar_lance_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showAddTraitDialog) {
        AddTraitDialog(
            onDismiss = { showAddTraitDialog = false },
            onAddTrait = { name, isBeauty ->
                onAddCustomTrait(name, isBeauty)
                showAddTraitDialog = false
            }
        )
    }

    if (showAllSuggestionsDialog) {
        AllSuggestionsDialog(
            suggestions = state.traitSuggestions,
            onSelectSuggestion = { suggestion ->
                onAddTraitFromSuggestion(suggestion)
            },
            onDismiss = { showAllSuggestionsDialog = false }
        )
    }

    if (showBidDialog) {
        BidDialog(
            auction = createAuction,
            onDismiss = { showBidDialog = false },
            onSubmitBid = onSubmitBid
        )
    }
}

@Composable
fun TraitRowItem(
    trait: FeatureTrait,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (trait.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = if (trait.isBeauty) PrimaryGreen else CoralRed,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = trait.name,
                    fontSize = 12.sp,
                    fontWeight = if (trait.isEnabled) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (trait.isEnabled) TextPrimary else TextMuted
                )
                if (trait.creatorName != null) {
                    Text(
                        text = "por ${trait.creatorName}",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover",
                    tint = CoralRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CaptionInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.width(55.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Sua legenda aqui...", fontSize = 11.sp) },
            singleLine = true,
            trailingIcon = {
                Text(
                    text = "${value.length}/60",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(end = 6.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = CardBorder
            ),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag(testTag)
        )
    }
}
