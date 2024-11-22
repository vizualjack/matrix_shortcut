package dev.vizualjack.matrix_shortcut

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.ui.Settings
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class SettingsStorage(private val context: Context) {

    private val FILE_NAME = "settings.json"
    fun loadSettings(): Settings {
        val file = File(context.filesDir, FILE_NAME)
        if(!file.exists()) {
            Log.i("SettingsStorage", "loadSettings() - no settings storage file")
            return Settings("","","", "")
        }
        try {
            val settings = Json.decodeFromString<Settings>(file.readText())
            Log.i("SettingsStorage", "loadSettings() - loaded settings")
            return settings
        }catch (ex:Exception) {
            Log.i("SettingsStorage", "loadSettings() - error on loading settings :(")
            return Settings("","","", "")
        }
    }

    fun saveSettings(settings: Settings) {
        Log.i("SettingsStorage", "saveSettings() - saving...")
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(Json.encodeToString(settings))
        Log.i("SettingsStorage", "saveSettings() - saved settings")
    }
}