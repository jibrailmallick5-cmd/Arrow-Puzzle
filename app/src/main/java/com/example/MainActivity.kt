package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.ArrowPuzzleApp
import com.example.ui.theme.ArrowPuzzleTheme
import com.example.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowPuzzleTheme {
                ArrowPuzzleApp(appViewModel = appViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appViewModel.soundEngine.setAppForeground(true)
    }

    override fun onStop() {
        super.onStop()
        appViewModel.soundEngine.setAppForeground(false)
    }
}
