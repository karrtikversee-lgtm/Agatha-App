package com.example.data.repository

import com.example.data.model.CollectorTier
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class AuthRepository {

  private val userDatabase = mutableMapOf<String, UserAccount>()

  private val _currentUser = MutableStateFlow<UserAccount?>(null)
  val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  init {
    // Seed default accounts
    val defaultPasswordHash = hashPassword("collector2026")

    val freeUser = UserAccount(
      id = "usr-moyo-01",
      name = "Moyo Shiro",
      email = "moyo@aethelvault.art",
      role = UserRole.PRINCIPAL_COLLECTOR,
      tier = CollectorTier.STANDARD,
      passwordHash = defaultPasswordHash
    )

    val vipUser = UserAccount(
      id = "usr-vip-02",
      name = "Elena Rostova",
      email = "vip@aethelvault.art",
      role = UserRole.PRINCIPAL_COLLECTOR,
      tier = CollectorTier.ARTISAN_VAULT_VIP,
      passwordHash = defaultPasswordHash,
      activePlan = SubscriptionPlan.ANNUAL,
      subscriptionExpiresAt = "October 2027"
    )

    userDatabase[freeUser.email.lowercase()] = freeUser
    userDatabase[vipUser.email.lowercase()] = vipUser

    // Automatically log in as default free user for seamless immediate exploration
    _currentUser.value = freeUser
    _isLoggedIn.value = true
  }

  fun login(email: String, password: String): Result<UserAccount> {
    val cleanEmail = email.trim().lowercase()
    val account = userDatabase[cleanEmail] ?: return Result.failure(Exception("No account found with this email"))

    val hash = hashPassword(password)
    if (account.passwordHash != hash) {
      return Result.failure(Exception("Incorrect password. Please verify credentials."))
    }

    _currentUser.value = account
    _isLoggedIn.value = true
    return Result.success(account)
  }

  fun signUp(name: String, email: String, password: String, role: UserRole): Result<UserAccount> {
    val cleanEmail = email.trim().lowercase()
    val cleanName = name.trim()

    if (cleanName.isBlank()) {
      return Result.failure(Exception("Please enter your name or curator pseudonym."))
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
      return Result.failure(Exception("Please enter a valid email address."))
    }
    if (password.length < 6) {
      return Result.failure(Exception("Password must be at least 6 characters."))
    }
    if (userDatabase.containsKey(cleanEmail)) {
      return Result.failure(Exception("An account already exists with this email."))
    }

    val newAccount = UserAccount(
      id = "usr-" + UUID.randomUUID().toString().take(8),
      name = cleanName,
      email = cleanEmail,
      role = role,
      tier = CollectorTier.STANDARD, // Starts on Free tier with limits as requested!
      passwordHash = hashPassword(password)
    )

    userDatabase[cleanEmail] = newAccount
    _currentUser.value = newAccount
    _isLoggedIn.value = true
    return Result.success(newAccount)
  }

  fun continueAsGuest(): UserAccount {
    val guestAccount = UserAccount(
      id = "usr-guest",
      name = "Guest Collector",
      email = "guest@aethelvault.art",
      role = UserRole.GUEST_APPRAISER,
      tier = CollectorTier.STANDARD,
      passwordHash = ""
    )
    _currentUser.value = guestAccount
    _isLoggedIn.value = true
    return guestAccount
  }

  fun logout() {
    _currentUser.value = null
    _isLoggedIn.value = false
  }

  fun upgradePlan(plan: SubscriptionPlan) {
    val user = _currentUser.value ?: return
    val updated = user.copy(
      tier = CollectorTier.ARTISAN_VAULT_VIP,
      activePlan = plan,
      subscriptionExpiresAt = if (plan == SubscriptionPlan.QUARTERLY) "3 Months Active" else "1 Year Active"
    )
    userDatabase[user.email.lowercase()] = updated
    _currentUser.value = updated
  }

  fun cancelPlan() {
    val user = _currentUser.value ?: return
    val updated = user.copy(
      tier = CollectorTier.STANDARD,
      activePlan = null,
      subscriptionExpiresAt = null
    )
    userDatabase[user.email.lowercase()] = updated
    _currentUser.value = updated
  }

  fun updateRole(newRole: UserRole) {
    val user = _currentUser.value ?: return
    val updated = user.copy(role = newRole)
    userDatabase[user.email.lowercase()] = updated
    _currentUser.value = updated
  }

  private fun hashPassword(password: String): String {
    val salt = "aethel_vault_security_salt_2026"
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest((password + salt).toByteArray())
    return bytes.fold("") { str, it -> str + "%02x".format(it) }
  }
}
