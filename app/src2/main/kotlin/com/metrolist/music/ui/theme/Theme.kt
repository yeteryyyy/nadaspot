/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.materialkolor.score.Score

val DefaultThemeColor = Color(0xFF1DB954)

// Spotify mobile, 2018 palette
val SpotifyGreen = Color(0xFF1DB954)
val SpotifyBlack = Color(0xFF121212)
val SpotifyNavBar = Color(0xFF181818)
val SpotifyMiniPlayer = Color(0xFF282828)
val SpotifyGrey = Color(0xFFB3B3B3)
val SpotifyDivider = Color(0xFF282828)

private val SpotifyColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyGreen,
    onPrimaryContainer = Color.Black,
    secondary = SpotifyGreen,
    onSecondary = Color.Black,
    secondaryContainer = SpotifyMiniPlayer,
    onSecondaryContainer = Color.White,
    tertiary = SpotifyGreen,
    onTertiary = Color.Black,
    background = SpotifyBlack,
    onBackground = Color.White,
    surface = SpotifyBlack,
    onSurface = Color.White,
    surfaceVariant = SpotifyMiniPlayer,
    onSurfaceVariant = SpotifyGrey,
    surfaceTint = Color.Transparent,
    surfaceBright = Color(0xFF333333),
    surfaceDim = SpotifyBlack,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = SpotifyBlack,
    surfaceContainer = SpotifyNavBar,
    surfaceContainerHigh = SpotifyMiniPlayer,
    surfaceContainerHighest = Color(0xFF333333),
    outline = SpotifyGrey,
    outlineVariant = SpotifyDivider,
    error = Color(0xFFE22134),
    onError = Color.White,
)

/**
 * Always dark, always Spotify green. [darkTheme] and [themeColor] are kept for
 * call-site compatibility; the album-art colour is applied by the player
 * gradient, not by the global scheme.
 */
@Composable
fun MetrolistTheme(
    darkTheme: Boolean = true,
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(pureBlack) {
        if (pureBlack) SpotifyColorScheme.pureBlack(true) else SpotifyColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpotifyTypography,
        content = content,
    )
}

private val SpotifyTypography = Typography().let { t ->
    fun TextStyle.f() = copy(fontFamily = SpotifyFontFamily)
    t.copy(
        displayLarge = t.displayLarge.f(),
        displayMedium = t.displayMedium.f(),
        displaySmall = t.displaySmall.f(),
        headlineLarge = t.headlineLarge.f(),
        headlineMedium = t.headlineMedium.f(),
        headlineSmall = t.headlineSmall.f(),
        titleLarge = t.titleLarge.f().copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.f().copy(fontWeight = FontWeight.Bold),
        titleSmall = t.titleSmall.f(),
        bodyLarge = t.bodyLarge.f(),
        bodyMedium = t.bodyMedium.f(),
        bodySmall = t.bodySmall.f(),
        labelLarge = t.labelLarge.f().copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        labelMedium = t.labelMedium.f(),
        labelSmall = t.labelSmall.f(),
    )
}

fun Bitmap.extractThemeColor(): Color = Color(
    Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .rankedColors(1, DefaultThemeColor.toArgb())
        .first()
)

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> = Score.score(
    swatches.associate { it.rgb to it.population },
    desiredColorCount,
    fallbackColor,
    true,
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
