package com.wc.fatclip;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;


public class ClickService extends AccessibilityService {
    public static ClickService mService;

    //初始化
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        mService = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService = null;
    }

    /**
     * 辅助功能是否启动
     */
    public static boolean isStart() {
        return mService != null;
    }
}