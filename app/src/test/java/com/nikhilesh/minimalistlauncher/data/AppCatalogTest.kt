package com.nikhilesh.minimalistlauncher.data

import com.nikhilesh.minimalistlauncher.model.LauncherApp
import java.text.Collator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppCatalogTest {
    private val apps = listOf(
        LauncherApp("pkg/.Zulu", "Zulu"),
        LauncherApp("pkg/.Cafe", "Café"),
        LauncherApp("pkg/.Alpha", "alpha"),
    )

    @Test
    fun `search is case and accent insensitive`() {
        assertEquals(
            listOf("Café"),
            filterApps(apps, "CAFE", Locale.ENGLISH).map(LauncherApp::label),
        )
    }

    @Test
    fun `sorting is locale aware`() {
        val collator = Collator.getInstance(Locale.ENGLISH)
        assertEquals(
            listOf("alpha", "Café", "Zulu"),
            sortApps(apps, collator).map(LauncherApp::label),
        )
    }

    @Test
    fun `favorites remove stale duplicates and enforce limit`() {
        val ids = listOf("a", "a", "missing") + ('b'..'n').map(Char::toString)
        val valid = ('a'..'n').map(Char::toString).toSet()

        assertEquals(
            ('a'..'l').map(Char::toString),
            sanitizeFavorites(ids, valid),
        )
    }
}

