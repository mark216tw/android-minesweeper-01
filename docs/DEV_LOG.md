# 開發紀錄

最新紀錄放最上面。每次開發工作都要更新，不要事後補寫。

---

## 2026-10-30 - v1.4.1（versionCode 9）

### 目標
讓 Android 狀態列與導覽列跟隨目前的復古風格與明暗模式變色。

### 變更
- `MainActivity.kt`：以目前 `colors.desktop` 建立動態 `SystemBarStyle`，在風格或明暗變更時重新套用 Edge-to-edge 系統列設定。
- `MainActivity.kt`：關閉 API 29+ 的導覽列對比覆蓋，避免三鍵導覽列出現不一致底色。
- `app/build.gradle.kts`：`versionCode` 9、`versionName` 1.4.1。

### 問題與解法
- Android 15 以後系統列顏色 API 已棄用且多為透明，因此不能只設定舊的 `statusBarColor`；改由 Edge-to-edge 系統列樣式搭配畫面背景色，讓新舊版本都能呈現一致的風格色。
- 導覽列在三鍵模式下可能被系統加上對比底色，因此關閉 `isNavigationBarContrastEnforced`。

### 待辦／已知限制
- 不同廠牌 Android 的系統列對比與手勢導覽行為可能略有差異，仍需實機確認。

### 發布
- 已建置 `app/build/outputs/apk/prerelease/app-prerelease.apk`（versionCode 9、versionName 1.4.1-prerelease）。
- 發布 GitHub Release `v1.4.1-prerelease`，並上傳上述 APK。

## 2026-10-30 - v1.4.0（versionCode 8）

### 目標
移除主題色選項並固定為天空藍；新增麥金塔外觀風格，套用到現有兩種版型且不移動操作按鈕。上方難度列依指示暫不調整。

### 變更
- `data/SettingsRepository.kt`：新增 `RetroStyle`（Windows 灰／麥金塔）、`retro_style` 設定與解碼；移除 `themeHue` 與 `theme_hue`。
- `GameViewModel.kt`：移除主題色預覽與延遲寫入邏輯，新增 `setRetroStyle()`。
- `ui/MinesweeperTheme.kt`：Windows 灰固定天空藍；新增麥金塔淺色／深色單色盤；`RetroColors` 增加邊框、旗幟、爆炸格、地雷與顯示器語意色。
- `ui/RetroComponents.kt`：麥金塔風格使用方正黑白邊框與條紋標題列；七段顯示器改用風格色。
- `ui/GameBoard.kt`：棋盤不再使用固定紅色旗幟與爆炸色；麥金塔使用黑格線、灰白格面與單色數字。
- `ui/Screens.kt`：設定頁新增「復古風格」，移除「主題色」區塊；紀錄頁勝敗標籤在麥金塔風格下改為單色。
- `app/src/test/.../data/RetroStyleTest.kt`：新增風格解碼測試。
- `app/build.gradle.kts`：`versionCode` 8、`versionName` 1.4.0。

### 問題與解法
- 原本棋盤的旗幟、錯誤旗、爆炸格與地雷亮點是硬編碼顏色，會破壞麥金塔單色風格；已全部移入 `RetroColors`。
- 麥金塔風格需要淺色與深色兩種變體，因此以獨立色盤處理，不改動顯示模式設定。

### 待辦／已知限制
- 依指示保留遊戲頁上方難度文字。
- 麥金塔風格不包含完整桌面視窗或選單列，以維持控制按鈕位置不變。
- 尚未實機檢查兩種版型 × 兩種麥金塔明暗變體的視覺呈現。
- 依專案規範未自動建置 prerelease/release。

## 2026-10-30 - v1.3.1（versionCode 7）

### 目標
移除主畫面右上角的離開按鈕，簡化兩種版型的頂部工具列。

### 變更
- `ui/Screens.kt`：移除 `GameLayoutActions.onExit`、`GameScreen.onMoveToBackground`、經典版標頭與掌機版工具列的離開按鈕。
- `MainActivity.kt`：移除 `moveTaskToBack` 回呼。
- 刪除不再使用的 `res/drawable/ic_exit.xml`。
- `docs/USER_GUIDE.md`：移除離開圖示說明。
- `docs/mockups/`：新增 Game Boy 綠與麥金塔單色各兩種版型的 SVG／PNG 視覺提案與 `index.html` 總覽；尚未改動程式。
- `app/build.gradle.kts`：`versionCode` 7、`versionName` 1.3.1。

### 問題與解法
- 離開按鈕同時存在於經典版與掌機版，兩處一併移除，避免兩版行為不一致。

### 待辦／已知限制
- 使用者仍可透過 Android 系統 Home 鍵或返回鍵離開 App。
- Game Boy 與麥金塔風格目前僅有視覺圖，尚未實作。

## 2026-10-30 - v1.3.0（versionCode 6）

### 目標
執行 UI/功能盤點的前九項：結果資訊、經典版資訊、深色配色、刪除確認、震動、螢幕保持、最佳時間、紀錄管理與大型棋盤縮放。

### 變更
- `ui/Screens.kt`：結果視窗補上難度、棋盤、地雷、剩餘悔棋、最佳時間與新紀錄提示，並新增分享按鈕。
- `ui/Screens.kt`：經典版標題與掌機版標題顯示難度與棋盤尺寸；經典狀態列加上「剩餘地雷／時間」標示與重開按鈕描述。
- `ui/MinesweeperTheme.kt`、`ui/GameBoard.kt`：新增淺色／深色數字色盤，深色模式改用高對比配色。
- `ui/Screens.kt`：自訂棋盤刪除改為二次確認。
- `ui/Screens.kt`：翻格、插旗、勝利與失敗加入震動回饋，沿用既有震動設定。
- `MainActivity.kt`：遊戲進行中設定 `keepScreenOn`。
- `ui/Screens.kt`：由現有勝利紀錄計算各難度最佳時間，顯示於新遊戲與結果視窗。
- `ui/Screens.kt`、`GameViewModel.kt`：紀錄新增結果篩選與單筆刪除。
- `ui/GameBoard.kt`：大型棋盤初始以可點擊大小顯示，大螢幕允許放大至畫面；「置中」仍可回到全盤視圖。
- `GameViewModel.kt`：新增 `GameState.shareText()` 供目前棋局直接分享。
- `app/src/test/.../ui/BestTimesTest.kt`：新增最佳時間計算測試。
- `app/build.gradle.kts`：`versionCode` 6、`versionName` 1.3.0。

### 問題與解法
- 結果視窗的最佳時間可能已包含剛完成的棋局；以「目前時間 ≤ 最佳時間」判斷新紀錄，無論結果是否已寫入都能正確判斷。
- 大型棋盤若只依畫面縮放，手機上格子過小；改為在初始時套用最小可讀格子比例，並允許使用者以雙指縮小或按「置中」看全盤。

### 待辦／已知限制
- 大型棋盤初始會放大並裁切部分盤面，偏好「一開始就看全盤」的使用者需按「置中」。
- 尚未補 ViewModel 與 Room migration 的自動化測試。
- 依專案規範未自動建置 prerelease/release。

## 2026-10-30 - v1.2.1（versionCode 5）

### 目標
執行技術盤點中的前五項低風險、高效益改善：存檔一致性、APK 體積、手勢穩定度、設定寫入頻率與系統相容性。

### 變更
- `GameViewModel.kt`：以單一 `Channel` 序列化所有棋局持久化操作，移除 `Mutex`；失敗紀錄存檔改為 `CompletableDeferred`，避免快速重開時新局被舊存檔覆蓋。
- `GameViewModel.kt`：主題色改為先更新預覽狀態，停止拖曳 250ms 後才寫入 DataStore。
- `ui/GameBoard.kt`：`pointerInput` 不再以 `cells`、縮放與位移為 key，改用 `rememberUpdatedState` 讀取最新值，避免手勢偵測器頻繁重啟。
- `ui/Screens.kt`：自訂棋盤改以衍生值夾取地雷數，移除 composition 中直接寫 state；改用本地 `ic_exit` 圖示。
- `data/SettingsRepository.kt`：DataStore 讀取遇到 `IOException` 時改用預設值。
- `MainActivity.kt`：改用 `enableEdgeToEdge()`，移除已棄用的系統列顏色設定。
- `app/build.gradle.kts`：移除 `material-icons-extended`；release 啟用 R8 與資源壓縮；debug 加上 `-debug` 版本後綴。
- `gradle.properties`：啟用 Gradle parallel、build cache 與 configuration cache。
- `app/build.gradle.kts`：`versionCode` 5、`versionName` 1.2.1。

### 問題與解法
- 原本失敗存檔與新局存檔各自透過 coroutine 競爭 `Mutex`，無法保證寫入順序；改為單一佇列後，操作順序即為寫入順序。
- 棋盤手勢若以變動中的 `cells`／`scale` 當 key，會在每次翻格或縮放時重啟偵測器；改以穩定 key＋`rememberUpdatedState` 解決。

### 待辦／已知限制
- 依專案規範未自動建置 prerelease/release，R8 設定僅以 debug 建置與 lint 驗證，尚未實機安裝 release APK。
- 尚未補 ViewModel 與 Room migration 的自動化測試。

## 2026-10-30 - v1.2.0（versionCode 4）

### 目標
將悔棋次數由每局固定一次改為依難度調整：初級 1 次、中級 2 次、高級 3 次、自訂 3 次。

### 變更
- `domain/GameModels.kt`：`Difficulty` 新增 `undoLimit`；`GameState` 以 `undoCount` 記錄已用次數，並提供剩餘次數與可否悔棋。
- `domain/MinesweeperEngine.kt`：悔棋改為在難度上限內累加 `undoCount`。
- `GameViewModel.kt`、`ui/Screens.kt`：依剩餘次數顯示悔棋按鈕並維持原有恢復流程。
- `data/GameEntities.kt`、`GameRepository.kt`、`AppDatabase.kt`：資料庫升至 version 3，新增 `MIGRATION_2_3` 重建 `active_game`，將舊 `undoUsed` 轉為 `undoCount`。
- `app/build.gradle.kts`：`versionCode` 4、`versionName` 1.2.0。
- 更新單元測試與相關文件。

### 問題與解法
- SQLite 舊版不支援重新命名欄位，因此 migration 以建立新表、複製資料、刪除舊表、重新命名的方式轉換。

### 待辦／已知限制
- 尚未實測從 version 2 資料庫實際升級的裝置流程。

## YYYY-MM-DD - vX.Y.Z（versionCode N）

### 目標
（這次要做什麼、為什麼）

### 變更
- （檔案／功能）

### 問題與解法
- （錯誤訊息 → 最後怎麼解）

### 待辦／已知限制
- （尚未完成或未驗證的部分）
