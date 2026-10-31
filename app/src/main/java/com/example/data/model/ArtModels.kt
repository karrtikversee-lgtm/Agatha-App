package com.example.data.model

import android.net.Uri

data class Artwork(
  val id: String,
  val title: String,
  val artist: String,
  val year: Int,
  val medium: String,
  val dimensions: String,
  val estimatedValue: Double,
  val previousValue: Double,
  val imageResId: Int? = null,
  val imageUri: String? = null,
  val exhibitionRoom: String,
  val isPrivate: Boolean = false,
  val isFeatured: Boolean = false,
  val blockchainTokenId: String,
  val provenanceHash: String,
  val blockHeight: Long,
  val provenanceTrail: List<ProvenanceEvent>,
  val curatorialStatement: String
)

data class ProvenanceEvent(
  val year: String,
  val eventType: String,
  val location: String,
  val custodian: String,
  val verifiedHash: String
)

data class ExhibitionRoom(
  val id: String,
  val name: String,
  val description: String,
  val artworkCount: Int
)

data class MarketPricePoint(
  val label: String,
  val value: Double
)

data class CollectorProfile(
  val name: String,
  val title: String,
  val avatarUrl: String? = null,
  val totalValuation: Double,
  val tier: CollectorTier,
  val role: UserRole,
  val bio: String
)

enum class CollectorTier {
  STANDARD, // Free for everyone by default with restrictions!
  ARTISAN_VAULT_VIP // Premium unlocked access!
}

// User requested exact plans: $5 for 3 months and $50 for 1 year
enum class SubscriptionPlan(
  val title: String,
  val price: String,
  val durationLabel: String,
  val perMonthEquiv: String,
  val discountBadge: String? = null,
  val durationMonths: Int
) {
  QUARTERLY("3 Months Access", "$5", "for 3 months", "$1.67/mo", "MOST POPULAR", 3),
  ANNUAL("1 Year Complete VIP", "$50", "for 1 year", "$4.17/mo", "BEST VALUE", 12)
}

object TierLimits {
  const val FREE_DAILY_ARTWORK_LIMIT = 10
  const val FREE_CUSTOM_AUDIO_LIMIT = 5
}

enum class UserRole {
  PRINCIPAL_COLLECTOR,
  ASSOCIATE_CURATOR,
  GUEST_APPRAISER
}

data class UserAccount(
  val id: String,
  val name: String,
  val email: String,
  val role: UserRole,
  val tier: CollectorTier,
  val passwordHash: String,
  val activePlan: SubscriptionPlan? = null,
  val subscriptionExpiresAt: String? = null
)

data class AudioTrack(
  val id: String,
  val title: String,
  val artist: String,
  val duration: String,
  val isUserCustom: Boolean = false,
  val uriString: String? = null,
  val genre: String = "Ambient Gallery"
)

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
  ENGLISH("en", "English", "🇺🇸"),
  FRENCH("fr", "Français", "🇫🇷"),
  JAPANESE("ja", "日本語", "🇯🇵"),
  GERMAN("de", "Deutsch", "🇩🇪"),
  SPANISH("es", "Español", "🇪🇸"),
  ITALIAN("it", "Italiano", "🇮🇹")
}

data class CommunityPost(
  val id: String,
  val authorName: String,
  val authorLocation: String,
  val content: String,
  val artworkTitle: String,
  val timestamp: String,
  var likes: Int,
  val commentsCount: Int,
  var isLiked: Boolean = false
)

// AI Features Data Models
data class VoiceMessage(
  val id: String,
  val isUser: Boolean,
  val text: String,
  val timestamp: String
)

data class AnimateVideoRequest(
  val imageUri: String?,
  val imageResId: Int?,
  val artworkTitle: String,
  val aspectRatio: String // "16:9" or "9:16"
)

data class SearchGroundingResult(
  val query: String,
  val synthesis: String,
  val sources: List<SearchSource>,
  val timestamp: String
)

data class SearchSource(
  val title: String,
  val domain: String,
  val snippet: String
)
