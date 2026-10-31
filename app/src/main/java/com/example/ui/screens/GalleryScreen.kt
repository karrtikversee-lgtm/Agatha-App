package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.localization.LocalizationManager
import com.example.data.model.AppLanguage
import com.example.data.model.Artwork
import com.example.ui.components.ArtworkCard
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.components.FloatingOrb
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
  artworks: List<Artwork>,
  totalPortfolioValue: Double,
  currentLanguage: AppLanguage,
  currentTier: com.example.data.model.CollectorTier,
  dailyUploadCount: Int,
  onArtworkClick: (Artwork) -> Unit,
  onToggleFeatured: (String) -> Unit,
  onViewCertificate: (Artwork) -> Unit,
  onShareArtwork: (Artwork) -> Unit,
  onAddArtwork: (title: String, artist: String, year: Int, medium: String, dimensions: String, value: Double, uri: String?, room: String, notes: String) -> Unit,
  onExportCatalog: () -> Unit,
  onShowUpgradeSheet: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedRoom by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }

  val rooms = listOf("All", "Lumina Neo-Form", "Modernist Kinetic", "Deep Vault")

  val filteredArtworks = remember(artworks, searchQuery, selectedRoom) {
    artworks.filter { art ->
      val matchesRoom = selectedRoom == "All" || art.exhibitionRoom.equals(selectedRoom, ignoreCase = true)
      val matchesSearch = art.title.contains(searchQuery, ignoreCase = true) ||
          art.artist.contains(searchQuery, ignoreCase = true) ||
          art.medium.contains(searchQuery, ignoreCase = true)
      matchesRoom && matchesSearch
    }
  }

  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  Scaffold(
    modifier = modifier.testTag("gallery_screen"),
    containerColor = AethelBackground,
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          if (currentTier == com.example.data.model.CollectorTier.STANDARD && dailyUploadCount >= 10) {
            onShowUpgradeSheet("You reached your Free limit of 10 artworks today. Upgrade to Premium ($5/mo, $20/3mo, $50/1yr) for unlimited acquisitions!")
          } else {
            showAddDialog = true
          }
        },
        containerColor = AethelPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_artwork_fab"),
        icon = { Icon(Icons.Default.Add, contentDescription = "Add Artwork") },
        text = {
          val limitText = if (currentTier == com.example.data.model.CollectorTier.STANDARD) " ($dailyUploadCount/10 Today)" else " (VIP Unlimited)"
          Text("Acquire Artwork$limitText", fontWeight = FontWeight.SemiBold)
        }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 120.dp)
    ) {
      // Hero Header inspired directly by the reference image
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          // Floating 3D Glowing Orb peeking out from the top right edge
          FloatingOrb(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 10.dp, y = (-8).dp),
            size = 46.dp,
            primaryColor = AethelNeonPink,
            secondaryColor = AethelNeonPurple
          )

          // Main Hero Portfolio Card with Contour Wave
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("hero_portfolio_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
              containerColor = AethelSurface
            ),
            border = BorderStroke(1.dp, AethelBorder.copy(alpha = 0.8f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
            ) {
              // Concentric contour wave lines from reference mockup
              ContourWaveCanvas(
                modifier = Modifier.fillMaxSize(),
                lineColor = Color.White.copy(alpha = 0.45f),
                strokeCount = 20
              )

              // Soft bottom gradient
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color.Transparent,
                        AethelSurface.copy(alpha = 0.75f),
                        AethelSurface
                      ),
                      startY = 140f
                    )
                  )
              )

              // Card Content
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
              ) {
                // Top row with user greeting and vault chip
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = "Good morning \u2728",
                      color = AethelTextSecondary,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium
                    )
                    Text(
                      text = "Moyo Shiro",
                      color = AethelTextPrimary,
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  // Cryptographic Vault Badge
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, AethelNeonCyan.copy(alpha = 0.35f))
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(8.dp)
                          .background(AethelSuccessGreen, CircleShape)
                      )
                      Text(
                        text = "GENEVA VAULT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                      )
                    }
                  }
                }

                // Middle/Bottom section with portfolio valuation
                Column {
                  Text(
                    text = LocalizationManager.string("total_portfolio", currentLanguage).uppercase(),
                    color = AethelTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(
                      text = currencyFormat.format(totalPortfolioValue),
                      color = Color.White,
                      fontSize = 32.sp,
                      fontWeight = FontWeight.ExtraBold,
                      letterSpacing = (-0.5).sp
                    )

                    Surface(
                      shape = RoundedCornerShape(16.dp),
                      color = Color(0xFF132B20),
                      border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.4f))
                    ) {
                      Text(
                        text = "+$12,400 (7.2%)",
                        color = AethelSuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  // Quick Export & Provenance Action Buttons
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Button(
                      onClick = onExportCatalog,
                      modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("export_catalog_hero_btn"),
                      shape = RoundedCornerShape(14.dp),
                      colors = ButtonDefaults.buttonColors(
                        containerColor = AethelPrimary
                      )
                    ) {
                      Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = LocalizationManager.string("export_catalog", currentLanguage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Search & Curation Filters
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          // Outlined Search Field
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("gallery_search_input"),
            placeholder = { Text("Search artworks, artists, mediums...", color = AethelTextMuted) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = AethelTextSecondary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AethelTextSecondary)
                }
              }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = AethelSurfaceElevated,
              unfocusedContainerColor = AethelSurface,
              focusedBorderColor = AethelPrimary,
              unfocusedBorderColor = AethelBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Exhibition Rooms Filter Chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
          ) {
            items(rooms) { room ->
              val isSelected = selectedRoom == room
              FilterChip(
                selected = isSelected,
                onClick = { selectedRoom = room },
                label = {
                  Text(
                    text = if (room == "All") LocalizationManager.string("all_rooms", currentLanguage) else room,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  containerColor = AethelSurface,
                  selectedContainerColor = AethelPrimary,
                  labelColor = AethelTextSecondary,
                  selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = AethelBorder,
                  selectedBorderColor = AethelPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("room_chip_$room")
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Results counter row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "CURATED ACQUISITIONS (${filteredArtworks.size})",
              color = AethelTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.6.sp
            )
            Text(
              text = "AES-256 ENCRYPTED",
              color = AethelNeonCyan,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // Empty State
      if (filteredArtworks.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(48.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = AethelTextMuted,
                modifier = Modifier.size(54.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No artworks found in this exhibition room.",
                color = AethelTextSecondary,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  searchQuery = ""
                  selectedRoom = "All"
                },
                colors = ButtonDefaults.buttonColors(containerColor = AethelSurfaceElevated)
              ) {
                Text("Reset Filters", color = Color.White)
              }
            }
          }
        }
      }

      // Artworks List
      items(filteredArtworks, key = { it.id }) { artwork ->
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
          ArtworkCard(
            artwork = artwork,
            onClick = { onArtworkClick(artwork) },
            onToggleFeatured = { onToggleFeatured(artwork.id) },
            onViewCertificate = { onViewCertificate(artwork) },
            onShare = { onShareArtwork(artwork) }
          )
        }
      }
    }
  }

  // Acquisition Dialog to Add Custom Artwork
  if (showAddDialog) {
    AddArtworkDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { title, artist, year, medium, dimensions, value, uri, room, notes ->
        onAddArtwork(title, artist, year, medium, dimensions, value, uri, room, notes)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun AddArtworkDialog(
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    artist: String,
    year: Int,
    medium: String,
    dimensions: String,
    value: Double,
    uri: String?,
    room: String,
    notes: String
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var artist by remember { mutableStateOf("") }
  var yearStr by remember { mutableStateOf("2026") }
  var medium by remember { mutableStateOf("Digital Fine Art & Algorithmic Geometry") }
  var dimensions by remember { mutableStateOf("150 × 100 cm") }
  var valueStr by remember { mutableStateOf("45000") }
  var selectedRoom by remember { mutableStateOf("Lumina Neo-Form") }
  var curatorialNotes by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    selectedImageUri = uri
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = AethelSurfaceElevated,
      border = BorderStroke(1.dp, AethelBorder),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("add_artwork_dialog")
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Acquire New Artwork",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Mint cryptographic provenance on chain",
                color = AethelNeonCyan,
                fontSize = 12.sp
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = AethelTextSecondary)
            }
          }
        }

        // Image Picker Area
        item {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clickable {
                photoPickerLauncher.launch(
                  androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                  )
                )
              },
            shape = RoundedCornerShape(16.dp),
            color = AethelBackground,
            border = BorderStroke(1.dp, AethelNeonPurple.copy(alpha = 0.5f))
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              if (selectedImageUri != null) {
                Text(
                  text = "✓ Image Attached: ${selectedImageUri?.lastPathSegment?.take(16)}...",
                  color = AethelSuccessGreen,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
              } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = AethelNeonPink,
                    modifier = Modifier.size(32.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Select High-Res Photo / Painting",
                    color = AethelTextSecondary,
                    fontSize = 12.sp
                  )
                  Text(
                    text = "Zero-Permission Photo Picker",
                    color = AethelTextMuted,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Artwork Title") },
            placeholder = { Text("e.g. Prism Horizon 04") },
            modifier = Modifier.fillMaxWidth().testTag("input_artwork_title"),
            shape = RoundedCornerShape(14.dp)
          )
        }

        item {
          OutlinedTextField(
            value = artist,
            onValueChange = { artist = it },
            label = { Text("Artist / Atelier") },
            placeholder = { Text("e.g. Moyo Shiro") },
            modifier = Modifier.fillMaxWidth().testTag("input_artwork_artist"),
            shape = RoundedCornerShape(14.dp)
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = yearStr,
              onValueChange = { yearStr = it },
              label = { Text("Year") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
              value = valueStr,
              onValueChange = { valueStr = it },
              label = { Text("Est. Value ($)") },
              modifier = Modifier.weight(1.5f).testTag("input_artwork_value"),
              shape = RoundedCornerShape(14.dp)
            )
          }
        }

        item {
          OutlinedTextField(
            value = medium,
            onValueChange = { medium = it },
            label = { Text("Medium") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )
        }

        item {
          OutlinedTextField(
            value = dimensions,
            onValueChange = { dimensions = it },
            label = { Text("Dimensions") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )
        }

        item {
          OutlinedTextField(
            value = curatorialNotes,
            onValueChange = { curatorialNotes = it },
            label = { Text("Curatorial Statement / Provenance") },
            placeholder = { Text("Add acquisition details, history, or condition...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 2
          )
        }

        item {
          Button(
            onClick = {
              if (title.isNotBlank()) {
                val yr = yearStr.toIntOrNull() ?: 2026
                val valD = valueStr.toDoubleOrNull() ?: 25000.0
                onConfirm(
                  title,
                  artist.ifBlank { "Unknown Master" },
                  yr,
                  medium,
                  dimensions,
                  valD,
                  selectedImageUri?.toString(),
                  selectedRoom,
                  curatorialNotes
                )
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("confirm_add_artwork_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary),
            enabled = title.isNotBlank()
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mint & Store in Vault", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
