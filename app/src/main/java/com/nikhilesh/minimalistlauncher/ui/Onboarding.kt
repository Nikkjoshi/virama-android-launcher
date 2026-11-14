package com.nikhilesh.minimalistlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nikhilesh.minimalistlauncher.data.filterApps
import com.nikhilesh.minimalistlauncher.model.LauncherApp
import com.nikhilesh.minimalistlauncher.model.MAX_FAVORITES

@Composable
fun HomeRoleOnboarding(
    onRequestHomeRole: () -> Unit,
) {
    BackHandler(enabled = true) {}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "A quieter home screen",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Virama replaces only your home screen. Your apps, notifications, calls, and data continue to work normally.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "It does not track usage, block apps, or connect to the internet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onRequestHomeRole,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Use Virama as home")
        }
    }
}

@Composable
fun FavoriteOnboarding(
    apps: List<LauncherApp>,
    selectedIds: List<String>,
    onToggleFavorite: (LauncherApp) -> Unit,
    onContinue: () -> Unit,
) {
    BackHandler(enabled = true) {}
    var query by rememberSaveable { mutableStateOf("") }
    val visibleApps = remember(apps, query) { filterApps(apps, query) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = "Choose your essentials",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Select up to $MAX_FAVORITES apps for the home screen. You can change this later.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_search"),
            placeholder = { Text("Search apps") },
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))

        if (apps.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Loading installed apps…")
            }
        } else if (visibleApps.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "No matches",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visibleApps, key = LauncherApp::id) { app ->
                    val selected = app.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = selected || selectedIds.size < MAX_FAVORITES,
                                onClick = { onToggleFavorite(app) },
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selected,
                            enabled = selected || selectedIds.size < MAX_FAVORITES,
                            onCheckedChange = { onToggleFavorite(app) },
                        )
                        Text(app.label, style = MaterialTheme.typography.bodyLarge)
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
                text = "${selectedIds.size} / $MAX_FAVORITES selected",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onContinue) { Text("Skip") }
                Button(onClick = onContinue) { Text("Continue") }
            }
        }
    }
}

