# Android Studio 安裝完成！開始使用指南

## ✅ 安裝狀態

Android Studio 已成功安裝到：`/opt/android-studio/`

## 🚀 啟動 Android Studio

### 方式 1：使用指令啟動（推薦用於首次設定）

```bash
/opt/android-studio/bin/studio.sh
```

或使用建立好的腳本：
```bash
~/start-android-studio.sh
```

### 方式 2：從應用程式選單啟動

在您的應用程式選單中搜尋 "Android Studio" 並點擊啟動。

---

## 📋 首次啟動設定步驟

### 1. 歡迎畫面
首次啟動時，會出現設定精靈：

**選項建議：**
- ✅ "Do not import settings"（不匯入設定）
- 點擊 "OK"

### 2. 資料分享
詢問是否分享使用資料：
- 選擇 "Don't send"（不分享）或 "Send usage statistics"（分享）
- 看您個人偏好

### 3. 安裝類型
**選擇 "Standard" （標準安裝）** ⭐
- 這會自動安裝所有需要的工具：
  - Android SDK
  - Android SDK Platform
  - Android Virtual Device (AVD)
  - 等等...

### 4. 選擇 UI 主題
- Light（淺色）或 Dark（深色）
- 看您個人喜好

### 5. 驗證設定
會顯示即將下載的內容：
- Android SDK
- Android SDK Platform 34
- Build Tools
- 等等...

總下載大小約 **3-5 GB**
- 點擊 "Next"

### 6. 下載元件
**這步驟會花 10-30 分鐘**（取決於網速）
- SDK 和工具會自動下載
- 請耐心等待，不要關閉視窗
- ☕ 可以去泡杯咖啡

### 7. 完成
看到 "Finish" 按鈕時：
- 點擊 "Finish"
- Android Studio 準備就緒！

---

## 📂 開啟故事寶盒專案

### 在歡迎畫面

1. 點擊 **"Open"** 按鈕
2. 瀏覽到：`path/to/KidsStoryBox`
3. 點擊 "OK"

### 首次開啟專案時

#### 步驟 1：Gradle Sync
- Android Studio 會自動開始 "Gradle Sync"
- 這會下載專案需要的依賴（5-10 分鐘）
- 底部會顯示進度條
- 等待顯示 "Gradle sync finished"

#### 步驟 2：索引建立
- Android Studio 會建立程式碼索引
- 右下角會顯示進度
- 等待完成（1-2 分鐘）

---

## 🎮 建立和啟動模擬器

### 建立 Android 虛擬裝置（第一次需要）

1. 點擊頂部工具列的 **裝置下拉選單**
2. 選擇 **"Device Manager"**
3. 點擊 **"Create Device"**

4. **選擇硬體**：
   - 選擇 "Phone" 分類
   - 推薦：**Pixel 6** 或 **Pixel 7**
   - 點擊 "Next"

5. **選擇系統映像**：
   - 選擇 **"UpsideDownCake" (API 34)**
   - 如果顯示 "Download"，點擊下載（約 1 GB）
   - 下載完成後點擊 "Next"

6. **驗證設定**：
   - 裝置名稱：可以保持預設或自訂
   - 點擊 "Finish"

7. **模擬器建立完成！**

---

## ▶️ 執行應用程式

### 第一次執行

1. **確認裝置**：
   - 頂部工具列，裝置下拉選單選擇剛建立的模擬器

2. **點擊綠色的播放按鈕（▶）**
   - 或按快捷鍵 `Shift + F10`

3. **等待**：
   - 模擬器會啟動（第一次較慢，約 1-2 分鐘）
   - 應用程式會自動建構和安裝
   - 應用程式會在模擬器中啟動

4. **成功！**
   - 您應該會看到「故事寶庫」畫面
   - 雖然目前功能還不完整，但證明專案可以執行了！

---

## 🔧 常見問題排解

### 問題 1：Gradle Sync 失敗

**解決方法：**
```bash
# 在終端機執行
cd path/to/KidsStoryBox
chmod +x gradlew
./gradlew clean
```

然後在 Android Studio 中：
- 點擊 File → Invalidate Caches → Invalidate and Restart

### 問題 2：找不到 Android SDK

**解決方法：**
1. 開啟 File → Settings → Appearance & Behavior → System Settings → Android SDK
2. SDK Location 應該是：`$HOME/Android/Sdk`
3. 如果路徑不對，點擊 "Edit" 重新設定

### 問題 3：模擬器無法啟動

**確認 KVM 已安裝：**
```bash
# 安裝 KVM
sudo apt-get install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils

# 將使用者加入 kvm 群組
sudo adduser $USER kvm

# 重新登入後再試
```

### 問題 4：建構速度很慢

**優化建議：**
1. 開啟 File → Settings → Build, Execution, Deployment → Compiler
2. 勾選：
   - ✅ "Compile independent modules in parallel"
   - ✅ "Configure on demand"

3. 開啟 File → Settings → Build, Execution, Deployment → Build Tools → Gradle
4. 設定 "Gradle JDK" 為：Android Studio 自帶的 JDK

---

## 💡 實用快捷鍵

| 功能 | Windows/Linux | Mac |
|------|---------------|-----|
| 執行應用程式 | `Shift + F10` | `Ctrl + R` |
| 建構專案 | `Ctrl + F9` | `Cmd + F9` |
| 搜尋檔案 | `Ctrl + Shift + N` | `Cmd + Shift + O` |
| 搜尋任何地方 | `雙擊 Shift` | `雙擊 Shift` |
| 程式碼補全 | `Ctrl + Space` | `Ctrl + Space` |
| 格式化程式碼 | `Ctrl + Alt + L` | `Cmd + Option + L` |
| 執行 | `Shift + F10` | `Ctrl + R` |
| 除錯 | `Shift + F9` | `Ctrl + D` |

---

## 📱 專案結構導覽

### 主要檔案位置

在 Android Studio 左側專案樹中：

```
story/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/kidsstory/
│   │   │   ├── MainActivity.kt          # 主要 Activity
│   │   │   ├── presentation/
│   │   │   │   ├── screens/            # 各個畫面
│   │   │   │   └── theme/              # UI 主題設定
│   │   │   ├── data/database/          # 資料庫相關
│   │   │   └── domain/model/           # 資料模型
│   │   ├── res/                        # 資源檔案
│   │   │   ├── values/strings.xml      # 字串資源
│   │   │   └── values/colors.xml       # 色彩資源
│   │   └── AndroidManifest.xml         # 應用程式設定
│   └── build.gradle.kts                # App 建構設定
└── build.gradle.kts                    # 專案建構設定
```

---

## 🎨 Jetpack Compose 預覽

### 查看 UI 預覽

1. 開啟任何有 `@Composable` 的檔案
2. 點擊右上角的 **"Split"** 按鈕
3. 左邊是程式碼，右邊會即時顯示 UI 預覽
4. 修改程式碼後，預覽會自動更新

### 互動式預覽

1. 點擊預覽右上角的 **"Interactive"** 按鈕
2. 可以直接在預覽中點擊按鈕測試互動

---

## 📊 查看應用程式 Log

### 在 Android Studio 中查看

1. 執行應用程式後
2. 底部會自動出現 **"Logcat"** 標籤
3. 可以在搜尋框輸入 "KidsStory" 過濾 Log
4. 不同等級的 Log：
   - 🔴 Error（錯誤）
   - 🟠 Warning（警告）
   - 🔵 Info（資訊）
   - ⚫ Debug（除錯）

---

## 下一步

專案已經可以執行了！雖然目前三個畫面只有基本架構，但您可以：

### 1. 探索現有程式碼
- 查看 `MainActivity.kt` 了解應用程式進入點
- 查看 `presentation/screens/` 下的各個畫面
- 查看 `data/database/` 了解資料庫結構

### 2. 修改 UI 看看效果
- 開啟 `StoryLibraryScreen.kt`
- 嘗試修改文字內容
- 看預覽即時更新

### 3. 繼續開發第 2 週功能
- 實作完整的故事列表 UI
- 加入預設故事資料
- 實作基礎播放器

---

## 🆘 需要協助？

如果遇到任何問題：
1. 查看這份指南的「常見問題排解」章節
2. 告訴我具體的錯誤訊息
3. 我會協助您解決

---

**祝開發順利！🎉**
