package com.example.cpsc411_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cpsc411_project.ui.Cpsc411App
import com.example.cpsc411_project.ui.theme.CPSC411Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CPSC411Theme {
                Cpsc411App()
            }
        }
    }
}