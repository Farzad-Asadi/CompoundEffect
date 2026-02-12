package com.example.compoundeffectV1_01.utils

import androidx.compose.ui.graphics.Color


data class ColorOption(val name: String, val hex: String, val color: androidx.compose.ui.graphics.Color)


fun buildColorOptions(): List<ColorOption> = listOf(
    ColorOption("Red", "#F44336", Color(0xFFF44336)),
    ColorOption("Pink", "#E91E63", Color(0xFFE91E63)),
    ColorOption("Purple", "#9C27B0", Color(0xFF9C27B0)),
    ColorOption("DeepPurple", "#673AB7", Color(0xFF673AB7)),
    ColorOption("Indigo", "#3F51B5", Color(0xFF3F51B5)),
    ColorOption("Blue", "#2196F3", Color(0xFF2196F3)),
    ColorOption("LightBlue", "#03A9F4", Color(0xFF03A9F4)),
    ColorOption("Cyan", "#00BCD4", Color(0xFF00BCD4)),
    ColorOption("Teal", "#009688", Color(0xFF009688)),
    ColorOption("Green", "#4CAF50", Color(0xFF4CAF50)),
    ColorOption("LightGreen", "#8BC34A", Color(0xFF8BC34A)),
    ColorOption("Lime", "#CDDC39", Color(0xFFCDDC39)),
    ColorOption("Yellow", "#FFEB3B", Color(0xFFFFEB3B)),
    ColorOption("Amber", "#FFC107", Color(0xFFFFC107)),
    ColorOption("Orange", "#FF9800", Color(0xFFFF9800)),
    ColorOption("DeepOrange", "#FF5722", Color(0xFFFF5722)),
    ColorOption("Brown", "#795548", Color(0xFF795548)),
    ColorOption("BlueGrey", "#607D8B", Color(0xFF607D8B)),
)

fun colorFromHex(hex: String): Color {
    // قبول می‌کنیم "#RRGGBB" یا "#AARRGGBB"
    val clean = hex.trim().removePrefix("#")
    return try {
        val value = clean.toLong(16)
        when (clean.length) {
            6 -> Color((0xFF000000 or value).toInt())
            8 -> Color(value.toInt())
            else -> Color(0xFF000000.toInt())
        }
    } catch (_: Throwable) {
        Color(0xFF000000)
    }
}