package com.nikhilesh.minimalistlauncher.model

const val MAX_FAVORITES = 12

enum class LauncherPalette {
    INK,
    PAPER,
}

enum class LauncherTextScale(val multiplier: Float) {
    COMPACT(0.9f),
    STANDARD(1f),
    LARGE(1.15f),
}

data class LauncherSettings(
    val favoriteIds: List<String> = emptyList(),
    val palette: LauncherPalette = LauncherPalette.INK,
    val textScale: LauncherTextScale = LauncherTextScale.STANDARD,
    val onboardingComplete: Boolean = false,
)

