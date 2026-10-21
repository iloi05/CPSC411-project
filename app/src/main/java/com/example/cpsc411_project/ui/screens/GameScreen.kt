package com.example.cpsc411_project.ui.screens

import android.content.Context
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.airbnb.lottie.compose.*
import com.example.cpsc411_project.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val DECAY_INTERVAL_MS = 6 * 60 * 1000L
private const val POINTS_PER_INTERVAL = 5

@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }
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

    // --- NEW: Outer Box for the background ---
    Box(modifier = Modifier.fillMaxSize()) {

        // If want to change background make sure to have/change the image named
        // "game_background" in the res/drawable folder
        Image(
            painter = painterResource(id = R.drawable.game_background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Scales image to fill screen while maintaining aspect ratio
        )

        // 2. The existing UI Column (now layered on top of the background)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding() // <--- Keeps UI away from status bar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Text is now White, Bold, with a Black Shadow to stand out against the sky ---
            Text(
                text = "Your Pet: ${petType.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                ),
                color = Color.White
            )

            PetDisplay(
                petType = petType,
                happiness = happiness,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier.offset(y = (-60).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onOpenSettings,
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Open settings",
                        fontSize = 20.sp
                    )
                }

                // --- Making the happiness text Black with a shadow ---
                Text(
                    text = "Happiness: $happiness / 100",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    )
                )
                LinearProgressIndicator(progress = { happiness / 100f })
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        val newValue = (applyDecay() + 15).coerceIn(0, 100)
                        happiness = newValue
                        prefs.edit {
                            putInt("happiness", happiness)
                            putLong("lastUpdate", System.currentTimeMillis())
                        }
                    },
                        modifier = Modifier
                            .width(100.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Play",
                            fontSize = 20.sp
                        )
                    }
                    Button(onClick = {
                        val newValue = (applyDecay() + 5).coerceIn(0, 100)
                        happiness = newValue
                        prefs.edit {
                            putInt("happiness", happiness)
                            putLong("lastUpdate", System.currentTimeMillis())
                        }

                    },
                        modifier = Modifier
                            .width(100.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Pet",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetDisplay(
    petType: String,
    happiness: Int,
    modifier: Modifier = Modifier
) {
    val breathDuration = (1500 - (happiness * 10)).coerceAtLeast(500)
    val infiniteTransition = rememberInfiniteTransition(label = "pet_idle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(breathDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // --- NEW: Determine which Lottie file to load ---
    // If happiness is 100, use the happy file. Otherwise, use the normal file.
    val animRes = if (happiness == 100) {
        if (petType == "cat") R.raw.cat_happy else R.raw.dog_happy
    } else {
        if (petType == "cat") R.raw.cat_anim else R.raw.dog_anim
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- NEW: Crossfade to smoothly switch between the two Lottie files ---
        Crossfade(targetState = animRes, label = "pet_animation_switch") { currentAnimRes ->

            // Load the composition of the current file
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(currentAnimRes)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(250.dp)
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}