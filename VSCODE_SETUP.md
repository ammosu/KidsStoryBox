# 使用 VSCode 開發 Android 專案指南

## ⚠️ 重要提醒

雖然可以使用 VSCode 開發 Android，但對於**第一次開發 Android 應用**的開發者，我**強烈建議使用 Android Studio**，原因如下：

### Android Studio 的優勢
- ✅ **一鍵安裝**所有需要的工具（SDK, Build Tools, 模擬器等）
- ✅ **Jetpack Compose 即時預覽**（寫 UI 時立即看到效果）
- ✅ **內建 Android 模擬器**管理和啟動
- ✅ **更好的程式碼自動補全**和錯誤提示
- ✅ **Layout Inspector**、**Profiler** 等調試工具
- ✅ **一鍵執行和部署**到裝置

### VSCode 的限制
- ❌ 需要手動安裝和配置所有工具
- ❌ 沒有 Compose 即時預覽（只能執行後才能看到 UI）
- ❌ 需要手動管理模擬器
- ❌ 程式碼補全不如 Android Studio 完善

---

## 方案 A：安裝 Android Studio（推薦 ⭐）

### 下載和安裝
```bash
# 1. 下載 Android Studio
# 訪問：https://developer.android.com/studio

# 2. 安裝（Ubuntu/Linux）
sudo apt-get install -y libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386
sudo tar -xzf android-studio-*.tar.gz -C /opt/
cd /opt/android-studio/bin
./studio.sh
```

### 首次設定
1. 啟動 Android Studio
2. 跟隨設定精靈（會自動下載 SDK、Build Tools 等）
3. 建立或連接 Android 虛擬裝置（AVD）

### 開啟本專案
```bash
# 在 Android Studio 中：
# File -> Open -> 選擇 path/to/KidsStoryBox
```

---

## 方案 B：使用 VSCode（進階使用者）

如果您堅持使用 VSCode，以下是完整的設定步驟：

### 步驟 1：安裝 Android Command Line Tools

```bash
# 1. 建立 Android SDK 目錄
mkdir -p $HOME/Android/Sdk
cd $HOME/Android/Sdk

# 2. 下載 Command Line Tools
# 訪問：https://developer.android.com/studio#command-line-tools-only
# 或使用 wget（需要最新的下載連結）
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# 3. 解壓縮
unzip commandlinetools-linux-*.zip
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# 4. 設定環境變數
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/emulator' >> ~/.bashrc
source ~/.bashrc

# 5. 安裝必要的 SDK 套件
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
sdkmanager "system-images;android-34;google_apis;x86_64"
sdkmanager "emulator"

# 6. 接受授權
yes | sdkmanager --licenses
```

### 步驟 2：設定 Java 17

```bash
# Android 需要 Java 17（您目前有 Java 21）
sudo apt-get install openjdk-17-jdk

# 設定 Java 17 為預設版本
sudo update-alternatives --config java
# 選擇 Java 17

# 驗證
java -version
# 應該顯示 openjdk version "17.x.x"
```

### 步驟 3：安裝 VSCode 擴充套件

在 VSCode 中安裝以下擴充套件：

1. **Kotlin** (by Mathias Fröhlich)
   - 提供 Kotlin 語法高亮和程式碼補全

2. **Gradle for Java** (by Microsoft)
   - 支援 Gradle 建構系統

3. **Android iOS Emulator** (by DiemasMichiels)
   - 管理 Android 模擬器

4. **XML** (by Red Hat)
   - 支援 XML 檔案編輯

```bash
# 使用指令安裝
code --install-extension mathiasfrohlich.Kotlin
code --install-extension vscjava.vscode-gradle
code --install-extension DiemasMichiels.emulate
code --install-extension redhat.vscode-xml
```

### 步驟 4：設定專案的 local.properties

```bash
cd path/to/KidsStoryBox

# 建立 local.properties 檔案
cat > local.properties << EOF
sdk.dir=$HOME/Android/Sdk
EOF
```

### 步驟 5：在 VSCode 中開啟專案

```bash
# 在專案目錄開啟 VSCode
cd path/to/KidsStoryBox
code .
```

### 步驟 6：建構專案

```bash
# 在專案根目錄執行
cd path/to/KidsStoryBox

# 給予 gradlew 執行權限
chmod +x gradlew

# 建構專案
./gradlew build

# 如果建構成功，會看到 BUILD SUCCESSFUL
```

### 步驟 7：建立和啟動模擬器

```bash
# 建立 Android 虛擬裝置（AVD）
avdmanager create avd \
  -n "Pixel_6_API_34" \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_6"

# 啟動模擬器
emulator -avd Pixel_6_API_34 &

# 等待模擬器完全啟動（約 1-2 分鐘）
```

### 步驟 8：安裝應用程式到模擬器

```bash
# 確認裝置已連接
adb devices
# 應該會看到模擬器

# 建構並安裝 APK
./gradlew installDebug

# 或使用 adb 直接安裝
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## VSCode 開發工作流程

### 日常開發步驟

1. **啟動模擬器**
   ```bash
   emulator -avd Pixel_6_API_34 &
   ```

2. **開啟 VSCode**
   ```bash
   cd path/to/KidsStoryBox
   code .
   ```

3. **編輯程式碼**
   - 在 VSCode 中編輯 Kotlin 檔案

4. **建構和安裝**
   ```bash
   ./gradlew installDebug
   ```

5. **查看 Log**
   ```bash
   adb logcat | grep "KidsStory"
   ```

### 常用指令

```bash
# 清理建構
./gradlew clean

# 建構 Debug 版本
./gradlew assembleDebug

# 建構並安裝
./gradlew installDebug

# 執行測試
./gradlew test

# 查看可用的 Gradle 任務
./gradlew tasks

# 檢查程式碼風格
./gradlew ktlintCheck

# 列出連接的裝置
adb devices

# 解除安裝應用程式
adb uninstall com.example.kidsstory

# 清除應用程式資料
adb shell pm clear com.example.kidsstory
```

---

## 疑難排解

### 問題 1：找不到 Android SDK
```bash
# 檢查環境變數
echo $ANDROID_HOME
# 應該顯示：$HOME/Android/Sdk

# 如果沒有，重新載入 bashrc
source ~/.bashrc
```

### 問題 2：Gradle 建構失敗
```bash
# 清理並重新建構
./gradlew clean build --refresh-dependencies

# 檢查 Java 版本（需要 Java 17）
java -version
```

### 問題 3：模擬器啟動失敗
```bash
# 檢查 KVM 是否啟用（Linux 需要）
sudo apt-get install qemu-kvm
sudo adduser $USER kvm

# 重新登入後再試
```

### 問題 4：找不到 adb
```bash
# 確保 platform-tools 在 PATH 中
export PATH=$PATH:$HOME/Android/Sdk/platform-tools

# 或完整路徑執行
$HOME/Android/Sdk/platform-tools/adb devices
```

---

## 與 Android Studio 的對照表

| 功能 | Android Studio | VSCode + Command Line |
|------|----------------|----------------------|
| 開啟專案 | File -> Open | `code .` |
| 建構專案 | Build -> Make Project | `./gradlew build` |
| 執行應用程式 | Run -> Run 'app' | `./gradlew installDebug` |
| 清理專案 | Build -> Clean Project | `./gradlew clean` |
| 查看 Log | Logcat 視窗 | `adb logcat` |
| 管理模擬器 | AVD Manager | `avdmanager` + `emulator` |
| UI 預覽 | Split 視窗即時預覽 | ❌ 無（需執行才能看到） |

---

## 我的建議

**如果您是第一次開發 Android**，我真心建議：

1. **先用 Android Studio** 熟悉 Android 開發
2. 了解 Jetpack Compose 的工作方式
3. 熟悉 Android 的建構流程和調試工具
4. 之後如果您真的偏好 VSCode，再轉換也不遲

**Android Studio 的學習曲線實際上比 VSCode + 手動配置更平緩**，因為很多事情它都自動幫您處理了。

---

## 快速開始：最小化設定（測試用）

如果您只是想快速看看專案能不能執行，最簡單的方式：

```bash
# 1. 安裝 Android Studio（自動包含所有工具）
# 2. 用 Android Studio 開啟專案
# 3. 點選 Run 按鈕
# 4. 選擇或建立模擬器
# 5. 應用程式就會啟動

# 整個過程約 10-15 分鐘（第一次下載 SDK 會久一點）
```

---

需要我協助您選擇哪個方案嗎？
