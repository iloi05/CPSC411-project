package com.example.cpsc411_project.ui.screens

import com.example.cpsc411_project.SoundManager
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.res.painterResource
import com.example.cpsc411_project.ui.components.SpriteRegion
import com.example.cpsc411_project.ui.pet.PetViewModel


// Buttons and information for sprite go here

@Composable
fun GameScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current

    val vm: PetViewModel = viewModel()
    val state by vm.state.collectAsState()

    val petType = state.petType
    val happiness = state.happiness
    val hunger = state.hunger
    val clean = state.clean
    val isEating = state.isEating
    val isPlaying = state.isPlaying
    val isCleaning = state.isCleaning
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


    var showSoap by remember { mutableStateOf(false) }
    var soapOffset by remember { mutableStateOf(Offset.Zero) }
    var soapCenter by remember { mutableStateOf(Offset.Zero) }
    var petBounds by remember { mutableStateOf(Rect.Zero) }

    // --- NEW: Outer Box for the background ---
    Box(modifier = Modifier.fillMaxSize()) {

        // If you want to change background make sure to have/change the image named
        // "game_background" in the res/drawable folder
        TiledBackground(
            sheetRes = R.drawable.interiorfree,
            modifier = Modifier.fillMaxSize()
        )
        // cat-tree display
        if (petType == "cat") {
            SpriteRegion(
                sheetRes = R.drawable.catitems,
                srcX = 0, srcY = 256, srcW = 96, srcH = 192,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 600.dp)
                    .size(width = 192.dp, height = 384.dp)
            )
        }
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
            bottomInset = 190.dp,
            onPetBoundsChanged = { petBounds = it }
        )
        // settings button
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
                    // play button
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
                                    val s = vm.play()
                                    chatViewModel.reactToAction("Play", s.petType, s.happiness, s.hunger, s.clean)
                                }
                        )
                    }
                    // pet button
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
                                   val s = vm.pet()
                                    chatViewModel.reactToAction("Pet", s.petType, s.happiness, s.hunger, s.clean)
                                }
                        )
                    }
                    // feed button
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
                                    val s = vm.feed()
                                    chatViewModel.reactToAction("Feed", s.petType, s.happiness, s.hunger, s.clean)
                                }
                        )
                    }
                    // clean button
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
                                    showSoap = !showSoap
                                    if (showSoap) soapOffset = Offset.Zero
                                }
                        )
                    }
                }
            }

        }
        // draggable soap (disappears when clean meter reaches 100)
        if (showSoap) {
            Image(
                painter = painterResource(R.drawable.soap),
                contentDescription = "Soap",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(96.dp)
                    .offset { IntOffset(soapOffset.x.roundToInt(), soapOffset.y.roundToInt()) }
                    .onGloballyPositioned { soapCenter = it.boundsInRoot().center}
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            soapOffset += dragAmount
                            // raises clean meter if soap is dragged over pet
                            if (petBounds.contains(soapCenter)) {
                                val s = vm.onScrub(dragAmount.getDistance())
                                if (s != null && s.clean >= 100) showSoap = false
                            }
                        }
                    }
            )
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