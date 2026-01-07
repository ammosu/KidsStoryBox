package com.example.kidsstory.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 角色實體 - 儲存故事角色的聲音配置
 */
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,               // 中文名稱
    val nameEn: String,             // 英文名稱
    val role: String,               // 角色類型：NARRATOR, PROTAGONIST, SUPPORTING
    val voiceId: String,            // TTS服務的聲音ID（例如ElevenLabs voice ID）
    val avatarPath: String,         // 角色頭像路徑
    val description: String         // 角色描述
)
