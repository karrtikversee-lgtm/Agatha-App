package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.CollectorTier
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun ProfileSettingsScreen(
  currentUser: com.example.data.model.UserAccount?,
  currentLanguage: AppLanguage,
  currentTier: CollectorTier,
  currentRole: UserRole,
  onLanguageChange: (AppLanguage) -> Unit,
  onTierChange: (CollectorTier) -> Unit,
  onRoleChange: (UserRole) -> Unit,
  onOpenPaywall: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var biometricLockEnabled by remember { mutableStateOf(true) }
  var pushNotificationsEnabled by remember { mutableStateOf(true) }
  var showVipUpgradeDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showRoleDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.testTag("profile_settings_screen"),
    containerColor = AethelBackground
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
      item {
        Text(
          text = "VAULT GOVERNANCE & PREFERENCES",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "Collector Profile & Settings",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Profile Header Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val displayName = currentUser?.name ?: "Moyo Shiro"
            val displayEmail = currentUser?.email ?: "moyo@aethelvault.art"
            val initials = displayName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

            Box(
              modifier = Modifier
                .size(62.dp)
                .background(
                  Brush.linearGradient(listOf(AethelNeonPink, AethelPrimary)),
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (initials.isNotEmpty()) initials else "MS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = displayName,
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
                if (currentTier == CollectorTier.ARTISAN_VAULT_VIP) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AethelAmberGold.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.6f))
                  ) {
                    Text(
                      text = "VIP",
                      color = AethelAmberGold,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.ExtraBold,
                      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                  }
                }
              }

              Text(
                text = displayEmail,
                color = AethelTextMuted,
                fontSize = 11.sp
              )
              Text(
                text = when (currentRole) {
                  UserRole.PRINCIPAL_COLLECTOR -> "Principal Collector (Admin Access)"
                  UserRole.ASSOCIATE_CURATOR -> "Associate Curator (Editor)"
                  UserRole.GUEST_APPRAISER -> "Guest Appraiser (Read-Only)"
                },
                color = AethelNeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
      }

      // Premium Membership VIP Banner
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceHighlight),
          border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.6f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("vip_banner_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "MEMBERSHIP PLANS",
                  color = AethelAmberGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp
                )
                Text(
                  text = if (currentTier == CollectorTier.ARTISAN_VAULT_VIP) "Artisan Vault VIP (Active)" else "Standard Plan (Free for All)",
                  color = Color.White,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AethelAmberGold, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (currentTier == CollectorTier.ARTISAN_VAULT_VIP)
                "VIP active: Unlimited daily image acquisitions (>10/day), unlimited personal music (>5 tracks), and high-res PDF Analytics downloads."
              else
                "Free tier includes 10 daily artworks & 5 custom audio tracks. Upgrade to VIP ($5/mo, $20/3mo, $50/1yr) to download analytics in PDF and get unlimited uploads.",
              color = AethelTextSecondary,
              fontSize = 12.sp,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = onOpenPaywall,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AethelAmberGold),
                modifier = Modifier
                  .weight(1f)
                  .testTag("open_paywall_screen_btn")
              ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "View Paywall Screen",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              if (currentTier == CollectorTier.ARTISAN_VAULT_VIP) {
                OutlinedButton(
                  onClick = {
                    onTierChange(CollectorTier.STANDARD)
                    Toast.makeText(context, "Switched to Free Standard Tier", Toast.LENGTH_SHORT).show()
                  },
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, AethelBorder)
                ) {
                  Text("Downgrade to Free", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Multi-Language & Interface
      item {
        Text(
          text = "LOCALIZATION & ACCESSIBILITY",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Language selector row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true }
                .testTag("change_language_row"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = AethelNeonCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Interface Language", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text("Global localized options", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
              Text(
                text = "${currentLanguage.flag} ${currentLanguage.displayName}",
                color = AethelNeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }

            HorizontalDivider(color = AethelBorder)

            // Dark Mode Indicator
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = AethelNeonPurple, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Ultra-Dark Obsidian Theme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text("Active (Optimized for OLED & eye comfort)", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
              Icon(Icons.Default.Check, contentDescription = "Active", tint = AethelSuccessGreen)
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Security & Role-Based Access Control
      item {
        Text(
          text = "ROLE-BASED ACCESS & DATA SECURITY",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Role switcher row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showRoleDialog = true }
                .testTag("change_role_row"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AethelPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Assigned Role", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text("Access permissions & encryption scope", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
              Text(
                text = currentRole.name.replace("_", " "),
                color = AethelPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }

            HorizontalDivider(color = AethelBorder)

            // Biometric Vault Lock
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AethelSuccessGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Biometric / Cryptographic Lock", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text("Protect private acquisitions with biometrics", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
              Switch(
                checked = biometricLockEnabled,
                onCheckedChange = {
                  biometricLockEnabled = it
                  Toast.makeText(context, if (it) "Biometric Lock Active" else "Biometric Lock Disabled", Toast.LENGTH_SHORT).show()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = AethelSuccessGreen
                )
              )
            }

            HorizontalDivider(color = AethelBorder)

            // Push Notifications for Real-Time Price Alerts
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = AethelNeonPink, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Push Notifications", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text("Real-time price alerts & auction drops", color = AethelTextSecondary, fontSize = 11.sp)
                }
              }
              Switch(
                checked = pushNotificationsEnabled,
                onCheckedChange = {
                  pushNotificationsEnabled = it
                  Toast.makeText(context, if (it) "Push Alerts Enabled" else "Push Alerts Paused", Toast.LENGTH_SHORT).show()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = AethelNeonPink
                )
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = onLogout,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("profile_logout_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B161B)),
          border = BorderStroke(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.5f))
        ) {
          Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign Out of Vault", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(100.dp))
      }
    }
  }

  // Language Picker Modal
  if (showLanguageDialog) {
    Dialog(onDismissRequest = { showLanguageDialog = false }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = AethelSurfaceElevated,
        border = BorderStroke(1.dp, AethelBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text("Select Language", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(14.dp))
          AppLanguage.values().forEach { lang ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  onLanguageChange(lang)
                  showLanguageDialog = false
                  Toast.makeText(context, "Language set to ${lang.displayName}", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 12.dp, horizontal = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("${lang.flag}  ${lang.displayName}", color = Color.White, fontSize = 15.sp)
              if (currentLanguage == lang) {
                Icon(Icons.Default.Check, contentDescription = null, tint = AethelNeonCyan)
              }
            }
          }
        }
      }
    }
  }

  // Role Picker Modal
  if (showRoleDialog) {
    Dialog(onDismissRequest = { showRoleDialog = false }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = AethelSurfaceElevated,
        border = BorderStroke(1.dp, AethelBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text("Role-Based Access Control", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Configure access tier for this session", color = AethelTextSecondary, fontSize = 12.sp)
          Spacer(modifier = Modifier.height(14.dp))
          UserRole.values().forEach { role ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  onRoleChange(role)
                  showRoleDialog = false
                  Toast.makeText(context, "Role switched to ${role.name}", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 12.dp, horizontal = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = role.name.replace("_", " "),
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = when (role) {
                    UserRole.PRINCIPAL_COLLECTOR -> "Full administrative ownership & export privileges"
                    UserRole.ASSOCIATE_CURATOR -> "Curate exhibitions & manage artwork records"
                    UserRole.GUEST_APPRAISER -> "Read-only access to valuations and certificates"
                  },
                  color = AethelTextMuted,
                  fontSize = 11.sp
                )
              }
              if (currentRole == role) {
                Icon(Icons.Default.Check, contentDescription = null, tint = AethelPrimary)
              }
            }
          }
        }
      }
    }
  }

  // VIP Upgrade Subscription Modal
  if (showVipUpgradeDialog) {
    com.example.ui.components.PremiumUpgradeSheet(
      onDismiss = { showVipUpgradeDialog = false },
      onUpgrade = { plan ->
        onTierChange(CollectorTier.ARTISAN_VAULT_VIP)
        showVipUpgradeDialog = false
        Toast.makeText(context, "Upgraded to VIP via ${plan.title} (${plan.price})!", Toast.LENGTH_LONG).show()
      }
    )
  }
}
