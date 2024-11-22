package dev.vizualjack.matrix_shortcut

import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class GestureStorage(private val context: Context) {

    private val FILE_NAME = "gestureStorage.json"
    fun loadGestures():List<Gesture> {
//        context.dataDir   context.filesDir
        val file = File(context.dataDir, FILE_NAME)
        if(!file.exists()) {
            Log.i("GestureStorage", "loadGestures() - no gesture storage file")
            return arrayListOf()
        }
        val loadedGestures = Json.decodeFromString<List<Gesture>>(file.readText())
        Log.i("GestureStorage", "loadGestures() - loaded ${loadedGestures.size} gesture")
        return loadedGestures
    }

    fun saveGestures(gestures: List<Gesture>) {
        Log.i("GestureStorage", "saveGestures() - saving...")
        val file = File(context.dataDir, FILE_NAME)
        file.writeText(Json.encodeToString(gestures))
        Log.i("GestureStorage", "saveGestures() - saved ${gestures.size} gestures")
    }
}