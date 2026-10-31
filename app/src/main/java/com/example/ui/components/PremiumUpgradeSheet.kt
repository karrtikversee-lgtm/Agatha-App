package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubscriptionPlan
import com.example.ui.theme.*

@Composable
fun PremiumUpgradeSheet(
  initialReason: String? = null,
  onDismiss: () -> Unit,
  onUpgrade: (SubscriptionPlan) -> Unit
) {
  var selectedPlan by remember { mutableStateOf(SubscriptionPlan.QUARTERLY) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = Color(0xFF0F1118),
      border = BorderStroke(1.5.dp, AethelAmberGold.copy(alpha = 0.7f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("premium_upgrade_modal")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Close Button & Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AethelAmberGold.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AethelAmberGold, modifier = Modifier.size(14.dp))
              Text("ARTISAN VAULT VIP", color = AethelAmberGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Unlock Premium Access",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )

        if (initialReason != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = initialReason,
            color = AethelNeonPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Feature Perks List
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PerkRow(
              icon = Icons.Default.PictureAsPdf,
              title = "Download Analytics in PDF",
              subtitle = "Unlimited formal audit & executive PDF catalog reports"
            )
            PerkRow(
              icon = Icons.Default.AddPhotoAlternate,
              title = "More than 10 Artworks / Day",
              subtitle = "Free is limited to 10/day; Premium gets unlimited acquisitions"
            )
            PerkRow(
              icon = Icons.Default.LibraryMusic,
              title = "More than 5 Personal Music Tracks",
              subtitle = "Free is limited to 5 tracks; Premium gets unlimited audio"
            )
            PerkRow(
              icon = Icons.Default.AutoAwesome,
              title = "Gemini Live Audio & Video Motion Studio",
              subtitle = "Live voice conversation & animate artworks into video (16:9 / 9:16)"
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "CHOOSE YOUR PLAN",
          color = AethelTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Subscription Plans: $5/month, $20 for 3 months, $50 for 1 year
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SubscriptionPlan.values().forEach { plan ->
            val isSelected = selectedPlan == plan
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { selectedPlan = plan }
                .testTag("plan_option_${plan.name}"),
              color = if (isSelected) AethelSurfaceHighlight else AethelSurface,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) AethelAmberGold else AethelBorder
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { selectedPlan = plan },
                    colors = RadioButtonDefaults.colors(
                      selectedColor = AethelAmberGold,
                      unselectedColor = AethelTextMuted
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = plan.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                      )
                      plan.discountBadge?.let { badge ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                          shape = RoundedCornerShape(4.dp),
                          color = AethelNeonCyan.copy(alpha = 0.2f)
                        ) {
                          Text(
                            text = badge,
                            color = AethelNeonCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                          )
                        }
                      }
                    }
                    Text(
                      text = "${plan.durationLabel} (${plan.perMonthEquiv})",
                      color = AethelTextSecondary,
                      fontSize = 11.sp
                    )
                  }
                }

                Text(
                  text = plan.price,
                  color = if (isSelected) AethelAmberGold else Color.White,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Button
        Button(
          onClick = { onUpgrade(selectedPlan) },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("confirm_upgrade_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AethelAmberGold)
        ) {
          Text(
            text = "Upgrade for ${selectedPlan.price} (${selectedPlan.durationLabel})",
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Cancel anytime. App is free for everyone with standard limits.",
          color = AethelTextMuted,
          fontSize = 10.sp
        )
      }
    }
  }
}

@Composable
fun PerkRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
  Row(verticalAlignment = Alignment.Top) {
    Icon(icon, contentDescription = null, tint = AethelAmberGold, modifier = Modifier.size(16.dp).padding(top = 2.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text(subtitle, color = AethelTextMuted, fontSize = 10.sp, lineHeight = 13.sp)
    }
  }
}
