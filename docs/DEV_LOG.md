# 開發紀錄

最新紀錄放最上面。每次開發工作都要更新，不要事後補寫。

---

## 2026-11-01 - 移除五款風格與更名（併入 1.5.0）

### 目標
移除 Windows 3.1、Windows 95/98、Windows 2000/XP、IBM CGA/DOS 與 ZX Spectrum 五款風格;「Windows 灰（綠）」更名為「Windows 灰綠」。

### 變更
- `data/SettingsRepository.kt`:移除 `WIN31`、`WIN98`、`WIN2K`、`DOS`、`ZXSPECTRUM`;`WINGREEN` 標籤改為「Windows 灰綠」。舊設定若存有已移除風格,`decodeRetroStyle` 自動回退 `WINDOWS`。
- `ui/MinesweeperTheme.kt`:移除五組色盤函式與 `retroColors()` 對應分支。
- `ui/RetroComponents.kt`:`RetroFrame` 移除 `THINBEVEL`、`DIALOG`、`SHADOWBLOCK`(已無風格使用);`headerChrome` 移除 WIN31 藏青標題列分支。
- `ui/Screens.kt`:風格說明移除五款,調整 Windows 灰綠說明文字。
- `app/src/test/.../data/RetroStyleTest.kt`:測試同步調整,新增「已移除風格回退 Windows」案例。
- `docs/mockups/generate_mockups.py`:移除五款設定檔、`thinbevel` 邊框與標題列 chrome;STYLE 編號重編(01–10);`Windows 灰綠` 更名。
- `docs/mockups/`:重生成 20 張 SVG 與 20 張 PNG(5 風格 × 2 版型 × 2 明暗),刪除 20 張 SVG 與 20 張 PNG 廢棄檔;`index.html` 改為 5 個風格區塊 + 麥金塔參考區。
- `docs/USER_GUIDE.md`、`README.md`、`docs/CHANGELOG.md`、`docs/RELEASE_NOTES.md`:同步七種風格與更名。

### 問題與解法
- 移除 enum 值後既有使用者可能殘留已移除的 `retro_style` 設定,沿用寬鬆解碼(`runCatching` + 回退 `WINDOWS`)不需資料移轉,並以測試固定此行為。
- 設計稿產生器的 STYLE 編號與邊框語言跟著重編與刪除,避免留下無引用的死碼。

### 待辦／已知限制
- 尚未實機檢視 7 風格 × 2 版型 × 明暗的視覺呈現;此模型無法目視檢視 PNG,設計稿僅做結構驗證。

## 2026-11-01 - v1.5.0 續（對比修正與 Windows 系列，併入 1.5.0）

### 目標
修正 Commodore 64、IBM CGA/DOS、ZX Spectrum 淺色模式文字不清的問題;新增四款 Windows 灰系列風格(Windows 灰（綠）、Windows 3.1、Windows 95/98、Windows 2000/XP),設計稿與 App 一次到位。

### 變更
- `ui/MinesweeperTheme.kt`:`RetroColors` 新增 `desktopText`、`desktopMutedText` 語意色;C64 淺色文字改亮(`#B1A7E3`)、DOS 淺色輔助文字與格面加深調亮、ZX 淺色數字改深色墨色並改反白選取;DOS/ZX 深色數字 1 調亮、紅白機綠色數字調暗;`colorFromHue` 改純 Kotlin 實作(JVM 可測)。
- `ui/MinesweeperTheme.kt`:新增 `winGreenColors`、`win31Colors`、`win98Colors`、`win2kColors` 四組淺/深色盤與 `retroColors()` 分支;Windows 系列數字採「調暗版經典配色」兼顧經典感與對比。
- `data/SettingsRepository.kt`:`RetroStyle` 新增 `WINGREEN`、`WIN31`、`WIN98`、`WIN2K`(依 Windows 家族排序於 `WINDOWS` 之後)。
- `ui/RetroComponents.kt`:`RetroFrame` 新增 `THINBEVEL`(Windows 3.1 平面細斜邊);`macTitleBar` 擴充更名為 `headerChrome`,WIN31 於主畫面標頭繪製藏青標題列。
- `ui/Screens.kt`:置於桌面背景的四處文字(主畫面標頭、頁面標題、空紀錄提示、版本文字)改用 `desktopText`／`desktopMutedText`;風格說明補四款。
- `app/src/test/.../ui/RetroPaletteContrastTest.kt`:新增對比回歸測試(12 風格 × 明暗:主文字 ≥4.5、輔助 ≥3、數字 ≥2.5、選取字與顯示字 ≥3;Windows 灰經典數字配色豁免)。
- `docs/mockups/generate_mockups.py`:新增四款 Windows 設定檔、`thinbevel` 邊框、藏青標題列、選取態按鈕與 accent 色片、桌布文字色;同步對比修正色盤;移除既有稿保護,統一重生成。
- `docs/mockups/`:重生成 40 張 SVG 與 40 張 PNG(10 風格 × 2 版型 × 2 明暗,含 gameboy 原稿重生成以呈現選取態),`index.html` 改為 10 個風格區塊 + 麥金塔參考區。
- `docs/USER_GUIDE.md`、`README.md`、`docs/CHANGELOG.md`、`docs/RELEASE_NOTES.md`:同步十二種風格與可讀性修正說明。

### 問題與解法
- C64 淺色亮藍字對藍底僅 2.3:1,DOS 淺色深字壓在藍色桌布、亮綠/青數字在灰格面近乎隱形;以對比度實測逐組調整色值,並以 `RetroPaletteContrastTest` 防止回歸。
- `RetroColors` 原本沒有「桌面上的文字」語意色,深色桌布(DOS 藍幕、95/98 湖水綠、2000/XP 經典藍)標頭會不可讀;新增 `desktopText`/`desktopMutedText`,多數風格等於 text/mutedText,行為不變。
- Windows 灰（綠）與現有 Windows 灰僅差 accent,設計稿加入選取態按鈕(✓ 插旗)與 accent 色片才能呈現差異;gameboy 原稿一併重生成維持全風格一致。
- 對比測試發現 DOS 深色輔助文字(2.8:1)與紅白機淺色 2/6 號數字(2.2:1)不足,一併微調。

### 待辦／已知限制
- 尚未實機檢視 12 風格 × 2 版型 × 明暗的視覺呈現;此模型無法目視檢視 PNG,設計稿僅做結構驗證。
- Windows 3.1 標題列目前只套用於經典版主畫面標頭(比照麥金塔條紋標題列的範圍)。

## 2026-11-01 - v1.5.0（versionCode 11）

### 目標
復古風格一次加入全部六款候選風格（Game Boy 綠、Commodore 64、IBM CGA/DOS、紅白機、ZX Spectrum、Apple II 綠幕），各含淺色與深色變體，操作按鈕位置不變。

### 變更
- `data/SettingsRepository.kt`：`RetroStyle` 新增 `GAMEBOY`、`C64`、`DOS`、`FAMICOM`、`ZXSPECTRUM`、`APPLE2` 六個列舉值；持久化以名稱編碼，舊資料免移轉。
- `ui/MinesweeperTheme.kt`：新增 `retroPalette()` 色盤建構器與六組風格淺色／深色色盤（對照 `docs/mockups/` 設計稿），並擴充 `retroColors()` 分支。
- `ui/RetroComponents.kt`：新增 `RetroFrame` 與共用 `drawRetroFrame()`，支援像素斜邊／扁平外框／PETSCII 雙線框／DOS 對話框投影／純黑硬像素描邊／硬陰影視窗六種邊框語言；`raisedBorder()`、`sunkenBorder()` 改用它；`ChoiceRow` 支援 `itemsPerRow` 多欄排版。
- `ui/GameBoard.kt`：未翻開格子的描邊改用 `drawRetroFrame(embedded = true)`，取代原本的麥金塔／斜邊二分分支。
- `ui/Screens.kt`：設定頁風格說明補上六款；「復古風格」選項改兩欄排版；遊戲紀錄勝敗標籤在單色風格（麥金塔、Game Boy 綠、Apple II 綠幕）改用單色。
- `app/src/test/.../data/RetroStyleTest.kt`：新增六款風格解碼、標籤唯一性與全枚舉可還原測試。
- `app/build.gradle.kts`：`versionCode` 11、`versionName` 1.5.0。
- `docs/USER_GUIDE.md`、`README.md`、`docs/CHANGELOG.md`、`docs/RELEASE_NOTES.md`：同步更新八種風格說明。

### 問題與解法
- 六款風格的邊框語言各不相同，將描邊抽成單一 `DrawScope.drawRetroFrame()`，共用於元件（按鈕、面板、顯示器）與棋盤格子；格子以 `embedded` 參數把硬陰影改畫在格內，避免被相鄰格子覆蓋。
- 八種風格選項在單列會過度擠壓，`ChoiceRow` 新增 `itemsPerRow`，復古風格以兩欄四列呈現，其餘選項維持單列。
- 麥金塔與 Windows 灰的既有繪製路徑以 `BEVEL`、`OUTLINE` 對應保留，輸出與原本一致。

### 待辦／已知限制
- 尚未實機檢視七種風格 × 兩種版型 × 明暗模式的視覺呈現。
- ZX Spectrum 彩虹載入條、DOS 選單列等設計稿裝飾未搬進 App（維持操作介面位置不變）。

### 發布
- 已建置 `app/build/outputs/apk/prerelease/app-prerelease.apk`（versionCode 11、versionName 1.5.0-prerelease）。
- 發布 GitHub Release `v1.5.0-prerelease`，並上傳上述 APK。

## 2026-11-01 - 復古風格候選設計稿（未發版）

### 目標
為「復古風格增加 2 種風格」先行完成 6 種候選風格（Game Boy 綠、Commodore 64、IBM CGA/DOS、紅白機 Famicom、ZX Spectrum、Apple II 綠幕）的視覺設計稿，涵蓋經典版／復古掌機版 × 淺色／深色，供挑選 2 種後進入 App 程式實作。

### 變更
- `docs/mockups/generate_mockups.py`：新增設計稿產生器；以共用 9×9 展示盤面與既有 mockup 實測骨架座標輸出各風格 SVG，色盤以語意色彩欄位統一管理，並同步產生 `index.html`。
- `docs/mockups/`：新增 Game Boy 綠（僅深色）、Commodore 64、IBM CGA/DOS、紅白機 Famicom、ZX Spectrum、Apple II 綠幕共 22 張 SVG 與 22 張 PNG（780×1688）；既有 gameboy／macintosh 淺色稿不變。
- `docs/mockups/index.html`：改寫為 6 種候選風格總覽，每種含風格說明、淺／深色色盤 hex 表與四圖比較；麥金塔單色改列參考區。

### 問題與解法
- 各風格邊框語言不同，產生器將邊框抽成 6 種繪製函式（像素斜邊／硬描邊／PETSCII 雙線框／DOS 對話框投影／ZX 硬陰影／扁平外框），供實作階段對應 `RetroComponents`、`GameBoard` 的繪製分支。
- PNG 以 headless Chrome（`--force-device-scale-factor=2`）自 SVG 匯出 780×1688，與既有 mockup 尺寸一致。
- 產生器預設不覆寫既有 gameboy 淺色兩張原稿，需 `--force` 才重寫；重跑輸出可重現。

### 待辦／已知限制
- 尚未挑選 2 種風格，App 程式未做任何變動；挑選後需實作 `RetroStyle` enum、`MinesweeperTheme` 色盤、邊框分支、`Screens.kt` 風格說明與 `ChoiceRow` 四選項排版。
- 設計稿僅做結構驗證（XML、尺寸、盤面比對），尚未人工目視確認渲染結果。
- 深色稿與既有淺色原稿的圖形浮點座標有 0.1px 內的格式差異，視覺等價。

## 2026-10-31 - v1.4.2（versionCode 10）

### 目標
將預設棋盤改為中級；取消結果視窗的分享選項，改在主畫面右上角提供遊戲畫面截圖分享。

### 變更
- `GameViewModel.kt`：新增 `DEFAULT_DIFFICULTY = Difficulty.INTERMEDIATE`，初始棋局改用它；移除未再使用的 `GameState.shareText()`。
- `ui/Screens.kt`：`GameLayoutActions` 新增 `onShareScreenshot`；經典版 `AppHeader` 與掌機版 `HandheldToolButtons` 在「遊戲紀錄」右側加入分享圖示；結果視窗移除分享選項。
- `ui/Screens.kt`：新增 `shareScreenshot()`、`Context.findActivity()`，以 Activity `decorView` 截圖並輸出 PNG。
- `res/drawable/ic_share.xml`：新增分享畫面圖示。
- `AndroidManifest.xml`、`res/xml/file_paths.xml`：新增 `FileProvider` 與快取圖片路徑，供分享截圖使用。
- `app/src/test/.../DefaultDifficultyTest.kt`：新增預設難度測試。
- `app/build.gradle.kts`：`versionCode` 10、`versionName` 1.4.2。
- `docs/CHANGELOG.md`、`docs/USER_GUIDE.md`、`docs/PRIVACY.md`、`README.md`、`docs/RELEASE_NOTES.md`：同步更新預設難度與分享畫面說明。

### 問題與解法
- 直接截取 Activity `decorView` 可取得目前完整遊戲畫面，包含標頭與棋盤；結果對話框為獨立視窗，不會出現在截圖中。
- Android 7.0 以後不可直接以 `file://` URI 分享快取檔案，改用 `FileProvider` 產生唯讀 content URI 並加上讀取授權旗標。
- 截圖失敗或無可用分享目標時以 Toast 提示，不影響既有遊戲流程。

### 待辦／已知限制
- 截圖解析度依目前裝置畫面而定，尚未提供固定尺寸的成績卡片。
- 尚未實機確認不同廠牌裝置的分享面板、截圖內容與掌機版四個圖示的窄螢幕排版。

### 發布
- 已建置 `app/build/outputs/apk/prerelease/app-prerelease.apk`（versionCode 10、versionName 1.4.2-prerelease）。
- 發布 GitHub Release `v1.4.2-prerelease`，並上傳上述 APK。

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
