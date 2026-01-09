package com.example.kidsstory.data.mapper

import com.example.kidsstory.data.database.entity.StoryEntity
import com.example.kidsstory.data.database.entity.StorySegmentEntity
import com.example.kidsstory.data.database.relation.StoryWithSegments
import com.example.kidsstory.data.local.model.StoryJson
import com.example.kidsstory.data.local.model.StorySegmentJson
import com.example.kidsstory.domain.model.CharacterRole
import com.example.kidsstory.domain.model.Story
import com.example.kidsstory.domain.model.StoryCategory
import com.example.kidsstory.domain.model.StorySegment

/**
 * 將 JSON 格式轉換為 Entity
 */
fun StoryJson.toEntity(): StoryEntity {
    return StoryEntity(
        id = id,
        title = title,
        titleEn = titleEn,
        ageRange = ageRange,
        duration = segments.sumOf { it.duration },
        category = category,
        isPreset = true,
        isDownloaded = true,
        coverImagePath = coverImage,
        createdAt = System.currentTimeMillis()
    )
}

/**
 * 將 JSON 段落轉換為 Entity
 */
fun StorySegmentJson.toEntity(storyId: String, imagePath: String?): StorySegmentEntity {
    return StorySegmentEntity(
        storyId = storyId,
        sequenceNumber = sequenceNumber,
        contentZh = contentZh,
        contentEn = contentEn,
        characterRole = characterRole,
        audioPathZh = null,
        audioPathEn = null,
        imageUrls = imagePath,
        duration = duration
    )
}

/**
 * 將 Entity 轉換為領域模型
 */
fun StoryEntity.toDomain(segments: List<StorySegmentEntity>): Story {
    return Story(
        id = id,
        title = title,
        titleEn = titleEn,
        category = StoryCategory.fromString(category),
        ageRange = ageRange,
        coverImage = coverImagePath,
        segments = segments.map { it.toDomain() },
        isPreset = isPreset,
        isDownloaded = isDownloaded,
        createdAt = createdAt
    )
}

/**
 * 將 StoryWithSegments 關係轉換為領域模型
 * 優化後的方法，避免 N+1 查詢問題
 */
fun StoryWithSegments.toDomain(): Story {
    return story.toDomain(segments)
}

/**
 * 將段落 Entity 轉換為領域模型
 */
fun StorySegmentEntity.toDomain(): StorySegment {
    return StorySegment(
        sequenceNumber = sequenceNumber,
        contentZh = contentZh,
        contentEn = contentEn,
        characterRole = CharacterRole.fromString(characterRole),
        duration = duration,
        image = imageUrls,
        audioPathZh = audioPathZh,
        audioPathEn = audioPathEn
    )
}

/**
 * 將領域模型轉換為 Entity
 */
fun Story.toEntity(): StoryEntity {
    return StoryEntity(
        id = id,
        title = title,
        titleEn = titleEn,
        ageRange = ageRange,
        duration = duration,
        category = category.name,
        isPreset = isPreset,
        isDownloaded = isDownloaded,
        coverImagePath = coverImage,
        createdAt = createdAt
    )
}

/**
 * 將領域段落轉換為 Entity
 */
fun StorySegment.toEntity(storyId: String): StorySegmentEntity {
    return StorySegmentEntity(
        storyId = storyId,
        sequenceNumber = sequenceNumber,
        contentZh = contentZh,
        contentEn = contentEn,
        characterRole = characterRole.name,
        audioPathZh = audioPathZh,
        audioPathEn = audioPathEn,
        imageUrls = image,
        duration = duration
    )
}
