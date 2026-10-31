package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Artwork
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

@Composable
fun AnalyticsExportScreen(
  artworks: List<Artwork>,
  currentTier: com.example.data.model.CollectorTier,
  onShowUpgradeSheet: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var emailDigestEnabled by remember { mutableStateOf(true) }
  var slackAlertsEnabled by remember { mutableStateOf(true) }
  var apiKey by remember { mutableStateOf("aeth_live_89f02c4b71e891d033a") }
  var showPdfPreviewDialog by remember { mutableStateOf(false) }

  val totalValuation = artworks.sumOf { it.estimatedValue }
  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  fun exportCsv() {
    val header = "ID,Title,Artist,Year,Medium,EstimatedValue,TokenID,ProvenanceHash,BlockHeight,ExhibitionRoom\n"
    val rows = artworks.joinToString("\n") { art ->
      "\"${art.id}\",\"${art.title}\",\"${art.artist}\",${art.year},\"${art.medium}\",${art.estimatedValue},\"${art.blockchainTokenId}\",\"${art.provenanceHash}\",${art.blockHeight},\"${art.exhibitionRoom}\""
    }
    val csvContent = header + rows

    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, csvContent)
      type = "text/csv"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export Vault Audit CSV"))
  }

  Scaffold(
    modifier = modifier.testTag("analytics_export_screen"),
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
          text = "BUSINESS INTELLIGENCE & AUDITS",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "Analytics & Reports",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Catalog Export Master Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceHighlight),
          border = BorderStroke(1.dp, AethelPrimary.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "DIGITAL CATALOG GENERATOR",
                  color = AethelNeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.6.sp
                )
                Text(
                  text = "Formal Private Catalog",
                  color = Color.White,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = AethelNeonCyan,
                modifier = Modifier.size(28.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Export publication-ready catalog with curated color plates, blockchain authentication hashes, and insurance valuation metrics.",
              color = AethelTextSecondary,
              fontSize = 12.sp,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  if (currentTier == com.example.data.model.CollectorTier.STANDARD) {
                    onShowUpgradeSheet("Downloading formal PDF Analytics and Digital Catalogs is a VIP feature. Upgrade ($5/mo, $20/3mo, $50/1yr) to download unlimited PDF reports!")
                  } else {
                    showPdfPreviewDialog = true
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("preview_pdf_catalog_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (currentTier == com.example.data.model.CollectorTier.STANDARD) AethelSurfaceHighlight else AethelPrimary
                ),
                border = if (currentTier == com.example.data.model.CollectorTier.STANDARD) BorderStroke(1.dp, AethelAmberGold) else null
              ) {
                Icon(
                  Icons.Default.PictureAsPdf,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (currentTier == com.example.data.model.CollectorTier.STANDARD) AethelAmberGold else Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentTier == com.example.data.model.CollectorTier.STANDARD) "PDF Catalog (VIP)" else "PDF Catalog",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (currentTier == com.example.data.model.CollectorTier.STANDARD) AethelAmberGold else Color.White
                )
              }

              OutlinedButton(
                onClick = { exportCsv() },
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("export_csv_btn"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AethelBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
              ) {
                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Key Portfolio Health Metrics
      item {
        Text(
          text = "KEY BUSINESS METRICS OVER TIME",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricTile(
            label = "LIQUIDITY SCORE",
            value = "88 / 100",
            subtext = "High Market Demand",
            color = AethelSuccessGreen,
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            label = "COLLECTION APPRECIATION",
            value = "+18.4%",
            subtext = "Trailing 12-Month ROI",
            color = AethelNeonPink,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricTile(
            label = "AVG HOLD PERIOD",
            value = "2.8 Yrs",
            subtext = "Long-Term Appreciation",
            color = AethelNeonCyan,
            modifier = Modifier.weight(1f)
          )
          MetricTile(
            label = "INSURANCE VALUATION",
            value = currencyFormat.format(totalValuation * 1.15),
            subtext = "115% Replacement Basis",
            color = AethelAmberGold,
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Stakeholder Automation (Email & Slack)
      item {
        Text(
          text = "STAKEHOLDER NOTIFICATION AUTOMATIONS",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Recurring Email Summary
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = AethelPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Recurring Email Summaries",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "Weekly digest automatically dispatched to stakeholders",
                    color = AethelTextSecondary,
                    fontSize = 11.sp
                  )
                }
              }
              Switch(
                checked = emailDigestEnabled,
                onCheckedChange = {
                  emailDigestEnabled = it
                  Toast.makeText(context, if (it) "Recurring Email Summaries Enabled" else "Email Summaries Paused", Toast.LENGTH_SHORT).show()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = AethelPrimary
                ),
                modifier = Modifier.testTag("email_digest_switch")
              )
            }

            HorizontalDivider(color = AethelBorder)

            // Integrated Slack Alerts
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AethelNeonPink, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Integrated Slack Notifications",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "Real-time alerts sent to #art-vault-acquisitions",
                    color = AethelTextSecondary,
                    fontSize = 11.sp
                  )
                }
              }
              Switch(
                checked = slackAlertsEnabled,
                onCheckedChange = {
                  slackAlertsEnabled = it
                  Toast.makeText(context, if (it) "Slack Live Alerts Connected" else "Slack Alerts Paused", Toast.LENGTH_SHORT).show()
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = AethelNeonPink
                ),
                modifier = Modifier.testTag("slack_alerts_switch")
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // External Data Synchronization API Access
      item {
        Text(
          text = "CUSTOMIZABLE API ACCESS FOR EXTERNAL SYNC",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Collector REST/GraphQL API Key",
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Authorize third-party portfolio management, family office, and insurance valuation software.",
              color = AethelTextSecondary,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AethelBackground,
              border = BorderStroke(1.dp, AethelBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = apiKey,
                  color = AethelNeonCyan,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("API Key", apiKey))
                    Toast.makeText(context, "API Key Copied to Clipboard", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
              onClick = {
                apiKey = "aeth_live_" + UUID.randomUUID().toString().replace("-", "").take(18)
                Toast.makeText(context, "Regenerated API Secret Key", Toast.LENGTH_SHORT).show()
              },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, AethelBorder)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = AethelTextSecondary)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Regenerate Access Token", fontSize = 11.sp, color = AethelTextSecondary)
            }
          }
        }
        Spacer(modifier = Modifier.height(100.dp))
      }
    }
  }

  // Digital Catalog PDF Preview Dialog
  if (showPdfPreviewDialog) {
    DigitalCatalogPdfDialog(
      artworks = artworks,
      totalValuation = totalValuation,
      onDismiss = { showPdfPreviewDialog = false },
      onSharePdf = {
        val shareIntent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_SUBJECT, "Aethel Vault 2026 Private Collection Digital Catalog")
          putExtra(
            Intent.EXTRA_TEXT,
            "AETHEL PRIVATE VAULT CATALOG 2026\n" +
                "Total Valuation: ${currencyFormat.format(totalValuation)}\n" +
                "Catalogued Works: ${artworks.size}\n" +
                "Blockchain Cryptographic Verification: All items sealed.\n\n" +
                artworks.joinToString("\n\n") { "• ${it.title} by ${it.artist} (${it.year})\n  Medium: ${it.medium}\n  Valuation: ${currencyFormat.format(it.estimatedValue)}\n  Provenance: ${it.provenanceHash.take(22)}..." }
          )
          type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Digital Catalog PDF"))
        showPdfPreviewDialog = false
      }
    )
  }
}

@Composable
fun MetricTile(
  label: String,
  value: String,
  subtext: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
    border = BorderStroke(1.dp, AethelBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = label,
        color = AethelTextMuted,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        color = color,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtext,
        color = AethelTextSecondary,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun DigitalCatalogPdfDialog(
  artworks: List<Artwork>,
  totalValuation: Double,
  onDismiss: () -> Unit,
  onSharePdf: () -> Unit
) {
  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF101218),
      border = BorderStroke(1.5.dp, AethelPrimary.copy(alpha = 0.7f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("digital_catalog_pdf_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "DIGITAL CATALOG PREVIEW",
              color = AethelNeonCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Aethel Vault • Edition 2026",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PDF Sheet Preview
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = "AETHEL PRIVATE GALLERY CATALOG",
              color = Color.Black,
              fontSize = 14.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 1.sp
            )
            Text(
              text = "CONFIDENTIAL AUDIT & VALUATION RECORD",
              color = Color.DarkGray,
              fontSize = 10.sp
            )
            Text(
              text = "Portfolio Valuation: ${currencyFormat.format(totalValuation)}",
              color = Color(0xFF1E3A8A),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

            artworks.forEach { art ->
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                  text = "${art.title} — ${art.artist} (${art.year})",
                  color = Color.Black,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Medium: ${art.medium} • ${art.dimensions}",
                  color = Color.DarkGray,
                  fontSize = 9.sp
                )
                Text(
                  text = "Value: ${currencyFormat.format(art.estimatedValue)} | CoA: ${art.provenanceHash.take(16)}...",
                  color = Color(0xFF047857),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onSharePdf,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("download_share_pdf_btn"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = AethelPrimary)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Download & Dispatch PDF Report", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
