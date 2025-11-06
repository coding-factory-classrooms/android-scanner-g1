package com.example.scanner.ui.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanner.data.model.Translation
import com.example.scanner.data.model.availableLanguages
import com.example.scanner.ui.components.atoms.LanguageItem

@Composable
fun TranslationItem(
    translation: Translation,
    onItemClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(translation.OriginaleText)
            Text(translation.TradText)
            Text(translation.inputLange)
            Text(translation.outputLange)

            // Bouton favori
        }
    }

}