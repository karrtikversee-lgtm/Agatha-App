package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.LocalizationManager
import com.example.data.model.Artwork
import com.example.ui.components.PremiumUpgradeSheet
import com.example.ui.components.SoundscapeMiniPlayer
import com.example.ui.screens.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val currentDestination by viewModel.currentDestination.collectAsState()
        val artworks by viewModel.artworks.collectAsState()
        val tracks by viewModel.tracks.collectAsState()
        val communityPosts by viewModel.communityPosts.collectAsState()
        val priceHistory = viewModel.priceHistory
        val selectedArtwork by viewModel.selectedArtwork.collectAsState()
        val currentLanguage by viewModel.currentLanguage.collectAsState()
        val currentTier by viewModel.currentTier.collectAsState()
        val currentRole by viewModel.currentRole.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()

        val dailyUploadCount by viewModel.dailyArtworkUploadCount.collectAsState()
        val customAudioCount by viewModel.customAudioCount.collectAsState()
        val isUpgradeSheetVisible by viewModel.isUpgradeSheetVisible.collectAsState()
        val upgradeSheetReason by viewModel.upgradeSheetReason.collectAsState()

        val currentTrack by viewModel.audioManager.currentTrack.collectAsState()
        val isPlaying by viewModel.audioManager.isPlaying.collectAsState()
        val progress by viewModel.audioManager.playbackProgress.collectAsState()

        // AI Feature States
        val voiceMessages by viewModel.voiceMessages.collectAsState()
        val isVoiceLive by viewModel.isVoiceLive.collectAsState()
        val isGeneratingVideo by viewModel.isGeneratingVideo.collectAsState()
        val generatedVideoAspectRatio by viewModel.generatedVideoAspectRatio.collectAsState()
        val searchGroundingResult by viewModel.searchGroundingResult.collectAsState()
        val isSearchingGrounding by viewModel.isSearchingGrounding.collectAsState()

        val totalPortfolioValue = remember(artworks) {
          artworks.sumOf { it.estimatedValue }
        }

        // System audio picker for quick Add Audio button in mini-player
        val audioPickerLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent()
        ) { uri ->
          uri?.let {
            val displayName = it.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
              ?: "Personal Audio"
            val added = viewModel.addCustomAudio(displayName, "My Library", it.toString())
            if (added) {
              Toast.makeText(this, "Personal audio added ($customAudioCount/5 Free)", Toast.LENGTH_SHORT).show()
            }
          }
        }

        // Hardware back handler
        BackHandler(enabled = currentDestination != NavDestination.GALLERY && currentDestination != NavDestination.AUTH) {
          viewModel.navigateTo(NavDestination.GALLERY)
        }

        val shareArtwork: (Artwork) -> Unit = { artwork ->
          val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 0
          }
          val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
              Intent.EXTRA_TEXT,
              "Explore \"${artwork.title}\" by ${artwork.artist} (${artwork.year}) in Aethel Vault.\n" +
                  "Valuation: ${currencyFormat.format(artwork.estimatedValue)}\n" +
                  "Blockchain Token: ${artwork.blockchainTokenId}\n" +
                  "Provenance Hash: ${artwork.provenanceHash}\n" +
                  "Verified on Block #${artwork.blockHeight}."
            )
            type = "text/plain"
          }
          startActivity(Intent.createChooser(shareIntent, "Share Artwork Provenance"))
        }

        val shouldShowBottomBar = currentDestination != NavDestination.AUTH && currentDestination != NavDestination.PAYWALL

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(AethelBackground),
          containerColor = AethelBackground,
          bottomBar = {
            if (shouldShowBottomBar) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Floating Mini Audio Player above navigation bar when not on dedicated music screen
                if (currentDestination != NavDestination.MUSIC && currentTrack != null) {
                  SoundscapeMiniPlayer(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    progress = progress,
                    onTogglePlayPause = { viewModel.audioManager.togglePlayPause() },
                    onOpenPlayer = { viewModel.navigateTo(NavDestination.MUSIC) },
                    onAddOwnAudio = {
                      if (viewModel.canAddCustomAudio()) {
                        audioPickerLauncher.launch("audio/*")
                      } else {
                        viewModel.openPaywall("You reached your Free limit of 5 personal audio tracks. Upgrade ($5 for 3mo, $50 for 1yr) for unlimited audio!")
                      }
                    }
                  )
                }

                NavigationBar(
                  containerColor = AethelSurface,
                  tonalElevation = 6.dp,
                  modifier = Modifier.testTag("main_navigation_bar")
                ) {
                  NavigationBarItem(
                    selected = currentDestination == NavDestination.GALLERY,
                    onClick = { viewModel.navigateTo(NavDestination.GALLERY) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Vault") },
                    label = {
                      Text(
                        LocalizationManager.string("nav_gallery", currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_gallery")
                  )

                  NavigationBarItem(
                    selected = currentDestination == NavDestination.MARKET,
                    onClick = { viewModel.navigateTo(NavDestination.MARKET) },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Valuation") },
                    label = {
                      Text(
                        LocalizationManager.string("nav_market", currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_market")
                  )

                  NavigationBarItem(
                    selected = currentDestination == NavDestination.MUSIC,
                    onClick = { viewModel.navigateTo(NavDestination.MUSIC) },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Acoustics") },
                    label = {
                      Text(
                        LocalizationManager.string("nav_music", currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_music")
                  )

                  NavigationBarItem(
                    selected = currentDestination == NavDestination.AI_STUDIO,
                    onClick = { viewModel.navigateTo(NavDestination.AI_STUDIO) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio") },
                    label = {
                      Text(
                        "AI Studio",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_ai_studio")
                  )

                  NavigationBarItem(
                    selected = currentDestination == NavDestination.ANALYTICS,
                    onClick = { viewModel.navigateTo(NavDestination.ANALYTICS) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Intelligence") },
                    label = {
                      Text(
                        LocalizationManager.string("nav_analytics", currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_analytics")
                  )

                  NavigationBarItem(
                    selected = currentDestination == NavDestination.SETTINGS,
                    onClick = { viewModel.navigateTo(NavDestination.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = {
                      Text(
                        "Profile",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = navItemColors(),
                    modifier = Modifier.testTag("nav_tab_settings")
                  )
                }
              }
            }
          }
        ) { paddingValues ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
              .background(AethelBackground)
          ) {
            when (currentDestination) {
              NavDestination.AUTH -> {
                AuthScreen(
                  onLogin = { email, pass -> viewModel.login(email, pass) },
                  onSignUp = { name, email, pass, role -> viewModel.signUp(name, email, pass, role) },
                  onContinueAsGuest = { viewModel.continueAsGuest() },
                  onQuickLoginPreset = { email, pass -> viewModel.login(email, pass) }
                )
              }

              NavDestination.PAYWALL -> {
                PaywallScreen(
                  currentUser = currentUser,
                  onBack = { viewModel.navigateTo(NavDestination.GALLERY) },
                  onSubscribe = { plan ->
                    viewModel.upgradeToPlan(plan)
                    viewModel.navigateTo(NavDestination.GALLERY)
                  },
                  onCancelSubscription = { viewModel.cancelSubscription() }
                )
              }

              NavDestination.GALLERY -> {
                GalleryScreen(
                  artworks = artworks,
                  totalPortfolioValue = totalPortfolioValue,
                  currentLanguage = currentLanguage,
                  currentTier = currentTier,
                  dailyUploadCount = dailyUploadCount,
                  onArtworkClick = { artwork -> viewModel.openArtworkDetail(artwork) },
                  onToggleFeatured = { id -> viewModel.toggleFeatured(id) },
                  onViewCertificate = { artwork -> viewModel.openArtworkDetail(artwork) },
                  onShareArtwork = shareArtwork,
                  onAddArtwork = { title, artist, year, medium, dimensions, value, uri, room, notes ->
                    val added = viewModel.addArtwork(title, artist, year, medium, dimensions, value, uri, room, notes)
                    if (added) {
                      Toast.makeText(this@MainActivity, "Acquisition added to vault!", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onExportCatalog = { viewModel.navigateTo(NavDestination.ANALYTICS) },
                  onShowUpgradeSheet = { reason -> viewModel.openPaywall(reason) }
                )
              }

              NavDestination.MARKET -> {
                MarketScreen(
                  artworks = artworks,
                  priceHistory = priceHistory,
                  onArtworkClick = { artwork -> viewModel.openArtworkDetail(artwork) }
                )
              }

              NavDestination.MUSIC -> {
                MusicScreen(
                  audioManager = viewModel.audioManager,
                  tracks = tracks,
                  currentTier = currentTier,
                  customAudioCount = customAudioCount,
                  onAddCustomAudio = { title, artist, uri ->
                    val added = viewModel.addCustomAudio(title, artist, uri)
                    if (added) {
                      Toast.makeText(this@MainActivity, "Custom audio track imported!", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onRemoveTrack = { id -> viewModel.removeTrack(id) },
                  onShowUpgradeSheet = { reason -> viewModel.openPaywall(reason) }
                )
              }

              NavDestination.AI_STUDIO -> {
                AiStudioScreen(
                  artworks = artworks,
                  voiceMessages = voiceMessages,
                  isVoiceLive = isVoiceLive,
                  onToggleVoiceLive = { viewModel.toggleVoiceLive() },
                  onSendVoiceQuery = { viewModel.sendVoiceQuery(it) },
                  isGeneratingVideo = isGeneratingVideo,
                  generatedVideoAspectRatio = generatedVideoAspectRatio,
                  onGenerateVideo = { uri, resId, title, ratio -> viewModel.generateArtVideo(uri, resId, title, ratio) },
                  searchResult = searchGroundingResult,
                  isSearchingGrounding = isSearchingGrounding,
                  onRunSearchGrounding = { viewModel.runSearchGrounding(it) }
                )
              }

              NavDestination.ANALYTICS -> {
                AnalyticsExportScreen(
                  artworks = artworks,
                  currentTier = currentTier,
                  onShowUpgradeSheet = { reason -> viewModel.openPaywall(reason) }
                )
              }

              NavDestination.COMMUNITY -> {
                CommunityScreen(
                  posts = communityPosts,
                  onToggleLike = { id -> viewModel.toggleLikePost(id) }
                )
              }

              NavDestination.SETTINGS -> {
                ProfileSettingsScreen(
                  currentUser = currentUser,
                  currentLanguage = currentLanguage,
                  currentTier = currentTier,
                  currentRole = currentRole,
                  onLanguageChange = { viewModel.setLanguage(it) },
                  onTierChange = { viewModel.setTier(it) },
                  onRoleChange = { viewModel.setRole(it) },
                  onOpenPaywall = { viewModel.openPaywall() },
                  onLogout = { viewModel.logout() }
                )
              }

              NavDestination.ARTWORK_DETAIL -> {
                selectedArtwork?.let { art ->
                  ArtworkDetailScreen(
                    artwork = art,
                    onBack = { viewModel.navigateTo(NavDestination.GALLERY) },
                    onTogglePrivacy = { id -> viewModel.togglePrivacy(id) },
                    onUpdateRoom = { id, room -> viewModel.updateExhibitionRoom(id, room) },
                    onShare = shareArtwork
                  )
                } ?: run {
                  viewModel.navigateTo(NavDestination.GALLERY)
                }
              }
            }
          }
        }

        // Global Premium Upgrade Sheet Trigger
        if (isUpgradeSheetVisible) {
          PremiumUpgradeSheet(
            initialReason = upgradeSheetReason,
            onDismiss = { viewModel.dismissUpgradeSheet() },
            onUpgrade = { plan ->
              viewModel.upgradeToPlan(plan)
              Toast.makeText(this@MainActivity, "Upgraded to VIP via ${plan.title} (${plan.price})!", Toast.LENGTH_LONG).show()
            }
          )
        }
      }
    }
  }

  @Composable
  private fun navItemColors(): NavigationBarItemColors {
    return NavigationBarItemDefaults.colors(
      selectedIconColor = AethelNeonCyan,
      unselectedIconColor = AethelTextMuted,
      selectedTextColor = AethelNeonCyan,
      unselectedTextColor = AethelTextMuted,
      indicatorColor = AethelSurfaceHighlight
    )
  }
}
