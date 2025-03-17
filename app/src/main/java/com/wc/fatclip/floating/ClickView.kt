package com.wc.fatclip.floating

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import com.wc.fatclip.R
import kotlin.math.abs

/**
 * 悬浮窗视图
 *
 * @date 2023/06/07
 * */
class ClickView(context: Context) :
    FrameLayout(context) {
    var wm: WindowManager? = null
    var wmp: WindowManager.LayoutParams? = null

    //拖拽
    private var mTouchSlop = 0
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mIsBeingDragged = false

    companion object {
        var clickView: ClickView? = null
    }

    init {
        //keepScreenOn = true
        mTouchSlop = ViewConfiguration.get(context).scaledPagingTouchSlop / 2
        View.inflate(context, R.layout.float_layout, this)
        clickView = this
    }

    fun setOutClick(canTouch: Boolean) {
        wmp?.let {
            if (canTouch) {
                it.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
        }
        wm?.updateViewLayout(this@ClickView, wmp)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mIsBeingDragged = false
                mLastMotionX = ev.rawX
                mLastMotionY = ev.rawY
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
                        it.x += xDiff.toInt()
                        it.y += yDiff.toInt()
                    }
                    wm?.updateViewLayout(this@ClickView, wmp)
                    mLastMotionX = x
                    mLastMotionY = y
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mIsBeingDragged) {
                    moveSide(ev.rawX.toInt())
                    mIsBeingDragged = false
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun moveSide(rawX: Int) {
//        wmp?.let {
//            val xMax = screenWidth - measuredWidth - edgeWith
//            val yMax = screenHeight - measuredHeight - edgeHeight
//            //X轴吸边
//            val fromX = it.x
//            val toX: Int = if (rawX < screenWidth / 2f) {
//                xMax
//            } else {
//                edgeWith
//            }
//            val diffX = toX - fromX
//            //Y轴
//            val fromY = it.y
//            val toY = if (it.y < edgeHeight) {//Y轴向上滑出了屏幕
//                edgeHeight
//            } else if (it.y > yMax) {
//                yMax
//            } else {
//                0
//            }
//            val diffY = toY - fromY
//            //开始动画
//            anim = ofFloat(0f, 1f)
//            anim?.duration = 100
//            anim?.interpolator = DecelerateInterpolator()
//            anim?.addUpdateListener { animation: ValueAnimator ->
//                val value = animation.animatedValue as Float
//                it.x = fromX + (value * diffX).toInt()
//                if (toY != 0) {
//                    it.y = fromY + (value * diffY).toInt()
//                }
//                wm?.updateViewLayout(this@ClickView, it)
//            }
//            anim?.start()
//        }
    }
}