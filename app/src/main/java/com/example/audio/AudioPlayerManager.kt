package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack as AndroidAudioTrack
import android.media.MediaPlayer
import android.net.Uri
import com.example.data.model.AudioTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

enum class EqualizerPreset(val label: String, val bassBoost: Float, val trebleBoost: Float, val spatialReverb: String) {
  ACOUSTIC_NEUTRAL("Neutral Master", 1.0f, 1.0f, "Direct"),
  GALLERY_HALL("Gallery Hall Reverb", 1.2f, 1.4f, "Spatial 3D"),
  DEEP_BASS("Deep Sub-Bass (432Hz)", 1.8f, 0.9f, "Resonant"),
  SPATIAL_AIR("Spatial Air & Harmonics", 0.9f, 1.7f, "Wide Holographic"),
  LOUNGE_WARMTH("Curator Lounge Warmth", 1.4f, 1.1f, "Cozy Warm")
}

class AudioPlayerManager(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private var mediaPlayer: MediaPlayer? = null
  private var syntheticAudioTrack: AndroidAudioTrack? = null
  private var synthesisJob: Job? = null
  private var progressJob: Job? = null

  private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
  val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _playbackProgress = MutableStateFlow(0f)
  val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

  private val _currentPositionSec = MutableStateFlow(0)
  val currentPositionSec: StateFlow<Int> = _currentPositionSec.asStateFlow()

  private val _totalDurationSec = MutableStateFlow(272)
  val totalDurationSec: StateFlow<Int> = _totalDurationSec.asStateFlow()

  private val _equalizerPreset = MutableStateFlow(EqualizerPreset.GALLERY_HALL)
  val equalizerPreset: StateFlow<EqualizerPreset> = _equalizerPreset.asStateFlow()

  private val _volume = MutableStateFlow(0.85f)
  val volume: StateFlow<Float> = _volume.asStateFlow()

  fun playTrack(track: AudioTrack) {
    stopCurrentPlayback()
    _currentTrack.value = track
    _totalDurationSec.value = parseDuration(track.duration)
    _currentPositionSec.value = 0
    _playbackProgress.value = 0f

    if (track.isUserCustom && track.uriString != null) {
      playLocalUri(Uri.parse(track.uriString))
    } else {
      startAmbientToneGenerator()
    }

    _isPlaying.value = true
    startProgressTicker()
  }

  fun togglePlayPause() {
    val track = _currentTrack.value ?: return
    if (_isPlaying.value) {
      pausePlayback()
    } else {
      resumePlayback(track)
    }
  }

  fun seekTo(fraction: Float) {
    val targetSec = (fraction * _totalDurationSec.value).toInt()
    _currentPositionSec.value = targetSec
    _playbackProgress.value = fraction
    mediaPlayer?.let { player ->
      val targetMs = (fraction * player.duration).toInt()
      player.seekTo(targetMs)
    }
  }

  fun setEqualizerPreset(preset: EqualizerPreset) {
    _equalizerPreset.value = preset
  }

  fun setVolume(vol: Float) {
    _volume.value = vol.coerceIn(0f, 1f)
    mediaPlayer?.setVolume(_volume.value, _volume.value)
    syntheticAudioTrack?.setVolume(_volume.value)
  }

  private fun playLocalUri(uri: Uri) {
    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(context, uri)
        prepare()
        setVolume(_volume.value, _volume.value)
        start()
        _totalDurationSec.value = (duration / 1000).coerceAtLeast(1)
        setOnCompletionListener {
          _isPlaying.value = false
          _playbackProgress.value = 1f
        }
      }
    } catch (e: Exception) {
      // Fallback gracefully to ambient sound generator if uri fails
      startAmbientToneGenerator()
    }
  }

  private fun startAmbientToneGenerator() {
    stopSynthesis()
    val sampleRate = 22050
    val minBufferSize = AndroidAudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )

    try {
      val track = AndroidAudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(minBufferSize * 2)
        .setTransferMode(AndroidAudioTrack.MODE_STREAM)
        .build()

      syntheticAudioTrack = track
      track.setVolume(_volume.value * 0.4f)
      track.play()

      synthesisJob = CoroutineScope(Dispatchers.Default).launch {
        val buffer = ShortArray(minBufferSize)
        var phase1 = 0.0
        var phase2 = 0.0
        val baseFreq = 216.0 // Aethel 432Hz ambient sub-harmonic
        val overtoneFreq = 324.0 // Perfect fifth harmonic

        while (isActive) {
          for (i in buffer.indices) {
            val sample1 = sin(phase1)
            val sample2 = sin(phase2) * 0.4
            val sample = ((sample1 + sample2) * 0.3 * Short.MAX_VALUE).toInt().toShort()
            buffer[i] = sample

            phase1 += 2.0 * Math.PI * baseFreq / sampleRate
            phase2 += 2.0 * Math.PI * overtoneFreq / sampleRate
            if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
            if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
          }
          track.write(buffer, 0, buffer.size)
        }
      }
    } catch (e: Exception) {
      // If audio device output not permitted in container, silence is safe
    }
  }

  private fun startProgressTicker() {
    progressJob?.cancel()
    progressJob = coroutineScope.launch {
      while (isActive && _isPlaying.value) {
        delay(1000)
        mediaPlayer?.let { player ->
          if (player.isPlaying) {
            val currentMs = player.currentPosition
            val totalMs = player.duration.coerceAtLeast(1)
            _currentPositionSec.value = currentMs / 1000
            _playbackProgress.value = currentMs.toFloat() / totalMs
          }
        } ?: run {
          // Synthetic ambient track progression
          val nextSec = _currentPositionSec.value + 1
          if (nextSec >= _totalDurationSec.value) {
            _currentPositionSec.value = 0
            _playbackProgress.value = 0f
          } else {
            _currentPositionSec.value = nextSec
            _playbackProgress.value = nextSec.toFloat() / _totalDurationSec.value.coerceAtLeast(1)
          }
        }
      }
    }
  }

  private fun pausePlayback() {
    _isPlaying.value = false
    progressJob?.cancel()
    mediaPlayer?.let {
      if (it.isPlaying) it.pause()
    }
    stopSynthesis()
  }

  private fun resumePlayback(track: AudioTrack) {
    _isPlaying.value = true
    if (track.isUserCustom && track.uriString != null && mediaPlayer != null) {
      mediaPlayer?.start()
    } else {
      startAmbientToneGenerator()
    }
    startProgressTicker()
  }

  private fun stopCurrentPlayback() {
    progressJob?.cancel()
    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
    } catch (e: Exception) {
      // Ignore
    }
    mediaPlayer = null
    stopSynthesis()
  }

  private fun stopSynthesis() {
    synthesisJob?.cancel()
    synthesisJob = null
    try {
      syntheticAudioTrack?.stop()
      syntheticAudioTrack?.release()
    } catch (e: Exception) {
      // Ignore
    }
    syntheticAudioTrack = null
  }

  private fun parseDuration(duration: String): Int {
    val parts = duration.split(":")
    return if (parts.size == 2) {
      val min = parts[0].toIntOrNull() ?: 3
      val sec = parts[1].toIntOrNull() ?: 30
      min * 60 + sec
    } else {
      240
    }
  }

  fun release() {
    stopCurrentPlayback()
    coroutineScope.cancel()
  }
}
