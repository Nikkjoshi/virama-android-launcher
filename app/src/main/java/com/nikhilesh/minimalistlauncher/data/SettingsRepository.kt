package com.nikhilesh.minimalistlauncher.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherSettings
import com.nikhilesh.minimalistlauncher.model.LauncherTextScale
import com.nikhilesh.minimalistlauncher.model.MAX_FAVORITES
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore by preferencesDataStore(name = "launcher_settings")

object SettingsCodec {
    private const val SEPARATOR = "\n"

    fun encodeFavorites(ids: List<String>): String = ids.joinToString(SEPARATOR)

    fun decodeFavorites(value: String?): List<String> = value
        .orEmpty()
        .split(SEPARATOR)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .take(MAX_FAVORITES)

    fun decodePalette(value: String?): LauncherPalette = value
        ?.let { runCatching { LauncherPalette.valueOf(it) }.getOrNull() }
        ?: LauncherPalette.INK

    fun decodeTextScale(value: String?): LauncherTextScale = value
        ?.let { runCatching { LauncherTextScale.valueOf(it) }.getOrNull() }
        ?: LauncherTextScale.STANDARD
}

class SettingsRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.launcherDataStore

    val settings: Flow<LauncherSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)

    suspend fun setFavorites(ids: List<String>) {
        dataStore.edit { preferences ->
            preferences[FAVORITES] = SettingsCodec.encodeFavorites(ids.distinct().take(MAX_FAVORITES))
        }
    }

    suspend fun setPalette(palette: LauncherPalette) {
        dataStore.edit { it[PALETTE] = palette.name }
    }

    suspend fun setTextScale(textScale: LauncherTextScale) {
        dataStore.edit { it[TEXT_SCALE] = textScale.name }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETE] = complete }
    }

    private fun decode(preferences: Preferences): LauncherSettings = LauncherSettings(
        favoriteIds = SettingsCodec.decodeFavorites(preferences[FAVORITES]),
        palette = SettingsCodec.decodePalette(preferences[PALETTE]),
        textScale = SettingsCodec.decodeTextScale(preferences[TEXT_SCALE]),
        onboardingComplete = preferences[ONBOARDING_COMPLETE] ?: false,
    )

    private companion object {
        val FAVORITES = stringPreferencesKey("favorite_components")
        val PALETTE = stringPreferencesKey("palette")
        val TEXT_SCALE = stringPreferencesKey("text_scale")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
}

