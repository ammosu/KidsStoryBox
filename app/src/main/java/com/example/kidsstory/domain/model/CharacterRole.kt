package com.example.kidsstory.domain.model

/**
 * 角色類型枚舉
 */
enum class CharacterRole(val displayNameZh: String, val displayNameEn: String) {
    NARRATOR("旁白", "Narrator"),
    PROTAGONIST("主角", "Protagonist"),
    SUPPORTING("配角", "Supporting Character"),
    PARENT("家長", "Parent"),
    ANIMAL("動物", "Animal");

    companion object {
        fun fromString(value: String): CharacterRole {
            return values().find { it.name == value } ?: NARRATOR
        }
    }
}
