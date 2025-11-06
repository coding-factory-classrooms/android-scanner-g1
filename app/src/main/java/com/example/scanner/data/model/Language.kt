package com.example.scanner.data.model

data class Language(
    val code: String, 
    val name: String
)

val availableLanguages = listOf(
    Language("fr", "Français"),
    Language("en", "English"),
    Language("es", "Español"),
    Language("de", "Deutsch"),
    Language("it", "Italiano"),
    Language("pt", "Português"),
    Language("ja", "日本語"),
    Language("ko", "한국어"),
    Language("zh", "中文")
)

fun findLanguageByCode(code: String): Language? {
    return availableLanguages.find { it.code == code }
}
