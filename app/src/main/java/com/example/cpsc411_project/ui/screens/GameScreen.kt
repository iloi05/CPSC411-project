package com.example.cpsc411_project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

// For happiness decay (drops 5 pts every 6 mins)
private const val DECAY_INTERVAL_MS = 6 * 60 * 1000L
// for the decay (5 pts)
private const val POINTS_PER_INTERVAL = 5
@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }
    // Keeping track of decay
    fun applyDecay(): Int {
        val now = System.currentTimeMillis()
        // player starts off with 50 happiness points, after it's whatever they have after the decay
        val saved = prefs.getInt("happiness", 50)
        // keeps track of last decay interval that was completed, and also last Play press
        val last = prefs.getLong("lastUpdate", now)
        // num of full decay intervals since the last update (when player last played with pet)
        val intervals = ((now - last) / DECAY_INTERVAL_MS).toInt().coerceAtLeast(0)
        // subtracting decay, keeping happiness level between 0 & 100
        val current = (saved - intervals * POINTS_PER_INTERVAL).coerceIn(0, 100)

        // keeps track of happiness meter, resets timer  every time user plays with pet
        prefs.edit {
            putInt("happiness", current)
            putLong("lastUpdate", last + intervals * DECAY_INTERVAL_MS)
        }
        // returns updated meter (current happiness level)
        return current
    }
    // keeps meter updated while game is open
    var happiness by remember { mutableIntStateOf(applyDecay()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10.seconds)
            happiness = applyDecay()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        // should center the button column wise
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Game screen placeholder",
            style = MaterialTheme.typography.headlineSmall
        )
        Button(onClick = onOpenSettings) {
            Text("Open settings")
        }
        Text("Happiness: $happiness / 100")
        LinearProgressIndicator(progress = { happiness / 100f })
        Button(onClick = {
            // updates happiness meter (15 pts everytime you play with the pet)
            // also resets decay timer
            val newValue = (applyDecay() + 15).coerceIn(0, 100)
            happiness = newValue
            prefs.edit { putInt("happiness", happiness)
            putLong("lastUpdate", System.currentTimeMillis())
            }
        }) {
            Text("Play")
        }
    }
}