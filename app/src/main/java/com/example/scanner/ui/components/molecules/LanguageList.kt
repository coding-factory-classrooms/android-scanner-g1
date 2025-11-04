package com.example.scanner.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanner.data.model.availableLanguages
import com.example.scanner.ui.components.atoms.LanguageItem

@Composable
fun LanguageList(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.padding(16.dp, 8.dp)) {
        items(availableLanguages) { language ->
            LanguageItem(
                languageName = language.name,
                isSelected = language.code == selectedLanguageCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(language.code) }
            )
        }
    }
}
