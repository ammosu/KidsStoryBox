package com.example.kidsstory.domain.model

/**
 * 故事分類枚舉
 */
enum class StoryCategory(val displayNameZh: String, val displayNameEn: String) {
    ADVENTURE("冒險", "Adventure"),
    FRIENDSHIP("友誼", "Friendship"),
    FAMILY("家庭", "Family"),
    SCIENCE("科普", "Science"),
    NATURE("自然", "Nature"),
    FANTASY("奇幻", "Fantasy"),
    DAILY_LIFE("日常生活", "Daily Life"),
    MORAL("品德教育", "Moral Education");

    companion object {
        fun fromString(value: String): StoryCategory {
            return values().find { it.name == value } ?: ADVENTURE
        }
    }
}
