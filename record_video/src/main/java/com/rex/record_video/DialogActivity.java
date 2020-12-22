package com.rex.record_video;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.rex.record_video.dialog.ShowloadDialog;

/**
 * author: rexkell
 * date: 2020/12/15
 * explain:
 */
public abstract class DialogActivity extends AppCompatActivity {
   ShowloadDialog baseDialog;
    protected Activity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
    }

    public void showLoading(String msg) {
        showLoading(getResources().getString(R.string.app_name), msg, true);
    }

    public void showLoading(String msg, boolean isCancel) {
        showLoading(getResources().getString(R.string.app_name), msg, isCancel);
    }

    public void showLoading(String title, String msg, boolean isCancel) {
        //展示加载对话框
        showLoadingDialog(title, msg, null, null, R.layout.dialog_video_loading, isCancel);
    }
    public void showLoadingDialog(String title, String msg, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener, int LayoutId, boolean isCancele) {
        if (baseDialog != null) {
            if (baseDialog.isShowing()) {
                baseDialog.dismiss();
            }
            baseDialog = null;
        }

        if (!mContext.isFinishing()) {
            baseDialog = new ShowloadDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(positiveListener)
                    .setNegativeButton(negativeListener)
                    .setContentView(LayoutId)
                    .setCanceledOutside(isCancele)
                    .createLoadingDialog();
            baseDialog.show();
        }
    }
    public void closeDialog() {
        if (mContext==null||mContext.isFinishing()){
            return;
        }
        if (baseDialog != null && baseDialog.isShowing()) {
            baseDialog.dismiss();
            baseDialog = null;
        }
    }
    @Override
    protected void onDestroy() {
        if (baseDialog != null && baseDialog.isShowing()) {
            baseDialog.dismiss();
            baseDialog = null;
        }
     super.onDestroy();
    }
}
