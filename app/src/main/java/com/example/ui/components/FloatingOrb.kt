package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AethelNeonPink
import com.example.ui.theme.AethelNeonPurple

@Composable
fun FloatingOrb(
  modifier: Modifier = Modifier,
  size: Dp = 38.dp,
  primaryColor: Color = AethelNeonPink,
  secondaryColor: Color = AethelNeonPurple,
  floatAmplitude: Float = 6f
) {
  val infiniteTransition = rememberInfiniteTransition(label = "orb_float")
  val offsetY by infiniteTransition.animateFloat(
    initialValue = -floatAmplitude,
    targetValue = floatAmplitude,
    animationSpec = infiniteRepeatable(
      animation = tween(2600, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb_offset"
  )

  Canvas(modifier = modifier.size(size)) {
    val radius = this.size.minDimension / 2
    val center = Offset(this.size.width / 2, this.size.height / 2 + offsetY)

    // Outer luminous glow
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          primaryColor.copy(alpha = 0.45f),
          primaryColor.copy(alpha = 0.12f),
          Color.Transparent
        ),
        center = center,
        radius = radius * 1.5f
      ),
      radius = radius * 1.5f,
      center = center
    )

    // 3D Sphere gradient with directional light spot
    val lightCenter = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.95f),
          primaryColor,
          secondaryColor,
          Color(0xFF2E0854)
        ),
        center = lightCenter,
        radius = radius * 1.15f
      ),
      radius = radius,
      center = center
    )
  }
}
