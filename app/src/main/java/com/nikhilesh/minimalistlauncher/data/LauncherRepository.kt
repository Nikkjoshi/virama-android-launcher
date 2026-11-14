package com.nikhilesh.minimalistlauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.nikhilesh.minimalistlauncher.model.LauncherApp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

interface LauncherRepository {
    val apps: Flow<List<LauncherApp>>

    fun launch(app: LauncherApp)

    fun openAppInfo(app: LauncherApp)
}

class AndroidLauncherRepository(
    context: Context,
) : LauncherRepository {
    private val applicationContext = context.applicationContext
    private val launcherApps = applicationContext.getSystemService(LauncherApps::class.java)
    private val currentUser = Process.myUserHandle()

    override val apps: Flow<List<LauncherApp>> = callbackFlow {
        fun publishApps() {
            trySend(queryApps())
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) = publishApps()

            override fun onPackageAdded(packageName: String, user: android.os.UserHandle) = publishApps()

            override fun onPackageChanged(packageName: String, user: android.os.UserHandle) = publishApps()

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) = publishApps()

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: android.os.UserHandle,
                replacing: Boolean,
            ) = publishApps()
        }

        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        publishApps()
        awaitClose { launcherApps.unregisterCallback(callback) }
    }.flowOn(Dispatchers.Default)

    override fun launch(app: LauncherApp) {
        val component = ComponentName.unflattenFromString(app.id) ?: return
        launcherApps.startMainActivity(component, currentUser, null, null)
    }

    override fun openAppInfo(app: LauncherApp) {
        val component = ComponentName.unflattenFromString(app.id) ?: return
        launcherApps.startAppDetailsActivity(component, currentUser, null, null)
    }

    private fun queryApps(): List<LauncherApp> = launcherApps
        .getActivityList(null, currentUser)
        .asSequence()
        .filterNot { it.componentName.packageName == applicationContext.packageName }
        .map { info ->
            LauncherApp(
                id = info.componentName.flattenToString(),
                label = info.label?.toString()?.trim().orEmpty()
                    .ifBlank { info.componentName.packageName },
            )
        }
        .distinctBy(LauncherApp::id)
        .toList()
        .let { sortApps(it) }
}

