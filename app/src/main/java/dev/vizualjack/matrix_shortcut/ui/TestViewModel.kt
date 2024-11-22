package dev.vizualjack.matrix_shortcut.ui

import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import dev.vizualjack.matrix_shortcut.Gesture
import dev.vizualjack.matrix_shortcut.GestureElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class KeyCode(val value:Int) {
    VOLUME_UP(value = KeyEvent.KEYCODE_VOLUME_UP),
    VOLUME_DOWN(value = KeyEvent.KEYCODE_VOLUME_DOWN)
}

data class ServerSettingsState(
    val url:String = "",
    val user:String = "",
    val accessToken:String = ""
)

data class GesturesState(
    val gestures: ArrayList<Gesture> = arrayListOf()
)

data class GestureState(
    val gesture: Gesture = Gesture(arrayListOf(),"")
)

class TestViewModel : ViewModel() {
    private val _uiServerSettingsState = MutableStateFlow(ServerSettingsState())
    private val _uiGesturesState = MutableStateFlow(GesturesState())
    private val _uiGestureState = MutableStateFlow(GestureState())
    val uiServerSettingsState: StateFlow<ServerSettingsState> = _uiServerSettingsState.asStateFlow()
    val uiGesturesState: StateFlow<GesturesState> = _uiGesturesState.asStateFlow()
    val uiGestureState: StateFlow<GestureState> = _uiGestureState.asStateFlow()
    private var selectedGesture: Gesture? = null

    fun setUrl(newUrl:String) {
        _uiServerSettingsState.update { it.copy(url = newUrl) }
    }

    fun setUser(newUser:String) {
        _uiServerSettingsState.update { it.copy(user = newUser) }
    }

    fun setAccessToken(newAccessToken:String) {
        _uiServerSettingsState.update { it.copy(accessToken = newAccessToken) }
    }

    fun removeGesture() {
        _uiGesturesState.update {
            val gestures = arrayListOf<Gesture>()
            gestures.addAll(it.gestures)
            gestures.remove(selectedGesture)
            Log.i("ViewModel", "gestures.size: ${gestures.size}")
            it.copy(gestures = gestures)
        }
    }

    fun saveGesture() {
        _uiGesturesState.update {
            val gestures = arrayListOf<Gesture>()
            gestures.addAll(it.gestures)
            it.copy(gestures = gestures)
        }
    }

    fun addGesture() {
        _uiGesturesState.update {
            val gestures = arrayListOf<Gesture>()
            gestures.addAll(it.gestures)
            gestures.add(Gesture(arrayListOf(), actionName = ""))
            Log.i("ViewModel", "gestures.size: ${gestures.size}")
            it.copy(gestures = gestures)
        }
    }

    fun selectLastGesture() {
        selectGesture(_uiGesturesState.value.gestures.last())
    }

    fun selectGesture(gesture: Gesture) {
        selectedGesture = gesture
        if(selectedGesture == null) return
        _uiGestureState.update { it.copy(gesture = selectedGesture!!) }
    }

    fun setActionName(actionName:String) {
        _uiGestureState.update { it.copy(gesture = Gesture(selectedGesture!!.gestureElementList, actionName)) }
    }

    fun setGestureElementMinDuration(gestureElement: GestureElement, minDuration:Int) {
        gestureElement.minDuration = minDuration
        _uiGestureState.update { it.copy(gesture = selectedGesture!!) }
    }

    fun setGestureElementKeyCode(gestureElement: GestureElement, keyCode: Int) {
        gestureElement.keyCode = keyCode
        _uiGestureState.update { it.copy(gesture = selectedGesture!!) }
    }

    fun removeGestureElement(gestureElement: GestureElement) {
        selectedGesture!!.gestureElementList.remove(gestureElement)
        _uiGestureState.update { it.copy(gesture = selectedGesture!!) }
    }
}