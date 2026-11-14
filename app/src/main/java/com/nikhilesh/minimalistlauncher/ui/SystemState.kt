package com.nikhilesh.minimalistlauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun rememberBatteryPercentage(): State<Int> {
    val context = LocalContext.current
    val battery = remember { mutableIntStateOf(readBatteryPercentage(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent != null) battery.intValue = intent.batteryPercentage()
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    return battery
}

@Composable
fun rememberCurrentMinute(): State<ZonedDateTime> = produceState(ZonedDateTime.now()) {
    while (isActive) {
        val now = ZonedDateTime.now()
        value = now
        val millisUntilNextMinute = 60_000L - (now.second * 1_000L + now.nano / 1_000_000L)
        delay(millisUntilNextMinute.coerceAtLeast(250L))
    }
}

private fun readBatteryPercentage(context: Context): Int = context
    .registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    ?.batteryPercentage()
    ?: 0

private fun Intent.batteryPercentage(): Int {
    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    return if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else 0
}

