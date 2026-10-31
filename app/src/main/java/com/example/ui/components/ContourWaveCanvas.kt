package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Recreates the signature hypnotic concentric elliptical wireframe contour art
 * featured in the reference design.
 */
@Composable
fun ContourWaveCanvas(
  modifier: Modifier = Modifier,
  lineColor: Color = Color.White.copy(alpha = 0.55f),
  strokeCount: Int = 18,
  isAnimated: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "wave_motion")
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Canvas(modifier = modifier) {
    val width = size.width
    val height = size.height
    val centerX = width * 0.5f
    val topY = height * 0.15f
    val bottomY = height * 0.9f

    // Draw concentric oval loops that taper toward the top and flare toward bottom
    for (i in 0 until strokeCount) {
      val t = (i + 1).toFloat() / strokeCount
      val currentPulse = if (isAnimated) 1f + (pulse - 1f) * (1f - t) else 1f

      val rx = (width * 0.46f * t) * currentPulse
      val ry = (height * 0.42f * t) * currentPulse
      val cy = topY + (bottomY - topY) * t * 0.65f

      val alpha = (0.20f + 0.55f * (1f - t * 0.6f)).coerceIn(0.1f, 0.85f)

      val path = Path().apply {
        addOval(
          Rect(
            left = centerX - rx,
            top = cy - ry * 0.7f,
            right = centerX + rx,
            bottom = cy + ry
          )
        )
      }

      drawPath(
        path = path,
        color = lineColor.copy(alpha = alpha),
        style = Stroke(width = if (i == 0) 2.2f else 1.2f)
      )
    }
  }
}
