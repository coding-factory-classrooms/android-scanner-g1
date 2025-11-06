package com.example.scanner.ui.screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.scanner.data.model.Translation
import com.example.scanner.ui.components.molecules.TranslationItem
import com.example.scanner.ui.viewmodel.ListTranslationViewModel.UiState


@Composable
fun TranslationScreenBody(
    uiState: UiState,
    onItemClick: (Long) -> Unit = {},
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Chargement...")
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Erreur: ${uiState.errorMessage}")
            }
        } else {
            TranslationList(translations = uiState.translations, onItemClick = onItemClick, onDelete = onDelete)
        }
    }
}

@Composable
fun TranslationList(
    translations: List<Translation>,
    onItemClick: (Long) -> Unit = {},
    onDelete: (Long) -> Unit
) {
    LazyColumn {
        items(translations) { translation ->
            TranslationItem(
                translation = translation,
                onItemClick = { onItemClick(translation.id) },
                onDeleteClick = { onDelete(translation.id) }
            )
        }
    }
}
