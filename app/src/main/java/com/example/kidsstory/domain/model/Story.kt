package com.example.kidsstory.domain.model

/**
 * 故事領域模型
 */
data class Story(
    val id: String,
    val title: String,
    val titleEn: String,
    val category: StoryCategory,
    val ageRange: String,
    val coverImage: String,
    val segments: List<StorySegment>,
    val isPreset: Boolean = true,
    val isDownloaded: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val duration: Int
        get() = segments.sumOf { it.duration }
}

/**
 * 故事段落領域模型
 */
data class StorySegment(
    val sequenceNumber: Int,
    val contentZh: String,
    val contentEn: String,
    val characterRole: CharacterRole,
    val duration: Int,
    val image: String? = null,
    val audioPathZh: String? = null,
    val audioPathEn: String? = null
)
