package com.nikhilesh.minimalistlauncher.data

import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherTextScale
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsCodecTest {
    @Test
    fun `favorite component order round trips`() {
        val favorites = listOf("com.android.phone/.Main", "com.android.camera/.CameraActivity")
        assertEquals(favorites, SettingsCodec.decodeFavorites(SettingsCodec.encodeFavorites(favorites)))
    }

    @Test
    fun `decoder drops blanks duplicates and values beyond twelve`() {
        val encoded = (listOf("a", "", "a") + ('b'..'n').map(Char::toString)).joinToString("\n")
        assertEquals(
            ('a'..'l').map(Char::toString),
            SettingsCodec.decodeFavorites(encoded),
        )
    }

    @Test
    fun `palette and text scale decode persisted enum names`() {
        assertEquals(LauncherPalette.PAPER, SettingsCodec.decodePalette("PAPER"))
        assertEquals(LauncherTextScale.LARGE, SettingsCodec.decodeTextScale("LARGE"))
    }

    @Test
    fun `unknown appearance values fall back safely`() {
        assertEquals(LauncherPalette.INK, SettingsCodec.decodePalette("future-palette"))
        assertEquals(LauncherTextScale.STANDARD, SettingsCodec.decodeTextScale(null))
    }
}

