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
import com.example.scanner.ui.components.organisms.TopBar
import com.example.scanner.ui.screen.AudioDetailsScreen
import com.example.scanner.ui.screen.AudioRecorderScreen
import com.example.scanner.ui.screen.TranslationListScreen
import com.example.scanner.ui.theme.ScannerTheme

enum class Screen {
    RECORDER,
    TRANSLATION_LIST,
    TRANSLATION_DETAILS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScannerTheme {
                var currentScreen by remember { mutableStateOf(Screen.RECORDER) }
                var selectedTranslationId by remember { mutableStateOf<Long?>(null) }
                
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = { TopBar() })

                { innerPadding ->
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
                                onItemClick = { id ->
                                    selectedTranslationId = id
                                    currentScreen = Screen.TRANSLATION_DETAILS
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        Screen.TRANSLATION_DETAILS -> {
                            selectedTranslationId?.let { id ->
                                AudioDetailsScreen(
                                    translationId = id,
                                    onBackClick = { currentScreen = Screen.TRANSLATION_LIST },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}