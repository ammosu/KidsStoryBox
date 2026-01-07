package com.example.kidsstory.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 播放歷史實體 - 記錄用戶的播放歷史
 */
@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storyId"), Index("playedAt")]
)
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: String,            // 故事ID
    val playedAt: Long,             // 播放時間戳
    val lastPosition: Int,          // 最後播放位置（秒）
    val completed: Boolean,         // 是否完整播放完畢
    val language: String            // 播放語言：zh 或 en
)
