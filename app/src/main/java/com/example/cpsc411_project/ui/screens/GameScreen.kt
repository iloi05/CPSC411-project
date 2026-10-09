package com.example.cpsc411_project.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val DECAY_INTERVAL_MS = 6 * 60 * 1000L
private const val POINTS_PER_INTERVAL = 5

@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }

    // Read the chosen pet
    val petType = remember { prefs.getString("pet_type", "dog") ?: "dog" }

    fun applyDecay(): Int {
        val now = System.currentTimeMillis()
        val saved = prefs.getInt("happiness", 50)
        val last = prefs.getLong("lastUpdate", now)
        val intervals = ((now - last) / DECAY_INTERVAL_MS).toInt().coerceAtLeast(0)
        val current = (saved - intervals * POINTS_PER_INTERVAL).coerceIn(0, 100)

        prefs.edit {
            putInt("happiness", current)
            putLong("lastUpdate", last + intervals * DECAY_INTERVAL_MS)
        }
        return current
    }

    var happiness by remember { mutableIntStateOf(50) }

    LaunchedEffect(Unit) {
        happiness = applyDecay()
        while (true) {
            delay(10.seconds)
            happiness = applyDecay()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- NEW: Pet Indicator ---
        Text(
            text = "Your Pet: ${petType.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        // --------------------------

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
            val newValue = (applyDecay() + 15).coerceIn(0, 100)
            happiness = newValue
            prefs.edit {
                putInt("happiness", happiness)
                putLong("lastUpdate", System.currentTimeMillis())
            }
        }) {
            Text("Play")
        }
    }
}