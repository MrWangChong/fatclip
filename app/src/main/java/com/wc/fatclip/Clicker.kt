package com.wc.fatclip

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.View.OnClickListener
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import java.util.concurrent.ConcurrentHashMap

@RequiresApi(Build.VERSION_CODES.N)
object Clicker {
    private val tasks: ConcurrentHashMap<Int, Point> = ConcurrentHashMap()
    var isStarting = false
    var listener: OnClickListener? = null

    private var timer: CountTimer? = null
    private var count = 500

    fun startClick(point: Point, @IntRange(from = 0, to = 4) id: Int = 0) {
        if (id < 0 || id > 4) {
            return
        }
        val service = ClickService.mService ?: return
        isStarting = true
        tasks[id] = point
        count = 500
        if (timer != null) {
            timer?.start()
            return
        }
        timer = object : CountTimer(100) {
            override fun onTick(millisUntilFinished: Long) {
                if (isStarting && count > 0) {
                    for (i in 0 until 5) {
                        service.dispatchGesture(
                            tasks[i]?.gesture ?: continue,
                            null, null
                        )
                    }
                } else {
                    timer?.cancel()
                    listener?.onClick(null)
                    isStarting = false
                }
                count--
            }
        }
        timer?.start()
    }


    fun stopClick(@IntRange(from = 0, to = 4) id: Int = 0) {
        isStarting = false
        tasks.remove(id)
        if (tasks.isEmpty()) {
            timer?.cancel()
            timer = null
        }
        Log.d("Clicker", "停止自动点击：<$id>")
    }

    fun clear() {
        isStarting = false
        timer?.cancel()
        timer = null
        tasks.clear()
    }

    data class Point(
        val x: Int, val y: Int,
    ) {
        private val path: Path
            get() {
                return Path().also {
                    val newPoint = randomized
                    it.moveTo(newPoint.x.toFloat(), newPoint.y.toFloat())
                }
            }

        val randomized: Point
            get() = Point(
                x + RANDOM_RANGE.random(),
                y + RANDOM_RANGE.random()
            )

        val gesture: GestureDescription
            get() {
                return GestureDescription.Builder()
                    .addStroke(
                        GestureDescription.StrokeDescription(
                            path, 0, 5
                        )
                    )
                    .build()
            }

        override fun toString(): String {
            return "Point(x: $x, y: $y)"
        }

        companion object {
            val RANDOM_RANGE = -5..5
        }
    }
}