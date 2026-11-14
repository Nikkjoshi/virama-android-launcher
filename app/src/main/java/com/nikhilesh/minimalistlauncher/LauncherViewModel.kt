package com.nikhilesh.minimalistlauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikhilesh.minimalistlauncher.data.AndroidLauncherRepository
import com.nikhilesh.minimalistlauncher.data.SettingsRepository
import com.nikhilesh.minimalistlauncher.data.filterApps
import com.nikhilesh.minimalistlauncher.data.sanitizeFavorites
import com.nikhilesh.minimalistlauncher.model.LauncherApp
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherSettings
import com.nikhilesh.minimalistlauncher.model.LauncherTextScale
import com.nikhilesh.minimalistlauncher.model.MAX_FAVORITES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<LauncherApp> = emptyList(),
    val filteredApps: List<LauncherApp> = emptyList(),
    val favorites: List<LauncherApp> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val query: String = "",
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val launcherRepository = AndroidLauncherRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<LauncherUiState> = combine(
        launcherRepository.apps,
        settingsRepository.settings,
        query,
    ) { apps, settings, currentQuery ->
        val appsById = apps.associateBy(LauncherApp::id)
        val sanitizedIds = sanitizeFavorites(settings.favoriteIds, appsById.keys)
        if (sanitizedIds != settings.favoriteIds) {
            viewModelScope.launch { settingsRepository.setFavorites(sanitizedIds) }
        }
        LauncherUiState(
            apps = apps,
            filteredApps = filterApps(apps, currentQuery),
            favorites = sanitizedIds.mapNotNull(appsById::get),
            settings = settings.copy(favoriteIds = sanitizedIds),
            query = currentQuery,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun launch(app: LauncherApp) = launcherRepository.launch(app)

    fun openAppInfo(app: LauncherApp) = launcherRepository.openAppInfo(app)

    fun toggleFavorite(app: LauncherApp) {
        val favorites = uiState.value.settings.favoriteIds.toMutableList()
        if (app.id in favorites) {
            favorites.remove(app.id)
        } else if (favorites.size < MAX_FAVORITES) {
            favorites.add(app.id)
        }
        viewModelScope.launch { settingsRepository.setFavorites(favorites) }
    }

    fun moveFavorite(appId: String, direction: Int) {
        val favorites = uiState.value.settings.favoriteIds.toMutableList()
        val from = favorites.indexOf(appId)
        val to = from + direction
        if (from == -1 || to !in favorites.indices) return
        val moved = favorites.removeAt(from)
        favorites.add(to, moved)
        viewModelScope.launch { settingsRepository.setFavorites(favorites) }
    }

    fun setPalette(palette: LauncherPalette) {
        viewModelScope.launch { settingsRepository.setPalette(palette) }
    }

    fun setTextScale(textScale: LauncherTextScale) {
        viewModelScope.launch { settingsRepository.setTextScale(textScale) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsRepository.setOnboardingComplete(true) }
    }
}

