package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.ui.theme.*

@Composable
fun SoundscapeMiniPlayer(
  currentTrack: AudioTrack?,
  isPlaying: Boolean,
  progress: Float,
  onTogglePlayPause: () -> Unit,
  onOpenPlayer: () -> Unit,
  onAddOwnAudio: () -> Unit,
  modifier: Modifier = Modifier
) {
  val track = currentTrack ?: return

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .testTag("soundscape_mini_player")
      .clickable(onClick = onOpenPlayer),
    shape = RoundedCornerShape(20.dp),
    color = AethelSurfaceHighlight.copy(alpha = 0.95f),
    tonalElevation = 8.dp,
    shadowElevation = 10.dp,
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.horizontalGradient(
        listOf(AethelNeonPurple.copy(alpha = 0.5f), AethelNeonCyan.copy(alpha = 0.5f))
      )
    )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Linear progress bar along the very top rim
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(2.5.dp),
        color = AethelNeonCyan,
        trackColor = Color.Transparent
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Track Icon with animated visualizer
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              Brush.linearGradient(
                listOf(AethelSurface, Color(0xFF1E1430))
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          if (isPlaying) {
            AnimatedEqualizerMini(modifier = Modifier.size(24.dp))
          } else {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = AethelNeonPink,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and artist
        Column(
          modifier = Modifier.weight(1f)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = track.title,
              color = AethelTextPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            if (track.isUserCustom) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = AethelNeonCyan.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "CUSTOM",
                  color = AethelNeonCyan,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
          Text(
            text = "${track.artist} • ${track.genre}",
            color = AethelTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // "Add Your Own Audio" quick button
        IconButton(
          onClick = onAddOwnAudio,
          modifier = Modifier
            .size(36.dp)
            .background(AethelSurfaceElevated, CircleShape)
            .testTag("add_own_audio_quick_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Personal Audio",
            tint = AethelNeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Play / Pause button
        IconButton(
          onClick = onTogglePlayPause,
          modifier = Modifier
            .size(40.dp)
            .background(AethelPrimary, CircleShape)
            .testTag("mini_player_toggle_btn")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    }
  }
}

@Composable
fun AnimatedEqualizerMini(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
  val b1 by infiniteTransition.animateFloat(
    initialValue = 0.2f, targetValue = 0.9f,
    animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
    label = "b1"
  )
  val b2 by infiniteTransition.animateFloat(
    initialValue = 0.7f, targetValue = 0.3f,
    animationSpec = infiniteRepeatable(tween(580, easing = LinearEasing), RepeatMode.Reverse),
    label = "b2"
  )
  val b3 by infiniteTransition.animateFloat(
    initialValue = 0.3f, targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
    label = "b3"
  )

  Canvas(modifier = modifier) {
    val barWidth = size.width / 5f
    val maxHeight = size.height

    // Bar 1
    val h1 = maxHeight * b1
    drawRoundRect(
      color = AethelNeonPink,
      topLeft = Offset(0f, maxHeight - h1),
      size = Size(barWidth * 0.75f, h1),
      cornerRadius = CornerRadius(4f, 4f)
    )

    // Bar 2
    val h2 = maxHeight * b2
    drawRoundRect(
      color = AethelNeonPurple,
      topLeft = Offset(barWidth * 1.5f, maxHeight - h2),
      size = Size(barWidth * 0.75f, h2),
      cornerRadius = CornerRadius(4f, 4f)
    )

    // Bar 3
    val h3 = maxHeight * b3
    drawRoundRect(
      color = AethelNeonCyan,
      topLeft = Offset(barWidth * 3f, maxHeight - h3),
      size = Size(barWidth * 0.75f, h3),
      cornerRadius = CornerRadius(4f, 4f)
    )
  }
}
