package com.example.scanner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.example.scanner.ui.viewmodel.ListTranslationViewModel

@Composable
fun TranslationListScreen(
    onNavigateBack: () -> Unit = {},
    onItemClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: ListTranslationViewModel = koinViewModel<ListTranslationViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    var isFiltered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTranslations()
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Retour")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 10.dp),
                horizontalArrangement = Arrangement.Absolute.Left,
                verticalAlignment = Alignment.Bottom

        ) {

            TextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Rechercher.") },
                modifier =  Modifier
                    .fillMaxWidth(0.80f)
                    .padding(end = 5.dp)
            )

            Button(
                onClick = { isFiltered = !isFiltered },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Transparent),
                interactionSource = remember { MutableInteractionSource() },
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "filtered",
                    modifier = Modifier.padding(bottom = 5.dp, top = 5.dp).height(30.dp),
                    tint = if (isFiltered) Color(0xFF1976D2) else Color.Gray
                )
                Text("")

            }
        }


        
        TranslationScreenBody(
            uiState = uiState,
            onItemClick = onItemClick,
            onDelete = { id -> viewModel.deleteTranslation(id) },
            onToggleFavorite = { id, currentIsFave -> viewModel.toggleFavorite(id, currentIsFave) },
            modifier = Modifier,
            isFiltered = isFiltered
        )
    }
}