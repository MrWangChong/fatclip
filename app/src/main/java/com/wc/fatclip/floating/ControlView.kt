package com.wc.fatclip.floating

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.wc.fatclip.Clicker
import com.wc.fatclip.R
import java.lang.Math.abs

class ControlView(context: Context) :
    FrameLayout(context) {
    var wm: WindowManager? = null
    var wmp: WindowManager.LayoutParams? = null
    private val play: ImageView
    private val close: View
    private val TAP_TIMEOUT = ViewConfiguration.getTapTimeout()
    private var clickTime = 0L
    private var playClickTime = 0L

    //拖拽
    private var mTouchSlop = 0
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mIsBeingDragged = false

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledPagingTouchSlop / 2
        View.inflate(context, R.layout.float_control_layout, this)
        close = findViewById(R.id.close)
        close.setOnClickListener {
            FloatingManager.instance.dismiss()
        }
        play = findViewById(R.id.click)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Clicker.isStarting) {
                play.setImageResource(R.drawable.icon_stop)
                close.visibility = View.GONE
            }
            play.setOnClickListener {
                val time = System.currentTimeMillis()
                //防止连续点击
                if (time - playClickTime < 300) {
                    return@setOnClickListener
                }
                playClickTime = time
                if (Clicker.isStarting) {
                    Log.d("Clicker", "点击：stop")
                    Clicker.stopClick()
                    play.setImageResource(R.drawable.icon_play)
                    close.visibility = View.VISIBLE
                    ClickView.clickView?.setOutClick(true)
                } else {
                    ClickView.clickView?.let {
                        Log.d("Clicker", "点击：start")
                        it.setOutClick(false)
                        val location = IntArray(2)
                        it.getLocationOnScreen(location)
                        val x = location[0] + it.width / 2// x 坐标
                        val y = location[1] + it.height / 2// y 坐标
                        val point = Clicker.Point(
                            x,
                            y
                        )
                        Clicker.startClick(point)
                        play.setImageResource(R.drawable.icon_stop)
                        close.visibility = View.GONE
                    }
                }
            }
            Clicker.listener = OnClickListener {
                post {
                    play.setImageResource(R.drawable.icon_play)
                    close.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mIsBeingDragged = false
                mLastMotionX = ev.rawX
                mLastMotionY = ev.rawY
                clickTime = System.currentTimeMillis()
            }

            MotionEvent.ACTION_MOVE -> {
                val x = ev.rawX
                val xDiff = x - mLastMotionX
                val y = ev.rawY
                val yDiff = y - mLastMotionY
                if (!mIsBeingDragged && (abs(xDiff) > mTouchSlop || abs(yDiff) > mTouchSlop)) {
                    mIsBeingDragged = true
                }
                if (mIsBeingDragged) {
                    wmp?.let {
                        //it.x += xDiff.toInt()
                        it.y += yDiff.toInt()
                    }
                    wm?.updateViewLayout(this@ControlView, wmp)
                    mLastMotionX = x
                    mLastMotionY = y
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mIsBeingDragged) {
                    //moveSide(ev.rawX.toInt())
                    mIsBeingDragged = false
                    return true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (System.currentTimeMillis() - clickTime < TAP_TIMEOUT) {
                        if (Clicker.isStarting) {
                            play.performClick()
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
