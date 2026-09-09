# 踩地雷

一款以經典 Windows 踩地雷為靈感、針對 Android 手機操作重新設計的離線益智遊戲。

> [!WARNING]
> 目前版本為 **Pre-release 測試版本**，可能仍有介面、相容性或資料格式調整。請勿視為正式上線版本。

## 專案特色

- 經典立體灰色棋盤、七段顯示器與表情按鈕
- 初級、中級、高級及自訂棋盤
- 最多儲存 3 組自訂棋盤設定
- 第一個探索格及周圍九宮格安全
- 點按探索、長按插旗與快速展開
- 大型棋盤支援雙指縮放、拖曳及置中
- 支援手機直式與橫式畫面
- 支援系統、淺色及深色外觀
- 六組預設主題色與自訂色相滑桿
- 支援全螢幕模式
- 自動保存目前棋局
- 保存勝利、失敗及放棄紀錄
- 遊戲結果可複製或透過 Android 分享
- 完全離線、無廣告、無追蹤

## 下載

請由 [GitHub Releases](../../releases) 下載標示為 Pre-release 的 APK。

目前的 Pre-release APK 使用 Android Debug 金鑰簽署，只供測試及功能預覽，不適用於 Google Play 正式發布。

## 快速開始

1. 下載最新的 Pre-release APK。
2. 在 Android 裝置允許瀏覽器或檔案管理器安裝未知來源應用程式。
3. 開啟 APK 並完成安裝。
4. 啟動「踩地雷」，選擇難度後開始遊戲。

完整操作方式請參閱 [使用指南](docs/USER_GUIDE.md)。

## 開發資訊

| 項目 | 內容 |
|---|---|
| Application ID | `com.tainanlins5.minesweeper` |
| 語言 | Kotlin |
| UI | Jetpack Compose |
| 本機資料庫 | Room |
| 設定儲存 | DataStore |
| 最低 Android 版本 | Android 6.0（API 23） |
| Target SDK | API 36 |
| Java | JDK 17 |
| 正式測試 Build Type | `prerelease` |

建置方式、工具版本與輸出位置請參閱 [開發與建置指南](docs/BUILDING.md)。

## 文件

- [文件索引](docs/README.md)
- [使用指南](docs/USER_GUIDE.md)
- [開發與建置指南](docs/BUILDING.md)
- [APP 優化建議](docs/APP_OPTIMIZATION.md)
- [隱私權說明](docs/PRIVACY.md)
- [變更紀錄](docs/CHANGELOG.md)
- [貢獻指南](CONTRIBUTING.md)
- [授權條款](LICENSE)

## 專案狀態

本專案仍在 Pre-release 階段。問題回報與改善建議可透過 GitHub Issues 提出。

## 授權

本專案採用 [MIT License](LICENSE) 授權。
