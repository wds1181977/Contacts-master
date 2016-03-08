package com.android.contacts_master.util;



import android.app.FragmentManager;
import android.os.Handler;
import android.os.Message;

import com.android.contacts_master.widget.SimpleProgressDialogFragment;

public class ProgressHandler extends Handler {
    private static final String TAG = "ProgressHandler";

    private static final int PROGRESS_DIALOG_SHOW = 0;
    private static final int PROGRESS_DIALOG_DISMISS = 1;

    public void showDialog(FragmentManager fm) {
        SimpleProgressDialogFragment.show(fm);
    }

    public void showDialogDelayed(FragmentManager fm, int millis) {
        sendMessageDelayed(obtainMessage(PROGRESS_DIALOG_SHOW, fm), millis);
    }

    public void dismissDialog(FragmentManager fm) {
        removeMessages(PROGRESS_DIALOG_SHOW);
        sendMessage(obtainMessage(PROGRESS_DIALOG_DISMISS, fm));
    }

    @Override
    public void handleMessage(Message msg) {
        LogUtils.d(TAG, "[handleMessage]msg.what = " + msg.what + ", msg.obj = " + msg.obj);
        switch (msg.what) {
        case PROGRESS_DIALOG_SHOW:
            SimpleProgressDialogFragment.show((FragmentManager) msg.obj);
            break;

        case PROGRESS_DIALOG_DISMISS:
            SimpleProgressDialogFragment.dismiss((FragmentManager) msg.obj);
            break;
        default:
            LogUtils.w(TAG, "[handleMessage]unexpected message: " + msg.what);
            break;
        }
    }
}
