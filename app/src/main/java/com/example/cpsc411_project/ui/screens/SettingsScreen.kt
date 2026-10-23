package com.example.cpsc411_project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding // <--- Added import
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.cpsc411_project.R

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRepickPet: () -> Unit // <--- Added callback for navigating back to loading screen
) {
    // State to control whether the confirmation pop-up is visible
    var showRepickDialog by remember { mutableStateOf(false) }
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

            // New Button to trigger the dialog
            Button(onClick = { showRepickDialog = true }) {
                Text("Repick Your Pet")
            }

            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }



    // The Pop-up Confirmation Dialog
    if (showRepickDialog) {
        AlertDialog(
            onDismissRequest = { showRepickDialog = false }, // Closes if user taps outside
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
}