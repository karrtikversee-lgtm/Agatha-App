package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Artwork
import com.example.data.model.SearchGroundingResult
import com.example.data.model.VoiceMessage
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.components.FloatingOrb
import com.example.ui.theme.*

enum class AiStudioTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  VOICE_LIVE("Live Voice (Gemini 3.8)", Icons.Default.Mic),
  IMAGE_TO_VIDEO("Animate to Video", Icons.Default.Movie),
  SEARCH_GROUNDING("Search Grounding", Icons.Default.Search)
}

@Composable
fun AiStudioScreen(
  artworks: List<Artwork>,
  voiceMessages: List<VoiceMessage>,
  isVoiceLive: Boolean,
  onToggleVoiceLive: () -> Unit,
  onSendVoiceQuery: (String) -> Unit,
  isGeneratingVideo: Boolean,
  generatedVideoAspectRatio: String,
  onGenerateVideo: (imageUri: String?, resId: Int?, title: String, aspectRatio: String) -> Unit,
  searchResult: SearchGroundingResult?,
  isSearchingGrounding: Boolean,
  onRunSearchGrounding: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(AiStudioTab.VOICE_LIVE) }

  Scaffold(
    modifier = modifier.testTag("ai_studio_screen"),
    containerColor = AethelBackground
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Header
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "GEMINI MULTIMODAL INTELLIGENCE",
              color = AethelNeonCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
            Text(
              text = "AI Curator & Motion Studio",
              color = Color.White,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold
            )
          }

          FloatingOrb(
            size = 34.dp,
            primaryColor = AethelNeonPink,
            secondaryColor = AethelNeonPurple
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Selector Row
        TabRow(
          selectedTabIndex = selectedTab.ordinal,
          containerColor = AethelSurfaceElevated,
          contentColor = AethelNeonCyan,
          divider = {},
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
              color = AethelNeonCyan
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
        ) {
          AiStudioTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
              selected = isSelected,
              onClick = { selectedTab = tab },
              text = {
                Text(
                  text = tab.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else AethelTextMuted,
                  maxLines = 1
                )
              },
              icon = {
                Icon(
                  tab.icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) AethelNeonCyan else AethelTextMuted
                )
              },
              modifier = Modifier.testTag("ai_tab_${tab.name}")
            )
          }
        }
      }

      // Content Body based on selected tab
      Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        when (selectedTab) {
          AiStudioTab.VOICE_LIVE -> {
            VoiceConversationView(
              messages = voiceMessages,
              isVoiceLive = isVoiceLive,
              onToggleVoiceLive = onToggleVoiceLive,
              onSendVoiceQuery = onSendVoiceQuery
            )
          }

          AiStudioTab.IMAGE_TO_VIDEO -> {
            ImageToVideoView(
              artworks = artworks,
              isGenerating = isGeneratingVideo,
              aspectRatio = generatedVideoAspectRatio,
              onGenerate = onGenerateVideo
            )
          }

          AiStudioTab.SEARCH_GROUNDING -> {
            SearchGroundingView(
              result = searchResult,
              isSearching = isSearchingGrounding,
              onSearch = onRunSearchGrounding
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 1. Voice Conversation with model: gemini-3.8-live (Live API)
// -------------------------------------------------------------
@Composable
fun VoiceConversationView(
  messages: List<VoiceMessage>,
  isVoiceLive: Boolean,
  onToggleVoiceLive: () -> Unit,
  onSendVoiceQuery: (String) -> Unit
) {
  var textQuery by remember { mutableStateOf("") }

  // Pulsing voice animation
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "pulseScale"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 120.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Live Status Banner & Animated Voice Orb
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
      border = BorderStroke(1.dp, if (isVoiceLive) AethelNeonCyan else AethelBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(9.dp)
                .background(if (isVoiceLive) AethelSuccessGreen else AethelTextMuted, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isVoiceLive) "LIVE AUDIO STREAM ACTIVE" else "LIVE API STANDBY",
              color = if (isVoiceLive) AethelSuccessGreen else AethelTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Text(
            text = "Model: gemini-3.8-live",
            color = AethelNeonCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Big Audio Pulsing Orb
        Box(
          modifier = Modifier
            .size(110.dp)
            .clickable { onToggleVoiceLive() }
            .testTag("voice_live_toggle_orb"),
          contentAlignment = Alignment.Center
        ) {
          if (isVoiceLive) {
            Box(
              modifier = Modifier
                .size(110.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                .background(AethelNeonCyan.copy(alpha = 0.22f), CircleShape)
            )
          }

          Box(
            modifier = Modifier
              .size(80.dp)
              .background(
                Brush.radialGradient(
                  colors = if (isVoiceLive) listOf(AethelNeonCyan, AethelPrimary, Color(0xFF0F172A))
                  else listOf(AethelSurfaceHighlight, AethelSurface, Color.Black)
                ),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isVoiceLive) Icons.Default.Mic else Icons.Default.MicOff,
              contentDescription = "Voice Live",
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (isVoiceLive) "Listening... Speak to your Gemini 3.8 Live Art Curator" else "Tap Orb to Start Real-Time Live Voice Conversation",
          color = if (isVoiceLive) Color.White else AethelTextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Conversation History
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      reverseLayout = false
    ) {
      items(messages, key = { it.id }) { msg ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (msg.isUser) AethelPrimary else AethelSurfaceHighlight,
            border = BorderStroke(1.dp, if (msg.isUser) Color.Transparent else AethelBorder),
            modifier = Modifier.widthIn(max = 300.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = if (msg.isUser) "You" else "Gemini 3.8 Live Curator",
                color = if (msg.isUser) Color.White.copy(alpha = 0.8f) else AethelNeonCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = msg.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )
            }
          }
        }
      }
    }

    // Input Box for Quick Voice / Text prompts
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = textQuery,
        onValueChange = { textQuery = it },
        placeholder = { Text("Ask appraisal, art style, or valuation...", color = AethelTextMuted, fontSize = 12.sp) },
        modifier = Modifier
          .weight(1f)
          .testTag("voice_chat_text_input"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = AethelSurfaceElevated,
          unfocusedContainerColor = AethelSurface,
          focusedBorderColor = AethelNeonCyan,
          unfocusedBorderColor = AethelBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (textQuery.isNotBlank()) {
            onSendVoiceQuery(textQuery)
            textQuery = ""
          }
        },
        modifier = Modifier
          .size(46.dp)
          .background(AethelPrimary, CircleShape)
          .testTag("send_voice_query_btn")
      ) {
        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
      }
    }
  }
}

// -------------------------------------------------------------
// 2. Animate Images into Video with model: gemini-omni-1.1-flash
// -------------------------------------------------------------
@Composable
fun ImageToVideoView(
  artworks: List<Artwork>,
  isGenerating: Boolean,
  aspectRatio: String,
  onGenerate: (imageUri: String?, resId: Int?, title: String, aspectRatio: String) -> Unit
) {
  var selectedArtwork by remember { mutableStateOf(artworks.firstOrNull()) }
  var chosenAspectRatio by remember { mutableStateOf("16:9") } // 16:9 (landscape) or 9:16 (portrait)
  var pickedCustomUri by remember { mutableStateOf<Uri?>(null) }
  var isPlayingPreview by remember { mutableStateOf(true) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    pickedCustomUri = uri
  }

  // Animation values simulating the generated video motion
  val infiniteTransition = rememberInfiniteTransition(label = "video_motion")
  val panOffsetX by infiniteTransition.animateFloat(
    initialValue = -12f,
    targetValue = 12f,
    animationSpec = infiniteRepeatable(tween(3500, easing = EaseInOutSine), RepeatMode.Reverse),
    label = "pan"
  )
  val zoomScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.14f,
    animationSpec = infiniteRepeatable(tween(4200, easing = EaseInOutSine), RepeatMode.Reverse),
    label = "zoom"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
        border = BorderStroke(1.dp, AethelBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "ANIMATE IMAGES INTO VIDEO",
            color = AethelNeonPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Model: gemini-omni-1.1-flash (Interactions API)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Transform high-resolution paintings into cinematic ambient loop videos with dynamic camera drift and parallax lighting.",
            color = AethelTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }

    // Video Player Preview Box
    item {
      val isPortrait = chosenAspectRatio == "9:16"
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.5.dp, AethelNeonPink.copy(alpha = 0.6f)),
        modifier = Modifier
          .fillMaxWidth()
          .height(if (isPortrait) 320.dp else 220.dp)
          .testTag("animated_video_preview_card")
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (pickedCustomUri != null) {
            AsyncImage(
              model = pickedCustomUri,
              contentDescription = "Video Source",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = if (isPlayingPreview) zoomScale else 1f,
                  scaleY = if (isPlayingPreview) zoomScale else 1f,
                  translationX = if (isPlayingPreview) panOffsetX else 0f
                )
            )
          } else if (selectedArtwork?.imageResId != null) {
            Image(
              painter = painterResource(id = selectedArtwork!!.imageResId!!),
              contentDescription = "Video Source",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = if (isPlayingPreview) zoomScale else 1f,
                  scaleY = if (isPlayingPreview) zoomScale else 1f,
                  translationX = if (isPlayingPreview) panOffsetX else 0f
                )
            )
          } else {
            ContourWaveCanvas(modifier = Modifier.fillMaxSize())
          }

          // Video Overlay Badge & Aspect Ratio Stamp
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Movie, contentDescription = null, tint = AethelNeonPink, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$chosenAspectRatio VIDEO",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Play/Pause Overlay indicator
          IconButton(
            onClick = { isPlayingPreview = !isPlayingPreview },
            modifier = Modifier
              .size(50.dp)
              .background(Color.Black.copy(alpha = 0.5f), CircleShape)
          ) {
            Icon(
              imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = "Toggle Motion",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }

    // Aspect Ratio Toggle: 16:9 (Landscape) or 9:16 (Portrait)
    item {
      Text(
        text = "SELECT VIDEO ASPECT RATIO",
        color = AethelTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        listOf("16:9", "9:16").forEach { ratio ->
          val isSelected = chosenAspectRatio == ratio
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { chosenAspectRatio = ratio }
              .testTag("ratio_btn_$ratio"),
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) AethelSurfaceHighlight else AethelSurfaceElevated,
            border = BorderStroke(1.dp, if (isSelected) AethelNeonCyan else AethelBorder)
          ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (ratio == "16:9") "16:9 (Landscape)" else "9:16 (Portrait)",
                color = if (isSelected) Color.White else AethelTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (ratio == "16:9") "Ideal for Gallery Screens" else "Ideal for Mobile Stories",
                color = AethelTextMuted,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // Photo Upload / Artwork Selector Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "CHOOSE ARTWORK SOURCE",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )

        OutlinedButton(
          onClick = {
            photoPickerLauncher.launch(
              androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
              )
            )
          },
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, AethelNeonCyan),
          modifier = Modifier.testTag("upload_custom_photo_for_video_btn")
        ) {
          Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AethelNeonCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Upload Photo", color = Color.White, fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        artworks.forEach { art ->
          val isSelected = selectedArtwork?.id == art.id && pickedCustomUri == null
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                selectedArtwork = art
                pickedCustomUri = null
              },
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) AethelPrimary.copy(alpha = 0.3f) else AethelSurfaceElevated,
            border = BorderStroke(1.dp, if (isSelected) AethelPrimary else AethelBorder)
          ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
              Text(text = art.title, color = Color.White, fontSize = 12.sp, maxLines = 1)
            }
          }
        }
      }
    }

    // Generate Button
    item {
      Button(
        onClick = {
          onGenerate(
            pickedCustomUri?.toString(),
            selectedArtwork?.imageResId,
            selectedArtwork?.title ?: "Custom Artwork",
            chosenAspectRatio
          )
        },
        enabled = !isGenerating,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("generate_animated_video_btn"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary)
      ) {
        if (isGenerating) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Generating $chosenAspectRatio Video via Omni 1.1 Flash...")
        } else {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Generate $chosenAspectRatio Video Motion", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

// -------------------------------------------------------------
// 3. Search Grounding with model: gemini-3.8-flash (googleSearch)
// -------------------------------------------------------------
@Composable
fun SearchGroundingView(
  result: SearchGroundingResult?,
  isSearching: Boolean,
  onSearch: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("Christie's 2026 digital fine art auction hammer prices") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 120.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
        border = BorderStroke(1.dp, AethelBorder)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "GOOGLE SEARCH DATA GROUNDING",
            color = AethelSuccessGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Model: gemini-3.8-flash (with googleSearch tool)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Retrieve verified, up-to-date art market valuations, Sotheby's and Christie's auction results, and artist exhibition histories grounded in web search.",
            color = AethelTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }

    // Search Query Field
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("Query Live Art Market & Web Search") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_grounding_input"),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
          IconButton(
            onClick = { onSearch(searchQuery) },
            enabled = !isSearching && searchQuery.isNotBlank(),
            modifier = Modifier.testTag("run_search_grounding_btn")
          ) {
            if (isSearching) {
              CircularProgressIndicator(color = AethelSuccessGreen, modifier = Modifier.size(20.dp))
            } else {
              Icon(Icons.Default.Search, contentDescription = "Run Search", tint = AethelSuccessGreen)
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = AethelSurfaceElevated,
          unfocusedContainerColor = AethelSurface,
          focusedBorderColor = AethelSuccessGreen,
          unfocusedBorderColor = AethelBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        )
      )
    }

    // Suggested Grounding Queries
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "Christie's Contemporary hammer results",
          "Eli Vance digital fine art appraisal",
          "Venice Biennale kinetic sculpture reviews",
          "Phillips 2026 generative art trends"
        ).forEach { suggestion ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AethelSurfaceElevated,
            border = BorderStroke(1.dp, AethelBorder),
            modifier = Modifier.clickable {
              searchQuery = suggestion
              onSearch(suggestion)
            }
          ) {
            Text(
              text = suggestion,
              color = AethelTextSecondary,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Result Card with Web Sources Citations
    item {
      result?.let { res ->
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurface),
          border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TravelExplore, contentDescription = null, tint = AethelSuccessGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "GROUNDED SEARCH SYNTHESIS",
                  color = AethelSuccessGreen,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(text = res.timestamp, color = AethelTextMuted, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = res.synthesis,
              color = Color.White,
              fontSize = 13.sp,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = "GROUNDED WEB SOURCES (${res.sources.size})",
              color = AethelTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            res.sources.forEach { source ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = AethelSurfaceElevated,
                border = BorderStroke(1.dp, AethelBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(text = source.title, color = AethelNeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Text(text = source.domain, color = AethelSuccessGreen, fontSize = 10.sp)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = source.snippet, color = AethelTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                }
              }
            }
          }
        }
      } ?: run {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Enter a query or select a topic above to query live Google Search grounded intelligence.",
            color = AethelTextMuted,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}
