package dev.vizualjack.matrix_shortcut

import kotlinx.serialization.Serializable


@Serializable
class GestureElement(var keyCode:Int, var minDuration:Int)

class GestureInput(var keyCode:Int, var duration:Int)

@Serializable
class Gesture(var gestureElementList:ArrayList<GestureElement>, var actionName:String)

class GestureManager{
    private val gestures : ArrayList<Gesture> = arrayListOf()
    private val currentGestureInput: ArrayList<GestureInput> = arrayListOf()

    fun addGesture(gestureElementList:ArrayList<GestureElement>, actionName:String) {
        gestures.add(Gesture(gestureElementList, actionName))
    }

    fun addGesture(gesture:Gesture) {
        gestures.add(gesture)
    }

    fun addGestures(addGestures:List<Gesture>) {
        gestures.addAll(addGestures)
    }

    fun clearGestures() {
        gestures.clear()
    }

    fun addGestureInput(keyCode:Int, duration:Int):Boolean {
        currentGestureInput.add(GestureInput(keyCode, duration))
        return true
    }

    private fun checkGestureMatchesInput(gesture: Gesture):Boolean {
        if(gesture.gestureElementList.size != currentGestureInput.size) return false
        for (i in 0 until gesture.gestureElementList.size) {
            val gestureInput = currentGestureInput[i]
            val gestureElement = gesture.gestureElementList[i]
            if(gestureInput.keyCode != gestureElement.keyCode) return false
            if(gestureInput.duration < gestureElement.minDuration) return false
        }
        return true
    }

    fun countMatches():Int {
        val inputNum = currentGestureInput.size - 1
        val deleteGestures = arrayListOf<Gesture>()

        for (gesture in gestures){
            if (gesture.gestureElementList.size <= inputNum){
                deleteGestures.add(gesture)
                continue
            }
            val gestureElement = gesture.gestureElementList[inputNum]
            val inputElement = currentGestureInput[inputNum]
            if( inputElement.keyCode != gestureElement.keyCode || inputElement.duration < gestureElement.minDuration)
            {
                deleteGestures.add(gesture)
            }
        }
        gestures.removeAll(deleteGestures)
        return gestures.size
    }

    fun getMatchActionName():String? {
        for (gesture in gestures) if(checkGestureMatchesInput(gesture)) return gesture.actionName
        return null
    }

    fun clearGestureInput() {
        currentGestureInput.clear()
    }
}