package com.example.scanner.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import com.example.scanner.ui.viewmodel.ListTranslationViewModel

@Composable
fun TranslationListScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: ListTranslationViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTranslations()
    }

    TranslationScreenBody(uiState = uiState, modifier = modifier)
}