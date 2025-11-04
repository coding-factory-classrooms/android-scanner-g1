package com.example.scanner.data.model

data class Language(val code: String, val name: String)

val availableLanguages = listOf(
    Language("fr-FR", "Français"),
    Language("en-US", "English"),
    Language("es-ES", "Español"),
    Language("de-DE", "Deutsch"),
    Language("it-IT", "Italiano"),
    Language("pt-BR", "Português"),
    Language("ja-JP", "日本語"),
    Language("ko-KR", "한국어"),
    Language("zh-CN", "中文")
)
