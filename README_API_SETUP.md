# API 設定指南

本專案使用 AI 服務來生成故事，需要配置 API 金鑰才能使用 AI 功能。

## 步驟 1：取得 Gemini API Key

1. 前往 [Google AI Studio](https://makersuite.google.com/app/apikey)
2. 使用 Google 帳號登入
3. 點擊「Create API Key」或「建立 API 金鑰」
4. 複製生成的 API 金鑰

**重要提示**：
- Gemini API 有免費額度（每分鐘 15 次，每天 1500 次請求）
- 免費版本適合開發和小規模測試
- 詳細定價：https://ai.google.dev/pricing

## 步驟 2：配置 local.properties

1. 在專案根目錄創建 `local.properties` 檔案（如果不存在）
2. 添加以下內容：

```properties
# Android SDK 位置（通常 Android Studio 會自動生成）
sdk.dir=/path/to/android/sdk

# Gemini API Key
GEMINI_API_KEY=你的_API_金鑰_在這裡

# ElevenLabs API Key（未來功能，目前可留空）
ELEVENLABS_API_KEY=
```

3. 將 `你的_API_金鑰_在這裡` 替換為步驟 1 取得的實際 API 金鑰

**範例**：
```properties
GEMINI_API_KEY=AIzaSyABCDEFGHIJKLMNOPQRSTUVWXYZ1234567
```

## 步驟 3：驗證配置

1. 在 Android Studio 中重新建構專案：
   ```
   Build -> Rebuild Project
   ```

2. 執行應用程式

3. 點擊「創造新故事」按鈕

4. 填寫表單並點擊「生成故事」

5. 如果配置正確，應該會在 10-30 秒內生成一個新故事

## 常見問題

### Q: 我的 API Key 會被提交到 Git 嗎？
A: 不會。`local.properties` 已經在 `.gitignore` 中，不會被版本控制追蹤。

### Q: 出現「API Key not found」錯誤
A: 請確認：
1. `local.properties` 檔案在專案根目錄（與 `build.gradle.kts` 同級）
2. API Key 的格式正確，沒有多餘的空格或引號
3. 重新建構專案（Rebuild Project）

### Q: 出現「Safety settings blocked the response」錯誤
A: Gemini 的安全設定可能攔截了內容。這通常發生在：
- 主題包含敏感詞彙
- 嘗試重新生成，使用更溫和的主題描述

### Q: 生成速度很慢
A: 正常情況下需要 10-30 秒。影響因素：
- 網路速度
- Gemini API 伺服器負載
- 故事長度（目前設定為 8-10 段）

### Q: 免費額度用完了怎麼辦？
A: Gemini API 免費額度：
- 每分鐘 15 次請求
- 每天 1500 次請求
- 如果超過，需要等待或升級到付費方案

## 未來功能：ElevenLabs TTS

目前應用使用 Android 系統 TTS（免費）。未來版本將整合 ElevenLabs 提供更高品質的多角色語音：

1. 前往 [ElevenLabs](https://elevenlabs.io/)
2. 註冊帳號
3. 取得 API Key
4. 添加到 `local.properties`：
   ```properties
   ELEVENLABS_API_KEY=你的_elevenlabs_金鑰
   ```

**定價**：
- 免費版：每月 10,000 字符
- Starter：$5/月，30,000 字符
- Creator：$22/月，100,000 字符

## 安全建議

1. **永遠不要**將 API Key 提交到 Git
2. **永遠不要**在程式碼中硬編碼 API Key
3. **定期輪換** API Key（建議每 3-6 個月）
4. **監控使用量**，避免意外超額費用
5. 如果 API Key 洩漏，**立即撤銷**並生成新的

## 參考資源

- [Gemini API 文檔](https://ai.google.dev/docs)
- [Gemini API 定價](https://ai.google.dev/pricing)
- [Google AI Studio](https://makersuite.google.com/)
- [ElevenLabs 文檔](https://elevenlabs.io/docs)
