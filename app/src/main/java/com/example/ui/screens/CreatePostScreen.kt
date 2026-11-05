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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.components.AddTraitDialog
import com.example.ui.components.BidDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralRed
import com.example.ui.theme.MintBorder
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NaVerdadeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    state: NaVerdadeUiState,
    onBackClick: () -> Unit,
    onSetPostType: (PostType) -> Unit,
    onSelectThumbnail: (Int) -> Unit,
    onUpdateCaption: (Int, String) -> Unit,
    onGenerateCaptions: () -> Unit,
    onToggleTrait: (String) -> Unit,
    onRemoveTrait: (String) -> Unit,
    onAddTrait: (String, Boolean) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onGenerateWithAI: () -> Unit,
    onSubmitBid: (auctionId: String, email: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showAddTraitDialog by remember { mutableStateOf(false) }
    var showBidDialog by remember { mutableStateOf(false) }

    // Android Photo Picker (zero-permission storage API)
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

    val previewThumbnails = listOf(
        R.drawable.img_creator_main,
        R.drawable.img_result_1,
        R.drawable.img_result_2,
        R.drawable.img_result_3
    )

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky Bottom CTA: "✨ Gerar com IA"
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
                            Text(
                                text = "Gerando 4 imagens com IA...",
                                fontSize = 15.sp,
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
                                text = "Gerar com IA",
                                fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(18.dp))

            // 2. MAIN PREVIEW PHOTO + UPLOAD TO SUPABASE STORAGE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3F4F6))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                if (state.uploadedPhotoUrl != null && state.selectedThumbnailIndex == 0) {
                    Image(
                        painter = rememberAsyncImagePainter(model = state.uploadedPhotoUrl),
                        contentDescription = "Foto Carregada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val currentImageRes = previewThumbnails.getOrElse(state.selectedThumbnailIndex) {
                        R.drawable.img_creator_main
                    }
                    Image(
                        painter = painterResource(id = currentImageRes),
                        contentDescription = "Foto Principal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
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
                        contentDescription = "Trocar Foto",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sua Foto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Storage upload indicator
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

                // Current caption badge over preview
                val currentCaption = when (state.selectedThumbnailIndex) {
                    0 -> state.captionLine1
                    1 -> state.captionLine2
                    2 -> state.captionLine3
                    3 -> state.captionLine4
                    else -> ""
                }
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

            Spacer(modifier = Modifier.height(10.dp))

            // Upload Photo Action Button
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_upload_photo_gallery")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.uploadedPhotoUrl != null) "Trocar Foto da Galeria (Supabase Storage)"
                    else "Enviar Foto da Galeria (Supabase Storage)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 4 THUMBNAILS CAROUSEL (1, 2, 3, 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                previewThumbnails.forEachIndexed { index, imgRes ->
                    val isSelected = state.selectedThumbnailIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelectThumbnail(index) }
                            .testTag("thumb_step_${index + 1}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryGreen else Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryGreen else CardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            if (index == 0 && state.uploadedPhotoUrl != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = state.uploadedPhotoUrl),
                                    contentDescription = "Thumb ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = imgRes),
                                    contentDescription = "Thumb ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Avançar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. SELETOR DE TRAÇOS (+ Verde / + Vermelho)
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
                                text = "Seletor de traços",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Toque para adicionar ou remover",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
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

                    // Beleza List
                    Text(
                        text = "Beleza",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    state.traits.filter { it.isBeauty }.forEach { trait ->
                        TraitRowItem(
                            trait = trait,
                            onToggle = { onToggleTrait(trait.id) },
                            onRemove = { onRemoveTrait(trait.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feiura List
                    Text(
                        text = "Feiura",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    state.traits.filter { !it.isBeauty }.forEach { trait ->
                        TraitRowItem(
                            trait = trait,
                            onToggle = { onToggleTrait(trait.id) },
                            onRemove = { onRemoveTrait(trait.id) }
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
                            text = "+ Criar traço personalizado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. LEGENDA DO POST COM BOTÃO IA GEMINI
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
                                text = "Gere com IA Gemini ou digite",
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

                    CaptionInputField(
                        label = "Linha 1",
                        value = state.captionLine1,
                        onValueChange = { onUpdateCaption(1, it) },
                        testTag = "input_caption_1"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CaptionInputField(
                        label = "Linha 2",
                        value = state.captionLine2,
                        onValueChange = { onUpdateCaption(2, it) },
                        testTag = "input_caption_2"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CaptionInputField(
                        label = "Linha 3",
                        value = state.captionLine3,
                        onValueChange = { onUpdateCaption(3, it) },
                        testTag = "input_caption_3"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CaptionInputField(
                        label = "Linha 4",
                        value = state.captionLine4,
                        onValueChange = { onUpdateCaption(4, it) },
                        testTag = "input_caption_4"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. BANNER PEQUENO DE LEILÃO
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
            onAddTrait = onAddTrait
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (trait.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = if (trait.isBeauty) PrimaryGreen else CoralRed,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trait.name,
                fontSize = 12.sp,
                color = if (trait.isEnabled) TextPrimary else TextMuted
            )
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
            modifier = Modifier.width(50.dp)
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
