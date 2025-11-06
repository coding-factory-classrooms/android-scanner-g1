package com.example.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.scanner.ui.screen.AudioRecorderScreen
import com.example.scanner.ui.screen.TranslationListScreen
import com.example.scanner.ui.theme.ScannerTheme

enum class Screen {
    RECORDER,
    TRANSLATION_LIST
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScannerTheme {
                var currentScreen by remember { mutableStateOf(Screen.RECORDER) }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.RECORDER -> {
                            AudioRecorderScreen(
                                onNavigateToTranslationList = { currentScreen = Screen.TRANSLATION_LIST },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.TRANSLATION_LIST -> {
                            TranslationListScreen(
                                onNavigateBack = { currentScreen = Screen.RECORDER },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}