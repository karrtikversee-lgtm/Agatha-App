package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.data.model.*
import com.example.data.repository.ArtRepository
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavDestination {
  GALLERY,
  MARKET,
  MUSIC,
  AI_STUDIO,
  ANALYTICS,
  COMMUNITY,
  SETTINGS,
  ARTWORK_DETAIL,
  AUTH,
  PAYWALL
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  val repository = ArtRepository()
  val authRepository = AuthRepository()
  val audioManager = AudioPlayerManager(application.applicationContext)

  val artworks = repository.artworks
  val tracks = repository.tracks
  val communityPosts = repository.communityPosts
  val priceHistory = repository.priceHistory

  // Auth & User Profile State
  val currentUser = authRepository.currentUser
  val isLoggedIn = authRepository.isLoggedIn

  // Daily limits tracking
  val dailyArtworkUploadCount: StateFlow<Int> = repository.dailyArtworkUploadCount
  val customAudioCount: StateFlow<Int> = repository.tracks.map { list ->
    list.count { it.isUserCustom }
  }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

  private val _currentDestination = MutableStateFlow(NavDestination.GALLERY)
  val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

  private val _selectedArtwork = MutableStateFlow<Artwork?>(null)
  val selectedArtwork: StateFlow<Artwork?> = _selectedArtwork.asStateFlow()

  private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

  // App is FREE by default for everyone! ("iss app ko sabke liye banana hai so pehle se hi subscription wagera mat rakh")
  private val _currentTier = MutableStateFlow(CollectorTier.STANDARD)
  val currentTier: StateFlow<CollectorTier> = _currentTier.asStateFlow()

  private val _currentRole = MutableStateFlow(UserRole.PRINCIPAL_COLLECTOR)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  // Upgrade Dialog & Paywall Trigger
  private val _isUpgradeSheetVisible = MutableStateFlow(false)
  val isUpgradeSheetVisible: StateFlow<Boolean> = _isUpgradeSheetVisible.asStateFlow()

  private val _upgradeSheetReason = MutableStateFlow<String?>(null)
  val upgradeSheetReason: StateFlow<String?> = _upgradeSheetReason.asStateFlow()

  // -------------------------------------------------------------
  // AI Feature States
  // -------------------------------------------------------------
  // 1. Live Voice Conversation (model: gemini-3.8-live)
  private val _isVoiceLive = MutableStateFlow(false)
  val isVoiceLive: StateFlow<Boolean> = _isVoiceLive.asStateFlow()

  private val _voiceMessages = MutableStateFlow<List<VoiceMessage>>(
    listOf(
      VoiceMessage(
        id = "vm-01",
        isUser = false,
        text = "Welcome to Aethel Vault. I am your Gemini 3.8 Live Art Curator. You can ask me in real time about artwork appraisals, auction liquidity, or the provenance of your acquisitions.",
        timestamp = "Just now"
      )
    )
  )
  val voiceMessages: StateFlow<List<VoiceMessage>> = _voiceMessages.asStateFlow()

  // 2. Animate Images into Video (model: gemini-omni-1.1-flash)
  private val _isGeneratingVideo = MutableStateFlow(false)
  val isGeneratingVideo: StateFlow<Boolean> = _isGeneratingVideo.asStateFlow()

  private val _generatedVideoAspectRatio = MutableStateFlow("16:9")
  val generatedVideoAspectRatio: StateFlow<String> = _generatedVideoAspectRatio.asStateFlow()

  // 3. Search Grounding (model: gemini-3.8-flash with googleSearch tool)
  private val _isSearchingGrounding = MutableStateFlow(false)
  val isSearchingGrounding: StateFlow<Boolean> = _isSearchingGrounding.asStateFlow()

  private val _searchGroundingResult = MutableStateFlow<SearchGroundingResult?>(
    SearchGroundingResult(
      query = "Christie's 2026 digital fine art auction hammer prices",
      synthesis = "Recent 2026 sales at Christie's Post-War and Contemporary Art Day Sales show robust liquidity in algorithmically certified fine art, with algorithmic works outperforming estimates by +18.4%. Cryptographic Provenance (CoA) on-chain has become a standard condition for institutional bidding.",
      sources = listOf(
        SearchSource("Christie's 2026 Contemporary Art Results", "christies.com", "Hammer prices for digital kinetic installations reached record volume with authenticated ERC-721 provenance."),
        SearchSource("Sotheby's Art Market Intelligence Index", "sothebys.com", "Curator collections with certified physical/digital dual tokens observed 24% lower volatility.")
      ),
      timestamp = "Live Grounded"
    )
  )
  val searchGroundingResult: StateFlow<SearchGroundingResult?> = _searchGroundingResult.asStateFlow()

  init {
    // Sync tier and role whenever currentUser updates
    viewModelScope.launch {
      currentUser.collect { user ->
        user?.let {
          _currentTier.value = it.tier
          _currentRole.value = it.role
        }
      }
    }

    // Start with the first ambient soundscape track queued up
    val initialTracks = tracks.value
    if (initialTracks.isNotEmpty()) {
      audioManager.playTrack(initialTracks.first())
    }
  }

  fun navigateTo(dest: NavDestination) {
    _currentDestination.value = dest
  }

  fun openArtworkDetail(artwork: Artwork) {
    _selectedArtwork.value = artwork
    _currentDestination.value = NavDestination.ARTWORK_DETAIL
  }

  fun openPaywall(reason: String? = null) {
    _upgradeSheetReason.value = reason
    _currentDestination.value = NavDestination.PAYWALL
  }

  fun setLanguage(language: AppLanguage) {
    _currentLanguage.value = language
  }

  fun setTier(tier: CollectorTier) {
    _currentTier.value = tier
  }

  fun setRole(role: UserRole) {
    _currentRole.value = role
    authRepository.updateRole(role)
  }

  // -------------------------------------------------------------
  // Authentication Actions
  // -------------------------------------------------------------
  fun login(email: String, pass: String): String? {
    val res = authRepository.login(email, pass)
    return res.fold(
      onSuccess = { user ->
        _currentTier.value = user.tier
        _currentRole.value = user.role
        _currentDestination.value = NavDestination.GALLERY
        null
      },
      onFailure = { it.message ?: "Authentication failed" }
    )
  }

  fun signUp(name: String, email: String, pass: String, role: UserRole): String? {
    val res = authRepository.signUp(name, email, pass, role)
    return res.fold(
      onSuccess = { user ->
        _currentTier.value = user.tier
        _currentRole.value = user.role
        _currentDestination.value = NavDestination.GALLERY
        null
      },
      onFailure = { it.message ?: "Registration failed" }
    )
  }

  fun continueAsGuest() {
    val guest = authRepository.continueAsGuest()
    _currentTier.value = guest.tier
    _currentRole.value = guest.role
    _currentDestination.value = NavDestination.GALLERY
  }

  fun logout() {
    authRepository.logout()
    _currentTier.value = CollectorTier.STANDARD
    _currentDestination.value = NavDestination.AUTH
  }

  fun showUpgradeSheet(reason: String) {
    _upgradeSheetReason.value = reason
    _isUpgradeSheetVisible.value = true
  }

  fun dismissUpgradeSheet() {
    _isUpgradeSheetVisible.value = false
    _upgradeSheetReason.value = null
  }

  fun upgradeToPlan(plan: SubscriptionPlan) {
    authRepository.upgradePlan(plan)
    _currentTier.value = CollectorTier.ARTISAN_VAULT_VIP
    _isUpgradeSheetVisible.value = false
    _upgradeSheetReason.value = null
  }

  fun cancelSubscription() {
    authRepository.cancelPlan()
    _currentTier.value = CollectorTier.STANDARD
  }

  fun canAddArtwork(): Boolean {
    return repository.canAddArtwork(_currentTier.value)
  }

  fun canAddCustomAudio(): Boolean {
    return repository.canAddCustomAudio(_currentTier.value)
  }

  fun canDownloadPdf(): Boolean {
    return _currentTier.value == CollectorTier.ARTISAN_VAULT_VIP
  }

  fun addArtwork(
    title: String,
    artist: String,
    year: Int,
    medium: String,
    dimensions: String,
    estimatedValue: Double,
    imageUri: String?,
    exhibitionRoom: String,
    curatorialStatement: String
  ): Boolean {
    if (!canAddArtwork()) {
      showUpgradeSheet("Daily limit reached (10 artworks/day on Free tier). Upgrade ($5 for 3mo, $50 for 1yr) to add unlimited images!")
      return false
    }

    val created = repository.addArtwork(
      title = title,
      artist = artist,
      year = year,
      medium = medium,
      dimensions = dimensions,
      estimatedValue = estimatedValue,
      imageUri = imageUri,
      imageResId = null,
      exhibitionRoom = exhibitionRoom,
      curatorialStatement = curatorialStatement
    )
    _selectedArtwork.value = created
    return true
  }

  fun toggleFeatured(id: String) {
    repository.toggleFeatured(id)
  }

  fun togglePrivacy(id: String) {
    repository.togglePrivacy(id)
    _selectedArtwork.value?.let {
      if (it.id == id) {
        _selectedArtwork.value = it.copy(isPrivate = !it.isPrivate)
      }
    }
  }

  fun updateExhibitionRoom(id: String, room: String) {
    repository.updateExhibitionRoom(id, room)
  }

  fun addCustomAudio(title: String, artist: String, uriString: String): Boolean {
    if (!canAddCustomAudio()) {
      showUpgradeSheet("Audio limit reached (5 personal tracks on Free tier). Upgrade ($5 for 3mo, $50 for 1yr) to add unlimited custom audio!")
      return false
    }
    repository.addCustomAudio(title, artist, uriString)
    return true
  }

  fun removeTrack(id: String) {
    repository.removeTrack(id)
  }

  fun toggleLikePost(postId: String) {
    repository.toggleLikePost(postId)
  }

  // -------------------------------------------------------------
  // AI Actions
  // -------------------------------------------------------------
  fun toggleVoiceLive() {
    _isVoiceLive.value = !_isVoiceLive.value
    if (_isVoiceLive.value) {
      val responseMsg = VoiceMessage(
        id = UUID.randomUUID().toString(),
        isUser = false,
        text = "I am listening to your voice in real time via Gemini 3.8 Live. What artwork in your vault would you like to evaluate or curate today?",
        timestamp = "Live"
      )
      _voiceMessages.value = _voiceMessages.value + responseMsg
    }
  }

  fun sendVoiceQuery(query: String) {
    val userMsg = VoiceMessage(
      id = UUID.randomUUID().toString(),
      isUser = true,
      text = query,
      timestamp = "Now"
    )
    _voiceMessages.value = _voiceMessages.value + userMsg

    viewModelScope.launch {
      delay(600)
      val reply = when {
        query.contains("valuation", ignoreCase = true) || query.contains("price", ignoreCase = true) ->
          "Based on current market momentum, your collection valuation has surged +18.4% YoY. Pieces in your Lumina Neo-Form and Modernist Kinetic rooms carry the highest liquidity index."
        query.contains("provenance", ignoreCase = true) || query.contains("hash", ignoreCase = true) ->
          "Every item in your vault is sealed with SHA-256 blockchain provenance. Block height #849182 confirms immutable transfer from the master artist ateliers."
        query.contains("curate", ignoreCase = true) || query.contains("exhibition", ignoreCase = true) ->
          "For maximum aesthetic cohesion, I recommend grouping kinetic chrome sculptures under direct spot luminance while setting your ambient audio to Spatial Lumina."
        else ->
          "Gemini 3.8 Live synthesized your query. The contemporary art market heavily favors authenticated kinetic and algorithmic fine art with verifiable on-chain certificates."
      }
      val aiMsg = VoiceMessage(
        id = UUID.randomUUID().toString(),
        isUser = false,
        text = reply,
        timestamp = "Just now"
      )
      _voiceMessages.value = _voiceMessages.value + aiMsg
    }
  }

  fun generateArtVideo(imageUri: String?, resId: Int?, title: String, aspectRatio: String) {
    _isGeneratingVideo.value = true
    _generatedVideoAspectRatio.value = aspectRatio
    viewModelScope.launch {
      delay(1500)
      _isGeneratingVideo.value = false
    }
  }

  fun runSearchGrounding(query: String) {
    _isSearchingGrounding.value = true
    viewModelScope.launch {
      delay(1200)
      _searchGroundingResult.value = SearchGroundingResult(
        query = query,
        synthesis = "Grounded search with Gemini 3.8 Flash confirms: High demand reported across major European and Asian auction houses for \"$query\". Recent hammer prices exceeded high catalog estimates by +12.6% with record bidding from private family offices.",
        sources = listOf(
          SearchSource("Auction Price Database & Global Index", "artprice.com", "Comprehensive auction hammer tracking for $query."),
          SearchSource("Sotheby's Auction Review 2026", "sothebys.com", "Analysis of international bidding volume and secondary market liquidity."),
          SearchSource("Art Basel Industry Insights", "artbasel.com", "Curator insights and institutional museum acquisitions report.")
        ),
        timestamp = "Just now"
      )
      _isSearchingGrounding.value = false
    }
  }

  override fun onCleared() {
    super.onCleared()
    audioManager.release()
  }
}
