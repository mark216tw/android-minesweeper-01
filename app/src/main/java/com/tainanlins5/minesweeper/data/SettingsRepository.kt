package com.tainanlins5.minesweeper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

enum class AppearanceMode(val label: String) {
    SYSTEM("系統"),
    LIGHT("淺色"),
    DARK("深色"),
}

data class AppSettings(
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val themeHue: Float = 190f,
    val fullscreen: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val flagModeDefault: Boolean = false,
    val customBoards: List<CustomBoardPreset> = emptyList(),
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
        val themeHue = floatPreferencesKey("theme_hue")
        val fullscreen = booleanPreferencesKey("fullscreen")
        val sound = booleanPreferencesKey("sound")
        val vibration = booleanPreferencesKey("vibration")
        val flagModeDefault = booleanPreferencesKey("flag_mode_default")
        val customBoards = stringPreferencesKey("custom_boards")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { values ->
        AppSettings(
            appearanceMode = values[Keys.appearance]
                ?.let { runCatching { AppearanceMode.valueOf(it) }.getOrNull() }
                ?: AppearanceMode.SYSTEM,
            themeHue = values[Keys.themeHue] ?: 190f,
            fullscreen = values[Keys.fullscreen] ?: false,
            soundEnabled = values[Keys.sound] ?: true,
            vibrationEnabled = values[Keys.vibration] ?: true,
            flagModeDefault = values[Keys.flagModeDefault] ?: false,
            customBoards = decodeCustomBoards(values[Keys.customBoards].orEmpty()),
        )
    }

    suspend fun setAppearance(value: AppearanceMode) = context.settingsDataStore.edit {
        it[Keys.appearance] = value.name
    }

    suspend fun setThemeHue(value: Float) = context.settingsDataStore.edit {
        it[Keys.themeHue] = value.coerceIn(0f, 360f)
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
