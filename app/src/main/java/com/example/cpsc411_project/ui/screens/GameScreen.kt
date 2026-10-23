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
import com.example.cpsc411_project.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntSize
import android.annotation.SuppressLint
import kotlin.time.Duration.Companion.milliseconds


// Buttons and information for sprite go here
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

        // If you want to change background make sure to have/change the image named
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

// Sprites go here
// defining the numbers needed for a sprite animation
private data class Anim(val row: Int, val frames: Int, val startCol: Int = 0)
// Defining everything needed to draw one sprite
private data class PetSprites(val sheetRes: Int, val idle: Anim, val happy: Anim)
// The png files for the animations are an array, start counting from 0 to find animation you want
// Dog animations
private val DOG_SPRITES = PetSprites(
    sheetRes = R.drawable.australian_shepherd,
    idle = Anim(row = 22, frames = 3),
    happy = Anim(row = 12, frames = 4)
)
// Cat animations
private val CAT_SPRITES = PetSprites(
    sheetRes = R.drawable.cat_3,
    idle = Anim(row = 19, frames = 5),
    happy = Anim(row = 15, frames = 3)
)


@Composable
// Displays sprite on screen
fun PetDisplay(
    petType: String,
    happiness: Int,
    modifier: Modifier = Modifier
) {
    // Making the sprite pulse on screen (will pulse faster when happiness meter is high)
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

    // For moving the sprite around on screen (remembering where they are left)

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // picking the sprite
    val sprites = if (petType == "cat") CAT_SPRITES else DOG_SPRITES
    val anim = if (happiness == 100) sprites.happy else sprites.idle
    // centers sprite, lets user drag sprite wherever
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

        SpriteAnimation(
            sheetRes = sprites.sheetRes,
            row = anim.row,
            frameCount = anim.frames,
            startCol = anim.startCol,
            modifier = Modifier
                .size(256.dp)
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    scaleX = scale
                    scaleY = scale
                }
        )

    }
}
// calling SpriteAnimation to draw chosen animation sequence
// Also moves pet, and pet moves smoothly, no recomposing entire layout every frame
@SuppressLint("LocalContextResourcesRead")
@Composable
fun SpriteAnimation(
    sheetRes: Int,
    row: Int,
    frameCount: Int,
    modifier: Modifier = Modifier,
    startCol: Int = 0,
    frameSize: Int = 64,
    fps: Int = 8
) {
    val context = LocalContext.current
    val sheet = remember(sheetRes) {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        BitmapFactory.decodeResource(context.resources, sheetRes, opts).asImageBitmap()
    }
    var frame by remember(row, frameCount, startCol) { mutableIntStateOf(0)}
    LaunchedEffect(row, frameCount, startCol, fps) {
        while (true) {
            delay((1000 / fps).milliseconds)
            frame = (frame + 1) % frameCount
        }
    }
    // For the animations, Compose redraws canvas, grabs next frame and stretches it to fill the square
    Canvas(modifier) {
        drawImage(
            image = sheet,
            srcOffset = IntOffset((startCol + frame) * frameSize, row * frameSize),
            srcSize = IntSize(frameSize, frameSize),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.None
        )
    }
}