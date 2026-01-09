package com.example.kidsstory.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kidsstory.data.database.entity.StoryEntity
import com.example.kidsstory.data.database.entity.StorySegmentEntity

/**
 * 故事與段落的一對多關係
 * 使用 Room @Relation 自動處理 JOIN 查詢，避免 N+1 查詢問題
 */
data class StoryWithSegments(
    @Embedded val story: StoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "storyId"
    )
    val segments: List<StorySegmentEntity>
)
