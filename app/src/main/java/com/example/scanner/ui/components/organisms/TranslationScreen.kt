package com.example.scanner.ui.components.organisms
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


fun getFilteredDataArray(data: List<Translation>, isFiltered: Boolean): List<Translation> {
    return if (isFiltered) {
        data.filter { it.isFave }
    } else {
        data
    }
}


@Composable
fun TranslationScreenBody(
    uiState: UiState,
    onItemClick: (Long) -> Unit = {},
    onDelete: (Long) -> Unit,
    onToggleFavorite: (id: Long, currentIsFave: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isFiltered: Boolean
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

            TranslationList(
                translations = uiState.filteredTranslations,
                onItemClick = onItemClick,
                onDelete = onDelete,
                onToggleFavorite = onToggleFavorite,
                isFiltered = isFiltered
            )
        }
    }
}

@Composable
fun TranslationList(
    translations: List<Translation>,
    onItemClick: (Long) -> Unit = {},
    onDelete: (Long) -> Unit,
    onToggleFavorite: (id: Long, currentIsFave: Boolean) -> Unit = { _, _ -> },
    isFiltered: Boolean
) {
    LazyColumn {
        items(getFilteredDataArray(translations, isFiltered = isFiltered)) { translation ->
            TranslationItem(
                translation = translation,
                onItemClick = { onItemClick(translation.id) },
                onDeleteClick = { onDelete(translation.id) },
                onFavoriteClick = { onToggleFavorite(translation.id, translation.isFave) }
            )
        }
    }
}
