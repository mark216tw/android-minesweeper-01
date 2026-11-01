package com.tainanlins5.minesweeper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

enum class AppearanceMode(val label: String) {
    SYSTEM("系統"),
    LIGHT("淺色"),
    DARK("深色"),
}

enum class GameLayout(val label: String) {
    CLASSIC("經典版"),
    HANDHELD("復古掌機版"),
}

enum class RetroStyle(val label: String) {
    WINDOWS("Windows 灰"),
    WINGREEN("Windows 灰綠"),
    MACINTOSH("麥金塔"),
    GAMEBOY("Game Boy 綠"),
    C64("Commodore 64"),
    FAMICOM("紅白機"),
    APPLE2("Apple II 綠幕"),
}

data class AppSettings(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val fullscreen: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val flagModeDefault: Boolean = false,
    val customBoards: List<CustomBoardPreset> = emptyList(),
    val gameLayout: GameLayout = GameLayout.CLASSIC,
    val retroStyle: RetroStyle = RetroStyle.WINDOWS,
)

data class CustomBoardPreset(
    val width: Int,
    val height: Int,
    val mines: Int,
) {
    val label: String get() = "$width × $height · $mines 雷"
}

class SettingsRepository(private val context: Context) {
    private object Keys {
        val appearance = stringPreferencesKey("appearance")
        val fullscreen = booleanPreferencesKey("fullscreen")
        val sound = booleanPreferencesKey("sound")
        val vibration = booleanPreferencesKey("vibration")
        val flagModeDefault = booleanPreferencesKey("flag_mode_default")
        val customBoards = stringPreferencesKey("custom_boards")
        val gameLayout = stringPreferencesKey("game_layout")
        val retroStyle = stringPreferencesKey("retro_style")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { values ->
            AppSettings(
                appearanceMode = values[Keys.appearance]
                    ?.let { runCatching { AppearanceMode.valueOf(it) }.getOrNull() }
                    ?: AppearanceMode.SYSTEM,
                fullscreen = values[Keys.fullscreen] ?: false,
                soundEnabled = values[Keys.sound] ?: true,
                vibrationEnabled = values[Keys.vibration] ?: true,
                flagModeDefault = values[Keys.flagModeDefault] ?: false,
                customBoards = decodeCustomBoards(values[Keys.customBoards].orEmpty()),
                gameLayout = decodeGameLayout(values[Keys.gameLayout]),
                retroStyle = decodeRetroStyle(values[Keys.retroStyle]),
            )
        }

    suspend fun setAppearance(value: AppearanceMode) = context.settingsDataStore.edit {
        it[Keys.appearance] = value.name
    }

    suspend fun setGameLayout(value: GameLayout) = context.settingsDataStore.edit {
        it[Keys.gameLayout] = value.name
    }

    suspend fun setRetroStyle(value: RetroStyle) = context.settingsDataStore.edit {
        it[Keys.retroStyle] = value.name
    }

    suspend fun setFullscreen(value: Boolean) = context.settingsDataStore.edit {
        it[Keys.fullscreen] = value
    }

    suspend fun setSound(value: Boolean) = context.settingsDataStore.edit { it[Keys.sound] = value }

    suspend fun setVibration(value: Boolean) = context.settingsDataStore.edit { it[Keys.vibration] = value }

    suspend fun setFlagModeDefault(value: Boolean) = context.settingsDataStore.edit {
        it[Keys.flagModeDefault] = value
    }

    suspend fun saveCustomBoard(preset: CustomBoardPreset) = context.settingsDataStore.edit { values ->
        val current = decodeCustomBoards(values[Keys.customBoards].orEmpty())
        if (preset !in current && current.size < MAX_CUSTOM_BOARDS) {
            values[Keys.customBoards] = encodeCustomBoards(current + preset)
        }
    }

    suspend fun deleteCustomBoard(preset: CustomBoardPreset) = context.settingsDataStore.edit { values ->
        val current = decodeCustomBoards(values[Keys.customBoards].orEmpty())
        values[Keys.customBoards] = encodeCustomBoards(current - preset)
    }

    companion object {
        const val MAX_CUSTOM_BOARDS = 3
    }
}

internal fun encodeCustomBoards(boards: List<CustomBoardPreset>): String = boards
    .take(SettingsRepository.MAX_CUSTOM_BOARDS)
    .joinToString("|") { "${it.width},${it.height},${it.mines}" }

internal fun decodeCustomBoards(value: String): List<CustomBoardPreset> = value
    .split('|')
    .mapNotNull { entry ->
        val values = entry.split(',').mapNotNull(String::toIntOrNull)
        if (values.size != 3) return@mapNotNull null
        val (width, height, mines) = values
        if (width !in 5..30 || height !in 5..24 || mines !in 1..(width * height - 9)) {
            return@mapNotNull null
        }
        CustomBoardPreset(width, height, mines)
    }
    .distinct()
    .take(SettingsRepository.MAX_CUSTOM_BOARDS)

internal fun decodeGameLayout(value: String?): GameLayout = value
    ?.let { runCatching { GameLayout.valueOf(it) }.getOrNull() }
    ?: GameLayout.CLASSIC

internal fun decodeRetroStyle(value: String?): RetroStyle = value
    ?.let { runCatching { RetroStyle.valueOf(it) }.getOrNull() }
    ?: RetroStyle.WINDOWS
