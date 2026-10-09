package com.example.cpsc411_project.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.cpsc411_project.R

@Composable
fun LoadingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pet", Context.MODE_PRIVATE) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            CircularProgressIndicator()
            Text("Choose your pet!", style = MaterialTheme.typography.headlineMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PetSelectionCard(
                    petName = "Dog",
                    imageRes = R.drawable.dog, // Make sure the file is named dog.png
                    onClick = {
                        prefs.edit { putString("pet_type", "dog") }
                        onContinue()
                    }
                )
                PetSelectionCard(
                    petName = "Cat",
                    imageRes = R.drawable.cat, // Make sure the file is named cat.png
                    onClick = {
                        prefs.edit { putString("pet_type", "cat") }
                        onContinue()
                    }
                )
            }
        }
    }
}

@Composable
fun PetSelectionCard(
    petName: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Hover animations
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1f, label = "scale")
    val elevation by animateFloatAsState(if (isHovered) 12f else 4f, label = "elevation")
    val borderColor = if (isHovered) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .size(150.dp)
            .scale(scale)
            .hoverable(interactionSource)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Displays the actual image from the drawable folder
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = petName,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text(petName, style = MaterialTheme.typography.titleLarge)
        }
    }
}