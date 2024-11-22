package dev.vizualjack.matrix_shortcut

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.lang.Exception
import kotlin.math.log

class MyAccessibilityService : AccessibilityService() {
    val gestureManager = GestureManager()
    var screenOnTime: Long = 0
    var checkForGesture: Boolean = true
    var isTracking: Boolean = false
    val START_TIMEOUT = 2000
    val KEYUP_TO_KEYDOWN_TIMEOUT = 400
    var currentKeyCode: Int = 0
    var keyDownTime: Long = 0
    var keyUpTime: Long = 0
    var lastKeyAction = KeyEvent.ACTION_UP
    var vibrator: Vibrator? = null
    var logSaver: LogSaver? = null

    private fun sendActionName(actionName:String) {
        try {
            Log.i("MyAccessibilityService", "sendActionName() - $actionName")
            vibrate(250L,VibrationEffect.DEFAULT_AMPLITUDE)
            val settings = SettingsStorage(applicationContext).loadSettings()
            sendToMatrixServer(settings, actionName, context = applicationContext)
        } catch (ex: Exception) {
            logSaver?.save("Exception at sendActionName:")
            logSaver?.save(ex)
        }
    }

    private fun initVibratorManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        }
        else vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    private fun vibrate(durationMillis:Long, vibrationEffect: Int) {
        try {
            if (vibrator == null) initVibratorManager()
            if(vibrator != null) vibrator!!.vibrate(VibrationEffect.createOneShot(durationMillis,vibrationEffect))
        } catch (ex: Exception) {
            logSaver?.save("Exception at vibrate:")
            logSaver?.save(ex)
        }
    }

    fun reset() {
        gestureManager.clearGestureInput()
        checkForGesture = true
        keyUpTime = 0
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.SCREEN_OFF")
        filter.addAction("android.intent.action.SCREEN_ON")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if(intent.action.toString() == "android.intent.action.SCREEN_ON") {
                        screenOnTime = System.currentTimeMillis()
                        Log.i("MyAccessibilityService", "screenOnTime set...${screenOnTime}")
                        reset()
                        vibrate(250L,VibrationEffect.EFFECT_DOUBLE_CLICK)
                    }
                } catch (ex: Exception) {
                    logSaver?.save("Exception at BroadcastReceiver.onReceive:")
                    logSaver?.save(ex)
                }
            }
        }
        registerReceiver(receiver, filter)
        logSaver = LogSaver(applicationContext)
        Log.i("MyAccessibilityService", "service connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i("MyAccessibilityService", "service disconnected")
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.i("MyAccessibilityService", "interrupted")
    }

    fun startCheckDoneThread() {
        try {
            val checkThread = Thread(Runnable {
                try {
                    Log.i("MyAccessibilityService", "checkThread started...")
                    while(true) {
                        Thread.sleep(KEYUP_TO_KEYDOWN_TIMEOUT.toLong())
                        if(!isTracking) {
                            Log.i("MyAccessibilityService", "tracking already done")
                            return@Runnable
                        }
                        Log.i("MyAccessibilityService", "check if done")
                        val timeSinceLastKeyUp = System.currentTimeMillis() - keyUpTime
                        if (timeSinceLastKeyUp > KEYUP_TO_KEYDOWN_TIMEOUT && lastKeyAction == KeyEvent.ACTION_UP) {
                            isTracking = false
                            Log.i("MyAccessibilityService", "done! check for match...")
                            val actionName = gestureManager.getMatchActionName()
                            if (actionName != null) sendActionName(actionName)
                            return@Runnable
                        }
                    }
                } catch (ex: Exception) {
                    logSaver?.save("CheckThread got exception:")
                    logSaver?.save(ex)
                }
            })
            checkThread.start()
        } catch (ex: Exception) {
            logSaver?.save("Exception at startCheckDoneThread:")
            logSaver?.save(ex)
        }
    }

    fun reloadGestures() {
        gestureManager.clearGestures()
        gestureManager.addGestures(GestureStorage(applicationContext).loadGestures())
        Log.i("MyAccessibilityService", "reloadGestures() - gestures reloaded")
    }

    fun checkForStartTracking(event: KeyEvent) {
        checkForGesture = false
        if(event.action == KeyEvent.ACTION_UP) {
            isTracking = false
            return
        }
        var timeBetween = System.currentTimeMillis() - screenOnTime
        Log.i("MyAccessibilityService", "checkForStartTracking() - timeBetween: $timeBetween")
        isTracking = timeBetween <= START_TIMEOUT
        if(isTracking) {
            Log.i("MyAccessibilityService", "checkForStartTracking() - tracking started...")
            reloadGestures()
            startCheckDoneThread()
        }
    }

    fun checkForTimeout() {
        if(keyUpTime <= 0) return
        val keyUpToKeyDownTime = keyDownTime - keyUpTime
        Log.i("MyAccessibilityService", "checkForTimeout() - $keyUpToKeyDownTime")
        if (keyUpToKeyDownTime <= KEYUP_TO_KEYDOWN_TIMEOUT) return
        Log.i("MyAccessibilityService", "checkForTimeout() - timeout reached!")
        isTracking = false
    }

    fun onKeyDown(event: KeyEvent) {
        keyDownTime = System.currentTimeMillis()
        currentKeyCode = event.keyCode
        checkForTimeout()
    }

    fun onKeyUp(event: KeyEvent) {
        keyUpTime = System.currentTimeMillis()
        val duration = keyUpTime - keyDownTime
        Log.i("MyAccessibilityService", "key $currentKeyCode pressed for $duration")
        gestureManager.addGestureInput(currentKeyCode, duration.toInt())
        val matches = gestureManager.countMatches()
        if(matches > 1 || matches == 0) return
        val actionName = gestureManager.getMatchActionName() ?: return
        sendActionName(actionName)
        Log.i("MyAccessibilityService", "found match: $actionName")
        isTracking = false
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        try {
            if(event == null || !checkForGesture && !isTracking) return super.onKeyEvent(event)
            if(!isTracking) checkForStartTracking(event)
            if(!isTracking) return super.onKeyEvent(event)
            if(event.action == KeyEvent.ACTION_DOWN) onKeyDown(event)
            else onKeyUp(event)
            lastKeyAction = event.action
            return super.onKeyEvent(event)
        } catch (ex: Exception) {
            logSaver?.save("Exception at onKeyEvent:")
            logSaver?.save(ex)
            return super.onKeyEvent(event)
        }
    }
}