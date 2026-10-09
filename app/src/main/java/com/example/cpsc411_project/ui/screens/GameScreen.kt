package com.example.cpsc411_project.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.cpsc411_project.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

// How long (in milliseconds) until the pet loses happiness. 6 minutes.
private const val DECAY_INTERVAL_MS = 6 * 60 * 1000L
// How many happiness points are lost per interval.
private const val POINTS_PER_INTERVAL = 5

/**
 * Main composable for the Game Screen.
 * It handles:
 * 1. Loading/Saving data from SharedPreferences.
 * 2. Calculating happiness decay over time.
 * 3. Displaying the pet and UI controls.
 */
@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    // Open the SharedPreferences file named "pet". 
    // `remember` prevents reopening it on every recomposition.
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }

    // Retrieve the saved pet type (dog or cat). Defaults to "dog" if nothing is saved.
    val petType = remember { prefs.getString("pet_type", "dog") ?: "dog" }

    /**
     * Calculates how much happiness has decayed since the last time the user played.
     * It reads the last saved timestamp, calculates how many full intervals have passed,
     * subtracts the points, and saves the new values to disk.
     * Returns the updated happiness level.
     */
    fun applyDecay(): Int {
        val now = System.currentTimeMillis()
        val saved = prefs.getInt("happiness", 50) // Default happiness is 50
        val last = prefs.getLong("lastUpdate", now)

        // How many full 6-minute intervals have passed since the last update?
        val intervals = ((now - last) / DECAY_INTERVAL_MS).toInt().coerceAtLeast(0)
        // Calculate new happiness, clamping it between 0 and 100.
        val current = (saved - intervals * POINTS_PER_INTERVAL).coerceIn(0, 100)

        // Save the new happiness and update the timestamp to reflect the decay we just applied.
        prefs.edit {
            putInt("happiness", current)
            putLong("lastUpdate", last + intervals * DECAY_INTERVAL_MS)
        }
        return current
    }

    // State variable that holds the current happiness. Starts at 50.
    var happiness by remember { mutableIntStateOf(50) }

    /**
     * LaunchedEffect runs once when the screen opens.
     * 1. Immediately applies any decay that happened while the app was closed.
     * 2. Starts an infinite loop that checks for decay every 10 seconds
     *    while the game screen is actively open.
     */
    LaunchedEffect(Unit) {
        happiness = applyDecay()
        while (true) {
            delay(10.seconds)
            happiness = applyDecay()
        }
    }

    // Main UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, // Center everything horizontally
        verticalArrangement = Arrangement.spacedBy(16.dp)    // Add spacing between elements
    ) {
        // Title showing which pet the user chose
        Text(
            text = "Your Pet: ${petType.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // The animated and draggable pet image
        PetDisplay(
            petType = petType,
            happiness = happiness,
            modifier = Modifier.weight(1f) // `weight(1f)` takes up all remaining vertical space
        )

        Button(onClick = onOpenSettings) {
            Text("Open settings")
        }

        Text("Happiness: $happiness / 100")

        // A visual progress bar for happiness
        LinearProgressIndicator(progress = { happiness / 100f })

        // "Play" Button: Increases happiness and resets the decay timer
        Button(onClick = {
            // Apply any pending decay first, then add 15 points. Cap at 100.
            val newValue = (applyDecay() + 15).coerceIn(0, 100)
            happiness = newValue
            // Save the new value and reset the timer to "now"
            prefs.edit {
                putInt("happiness", happiness)
                putLong("lastUpdate", System.currentTimeMillis())
            }
        }) {
            Text("Play")
        }
    }
}

/**
 * A composable that draws the pet and handles its animations and touch interactions.
 */
@Composable
fun PetDisplay(
    petType: String,
    happiness: Int,
    modifier: Modifier = Modifier
) {
    // 1. Idle "Breathing" Animation
    // The happier the pet, the faster it "breathes". Minimum duration is 500ms.
    val breathDuration = (1500 - (happiness * 10)).coerceAtLeast(500)

    // `rememberInfiniteTransition` creates a looping animation that runs forever.
    val infiniteTransition = rememberInfiniteTransition(label = "pet_idle")
    // Animates a scale value back and forth between 1.0 and 1.05 (a 5% size increase)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(breathDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse // Reverses the animation to create a loop
        ),
        label = "scale"
    )

    // 2. Dragging State
    // These hold the current X and Y offset of the pet on the screen.
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Choose the correct image resource based on the saved pet type
    val imageRes = if (petType == "cat") R.drawable.cat else R.drawable.dog

    Box(
        modifier = modifier
            .fillMaxSize()
            // `pointerInput` allows us to listen for touch/drag gestures on this Box.
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume() // Prevents other composables from also handling this drag
                    // Update the position by adding the drag distance to our offset state
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = petType,
            modifier = Modifier
                .size(200.dp)
                // `graphicsLayer` applies visual changes without causing a full layout recalculation.
                .graphicsLayer {
                    // Move the image based on the user's drag
                    translationX = offsetX
                    translationY = offsetY
                    // Apply the "breathing" animation
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}
