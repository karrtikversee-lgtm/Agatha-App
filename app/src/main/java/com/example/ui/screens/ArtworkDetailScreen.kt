package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Artwork
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkDetailScreen(
  artwork: Artwork,
  onBack: () -> Unit,
  onTogglePrivacy: (String) -> Unit,
  onUpdateRoom: (String, String) -> Unit,
  onShare: (Artwork) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCertificateDialog by remember { mutableStateOf(false) }
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }

  val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
    scale = (scale * zoomChange).coerceIn(1f, 4f)
    if (scale > 1f) {
      offset += offsetChange
    } else {
      offset = Offset.Zero
    }
  }

  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  Scaffold(
    modifier = modifier.testTag("artwork_detail_screen"),
    containerColor = AethelBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = artwork.title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(onClick = { onTogglePrivacy(artwork.id) }) {
            Icon(
              imageVector = if (artwork.isPrivate) Icons.Default.Lock else Icons.Default.LockOpen,
              contentDescription = "Privacy Toggle",
              tint = if (artwork.isPrivate) AethelNeonPink else AethelTextSecondary
            )
          }
          IconButton(onClick = { onShare(artwork) }) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = AethelBackground
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
    ) {
      // Zoomable High-Resolution Artwork Viewer
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
          .background(Color.Black)
          .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformState)
        ) {
          if (artwork.imageResId != null) {
            Image(
              painter = painterResource(id = artwork.imageResId),
              contentDescription = artwork.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = scale,
                  scaleY = scale,
                  translationX = offset.x,
                  translationY = offset.y
                )
            )
          } else if (artwork.imageUri != null) {
            AsyncImage(
              model = artwork.imageUri,
              contentDescription = artwork.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                  scaleX = scale,
                  scaleY = scale,
                  translationX = offset.x,
                  translationY = offset.y
                )
            )
          } else {
            ContourWaveCanvas(
              modifier = Modifier.fillMaxSize(),
              lineColor = AethelNeonCyan.copy(alpha = 0.7f),
              strokeCount = 24
            )
          }
        }

        // Loupe zoom indicator overlay
        Surface(
          modifier = Modifier
            .padding(14.dp)
            .align(Alignment.BottomEnd),
          shape = RoundedCornerShape(12.dp),
          color = Color.Black.copy(alpha = 0.65f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(
              text = if (scale > 1.05f) "ZOOM ${String.format(Locale.US, "%.1f", scale)}x" else "PINCH TO ZOOM 8K",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Metadata & Valuation Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = artwork.title,
              color = Color.White,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${artwork.artist} • ${artwork.year}",
              color = AethelNeonCyan,
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = AethelSurfaceElevated,
            border = BorderStroke(1.dp, AethelBorder)
          ) {
            Text(
              text = artwork.exhibitionRoom,
              color = AethelTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Valuation Readout Panel
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = AethelSurface,
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "CURRENT VALUATION",
                color = AethelTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = currencyFormat.format(artwork.estimatedValue),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF132B20),
                border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.4f))
              ) {
                Text(
                  text = "+$${String.format(Locale.US, "%,.0f", artwork.estimatedValue - artwork.previousValue)}",
                  color = AethelSuccessGreen,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Sotheby's Index Benchmark",
                color = AethelTextMuted,
                fontSize = 10.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Certificate of Authenticity Callout
        Button(
          onClick = { showCertificateDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("open_coa_btn"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AethelSurfaceHighlight),
          border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.5f))
        ) {
          Icon(Icons.Default.QrCode2, contentDescription = null, tint = AethelAmberGold, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "View Certificate of Authenticity (CoA)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Technical Specs
        Text(
          text = "PHYSICAL & DIGITAL SPECIFICATIONS",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.7.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SpecRow("Medium", artwork.medium)
            SpecRow("Dimensions", artwork.dimensions)
            SpecRow("Token Standard", "ERC-721 Vault-Certified")
            SpecRow("Contract Token ID", artwork.blockchainTokenId)
            SpecRow("Block Height", "#${artwork.blockHeight}")
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Curatorial Statement
        Text(
          text = "CURATORIAL STATEMENT",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.7.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = artwork.curatorialStatement,
          color = AethelTextSecondary,
          fontSize = 14.sp,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Provenance Audit Trail (Blockchain verification)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "IMMUTABLE PROVENANCE TRAIL",
            color = AethelTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
          )
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = AethelSuccessGreen, modifier = Modifier.size(13.dp))
            Text("VERIFIED ON CHAIN", color = AethelSuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
          artwork.provenanceTrail.forEachIndexed { index, event ->
            ProvenanceTimelineItem(
              event = event,
              isLast = index == artwork.provenanceTrail.size - 1
            )
          }
        }
      }
    }
  }

  // Certificate of Authenticity Modal
  if (showCertificateDialog) {
    CertificateOfAuthenticityDialog(
      artwork = artwork,
      onDismiss = { showCertificateDialog = false }
    )
  }
}

@Composable
fun SpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, color = AethelTextMuted, fontSize = 12.sp)
    Text(
      text = value,
      color = Color.White,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      fontFamily = if (label.contains("ID") || label.contains("Block")) FontFamily.Monospace else FontFamily.Default
    )
  }
}

@Composable
fun ProvenanceTimelineItem(
  event: com.example.data.model.ProvenanceEvent,
  isLast: Boolean
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(
        modifier = Modifier
          .size(12.dp)
          .background(AethelNeonCyan, CircleShape)
      )
      if (!isLast) {
        Box(
          modifier = Modifier
            .width(2.dp)
            .height(58.dp)
            .background(AethelBorder)
        )
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = event.eventType,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = event.year,
          color = AethelNeonCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Text(
        text = "${event.custodian} • ${event.location}",
        color = AethelTextSecondary,
        fontSize = 11.sp
      )
      Text(
        text = "Hash: ${event.verifiedHash}",
        color = AethelTextMuted,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun CertificateOfAuthenticityDialog(
  artwork: Artwork,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF0F1117),
      border = BorderStroke(1.5.dp, AethelAmberGold.copy(alpha = 0.8f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("coa_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.VerifiedUser,
          contentDescription = null,
          tint = AethelAmberGold,
          modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "CERTIFICATE OF AUTHENTICITY",
          color = AethelAmberGold,
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.2.sp
        )

        Text(
          text = "Cryptographically Minted & Sealed",
          color = AethelTextSecondary,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stylized QR / Cryptographic Seal
        Box(
          modifier = Modifier
            .size(140.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.QrCode2,
            contentDescription = "QR Seal",
            tint = Color.Black,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = artwork.title,
          color = Color.White,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Artist: ${artwork.artist} (${artwork.year})",
          color = AethelTextSecondary,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = AethelSurfaceElevated,
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "PROVENANCE HASH",
              color = AethelTextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = artwork.provenanceHash,
              color = AethelNeonCyan,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              maxLines = 2
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary)
        ) {
          Text("Verified & Stored", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
