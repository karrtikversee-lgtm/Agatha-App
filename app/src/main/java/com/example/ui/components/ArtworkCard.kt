package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Artwork
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArtworkCard(
  artwork: Artwork,
  onClick: () -> Unit,
  onToggleFeatured: () -> Unit,
  onViewCertificate: () -> Unit,
  onShare: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }
  val priceDifference = artwork.estimatedValue - artwork.previousValue
  val percentageGain = if (artwork.previousValue > 0) {
    (priceDifference / artwork.previousValue) * 100
  } else 0.0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("artwork_card_${artwork.id}")
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = AethelSurfaceElevated
    ),
    border = BorderStroke(1.dp, AethelBorder.copy(alpha = 0.65f))
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Artwork Hero Image Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(220.dp)
          .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
          .background(AethelBackground)
      ) {
        if (artwork.imageResId != null) {
          Image(
            painter = painterResource(id = artwork.imageResId),
            contentDescription = artwork.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else if (artwork.imageUri != null) {
          AsyncImage(
            model = artwork.imageUri,
            contentDescription = artwork.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          // Abstract wave canvas fallback
          ContourWaveCanvas(
            modifier = Modifier.fillMaxSize(),
            lineColor = AethelNeonPurple.copy(alpha = 0.65f)
          )
        }

        // Gradient overlay for smooth transition to dark card body
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.35f),
                  AethelSurfaceElevated.copy(alpha = 0.95f)
                ),
                startY = 100f
              )
            )
        )

        // Room / Category badge top-left
        Surface(
          modifier = Modifier
            .padding(14.dp)
            .align(Alignment.TopStart),
          shape = RoundedCornerShape(12.dp),
          color = Color.Black.copy(alpha = 0.65f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
          Text(
            text = artwork.exhibitionRoom,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }

        // Top right quick action icons
        Row(
          modifier = Modifier
            .padding(10.dp)
            .align(Alignment.TopEnd),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          IconButton(
            onClick = onToggleFeatured,
            modifier = Modifier
              .size(38.dp)
              .background(Color.Black.copy(alpha = 0.60f), CircleShape)
              .testTag("featured_btn_${artwork.id}")
          ) {
            Icon(
              imageVector = if (artwork.isFeatured) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (artwork.isFeatured) AethelNeonPink else Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onViewCertificate,
            modifier = Modifier
              .size(38.dp)
              .background(Color.Black.copy(alpha = 0.60f), CircleShape)
              .testTag("certificate_btn_${artwork.id}")
          ) {
            Icon(
              imageVector = Icons.Default.QrCode2,
              contentDescription = "Certificate",
              tint = AethelAmberGold,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Blockchain verified chip at bottom right of image
        Surface(
          modifier = Modifier
            .padding(14.dp)
            .align(Alignment.BottomEnd),
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF0F231D),
          border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = AethelSuccessGreen,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = "PROVENANCE",
              color = AethelSuccessGreen,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.6.sp
            )
          }
        }
      }

      // Card Information Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 16.dp)
      ) {
        Text(
          text = artwork.title,
          color = AethelTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${artwork.artist} • ${artwork.year}",
            color = AethelTextSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Text(
            text = artwork.blockchainTokenId.take(10) + "...",
            color = AethelNeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Price Valuation & Metric Pill
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "ESTIMATED VALUE",
              color = AethelTextMuted,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
            Text(
              text = currencyFormat.format(artwork.estimatedValue),
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Valuation Gain Pill
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (priceDifference >= 0) Color(0xFF132B20) else Color(0xFF33151D),
            border = BorderStroke(
              1.dp,
              if (priceDifference >= 0) AethelSuccessGreen.copy(alpha = 0.35f) else Color(0xFFFF4D4D).copy(alpha = 0.35f)
            )
          ) {
            Text(
              text = "+${String.format(Locale.US, "%.1f", percentageGain)}%",
              color = if (priceDifference >= 0) AethelSuccessGreen else Color(0xFFFF6B6B),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onClick,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("curate_btn_${artwork.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AethelPrimary
            )
          ) {
            Icon(
              imageVector = Icons.Default.ZoomIn,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Inspect & Curate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }

          IconButton(
            onClick = onShare,
            modifier = Modifier
              .size(44.dp)
              .background(AethelSurfaceHighlight, RoundedCornerShape(14.dp))
              .testTag("share_btn_${artwork.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = AethelTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
