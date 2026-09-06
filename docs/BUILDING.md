# 開發與建置指南

## 技術架構

- Kotlin
- Jetpack Compose
- Room
- DataStore Preferences
- Kotlin Symbol Processing（KSP）
- Gradle Kotlin DSL

主要程式碼位於 `app/src/main/java/com/tainanlins5/minesweeper/`：

| 路徑 | 用途 |
|---|---|
| `domain/` | 遊戲狀態、規則與踩地雷引擎 |
| `data/` | Room、DataStore 與資料模型 |
| `ui/` | Compose 畫面、復古元件、棋盤與主題 |
| `GameViewModel.kt` | 畫面狀態、計時與功能協調 |
| `MainActivity.kt` | Activity、系統列、全螢幕及返回操作 |

## 開發環境

- JDK 17
- Android SDK Platform 36
- Android SDK Build Tools
- Android Studio 或命令列 Gradle Wrapper

專案使用 Gradle Wrapper，不需要另外安裝全域 Gradle。

## 取得原始碼

```bash
git clone https://github.com/mark216tw/android-minesweeper-01.git
cd android-minesweeper-01
```

使用 Android Studio 開啟專案後，等待 Gradle Sync 完成。

## Build Types

### debug

供本機開發與除錯使用，不作為 GitHub Release 成品。

```bash
./gradlew assembleDebug
```

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
```

輸出位置：`app/build/outputs/apk/debug/app-debug.apk`

### prerelease

測試發行版本，具備以下設定：

- Build Type：`prerelease`
- 啟用 R8 程式碼壓縮與混淆
- 啟用未使用資源移除
- 使用 Debug 金鑰簽署
- 版本名稱加上 `-prerelease`
- 不適用於 Google Play 正式上架

```bash
./gradlew assemblePrerelease
```

Windows PowerShell：

```powershell
.\gradlew.bat assemblePrerelease
```

輸出檔案：

- APK：`app/build/outputs/apk/prerelease/app-prerelease.apk`
- R8 mapping：`app/build/outputs/mapping/prerelease/mapping.txt`

正式發布前必須新增獨立且安全保存的 Release signing config，不可沿用 Debug 金鑰。

## 測試

執行 JVM 單元測試：

```bash
./gradlew testDebugUnitTest
```

目前測試涵蓋：

- 第一格九宮格安全
- 插旗與探索規則
- 踩雷與勝負判定
- 快速展開與錯誤旗幟
- 自訂棋盤輸入邊界
- 棋盤縮放及平移後的觸控座標
- 自訂棋盤預設的編碼、驗證與數量上限

## 靜態檢查

```bash
./gradlew lintDebug
```

HTML 報告：`app/build/reports/lint-results-debug.html`

## 完整驗證

```bash
./gradlew clean testDebugUnitTest lintDebug assemblePrerelease
```

## APK 簽章檢查

使用 Android SDK Build Tools 的 `apksigner`：

```bash
apksigner verify --verbose --print-certs app/build/outputs/apk/prerelease/app-prerelease.apk
```

Prerelease 應顯示簽署者為 `CN=Android Debug`。

## 離線與權限

APP 不宣告 `android.permission.INTERNET`，也不包含廣告、分析或追蹤 SDK。新增相依套件時，請檢查合併後 Manifest，避免意外加入不必要權限。

## 資料與相容性

- Room 保存進行中的棋局及遊戲紀錄。
- DataStore 保存外觀、主題色、操作偏好及自訂棋盤預設。
- 修改 Room schema 時必須提供 Migration 或明確處理資料重建策略。
- 修改持久化格式時應保留既有資料相容性，並增加測試。

## 發布檢查清單

- 更新 `versionCode` 與 `versionName`。
- 執行單元測試與 Lint。
- 建置 prerelease APK 並執行實機冒煙測試。
- 驗證 APK 套件名稱、版本與簽章。
- 確認未宣告網路或其他非必要權限。
- 撰寫 Release notes。
- Pre-release 必須在 GitHub Release 勾選「Set as a pre-release」。
- 正式版必須改用正式簽署金鑰。
