# 圖片生成功能說明

本應用整合了本地圖片生成服務，為 AI 生成的故事自動創建精美的封面插圖。

## 功能特點

### ✨ 自動封面生成
- 每個 AI 生成的故事都會自動生成封面圖
- 兒童友好的可愛插畫風格
- 根據故事主題、分類和角色生成適合的圖片
- 使用柔和的粉彩色調

### 🎨 智能 Prompt 設計
根據故事屬性自動構建 Prompt：
- **主題**：從故事標題提取關鍵信息
- **分類風格**：
  - 友誼：孩子們一起玩耍，溫暖氛圍
  - 冒險：刺激的旅程，魔法景觀
  - 家庭：溫馨的家庭場景
  - 教育：學習和發現，好奇的孩子
  - 品德：善良和關懷的場景
  - 奇幻：魔法生物，夢幻森林
  - 動物：可愛的動物，大自然
  - 科學：太空或自然探索

### 📐 生成規格
- **解析度**：1024x1024 (1:1 正方形，適合封面)
- **生成步數**：8（快速生成，約 10-30 秒）
- **風格**：兒童書籍插圖，可愛彩色，柔和色彩
- **安全性**：適合 3-6 歲兒童

## 技術架構

### 後端服務
使用本地 Z-Image-Turbo API：
- **服務地址**：`http://10.0.2.2:7860`（Android 模擬器訪問 localhost）
- **真實設備**：需要將 `ImageGenerationService.kt` 中的 `baseUrl` 改為實際 IP

### 整合流程
```
用戶輸入故事參數
    ↓
Gemini 生成故事文本
    ↓
ImageGenerationService 生成封面
    ↓
保存圖片到本地 (story_images/)
    ↓
Story 對象更新 coverImage 路徑
    ↓
儲存到資料庫
    ↓
UI 使用 Coil 顯示圖片
```

### 關鍵組件

#### 1. ImageGenerationService
```kotlin
@Singleton
class ImageGenerationService {
    // 為故事生成封面圖
    suspend fun generateStoryCover(
        storyTitle: String,
        category: StoryCategory,
        protagonist: String,
        language: String
    ): String?

    // 檢查服務是否可用
    suspend fun isServiceAvailable(): Boolean
}
```

#### 2. GenerateStoryUseCase
```kotlin
suspend operator fun invoke(
    theme: String,
    protagonist: String,
    educationalGoal: String,
    language: String = "zh",
    generateCoverImage: Boolean = true  // 控制是否生成圖片
): Result<Story>
```

#### 3. UI 顯示
- 使用 Coil 的 `AsyncImage` 組件
- 自動從本地檔案路徑加載
- 支援 ContentScale.Crop 裁切顯示
- 預設故事顯示漸變背景

## 使用說明

### 前置需求

1. **啟動圖片生成服務**
   ```bash
   cd /home/cw/git_project/Z-Image-Turbo
   docker compose up -d
   ```

2. **確認服務正常運行**
   ```bash
   curl http://localhost:7860
   ```

### Android 模擬器設定

Android 模擬器無法直接訪問 `localhost`，需要使用 `10.0.2.2`：
- 已在代碼中設定為 `http://10.0.2.2:7860`
- 無需額外配置

### 真實設備設定

如果在真實 Android 設備上測試：

1. 找到電腦的 IP 地址：
   ```bash
   ip addr show | grep "inet "
   # 例如：192.168.1.100
   ```

2. 修改 `ImageGenerationService.kt`：
   ```kotlin
   private val baseUrl = "http://192.168.1.100:7860"  // 改為實際 IP
   ```

3. 確保防火牆允許訪問：
   ```bash
   sudo ufw allow 7860/tcp
   ```

## 生成進度顯示

在 AI 生成畫面，用戶可以看到：
1. ✨ 正在創作故事...
2. 🎨 正在繪製封面插圖...
3. ✅ 完成！自動跳轉播放器

預計總時間：20-60 秒（故事生成 10-30s + 圖片生成 10-30s）

## Prompt 範例

### 友誼故事
```
A beautiful cover illustration for a children's story titled '小兔子找朋友',
featuring 小白兔 as the main character,
happy children playing together, warm atmosphere,
children's book illustration, cute and colorful, soft pastel colors,
whimsical art style, friendly and safe for kids ages 3-6,
digital art, high quality, detailed,
book cover design, centered composition, no text
```

### 冒險故事
```
A beautiful cover illustration for a children's story titled '勇敢的小熊',
exciting journey, magical landscape, discovery,
children's book illustration, cute and colorful, soft pastel colors,
whimsical art style, friendly and safe for kids ages 3-6,
digital art, high quality, detailed,
book cover design, centered composition, no text
```

## 儲存與管理

### 圖片儲存位置
```
/data/data/com.example.kidsstory/files/story_images/
└── story_cover_1704639123456.png
```

### 檔案命名規則
- 格式：`story_cover_{timestamp}.png`
- 時間戳：使用 `System.currentTimeMillis()`
- 格式：PNG (90% 質量)

### 清理策略
- 圖片永久保存直到故事被刪除
- 未來可實作自動清理舊圖片功能
- 建議：保留最近 50 個 AI 生成的故事

## 錯誤處理

### 圖片生成失敗
- 不影響故事生成
- Story 的 coverImage 欄位為空字串
- UI 顯示漸變背景代替

### 服務不可用
- 使用 `isServiceAvailable()` 檢查
- 失敗時自動跳過圖片生成
- 用戶仍可正常使用故事功能

### 常見問題

**Q: 為什麼圖片沒有顯示？**
A: 檢查：
1. Z-Image-Turbo 服務是否正在運行
2. 網絡連接是否正常
3. 檔案路徑是否正確
4. 檢查 Logcat 錯誤訊息

**Q: 生成速度很慢？**
A: 正常情況：
- 首次啟動需要載入模型（較慢）
- 後續生成會快很多
- 取決於 GPU 性能

**Q: 能否自定義圖片風格？**
A: 可以修改 `buildStoryCoverPrompt()` 函數中的 prompt 模板

## 未來擴展

### 計畫功能
- [ ] 為每個故事段落生成配圖
- [ ] 允許用戶選擇不同的插畫風格
- [ ] 圖片編輯和裁切功能
- [ ] 多張封面選項讓用戶挑選
- [ ] 批次預生成熱門故事封面
- [ ] 圖片快取管理 UI

### 性能優化
- [ ] 使用 WorkManager 在後台生成圖片
- [ ] 實作圖片壓縮和 WebP 轉換
- [ ] 添加圖片生成隊列管理
- [ ] 實作智能預載入策略

## 參考資源

- [Z-Image-Turbo API 文檔](/home/cw/git_project/Z-Image-Turbo/API_USAGE.md)
- [Coil 圖片加載庫](https://coil-kt.github.io/coil/)
- [Android AsyncImage](https://developer.android.com/jetpack/compose/graphics/images/loading)
