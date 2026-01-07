package com.example.kidsstory.domain.model

/**
 * 語言枚舉
 */
enum class Language(val code: String, val displayName: String) {
    CHINESE("zh", "中文"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: CHINESE
        }
    }
}
