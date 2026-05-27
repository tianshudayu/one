package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
      primary = DarkPrimary,
      secondary = DarkSecondary,
      background = DarkBackground,
      surface = DarkSurface,
      onPrimary = DarkBackground,
      onSecondary = DarkBackground,
      onBackground = DarkOnSurface,
      onSurface = DarkOnSurface,
  )

private val LightColorScheme =
  lightColorScheme(
      primary = LightPrimary,
      secondary = LightSecondary,
      background = LightBackground,
      surface = LightSurface,
      onPrimary = LightSurface,
      onSecondary = LightSurface,
      onBackground = LightOnSurface,
      onSurface = LightOnSurface,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
