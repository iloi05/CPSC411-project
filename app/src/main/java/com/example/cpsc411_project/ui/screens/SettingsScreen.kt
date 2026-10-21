package com.example.cpsc411_project.ui.screens

import androidx.compose.foundation.layout.Arrangement
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

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onRepickPet: () -> Unit // <--- Added callback for navigating back to loading screen
) {
    // State to control whether the confirmation pop-up is visible
    var showRepickDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding() // <--- Added this line for safe area padding
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings screen placeholder",
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