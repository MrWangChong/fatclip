package com.wc.fatclip;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public abstract class CountTimer {
    private final long mCountdownInterval;
    private long mStartTimeInFuture;
    private boolean mCancelled = false;

    public CountTimer(long countDownInterval) {
        mCountdownInterval = countDownInterval;
    }

    public synchronized final void cancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG);
    }

    public synchronized final CountTimer start() {
        mCancelled = false;
        mStartTimeInFuture = SystemClock.elapsedRealtime();
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    public abstract void onTick(long millisUntilFinished);

    private static final int MSG = 1;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            synchronized (CountTimer.this) {
                if (mCancelled) {
                    return true;
                }

                final long millisLeft = SystemClock.elapsedRealtime() - mStartTimeInFuture;

                if (millisLeft >= mCountdownInterval) {
                    onTick(millisLeft);
                }

                long lastTickStart = SystemClock.elapsedRealtime();

                // take into account user's onTick taking time to execute
                long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                long delay;

                delay = mCountdownInterval - lastTickDuration;

                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) delay += mCountdownInterval;

                mStartTimeInFuture = lastTickStart;

                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), delay);
            }
            return true;
        }
    });
}
