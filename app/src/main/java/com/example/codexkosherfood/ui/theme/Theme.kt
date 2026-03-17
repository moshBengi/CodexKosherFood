package com.example.codexkosherfood.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppOnPrimary,
    primaryContainer = AppSecondary,
    onPrimaryContainer = AppPrimaryDark,
    secondary = AppPrimaryDark,
    onSecondary = AppOnPrimary,
    secondaryContainer = AppSurfaceSoft,
    onSecondaryContainer = AppText,
    tertiary = AppWarning,
    onTertiary = AppOnPrimary,
    background = AppBackground,
    onBackground = AppText,
    surface = AppSurface,
    onSurface = AppText,
    surfaceVariant = AppSecondary,
    onSurfaceVariant = AppMuted,
    outline = AppOutline,
    error = AppError,
    onError = AppOnPrimary,
)

@Composable
fun CodexKosherFoodTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}
