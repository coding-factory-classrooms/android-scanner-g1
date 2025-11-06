package com.example.scanner.ui.screen

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
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
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDetailsScreen(
    translationId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AudioDetailsViewModel = koinViewModel<AudioDetailsViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(translationId) {
        viewModel.loadTranslation(translationId)
    }

    Scaffold(
        modifier = modifier,
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
                },
                actions = {
                    val isFave = uiState.translation?.isFave == true
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFave) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFave) "Retirer des favoris" else "Ajouter aux favoris"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deleteTranslation(onBackClick) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer"
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
