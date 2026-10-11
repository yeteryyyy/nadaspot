package com.metrolist.music.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.metrolist.music.R

/** App font: light / medium / bold. Regular text uses the light cut. */
val SpotifyFontFamily = FontFamily(
    Font(R.font.spotify_light, FontWeight.Light),
    Font(R.font.spotify_light, FontWeight.Normal),
    Font(R.font.spotify_medium, FontWeight.Medium),
    Font(R.font.spotify_bold, FontWeight.SemiBold),
    Font(R.font.spotify_bold, FontWeight.Bold),
)
