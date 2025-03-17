package com.wc.fatclip

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import com.wc.fatclip.floating.FloatingManager
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

class MainActivity : ComponentActivity() {
    lateinit var root: ViewGroup
    lateinit var overlay: LinearLayout
    lateinit var overlaySwitch: MaterialSwitch
    lateinit var accessibility: LinearLayout
    lateinit var accessibilitySwitch: MaterialSwitch
    lateinit var start: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = findViewById(R.id.root)
        overlay = findViewById(R.id.overlay)
        overlaySwitch = findViewById(R.id.overlaySwitch)
        accessibility = findViewById(R.id.accessibility)
        accessibilitySwitch = findViewById(R.id.accessibilitySwitch)
        overlay.setOnClickListener {
            if (!overlaySwitch.isChecked) {
                overlaySwitch.isChecked = true
            }
        }
        overlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            overlaySwitch.isEnabled = !isChecked
            start.isEnabled = isChecked
            if (isChecked) {
                XXPermissions.with(this@MainActivity)
                    .permission(Permission.SYSTEM_ALERT_WINDOW)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(
                            permissions: MutableList<String>,
                            allGranted: Boolean
                        ) {
                        }

                        override fun onDenied(
                            permissions: MutableList<String>,
                            doNotAskAgain: Boolean
                        ) {
                            overlaySwitch.isChecked = false
                            Snackbar.make(
                                root,
                                "胖夹子点赞器 需要悬浮窗权限才能正常使用",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }
        accessibility.setOnClickListener {
            if (!accessibilitySwitch.isChecked) {
                accessibilitySwitch.isChecked = true
            }
        }
        accessibilitySwitch.setOnCheckedChangeListener { _, isChecked ->
            accessibilitySwitch.isEnabled = !isChecked
            if (isChecked) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        start = findViewById(R.id.start)
        if (FloatingManager.instance.isShowing()) {
            start.text = "关闭悬浮窗"
        }
        start.setOnClickListener {
            if (start.text == "关闭悬浮窗") {
                FloatingManager.instance.dismiss()
                start.text = "打开悬浮窗"
            } else {
                if (!XXPermissions.isGranted(this@MainActivity, Permission.SYSTEM_ALERT_WINDOW)) {
                    Snackbar.make(
                        root,
                        "请先开启悬浮窗权限",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (!ClickService.isStart()) {
                    Snackbar.make(
                        root,
                        "请先启用无碍模式",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                FloatingManager.instance.checkCurrentActivityAndUpdateIfNeeded()
                start.text = "关闭悬浮窗"
                moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlaySwitch.isChecked =
            XXPermissions.isGranted(this@MainActivity, Permission.SYSTEM_ALERT_WINDOW)
        accessibilitySwitch.isChecked = ClickService.isStart()
        start.text = if (FloatingManager.instance.isShowing()) "关闭悬浮窗" else "打开悬浮窗"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Clicker.clear()
        }
    }
}