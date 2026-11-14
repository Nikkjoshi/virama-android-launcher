package com.nikhilesh.minimalistlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nikhilesh.minimalistlauncher.LauncherUiState
import com.nikhilesh.minimalistlauncher.model.LauncherApp
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.model.LauncherTextScale
import com.nikhilesh.minimalistlauncher.model.MAX_FAVORITES
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun LauncherPager(
    uiState: LauncherUiState,
    onQueryChange: (String) -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onOpenAppInfo: (LauncherApp) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onSetPalette: (LauncherPalette) -> Unit,
    onSetTextScale: (LauncherTextScale) -> Unit,
    onOpenHomeSettings: () -> Unit,
    onOpenWebSearch: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        if (pagerState.currentPage != 0) {
            scope.launch { pagerState.animateScrollToPage(0) }
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            0 -> HomeScreen(
                favorites = uiState.favorites,
                onLaunch = onLaunch,
                onRemoveFavorite = onToggleFavorite,
                onOpenAppInfo = onOpenAppInfo,
                onShowApps = { scope.launch { pagerState.animateScrollToPage(1) } },
                onOpenWebSearch = onOpenWebSearch,
            )

            1 -> AppListScreen(
                apps = uiState.filteredApps,
                favoriteIds = uiState.settings.favoriteIds.toSet(),
                favorites = uiState.favorites,
                query = uiState.query,
                palette = uiState.settings.palette,
                textScale = uiState.settings.textScale,
                onQueryChange = onQueryChange,
                onLaunch = onLaunch,
                onToggleFavorite = onToggleFavorite,
                onOpenAppInfo = onOpenAppInfo,
                onMoveFavorite = onMoveFavorite,
                onSetPalette = onSetPalette,
                onSetTextScale = onSetTextScale,
                onOpenHomeSettings = onOpenHomeSettings,
            )
        }
    }
}

@Composable
fun HomeScreen(
    favorites: List<LauncherApp>,
    onLaunch: (LauncherApp) -> Unit,
    onRemoveFavorite: (LauncherApp) -> Unit,
    onOpenAppInfo: (LauncherApp) -> Unit,
    onShowApps: () -> Unit,
    onOpenWebSearch: () -> Unit,
) {
    val now by rememberCurrentMinute()
    val battery by rememberBatteryPercentage()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
            .pointerInput(onOpenWebSearch, onShowApps) {
                var horizontalDistance = 0f
                val triggerDistance = 56.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDistance = 0f },
                    onDragCancel = { horizontalDistance = 0f },
                    onDragEnd = {
                        when {
                            horizontalDistance >= triggerDistance -> onOpenWebSearch()
                            horizontalDistance <= -triggerDistance -> onShowApps()
                        }
                        horizontalDistance = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        horizontalDistance += dragAmount
                    },
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = now.format(timeFormatter),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = now.format(dateFormatter),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "$battery% battery",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (favorites.isEmpty()) {
                Column {
                    Text(
                        text = "No favorites yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Swipe left or open All apps to add some.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    items(favorites, key = LauncherApp::id) { app ->
                        LauncherAppRow(
                            app = app,
                            isFavorite = true,
                            onLaunch = onLaunch,
                            onToggleFavorite = onRemoveFavorite,
                            onOpenAppInfo = onOpenAppInfo,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Swipe right for Chrome",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onShowApps) {
                Text("All apps  →")
            }
        }
    }
}

@Composable
fun AppListScreen(
    apps: List<LauncherApp>,
    favoriteIds: Set<String>,
    favorites: List<LauncherApp>,
    query: String,
    palette: LauncherPalette,
    textScale: LauncherTextScale,
    onQueryChange: (String) -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onOpenAppInfo: (LauncherApp) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onSetPalette: (LauncherPalette) -> Unit,
    onSetTextScale: (LauncherTextScale) -> Unit,
    onOpenHomeSettings: () -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_list_screen")
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "All apps",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineLarge,
            )
            TextButton(onClick = { showSettings = true }) { Text("Settings") }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search"),
            placeholder = { Text("Search apps") },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.semantics { contentDescription = "Clear search" },
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            } else {
                null
            },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))

        if (apps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (query.isBlank()) "No launchable apps found" else "No matches",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(apps, key = LauncherApp::id) { app ->
                    LauncherAppRow(
                        app = app,
                        isFavorite = app.id in favoriteIds,
                        favoritesFull = favoriteIds.size >= MAX_FAVORITES,
                        onLaunch = { selectedApp ->
                            onQueryChange("")
                            onLaunch(selectedApp)
                        },
                        onToggleFavorite = onToggleFavorite,
                        onOpenAppInfo = onOpenAppInfo,
                    )
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            favorites = favorites,
            palette = palette,
            textScale = textScale,
            onDismiss = { showSettings = false },
            onMoveFavorite = onMoveFavorite,
            onSetPalette = onSetPalette,
            onSetTextScale = onSetTextScale,
            onOpenHomeSettings = onOpenHomeSettings,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppRow(
    app: LauncherApp,
    isFavorite: Boolean,
    favoritesFull: Boolean = false,
    onLaunch: (LauncherApp) -> Unit,
    onToggleFavorite: (LauncherApp) -> Unit,
    onOpenAppInfo: (LauncherApp) -> Unit,
) {
    var menuExpanded by remember(app.id) { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = "Open ${app.label}",
                    onLongClickLabel = "App options",
                    onClick = { onLaunch(app) },
                    onLongClick = { menuExpanded = true },
                )
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = app.label,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (isFavorite) {
                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            val favoriteActionText = when {
                isFavorite -> "Remove from favorites"
                favoritesFull -> "Favorites full ($MAX_FAVORITES)"
                else -> "Add to favorites"
            }
            DropdownMenuItem(
                text = { Text(favoriteActionText) },
                enabled = isFavorite || !favoritesFull,
                onClick = {
                    menuExpanded = false
                    onToggleFavorite(app)
                },
            )
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = {
                    menuExpanded = false
                    onOpenAppInfo(app)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    favorites: List<LauncherApp>,
    palette: LauncherPalette,
    textScale: LauncherTextScale,
    onDismiss: () -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onSetPalette: (LauncherPalette) -> Unit,
    onSetTextScale: (LauncherTextScale) -> Unit,
    onOpenHomeSettings: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LauncherPalette.entries.forEach { option ->
                    FilterChip(
                        selected = palette == option,
                        onClick = { onSetPalette(option) },
                        label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LauncherTextScale.entries.forEach { option ->
                    FilterChip(
                        selected = textScale == option,
                        onClick = { onSetTextScale(option) },
                        label = { Text(option.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            HorizontalDivider()
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Favorite order · ${favorites.size}/$MAX_FAVORITES",
                style = MaterialTheme.typography.titleLarge,
            )
            if (favorites.isEmpty()) {
                Text(
                    text = "Long-press an app to add it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                favorites.forEachIndexed { index, app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = app.label,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        TextButton(
                            enabled = index > 0,
                            onClick = { onMoveFavorite(app.id, -1) },
                        ) { Text("↑") }
                        Spacer(Modifier.width(2.dp))
                        TextButton(
                            enabled = index < favorites.lastIndex,
                            onClick = { onMoveFavorite(app.id, 1) },
                        ) { Text("↓") }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onOpenHomeSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Default home settings")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Offline by design · no analytics · no usage tracking",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

