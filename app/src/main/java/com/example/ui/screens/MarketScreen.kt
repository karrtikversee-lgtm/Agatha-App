package com.example.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Artwork
import com.example.data.model.MarketPricePoint
import com.example.ui.components.ContourWaveCanvas
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MarketScreen(
  artworks: List<Artwork>,
  priceHistory: List<MarketPricePoint>,
  onArtworkClick: (Artwork) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTimeframe by remember { mutableStateOf("1Y") }
  val timeframes = listOf("1M", "3M", "6M", "1Y", "ALL")

  val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
  }

  val totalValuation = artworks.sumOf { it.estimatedValue }
  val totalPrevious = artworks.sumOf { it.previousValue }
  val totalAppreciation = totalValuation - totalPrevious

  Scaffold(
    modifier = modifier.testTag("market_screen"),
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
          text = "MARKET VALUATION INTELLIGENCE",
          color = AethelTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Text(
          text = "Real-Time Price Tracking",
          color = Color.White,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Chart Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "TOTAL ASSET VALUE",
                  color = AethelTextMuted,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = currencyFormat.format(totalValuation),
                  color = Color.White,
                  fontSize = 28.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF132B20),
                border = BorderStroke(1.dp, AethelSuccessGreen.copy(alpha = 0.4f))
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = AethelSuccessGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "+${String.format(Locale.US, "%.1f", (totalAppreciation / totalPrevious) * 100)}%",
                    color = AethelSuccessGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Trend Graph Canvas
            ValuationLineChart(
              pricePoints = priceHistory,
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeframe Selector Tabs
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              timeframes.forEach { tf ->
                val isSelected = selectedTimeframe == tf
                Surface(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedTimeframe = tf }
                    .testTag("timeframe_btn_$tf"),
                  color = if (isSelected) AethelPrimary else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text(
                    text = tf,
                    color = if (isSelected) Color.White else AethelTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }

      // Benchmark indices
      item {
        Text(
          text = "GLOBAL AUCTION HOUSE BENCHMARKS",
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
          BenchmarkCard(title = "Sotheby's Contemp.", change = "+8.4%", isPositive = true, modifier = Modifier.weight(1f))
          BenchmarkCard(title = "Christie's 21st Cent.", change = "+6.1%", isPositive = true, modifier = Modifier.weight(1f))
          BenchmarkCard(title = "Phillips Digital Fine", change = "+14.7%", isPositive = true, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Listed Works & Liquidity Tracking
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "INDIVIDUAL PIECE VALUATION TICKER",
            color = AethelTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "LIVE FEEDS",
            color = AethelNeonCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      items(artworks) { artwork ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onArtworkClick(artwork) },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
          border = BorderStroke(1.dp, AethelBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(AethelSuccessGreen, CircleShape)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = artwork.title,
                  color = Color.White,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
                Text(
                  text = artwork.artist,
                  color = AethelTextSecondary,
                  fontSize = 12.sp
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = currencyFormat.format(artwork.estimatedValue),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "+$${String.format(Locale.US, "%,.0f", artwork.estimatedValue - artwork.previousValue)}",
                color = AethelSuccessGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }
}

@Composable
fun BenchmarkCard(
  title: String,
  change: String,
  isPositive: Boolean,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = AethelSurfaceElevated),
    border = BorderStroke(1.dp, AethelBorder)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        color = AethelTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = change,
        color = if (isPositive) AethelSuccessGreen else Color(0xFFFF6B6B),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ValuationLineChart(
  pricePoints: List<MarketPricePoint>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    if (pricePoints.size < 2) return@Canvas

    val width = size.width
    val height = size.height

    val minVal = pricePoints.minOf { it.value } * 0.95
    val maxVal = pricePoints.maxOf { it.value } * 1.05
    val valRange = (maxVal - minVal).coerceAtLeast(1.0)

    val stepX = width / (pricePoints.size - 1)

    val path = Path()
    val fillPath = Path()

    pricePoints.forEachIndexed { index, point ->
      val x = index * stepX
      val normalized = (point.value - minVal) / valRange
      val y = (height - (normalized * height)).toFloat()

      if (index == 0) {
        path.moveTo(x, y)
        fillPath.moveTo(x, height)
        fillPath.lineTo(x, y)
      } else {
        // Smooth cubic curve
        val prevX = (index - 1) * stepX
        val prevNormalized = (pricePoints[index - 1].value - minVal) / valRange
        val prevY = (height - (prevNormalized * height)).toFloat()

        val cx1 = prevX + (x - prevX) / 2
        val cy1 = prevY
        val cx2 = prevX + (x - prevX) / 2
        val cy2 = y

        path.cubicTo(cx1, cy1, cx2, cy2, x, y)
        fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
      }

      if (index == pricePoints.size - 1) {
        fillPath.lineTo(x, height)
        fillPath.close()
      }
    }

    // Draw soft gradient area fill underneath the curve
    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          AethelNeonCyan.copy(alpha = 0.35f),
          AethelNeonPurple.copy(alpha = 0.15f),
          Color.Transparent
        )
      )
    )

    // Draw main glowing line
    drawPath(
      path = path,
      brush = Brush.horizontalGradient(
        listOf(AethelNeonCyan, AethelNeonPurple, AethelNeonPink)
      ),
      style = Stroke(width = 3.5.dp.toPx())
    )

    // Draw glowing current point on the last item
    val lastX = (pricePoints.size - 1) * stepX
    val lastNormalized = (pricePoints.last().value - minVal) / valRange
    val lastY = (height - (lastNormalized * height)).toFloat()

    drawCircle(
      color = Color.White,
      radius = 5.dp.toPx(),
      center = Offset(lastX, lastY)
    )
    drawCircle(
      color = AethelNeonCyan.copy(alpha = 0.4f),
      radius = 10.dp.toPx(),
      center = Offset(lastX, lastY)
    )
  }
}
