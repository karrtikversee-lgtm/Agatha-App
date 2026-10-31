package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerManager
import com.example.audio.EqualizerPreset
import com.example.data.model.AudioTrack
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.theme.*

@Composable
fun MusicScreen(
  audioManager: AudioPlayerManager,
  tracks: List<AudioTrack>,
  currentTier: com.example.data.model.CollectorTier,
  customAudioCount: Int,
  onAddCustomAudio: (title: String, artist: String, uriString: String) -> Unit,
  onRemoveTrack: (String) -> Unit,
  onShowUpgradeSheet: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTrack by audioManager.currentTrack.collectAsState()
  val isPlaying by audioManager.isPlaying.collectAsState()
  val progress by audioManager.playbackProgress.collectAsState()
  val currentPositionSec by audioManager.currentPositionSec.collectAsState()
  val totalDurationSec by audioManager.totalDurationSec.collectAsState()
  val currentPreset by audioManager.equalizerPreset.collectAsState()
  val volume by audioManager.volume.collectAsState()

  val context = LocalContext.current

  // System Audio Picker for "Add your own audio"
  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val displayName = it.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
        ?: "Personal Audio"
      onAddCustomAudio(displayName, "My Library", it.toString())
    }
  }

  fun formatTime(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
  }

  Scaffold(
    modifier = modifier.testTag("music_screen"),
    containerColor = AethelBackground
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
      // Header & Offline Status
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AMBIENT ACOUSTIC LOUNGE",
              color = AethelTextMuted,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
            Text(
              text = "Gallery Soundscapes",
              color = Color.White,
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0D251D),
            border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(modifier = Modifier.size(7.dp).background(AethelSuccessGreen, CircleShape))
              Text(
                text = "OFFLINE READY",
                color = AethelSuccessGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Add Your Own Audio Primary Banner
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceHighlight),
          border = BorderStroke(1.dp, AethelNeonCyan.copy(alpha = 0.45f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_own_audio_banner")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .background(
                    Brush.radialGradient(listOf(AethelNeonCyan.copy(alpha = 0.4f), Color.Transparent)),
                    CircleShape
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudUpload,
                  contentDescription = null,
                  tint = AethelNeonCyan,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Add Your Own Audio",
                  color = Color.White,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (currentTier == com.example.data.model.CollectorTier.STANDARD)
                    "Import personal music ($customAudioCount/5 Free Tracks)"
                  else
                    "Import personal music (VIP Unlimited)",
                  color = AethelTextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            Button(
              onClick = {
                if (currentTier == com.example.data.model.CollectorTier.STANDARD && customAudioCount >= 5) {
                  onShowUpgradeSheet("You reached your Free limit of 5 personal audio tracks. Upgrade to Premium ($5/mo, $20/3mo, $50/1yr) for unlimited personal music!")
                } else {
                  audioPickerLauncher.launch("audio/*")
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary),
              modifier = Modifier.testTag("add_audio_file_btn")
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Audio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }

      // Now Playing Hero Player
      item {
        val track = currentTrack ?: tracks.firstOrNull()
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurface),
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Live animated wave visualizer
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
            ) {
              ContourWaveCanvas(
                modifier = Modifier.fillMaxSize(),
                lineColor = if (isPlaying) AethelNeonPink else AethelNeonCyan.copy(alpha = 0.4f),
                strokeCount = 14,
                isAnimated = isPlaying
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
              text = track?.title ?: "Select a Soundscape",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Text(
              text = "${track?.artist ?: "Aethel Acoustics"} • ${track?.genre ?: "Ambient"}",
              color = AethelNeonCyan,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Seekbar
            Slider(
              value = progress,
              onValueChange = { audioManager.seekTo(it) },
              colors = SliderDefaults.colors(
                thumbColor = AethelNeonCyan,
                activeTrackColor = AethelNeonCyan,
                inactiveTrackColor = AethelBorder
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = formatTime(currentPositionSec),
                color = AethelTextMuted,
                fontSize = 11.sp
              )
              Text(
                text = formatTime(totalDurationSec),
                color = AethelTextMuted,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback controls row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  val currentIndex = tracks.indexOf(currentTrack)
                  val prevIndex = if (currentIndex > 0) currentIndex - 1 else tracks.size - 1
                  if (tracks.isNotEmpty()) audioManager.playTrack(tracks[prevIndex])
                },
                modifier = Modifier.size(44.dp)
              ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
              }

              Spacer(modifier = Modifier.width(18.dp))

              IconButton(
                onClick = {
                  if (currentTrack == null && tracks.isNotEmpty()) {
                    audioManager.playTrack(tracks.first())
                  } else {
                    audioManager.togglePlayPause()
                  }
                },
                modifier = Modifier
                  .size(60.dp)
                  .background(AethelPrimary, CircleShape)
                  .testTag("main_play_pause_btn")
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPlaying) "Pause" else "Play",
                  tint = Color.White,
                  modifier = Modifier.size(32.dp)
                )
              }

              Spacer(modifier = Modifier.width(18.dp))

              IconButton(
                onClick = {
                  val currentIndex = tracks.indexOf(currentTrack)
                  val nextIndex = if (currentIndex < tracks.size - 1) currentIndex + 1 else 0
                  if (tracks.isNotEmpty()) audioManager.playTrack(tracks[nextIndex])
                },
                modifier = Modifier.size(44.dp)
              ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume control
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.VolumeDown, contentDescription = null, tint = AethelTextMuted, modifier = Modifier.size(18.dp))
              Slider(
                value = volume,
                onValueChange = { audioManager.setVolume(it) },
                colors = SliderDefaults.colors(
                  thumbColor = Color.White,
                  activeTrackColor = AethelPrimary,
                  inactiveTrackColor = AethelBorder
                ),
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
              )
              Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AethelTextMuted, modifier = Modifier.size(18.dp))
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Equalizer Presets
      item {
        Text(
          text = "CUSTOMIZABLE ACOUSTIC EQUALIZER",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          EqualizerPreset.values().forEach { preset ->
            val isSelected = currentPreset == preset
            Card(
              modifier = Modifier
                .clickable { audioManager.setEqualizerPreset(preset) }
                .testTag("eq_preset_${preset.name}"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) AethelSurfaceHighlight else AethelSurfaceElevated
              ),
              border = BorderStroke(
                1.dp,
                if (isSelected) AethelNeonCyan else AethelBorder
              )
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = preset.label,
                  color = if (isSelected) Color.White else AethelTextSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = preset.spatialReverb,
                  color = AethelNeonCyan,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Playlist Tracks Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "GALLERY PLAYLIST (${tracks.size})",
            color = AethelTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "HIGH-FIDELITY FLAC/PCM",
            color = AethelNeonCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      items(tracks, key = { it.id }) { track ->
        val isCurrent = currentTrack?.id == track.id
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { audioManager.playTrack(track) }
            .testTag("track_item_${track.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) AethelSurfaceHighlight else AethelSurfaceElevated
          ),
          border = BorderStroke(
            1.dp,
            if (isCurrent) AethelNeonCyan.copy(alpha = 0.6f) else AethelBorder
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .background(
                    if (isCurrent) AethelNeonCyan.copy(alpha = 0.2f) else AethelBackground,
                    RoundedCornerShape(10.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isCurrent && isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                  contentDescription = null,
                  tint = if (isCurrent) AethelNeonCyan else AethelTextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = track.title,
                    color = if (isCurrent) Color.White else AethelTextPrimary,
                    fontSize = 14.sp,
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
                        text = "LOCAL",
                        color = AethelNeonCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "${track.artist} • ${track.duration}",
                  color = AethelTextSecondary,
                  fontSize = 12.sp
                )
              }
            }

            if (track.isUserCustom) {
              IconButton(onClick = { onRemoveTrack(track.id) }) {
                Icon(
                  imageVector = Icons.Default.DeleteOutline,
                  contentDescription = "Remove Track",
                  tint = AethelTextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            } else {
              IconButton(onClick = { audioManager.playTrack(track) }) {
                Icon(
                  imageVector = if (isCurrent && isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                  contentDescription = "Play Track",
                  tint = if (isCurrent) AethelNeonCyan else Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(100.dp))
      }
    }
  }
}
