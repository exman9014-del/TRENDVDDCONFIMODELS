package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.AuctionScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.theme.CoralRed
import com.example.ui.theme.NaVerdadeTheme
import com.example.ui.theme.PrimaryGreenDark
import com.example.viewmodel.NaVerdadeViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NaVerdadeTheme {
                NaVerdadeApp()
            }
        }
    }
}

@Composable
fun NaVerdadeApp(
    viewModel: NaVerdadeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle Back Press navigation
    BackHandler(enabled = state.currentScreen != ScreenDestination.LANDING) {
        viewModel.navigateTo(ScreenDestination.LANDING)
    }

    // Dismiss notifications after delay
    LaunchedEffect(state.notification) {
        if (state.notification != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                currentScreen = state.currentScreen,
                onNavigate = { destination ->
                    viewModel.navigateTo(destination)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentScreen) {
                ScreenDestination.LANDING -> {
                    LandingScreen(
                        state = state,
                        onCreatePostClick = { viewModel.navigateTo(ScreenDestination.CREATE_POST) },
                        onSubmitBid = { auctionId, email, amount ->
                            viewModel.submitBid(auctionId, email, amount)
                        },
                        onSubscribeNewsletter = { email ->
                            viewModel.subscribeNewsletter(email)
                        }
                    )
                }

                ScreenDestination.CREATE_POST -> {
                    CreatePostScreen(
                        state = state,
                        onBackClick = { viewModel.navigateTo(ScreenDestination.LANDING) },
                        onSetPostType = { type -> viewModel.setPostType(type) },
                        onSelectThumbnail = { index -> viewModel.selectThumbnail(index) },
                        onUpdateCaption = { lineIndex, text ->
                            viewModel.updateCaptionLine(lineIndex, text)
                        },
                        onGenerateCaptions = { viewModel.generateCaptionsWithAI() },
                        onToggleTrait = { id -> viewModel.toggleTrait(id) },
                        onRemoveTrait = { id -> viewModel.removeTrait(id) },
                        onAddTrait = { name, isBeauty -> viewModel.addCustomTrait(name, isBeauty) },
                        onGenerateWithAI = { viewModel.generateWithAI() },
                        onSubmitBid = { auctionId, email, amount ->
                            viewModel.submitBid(auctionId, email, amount)
                        }
                    )
                }

                ScreenDestination.RESULTS -> {
                    ResultsScreen(
                        state = state,
                        onBackClick = { viewModel.navigateTo(ScreenDestination.CREATE_POST) },
                        onPublishToFeed = { viewModel.publishToFeed() },
                        onSaveAutoPostEmail = { email -> viewModel.saveAutoPostEmail(email) },
                        onSubmitBid = { auctionId, email, amount ->
                            viewModel.submitBid(auctionId, email, amount)
                        }
                    )
                }

                ScreenDestination.AUCTIONS -> {
                    AuctionScreen(
                        state = state,
                        onSubmitBid = { auctionId, email, amount ->
                            viewModel.submitBid(auctionId, email, amount)
                        }
                    )
                }

                ScreenDestination.FEED -> {
                    FeedScreen(
                        state = state,
                        onToggleLike = { postId -> viewModel.toggleLike(postId) },
                        onSubmitBid = { auctionId, email, amount ->
                            viewModel.submitBid(auctionId, email, amount)
                        }
                    )
                }

                ScreenDestination.PROFILE -> {
                    ProfileScreen(
                        state = state,
                        onLoginOrRegister = { email, pass -> viewModel.loginOrRegister(email, pass) },
                        onLogout = { viewModel.logout() },
                        onCreatePostClick = { viewModel.navigateTo(ScreenDestination.CREATE_POST) }
                    )
                }
            }

            // In-app Notification Banner
            AnimatedVisibility(
                visible = state.notification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                state.notification?.let { notif ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (notif.isError) CoralRed else PrimaryGreenDark,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = notif.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
