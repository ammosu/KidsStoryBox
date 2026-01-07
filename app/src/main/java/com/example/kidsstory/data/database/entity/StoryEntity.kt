package com.example.kidsstory.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 故事實體 - 儲存故事的基本資訊
 */
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title: String,              // 中文標題
    val titleEn: String,            // 英文標題
    val ageRange: String,           // 適合年齡範圍，例如 "3-6"
    val duration: Int,              // 播放時長（秒）
    val category: String,           // 分類：冒險、友誼、科普等
    val isPreset: Boolean,          // 是否為預設故事
    val isDownloaded: Boolean,      // 是否已下載完整資源
    val coverImagePath: String,     // 封面圖片路徑
    val createdAt: Long,            // 創建時間戳
    val lastPlayedAt: Long? = null  // 最後播放時間戳
)
