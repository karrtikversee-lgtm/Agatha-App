package com.example.data.repository

import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class ArtRepository {

  private val _artworks = MutableStateFlow<List<Artwork>>(initialArtworks)
  val artworks: StateFlow<List<Artwork>> = _artworks.asStateFlow()

  private val _tracks = MutableStateFlow<List<AudioTrack>>(initialAudioTracks)
  val tracks: StateFlow<List<AudioTrack>> = _tracks.asStateFlow()

  private val _communityPosts = MutableStateFlow<List<CommunityPost>>(initialCommunityPosts)
  val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

  private val _dailyArtworkUploadCount = MutableStateFlow(2) // Initial count for today
  val dailyArtworkUploadCount: StateFlow<Int> = _dailyArtworkUploadCount.asStateFlow()

  val priceHistory = listOf(
    MarketPricePoint("Jan", 142000.0),
    MarketPricePoint("Mar", 151500.0),
    MarketPricePoint("May", 158200.0),
    MarketPricePoint("Jul", 164800.0),
    MarketPricePoint("Sep", 172600.0),
    MarketPricePoint("Oct", 184250.0)
  )

  fun canAddArtwork(tier: CollectorTier): Boolean {
    if (tier == CollectorTier.ARTISAN_VAULT_VIP) return true
    return _dailyArtworkUploadCount.value < TierLimits.FREE_DAILY_ARTWORK_LIMIT
  }

  fun canAddCustomAudio(tier: CollectorTier): Boolean {
    if (tier == CollectorTier.ARTISAN_VAULT_VIP) return true
    val customCount = _tracks.value.count { it.isUserCustom }
    return customCount < TierLimits.FREE_CUSTOM_AUDIO_LIMIT
  }

  fun addArtwork(
    title: String,
    artist: String,
    year: Int,
    medium: String,
    dimensions: String,
    estimatedValue: Double,
    imageUri: String?,
    imageResId: Int?,
    exhibitionRoom: String,
    curatorialStatement: String
  ): Artwork {
    val id = UUID.randomUUID().toString().take(8)
    val hash = generateSha256("$title-$artist-$year-${System.currentTimeMillis()}")
    val blockHeight = 849201L + _artworks.value.size * 37L
    val newArtwork = Artwork(
      id = id,
      title = title,
      artist = artist,
      year = year,
      medium = medium,
      dimensions = dimensions,
      estimatedValue = estimatedValue,
      previousValue = estimatedValue * 0.95,
      imageResId = imageResId,
      imageUri = imageUri,
      exhibitionRoom = exhibitionRoom,
      isPrivate = false,
      isFeatured = true,
      blockchainTokenId = "0x${hash.take(16)}",
      provenanceHash = "0x$hash",
      blockHeight = blockHeight,
      provenanceTrail = listOf(
        ProvenanceEvent(
          year = year.toString(),
          eventType = "Genesis Creation",
          location = "Artist Studio",
          custodian = artist,
          verifiedHash = "0x${hash.take(24)}...001"
        ),
        ProvenanceEvent(
          year = "2026",
          eventType = "Acquisition into Private Vault",
          location = "Geneva FreePort Vault",
          custodian = "Moyo Shiro Collection",
          verifiedHash = "0x$hash"
        )
      ),
      curatorialStatement = curatorialStatement.ifBlank {
        "A standout acquisition embodying aesthetic tension between organic form and technological precision."
      }
    )
    _artworks.value = listOf(newArtwork) + _artworks.value
    _dailyArtworkUploadCount.value = _dailyArtworkUploadCount.value + 1
    return newArtwork
  }

  fun toggleFeatured(id: String) {
    _artworks.value = _artworks.value.map {
      if (it.id == id) it.copy(isFeatured = !it.isFeatured) else it
    }
  }

  fun updateExhibitionRoom(id: String, room: String) {
    _artworks.value = _artworks.value.map {
      if (it.id == id) it.copy(exhibitionRoom = room) else it
    }
  }

  fun togglePrivacy(id: String) {
    _artworks.value = _artworks.value.map {
      if (it.id == id) it.copy(isPrivate = !it.isPrivate) else it
    }
  }

  fun addCustomAudio(title: String, artist: String, uriString: String, duration: String = "Custom File") {
    val newTrack = AudioTrack(
      id = UUID.randomUUID().toString(),
      title = title,
      artist = artist,
      duration = duration,
      isUserCustom = true,
      uriString = uriString,
      genre = "Personal Audio"
    )
    _tracks.value = listOf(newTrack) + _tracks.value
  }

  fun removeTrack(id: String) {
    _tracks.value = _tracks.value.filter { it.id != id }
  }

  fun toggleLikePost(postId: String) {
    _communityPosts.value = _communityPosts.value.map {
      if (it.id == postId) {
        val nextLiked = !it.isLiked
        it.copy(
          isLiked = nextLiked,
          likes = if (nextLiked) it.likes + 1 else it.likes - 1
        )
      } else it
    }
  }

  private fun generateSha256(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(input.toByteArray())
    return digest.fold("") { str, it -> str + "%02x".format(it) }
  }

  companion object {
    private val initialArtworks = listOf(
      Artwork(
        id = "art-01",
        title = "Celestial Harmony: Vortex IX",
        artist = "Eli Vance",
        year = 2024,
        medium = "Digital Fine Art & Algorithmic Geometry",
        dimensions = "180 × 120 cm (Ultra HD 8K Canvas)",
        estimatedValue = 68500.0,
        previousValue = 61200.0,
        imageResId = R.drawable.artwork_celestial_nebula_1793393681481,
        exhibitionRoom = "Lumina Neo-Form",
        isPrivate = false,
        isFeatured = true,
        blockchainTokenId = "0x7F9B1E4A32D6C088",
        provenanceHash = "0x9e8a7c6b5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a",
        blockHeight = 849182L,
        provenanceTrail = listOf(
          ProvenanceEvent(
            year = "2024",
            eventType = "Mint & Signature",
            location = "London, UK",
            custodian = "Eli Vance Studio",
            verifiedHash = "0x9e8a7c6b5d4e3f2a...mint"
          ),
          ProvenanceEvent(
            year = "2025",
            eventType = "Exhibition Loan",
            location = "Palais de Tokyo, Paris",
            custodian = "Fondation Lumina",
            verifiedHash = "0x4b2c1d0e9f8a9e8a...loan"
          ),
          ProvenanceEvent(
            year = "2026",
            eventType = "Primary Vault Allocation",
            location = "Zurich Vaults",
            custodian = "Moyo Shiro",
            verifiedHash = "0x9e8a7c6b5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a"
          )
        ),
        curatorialStatement = "A hypnotic synthesis of sacred geometric tessellations and cosmic vortex dynamics. The luminescent violet epicenter radiates perpetual visual energy."
      ),
      Artwork(
        id = "art-02",
        title = "Kinetic Geometry: Lumina",
        artist = "Aethel Art Collective",
        year = 2024,
        medium = "Kinetic Chrome, Polarized Glass & Neon Luminescence",
        dimensions = "210 × 140 × 90 cm",
        estimatedValue = 92400.0,
        previousValue = 84000.0,
        imageResId = R.drawable.artwork_kinetic_prism_1793393694998,
        exhibitionRoom = "Modernist Kinetic",
        isPrivate = false,
        isFeatured = true,
        blockchainTokenId = "0x4C8A2E1F99D0B752",
        provenanceHash = "0x8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a4f3e2d1c0b9a8f7e",
        blockHeight = 849220L,
        provenanceTrail = listOf(
          ProvenanceEvent(
            year = "2024",
            eventType = "Sculptural Commission",
            location = "Berlin Atelier",
            custodian = "Aethel Collective",
            verifiedHash = "0x8f7e6d5c...comm"
          ),
          ProvenanceEvent(
            year = "2025",
            eventType = "Biennale Spotlight",
            location = "Venice Biennale",
            custodian = "Arsenale Exhibition",
            verifiedHash = "0x3a2f1e0d...bien"
          ),
          ProvenanceEvent(
            year = "2026",
            eventType = "Private Vault Transfer",
            location = "Geneva Sanctuary",
            custodian = "Moyo Shiro",
            verifiedHash = "0x8f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c"
          )
        ),
        curatorialStatement = "A monumental mechanical marvel fusing industrial precision chrome with soft neon refractive optics, exploring temporal distortion in physical space."
      ),
      Artwork(
        id = "art-03",
        title = "Singularity Waveform 00",
        artist = "Moyo Shiro & AI Collaborator",
        year = 2025,
        medium = "Generative Sound & Topographic Waveforms",
        dimensions = "Dynamic Variable (Generative Sound-Sculpture)",
        estimatedValue = 23350.0,
        previousValue = 20800.0,
        imageResId = R.drawable.art_gallery_icon_1793393664700,
        exhibitionRoom = "Deep Vault",
        isPrivate = true,
        isFeatured = false,
        blockchainTokenId = "0x1102AF840BC9441B",
        provenanceHash = "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
        blockHeight = 849312L,
        provenanceTrail = listOf(
          ProvenanceEvent(
            year = "2025",
            eventType = "Autonomous Generative Mint",
            location = "Decentralized Node 04",
            custodian = "Smart Contract #8491",
            verifiedHash = "0x1a2b3c4d...auto"
          ),
          ProvenanceEvent(
            year = "2026",
            eventType = "Encrypted Vault Stash",
            location = "Private Vault",
            custodian = "Moyo Shiro",
            verifiedHash = "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d"
          )
        ),
        curatorialStatement = "Intimate exploration of sonic resonance visual patterns, rendering gravitational waves in pulsating ultraviolet contour fields."
      )
    )

    private val initialAudioTracks = listOf(
      AudioTrack(
        id = "trk-01",
        title = "Obsidian Reverie",
        artist = "Aethel Sound Studio",
        duration = "04:32",
        genre = "Deep Ambient Soundscape"
      ),
      AudioTrack(
        id = "trk-02",
        title = "Spatial Lumina (432Hz)",
        artist = "Kengo Murata",
        duration = "05:14",
        genre = "Minimalist Acoustic Piano"
      ),
      AudioTrack(
        id = "trk-03",
        title = "Gallery Drift: Nocturne",
        artist = "Sylvan Drone Project",
        duration = "06:40",
        genre = "Binaural Lo-Fi Acoustic"
      ),
      AudioTrack(
        id = "trk-04",
        title = "Kinetic Resonance",
        artist = "Aethel Acoustics",
        duration = "03:55",
        genre = "Contemporary Minimalist Drone"
      )
    )

    private val initialCommunityPosts = listOf(
      CommunityPost(
        id = "post-01",
        authorName = "Hélène Laurent",
        authorLocation = "Paris, France",
        content = "The physical presence of kinetic sculptures in dark architecture transforms the contemplation ritual. Here is my curatorial review of Lumina.",
        artworkTitle = "Kinetic Geometry: Lumina",
        timestamp = "2h ago",
        likes = 142,
        commentsCount = 18,
        isLiked = true
      ),
      CommunityPost(
        id = "post-02",
        authorName = "Kenji Takahashi",
        authorLocation = "Tokyo, Japan",
        content = "Verified provenance on chain brings an unprecedented level of peace of mind when curating blue-chip acquisitions. The cryptographic hash is impeccable.",
        artworkTitle = "Celestial Harmony: Vortex IX",
        timestamp = "5h ago",
        likes = 98,
        commentsCount = 12,
        isLiked = false
      ),
      CommunityPost(
        id = "post-03",
        authorName = "Alister Vance",
        authorLocation = "New York, USA",
        content = "Exported the entire 2026 digital catalog as PDF for my insurance underwriters and board members. The valuation breakdown with Sotheby's index alignment is peerless.",
        artworkTitle = "Private Vault Portfolio",
        timestamp = "1d ago",
        likes = 230,
        commentsCount = 34,
        isLiked = true
      )
    )
  }
}
