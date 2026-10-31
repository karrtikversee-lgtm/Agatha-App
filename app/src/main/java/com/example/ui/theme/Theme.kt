package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = AethelPrimary,
  onPrimary = Color.White,
  primaryContainer = AethelSurfaceElevated,
  onPrimaryContainer = AethelTextPrimary,
  secondary = AethelNeonCyan,
  onSecondary = Color.Black,
  tertiary = AethelNeonPink,
  onTertiary = Color.White,
  background = AethelBackground,
  onBackground = AethelTextPrimary,
  surface = AethelSurface,
  onSurface = AethelTextPrimary,
  surfaceVariant = AethelSurfaceElevated,
  onSurfaceVariant = AethelTextSecondary,
  outline = AethelBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to stunning dark theme matching design reference
  dynamicColor: Boolean = false, // Keep bespoke luxury theme consistent
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
