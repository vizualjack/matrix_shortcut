package dev.vizualjack.matrix_shortcut.ui


import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.vizualjack.matrix_shortcut.Gesture
import dev.vizualjack.matrix_shortcut.GestureStorage
import dev.vizualjack.matrix_shortcut.MainActivity
import dev.vizualjack.matrix_shortcut.SettingsStorage

enum class TestScreen() {
    Gestures,
    Gesture,
    Settings
}

@Composable
fun TestApp(
    activity: MainActivity,
    viewModel: TestViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    var gestures by remember { mutableStateOf(GestureStorage(activity.applicationContext).loadGestures()) }
    var selectedGesture: Gesture? = null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = TestScreen.Gestures.name,
        ) {
            composable(route = TestScreen.Settings.name) {
                SettingsPage(activity,
                    SettingsStorage(activity.applicationContext).loadSettings(),
                    backAction = {
                    navController.backQueue.clear()
                    navController.navigate(TestScreen.Gestures.name)
                })
            }
            composable(route = TestScreen.Gestures.name) {
                GestureList(
                    activity = activity,
                    gestures = gestures,
                    openGesture = {index ->
                        selectedGesture = gestures[index]
                        navController.backQueue.clear()
                        navController.navigate(TestScreen.Gesture.name)
                    },
                    addGesture = {
                        gestures = gestures + Gesture(arrayListOf(),"")
                    },
                    onSettingsClick = {
                        navController.backQueue.clear()
                        navController.navigate(TestScreen.Settings.name)
                    }
                )
            }
            composable(route = TestScreen.Gesture.name) {
                GestureEdit(
                    gesture = selectedGesture!!,
                    onBack = {
                        Log.i("Screen", "selectedGesture gestureElements...")
                        for(gestureElement in selectedGesture!!.gestureElementList) {
                            Log.i("Screen", "key ${gestureElement.keyCode} for ${gestureElement.minDuration}ms")
                        }
                        Log.i("Screen", "selectedGesture gestureElements...done")
                        navController.backQueue.clear()
                        navController.navigate(TestScreen.Gestures.name)
                        GestureStorage(activity.applicationContext).saveGestures(gestures)
                    },
                    onDelete = {
                        gestures = gestures.toMutableList().apply { remove(selectedGesture) }
                        navController.backQueue.clear()
                        navController.navigate(TestScreen.Gestures.name)
                    }
                )
            }
        }
    }
}