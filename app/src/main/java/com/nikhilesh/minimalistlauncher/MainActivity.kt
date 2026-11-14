package com.nikhilesh.minimalistlauncher

import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikhilesh.minimalistlauncher.model.LauncherPalette
import com.nikhilesh.minimalistlauncher.ui.FavoriteOnboarding
import com.nikhilesh.minimalistlauncher.ui.HomeRoleOnboarding
import com.nikhilesh.minimalistlauncher.ui.LauncherPager
import com.nikhilesh.minimalistlauncher.ui.theme.MinimalistTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()
    private val homeRoleHeld = mutableStateOf(false)
    private val roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refreshHomeRole()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureSystemBars()
        refreshHomeRole()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MinimalistTheme(settings = uiState.settings) {
                SystemBarAppearance(uiState.settings.palette)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    when {
                        !homeRoleHeld.value -> HomeRoleOnboarding(
                            onRequestHomeRole = ::requestHomeRole,
                        )

                        !uiState.settings.onboardingComplete -> FavoriteOnboarding(
                            apps = uiState.apps,
                            selectedIds = uiState.settings.favoriteIds,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onContinue = viewModel::completeOnboarding,
                        )

                        else -> LauncherPager(
                            uiState = uiState,
                            onQueryChange = viewModel::setQuery,
                            onLaunch = viewModel::launch,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onOpenAppInfo = viewModel::openAppInfo,
                            onMoveFavorite = viewModel::moveFavorite,
                            onSetPalette = viewModel::setPalette,
                            onSetTextScale = viewModel::setTextScale,
                            onOpenHomeSettings = ::openHomeSettings,
                            onOpenWebSearch = ::openWebSearch,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHomeRole()
        configureSystemBars()
    }

    private fun requestHomeRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        roleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
    }

    private fun refreshHomeRole() {
        val roleManager = getSystemService(RoleManager::class.java)
        homeRoleHeld.value = roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
            roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    }

    private fun openHomeSettings() {
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    private fun openWebSearch() {
        val googleHome = "https://www.google.com".toUri()
        val chrome = Intent(Intent.ACTION_VIEW, googleHome).apply {
            setPackage("com.android.chrome")
        }
        try {
            startActivity(chrome, chromeLaunchAnimation())
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, googleHome), chromeLaunchAnimation())
        }
    }

    private fun chromeLaunchAnimation(): Bundle = ActivityOptions.makeCustomAnimation(
        this,
        R.anim.chrome_enter_from_left,
        R.anim.launcher_exit_to_right,
    ).toBundle()

    private fun configureSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
private fun ComponentActivity.SystemBarAppearance(palette: LauncherPalette) {
    val view = LocalView.current
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightNavigationBars = palette == LauncherPalette.PAPER
            isAppearanceLightStatusBars = palette == LauncherPalette.PAPER
        }
    }
}

