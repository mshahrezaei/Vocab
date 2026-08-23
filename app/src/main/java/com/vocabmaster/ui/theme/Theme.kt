package com.vocabmaster.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepNavy = Color(0xFF1E3A5F)
val SoftGold = Color(0xFFD4AF37)
val WarmCream = Color(0xFFF5F1E8)
val BurntBrown = Color(0xFF8B6F47)
val DarkText = Color(0xFF2C2C2C)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = SoftGold,
    tertiary = BurntBrown,
    background = WarmCream,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun VocabMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
