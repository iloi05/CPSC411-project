package com.example.cpsc411_project.ui.screens

import com.example.cpsc411_project.SoundManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cpsc411_project.R
import com.example.cpsc411_project.ui.chat.PetChatViewModel
import com.example.cpsc411_project.ui.components.ChatDialog
import com.example.cpsc411_project.ui.components.PetDisplay
import com.example.cpsc411_project.ui.components.SpriteAnimation
import com.example.cpsc411_project.ui.components.TiledBackground
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

// Buttons and information for sprite go here
private const val DECAY_INTERVAL_MS = 6 * 60 * 1000L
private const val POINTS_PER_INTERVAL = 5

@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }
    val petType = remember { prefs.getString("pet_type", "dog") ?: "dog" }

    // State for the Chat Popup
    var showChatDialog by remember { mutableStateOf(false) }
    val chatViewModel: PetChatViewModel = viewModel()

    // --- NEW: Observe the pet's AI reaction ---
    val petReaction by chatViewModel.petReaction.collectAsState()

    // State for draggable speech bubble
    var bubbleOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val initialBubbleY = with(density) { 120.dp.toPx() } // Places it just below the stats bar

    // Automatically clear the reaction after 5 seconds
    LaunchedEffect(petReaction) {
        if (petReaction != null) {
            delay(5.seconds)
            chatViewModel.clearReaction()
        }
    }

    LaunchedEffect(Unit) {
        SoundManager.startMusic(context)
    }
    // happiness and update keys depend on pet type
    val happinessKey = "happiness_$petType"
    val happinessTimeKey = "happinessUpdate_$petType"
    val hungerKey = "hunger_$petType"
    val hungerTimeKey = "hungerUpdate_$petType"
    val cleanKey = "clean_$petType"
    val cleanTimeKey = "cleanUpdate_$petType"
    fun applyDecay(valueKey: String, timeKey: String, default: Int): Int {
        val now = System.currentTimeMillis()
        val saved = prefs.getInt(valueKey, default)
        val last = prefs.getLong(timeKey, now)
        val intervals = ((now - last) / DECAY_INTERVAL_MS).toInt().coerceAtLeast(0)
        val current = (saved - intervals * POINTS_PER_INTERVAL).coerceIn(0, 100)

        prefs.edit {
            putInt(valueKey, current)
            putLong(timeKey, last + intervals * DECAY_INTERVAL_MS)
        }
        return current
    }

    fun changeStat(valueKey: String, timeKey: String, default: Int, amount: Int): Int {
        val newValue = (applyDecay(valueKey, timeKey, default) + amount).coerceIn(0, 100)
        prefs.edit(commit = true) { putInt(valueKey, newValue) }
        return newValue
    }

    var happiness by remember { mutableIntStateOf(prefs.getInt(happinessKey, 50)) }
    var hunger by remember { mutableIntStateOf(prefs.getInt(hungerKey, 50))}
    var clean by remember { mutableIntStateOf(prefs.getInt(cleanKey, 50))}
    // for eating animation, looks to see if sprite is in eating state or not
    var feedCount by remember { mutableIntStateOf(0) }
    var isEating by remember { mutableStateOf(false) }
    // switches to eating state for 2 seconds
    LaunchedEffect(feedCount) {
        if (feedCount > 0) {
            isEating = true
            delay(2.seconds)
            isEating = false
        }
    }
    // switches to playing state for 2 seconds
    var playCount by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(playCount) {
        if (playCount > 0) {
            isPlaying = true
            delay(2.seconds)
            isPlaying = false
        }
    }
    // // switches to cleaning state for 2 seconds
    var cleanCount by remember { mutableIntStateOf(0) }
    var isCleaning by remember { mutableStateOf(false) }

    LaunchedEffect(cleanCount) {
        if (cleanCount > 0) {
            isCleaning = true
            delay(2.seconds)
            isCleaning = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            happiness = applyDecay(happinessKey, happinessTimeKey, 50)
            hunger = applyDecay(hungerKey, hungerTimeKey, 50)
            clean = applyDecay(cleanKey, cleanTimeKey, 50)
            delay(10.seconds)

        }
    }

    // --- NEW: Outer Box for the background ---
    Box(modifier = Modifier.fillMaxSize()) {

        // If you want to change background make sure to have/change the image named
        // "game_background" in the res/drawable folder
        TiledBackground(
            sheetRes = R.drawable.interiorfree,
            modifier = Modifier.fillMaxSize()
        )
        PetDisplay(
            // hoisting states
            petType = petType,
            happiness = happiness,
            isEating = isEating,
            isPlaying = isPlaying,
            isCleaning = isCleaning,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            topInset = 100.dp,
            bottomInset = 190.dp
        )
        SpriteAnimation(
            sheetRes = R.drawable.button,
            row = 9,
            frameCount = 1,
            startCol = 8,
            frameSize = 16,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .size(64.dp)
                .clickable(onClick = onOpenSettings)
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
            // --- Making the happiness text Black with a shadow ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "\uD83D\uDE0A" to happiness,
                    "\uD83C\uDF7D\uFE0F" to hunger,
                    "\uD83E\uDDFC" to clean
                ).forEach { (label, value) ->
                    Text(
                        text = "$label: $value / 100",
                        color = Color.White,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        )
                    )
                }

            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.offset(y = (-60).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CHANGED: Now just sets a boolean to true instead of navigating
                Button(
                    onClick = { showChatDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    // FIXED: Redundancy caused by import and the names you had, shortened this
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Chat with your Pet!", fontSize = 18.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f).height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpriteAnimation(
                            sheetRes = R.drawable.play,
                            row = 0,
                            frameCount = 1,
                            frameSize = 32,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    happiness = changeStat(happinessKey, happinessTimeKey, 50, 5)
                                    chatViewModel.reactToAction("Play", petType, happiness, hunger, clean)
                                }
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpriteAnimation(
                            sheetRes = R.drawable.pet,
                            row = 0,
                            frameCount = 1,
                            frameSize = 32,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    happiness = changeStat(happinessKey, happinessTimeKey, 50, 5)
                                    chatViewModel.reactToAction("Pet", petType, happiness, hunger, clean)
                                }
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpriteAnimation(
                            sheetRes = R.drawable.feed,
                            row = 0,
                            frameCount = 1,
                            frameSize = 32,
                            modifier = Modifier
                                .size(85.dp)
                                .clickable {
                                    hunger = changeStat(hungerKey, hungerTimeKey, 50, 5)
                                    chatViewModel.reactToAction("Feed", petType, happiness, hunger, clean)
                                }
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SpriteAnimation(
                            sheetRes = R.drawable.clean,
                            row = 0,
                            frameCount = 1,
                            frameSize = 32,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    clean = changeStat(cleanKey, cleanTimeKey, 50, 5)
                                    chatViewModel.reactToAction("Clean", petType, happiness, hunger, clean)
                                }
                        )
                    }
                }
            }
        }

        // --- NEW: Draggable AI Speech Bubble ---
        AnimatedVisibility(
            visible = petReaction != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                // Offset placed here so it starts below the state bar, but can be dragged anywhere
                .offset { IntOffset(bubbleOffset.x.roundToInt(), (bubbleOffset.y + initialBubbleY).roundToInt()) }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    // Added pointerInput to make the bubble draggable
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            bubbleOffset += dragAmount
                        }
                    }
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = petReaction ?: "",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // --- NEW: Chat Popup Dialog ---
    if (showChatDialog) {
        ChatDialog(
            petType = petType,
            happiness = happiness,
            hunger = hunger,
            clean = clean,
            onDismiss = { showChatDialog = false },
            viewModel = chatViewModel
        )
    }
}