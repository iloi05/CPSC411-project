package com.example.cpsc411_project.ui.screens

import android.graphics.BitmapFactory
import com.example.cpsc411_project.SoundManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding // <--- Added import
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.cpsc411_project.R
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import  androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalResources

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRepickPet: () -> Unit,
    onLogout: () -> Unit // <--- Added logout callback
) {
    val context = LocalContext.current
    // State to control whether the confirmation pop-ups are visible
    var showRepickDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(SoundManager.getVolume()) }

    // starts music when game is opened (game screen and setting screen only)
    LaunchedEffect(Unit) {
        SoundManager.startMusic(context)
    }

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
            Text(
                text = "Settings",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )

            Text( text = "Volume", color = Color.White, fontSize = 20.sp)
            VolumeBar(
                sheetRes = R.drawable.settings,
                volume = volume,
                // volume bar sprite reports change
                onVolumeChange = {
                    volume = it // brown bars turn green vice versa
                    SoundManager.setVolume(context, it) // saves changed volume
                },
                modifier = Modifier.width(200.dp).height(60.dp),
                emptyX = 2, fullX = 50, srcY = 195, srcW = 44, srcH = 12,
                thumbX = 2, thumbY = 214, thumbW = 16, thumbH = 22
            )

            // New Button to trigger the dialog
            Button(onClick = { showRepickDialog = true }) {
                Text("Repick Your Pet")
            }

            // Log Out Button
            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Log Out")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }

    // The Repick Confirmation Dialog
    if (showRepickDialog) {
        AlertDialog(
            onDismissRequest = { showRepickDialog = false },
            title = { Text("Repick Your Pet") },
            text = { Text("Are you sure you want to go back and switch pets?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRepickDialog = false
                        onRepickPet() // Triggers navigation
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRepickDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    // The Log-Out Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout() // Triggers navigation and clears state
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}
@Composable
fun VolumeBar(
    sheetRes: Int,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    emptyX: Int,
    fullX: Int,
    srcY: Int,
    srcW: Int,
    srcH: Int,
    thumbX: Int,
    thumbY: Int,
    thumbW: Int,
    thumbH: Int,
    segments: Int = 10
) {
    val resources = LocalResources.current
    val sheet = remember(sheetRes, resources) {
        val opts = BitmapFactory.Options().apply { inScaled = false }
        BitmapFactory.decodeResource(resources, sheetRes, opts).asImageBitmap()
    }
    val currentOnChange by rememberUpdatedState(onVolumeChange)
    fun snap(x: Float, width: Float): Float =
        (((x / width) * segments).roundToInt() / segments.toFloat()).coerceIn(0f, 1f)

    Canvas(
        modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentOnChange(snap(offset.x, size.width.toFloat()))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    currentOnChange(snap(change.position.x, size.width.toFloat()))
                }
            }
    ){
        val filled = (volume * segments).roundToInt()
        val segSrcW = srcW / segments
        val segDstW = size.width /segments

        val barH = size.height * 0.5f
        val barTop = ((size.height - barH) / 2f).roundToInt()

        for (i in 0 until segments) {
            val startX = (if (i < filled) fullX else emptyX) + i * segSrcW
            val dstLeft = (i * segDstW).roundToInt()
            val dstRight = ((i + 1) * segDstW).roundToInt()
            drawImage(
                image = sheet,
                srcOffset = IntOffset(startX, srcY),
                srcSize = IntSize(segSrcW, srcH),
                dstOffset = IntOffset(dstLeft, barTop),
                dstSize = IntSize(dstRight - dstLeft, barH.roundToInt()),
                filterQuality = FilterQuality.None
            )
        }
        val thumbDstH = size.height
        val thumbDstW = thumbDstH * thumbW / thumbH
        val thumbLeft = (volume * size.width - thumbDstW / 2f)
            .coerceIn(0f, size.width - thumbDstW)
        // draws the nob on the volume bar
        drawImage(
            image = sheet,
            srcOffset = IntOffset(thumbX, thumbY),
            srcSize = IntSize(thumbW, thumbH),
            dstOffset = IntOffset(thumbLeft.roundToInt(), 0),
            dstSize = IntSize(thumbDstW.roundToInt(), thumbDstH.roundToInt()),
            filterQuality = FilterQuality.None
        )
    }
}