package com.example.scanner.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.data.model.Translation
import com.example.scanner.ui.viewmodel.AudioDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDetailsScreen(
    viewModel: AudioDetailsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails audio") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = "Erreur : ${uiState.error}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.translation != null -> {
                    AudioDetailsContent(
                        translation = uiState.translation!!
                    )
                }

                else -> {
                    Text(
                        text = "Aucune donnée disponible.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}


@Composable
fun AudioDetailsContent(
    translation: Translation
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // === Player ===
        AudioPlayer(path = translation.pathAudioFile)

        // === Original text ===
        Column {
            Text(
                text = "Langue d’origine : ${translation.inputLange}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = translation.originalText,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // === Translated text ===
        Column {
            Text(
                text = "Langue traduite : ${translation.outputLange}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = translation.tradText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AudioPlayer(path: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setDataSource(path)
            setOnPreparedListener {
                // Optionnel : tu peux démarrer automatiquement ici si tu veux
            }
            prepareAsync()
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = {
            if (isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }
            isPlaying = !isPlaying
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }

        Text(
            text = if (isPlaying) "Lecture en cours..." else "Appuyer pour écouter",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
