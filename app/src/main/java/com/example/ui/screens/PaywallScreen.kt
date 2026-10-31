package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.CollectorTier
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserAccount
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.components.FloatingOrb
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
  currentUser: UserAccount?,
  onBack: () -> Unit,
  onSubscribe: (SubscriptionPlan) -> Unit,
  onCancelSubscription: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedPlan by remember { mutableStateOf(SubscriptionPlan.QUARTERLY) }

  val isPremium = currentUser?.tier == CollectorTier.ARTISAN_VAULT_VIP

  Scaffold(
    modifier = modifier.testTag("paywall_screen"),
    containerColor = AethelBackground,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Artisan Vault VIP",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("paywall_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AethelBackground)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header Card with Contour Waves
      item {
        Card(
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.5.dp, AethelAmberGold.copy(alpha = 0.65f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
          ) {
            ContourWaveCanvas(
              modifier = Modifier.fillMaxSize(),
              lineColor = AethelAmberGold.copy(alpha = 0.45f),
              strokeCount = 16
            )

            FloatingOrb(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp),
              size = 46.dp,
              primaryColor = AethelAmberGold,
              secondaryColor = AethelNeonPink
            )

            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.6f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AethelAmberGold, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isPremium) "ACTIVE VIP SUBSCRIBER" else "FREE COLLECTOR TIER",
                    color = AethelAmberGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                  )
                }
              }

              Column {
                Text(
                  text = "Unlock Unlimited Provenance",
                  color = Color.White,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Full PDF analytics, unlimited image uploads & custom soundscapes",
                  color = AethelTextSecondary,
                  fontSize = 12.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Feature Comparison Table
      item {
        Text(
          text = "TIER ACCESS & LIMITS COMPARISON",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurface),
          border = BorderStroke(1.dp, AethelBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ComparisonRow(
              feature = "Daily Artwork Uploads",
              freeAccess = "Max 10 / day",
              vipAccess = "Unlimited",
              highlight = true
            )
            ComparisonRow(
              feature = "Personal Audio Tracks",
              freeAccess = "Max 5 tracks",
              vipAccess = "Unlimited",
              highlight = true
            )
            ComparisonRow(
              feature = "PDF Analytics & Catalog Export",
              freeAccess = "Restricted",
              vipAccess = "Full 8K Export",
              highlight = true
            )
            ComparisonRow(
              feature = "Sotheby's Auction Liquidity Ticker",
              freeAccess = "Delayed 15m",
              vipAccess = "Real-time Live",
              highlight = false
            )
            ComparisonRow(
              feature = "Gemini 3.8 Live Voice Conversations",
              freeAccess = "Standard",
              vipAccess = "Priority Audio",
              highlight = false
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Plans Selection
      item {
        Text(
          text = "SELECT SUBSCRIPTION PLAN",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SubscriptionPlan.values().forEach { plan ->
            val isSelected = selectedPlan == plan
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { selectedPlan = plan }
                .testTag("paywall_plan_${plan.name}"),
              color = if (isSelected) AethelSurfaceHighlight else AethelSurfaceElevated,
              shape = RoundedCornerShape(18.dp),
              border = BorderStroke(
                if (isSelected) 1.8.dp else 1.dp,
                if (isSelected) AethelAmberGold else AethelBorder
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
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
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = plan.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                      )
                      plan.discountBadge?.let { badge ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                          shape = RoundedCornerShape(4.dp),
                          color = AethelAmberGold.copy(alpha = 0.2f),
                          border = BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.5f))
                        ) {
                          Text(
                            text = badge,
                            color = AethelAmberGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                          )
                        }
                      }
                    }
                    Text(
                      text = "${plan.durationLabel} • Just ${plan.perMonthEquiv}",
                      color = AethelTextSecondary,
                      fontSize = 12.sp
                    )
                  }
                }

                Text(
                  text = plan.price,
                  color = if (isSelected) AethelAmberGold else Color.White,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Action Buttons
      item {
        Button(
          onClick = {
            onSubscribe(selectedPlan)
            Toast.makeText(context, "Upgraded to VIP via ${selectedPlan.title} (${selectedPlan.price})!", Toast.LENGTH_LONG).show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("paywall_subscribe_btn"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AethelAmberGold)
        ) {
          Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isPremium) "Switch to ${selectedPlan.title} (${selectedPlan.price})" else "Subscribe for ${selectedPlan.price} (${selectedPlan.durationLabel})",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        if (isPremium) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = {
              onCancelSubscription()
              Toast.makeText(context, "Subscription paused. Switched to Free tier.", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("cancel_subscription_btn"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AethelBorder)
          ) {
            Text("Downgrade to Free Tier", color = AethelTextSecondary, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Secure payment processing via Google Play billing. Cancel anytime. All sales backed by Aethel Vault 100% satisfaction guarantee.",
          color = AethelTextMuted,
          fontSize = 10.sp,
          lineHeight = 14.sp,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
      }
    }
  }
}

@Composable
fun ComparisonRow(
  feature: String,
  freeAccess: String,
  vipAccess: String,
  highlight: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = feature,
      color = if (highlight) Color.White else AethelTextSecondary,
      fontSize = 12.sp,
      fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal,
      modifier = Modifier.weight(1.3f)
    )

    Surface(
      shape = RoundedCornerShape(6.dp),
      color = AethelSurfaceElevated,
      modifier = Modifier.weight(0.9f)
    ) {
      Text(
        text = freeAccess,
        color = AethelTextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }

    Spacer(modifier = Modifier.width(6.dp))

    Surface(
      shape = RoundedCornerShape(6.dp),
      color = if (highlight) AethelAmberGold.copy(alpha = 0.2f) else AethelSurfaceElevated,
      border = if (highlight) BorderStroke(1.dp, AethelAmberGold.copy(alpha = 0.4f)) else null,
      modifier = Modifier.weight(0.9f)
    ) {
      Text(
        text = vipAccess,
        color = if (highlight) AethelAmberGold else AethelSuccessGreen,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
