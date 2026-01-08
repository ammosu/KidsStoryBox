package com.example.kidsstory.data.local.model

import com.google.gson.annotations.SerializedName

/**
 * JSON 格式的故事資料
 */
data class StoryJson(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("titleEn")
    val titleEn: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("ageRange")
    val ageRange: String,

    @SerializedName("coverImage")
    val coverImage: String,

    @SerializedName("segments")
    val segments: List<StorySegmentJson>
)

/**
 * JSON 格式的故事段落資料
 */
data class StorySegmentJson(
    @SerializedName("sequenceNumber")
    val sequenceNumber: Int,

    @SerializedName("contentZh")
    val contentZh: String,

    @SerializedName("contentEn")
    val contentEn: String,

    @SerializedName("characterRole")
    val characterRole: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("image")
    val image: String? = null
)
