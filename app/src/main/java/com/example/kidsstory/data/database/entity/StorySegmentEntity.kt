package com.example.kidsstory.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 故事段落實體 - 儲存故事的每個段落
 * 每個段落對應一個角色的一段對話或旁白
 */
@Entity(
    tableName = "story_segments",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storyId")]
)
data class StorySegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: String,            // 所屬故事ID
    val sequenceNumber: Int,        // 段落順序號
    val contentZh: String,          // 中文內容
    val contentEn: String,          // 英文內容
    val characterRole: String,      // 角色類型：NARRATOR, PROTAGONIST, SUPPORTING等
    val audioPathZh: String?,       // 中文音訊本地路徑
    val audioPathEn: String?,       // 英文音訊本地路徑
    val imageUrls: String?,         // 配圖URL（JSON陣列格式）
    val duration: Int               // 該段落播放時長（秒）
)
