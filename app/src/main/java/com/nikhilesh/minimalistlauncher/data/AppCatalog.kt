package com.nikhilesh.minimalistlauncher.data

import com.nikhilesh.minimalistlauncher.model.LauncherApp
import com.nikhilesh.minimalistlauncher.model.MAX_FAVORITES
import java.text.Collator
import java.text.Normalizer
import java.util.Locale

fun sortApps(
    apps: List<LauncherApp>,
    collator: Collator = Collator.getInstance(Locale.getDefault()),
): List<LauncherApp> = apps.sortedWith { left, right ->
    val labelComparison = collator.compare(left.label, right.label)
    if (labelComparison != 0) labelComparison else left.id.compareTo(right.id)
}

fun filterApps(
    apps: List<LauncherApp>,
    query: String,
    locale: Locale = Locale.getDefault(),
): List<LauncherApp> {
    val normalizedQuery = query.searchKey(locale)
    if (normalizedQuery.isBlank()) return apps
    return apps.filter { it.label.searchKey(locale).contains(normalizedQuery) }
}

fun sanitizeFavorites(
    favoriteIds: List<String>,
    validIds: Set<String>,
    limit: Int = MAX_FAVORITES,
): List<String> = favoriteIds
    .asSequence()
    .distinct()
    .filter(validIds::contains)
    .take(limit)
    .toList()

private fun String.searchKey(locale: Locale): String = Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace("\\p{Mn}+".toRegex(), "")
    .lowercase(locale)
    .trim()

