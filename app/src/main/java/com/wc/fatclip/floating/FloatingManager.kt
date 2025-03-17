package com.wc.fatclip.floating

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import android.view.inspector.WindowInspector
import android.widget.Toast
import com.wc.fatclip.App
import com.wc.fatclip.ClickService
import com.wc.fatclip.dp2px
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 悬浮窗管理器
 *
 * @date 2023/06/07
 * */
class FloatingManager private constructor() {
    private var clickView: ClickView? = null//窗口View
    private var controlView: ControlView? = null//控制View
    private var isShowing = false//窗口显示中状态

    //窗口管理器
    private val windowManager: WindowManager by lazy {
        App.instance?.getSystemService(
            Activity.WINDOW_SERVICE
        ) as WindowManager
    }
    private val wmParams: WindowManager.LayoutParams by lazy { buildParams() }

    private val windowControlManager: WindowManager by lazy {
        ClickService.mService.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    }
    private val wmControlParams: WindowManager.LayoutParams by lazy { buildControlParams() }

    /**
     * 正在显示中
     * */
    fun isShowing(): Boolean {
        return isShowing
    }

    /**
     * 前后台切换的时候，检测当前所在的activity判定是否需要显示和关闭悬浮窗
     * */
    fun checkCurrentActivityAndUpdateIfNeeded() {
        if (!canDrawOverlays()) {
            Toast.makeText(App.instance, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isShowing) {
            show()
        }
    }

    private fun show() {
        if (!canDrawOverlays()) {
            return
        }
        clickView?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (WindowInspector.getGlobalWindowViews().contains(it)) {
                    return
                }
            }
            if (it.isAttachedToWindow || it.parent != null) {
                return
            }
            val p = it.layoutParams
            if (p is WindowManager.LayoutParams) {
                wmParams.copyFrom(p)
            }
        }
        if (clickView == null) {
            clickView = ClickView(App.instance!!).apply {
                wm = windowManager
                wmp = wmParams
            }
        }
        wmParams.gravity = Gravity.TOP or Gravity.LEFT
        wmParams.x = 160f.dp2px()
        wmParams.y = 250f.dp2px()
        windowManager.addView(clickView, wmParams)

        controlView?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (WindowInspector.getGlobalWindowViews().contains(it)) {
                    return
                }
            }
            if (it.isAttachedToWindow || it.parent != null) {
                return
            }
            val p = it.layoutParams
            if (p is WindowManager.LayoutParams) {
                wmControlParams.copyFrom(p)
            }
        }
        if (controlView == null) {
            controlView = ControlView(App.instance!!).apply {
                wm = windowControlManager
                wmp = wmControlParams
            }
        }
        wmControlParams.gravity = Gravity.TOP or Gravity.LEFT
        wmControlParams.x = 0
        wmControlParams.y = 500f.dp2px()
        windowControlManager.addView(controlView, wmControlParams)
        isShowing = true
    }

    /**
     * 取消悬浮窗，自动判断当前线程
     * */
    @OptIn(DelicateCoroutinesApi::class)
    fun dismiss() {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            dismissInternal()
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                dismissInternal()
            }
        }
    }

    private fun dismissInternal() {
        if (!isShowing) return
        try {
            windowManager.removeViewImmediate(clickView)
            val p = clickView?.layoutParams
            if (p is WindowManager.LayoutParams) {
                wmParams.copyFrom(p)
            }
            windowControlManager.removeViewImmediate(controlView)
            val p1 = controlView?.layoutParams
            if (p1 is WindowManager.LayoutParams) {
                wmControlParams.copyFrom(p1)
            }
            isShowing = false
            clickView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    ).apply {
        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.RGBA_8888
        flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        gravity = Gravity.END or Gravity.BOTTOM
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    private fun buildControlParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    ).apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.RGBA_8888
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
//        flags =
//            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        //flags = 1800
        flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
    }

    companion object {
        const val TAG = "FloatingManager"
        val instance: FloatingManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FloatingManager()
        }

        /**
         * 判断是否具有悬浮窗权限
         * */
        @JvmStatic
        fun canDrawOverlays() = Settings.canDrawOverlays(App.instance!!)

        /**
         * 跳转到悬浮窗权限设置页面
         * */
        @JvmStatic
        fun requestFloatPermission(context: Context?, requestCode: Int) {
            context ?: return
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + App.instance!!.packageName)
            if (context is Activity) {
                context.startActivityForResult(intent, requestCode)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }

        /**
         * 判断当前手机是小米
         * */
        @JvmStatic
        val isMiui: Boolean
            get() {
                return "XIAOMI" == Build.MANUFACTURER.uppercase()
            }

        /**
         * 检测当前是否具有后台弹出界面权限
         * */
        @JvmStatic
        fun isAllowedForMiui(context: Context): Boolean {
            val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val op = 10021
                val method = ops.javaClass.getMethod(
                    "checkOpNoThrow", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        String::class.java
                    )
                )
                val result = method.invoke(ops, op, Process.myUid(), context.packageName) as Int
                return result == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * 反射miui的mMiuiFlags字段绕过后台弹出界面权限
         * */
        @JvmStatic
        fun hookIntentForMiui(intent: Intent): Intent {
            try {
                val clazz: Class<*> = intent.javaClass
                val field = clazz.getDeclaredField("mMiuiFlags")
                field.isAccessible = true
                field[intent] = 0x2
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            return intent
        }
    }
}