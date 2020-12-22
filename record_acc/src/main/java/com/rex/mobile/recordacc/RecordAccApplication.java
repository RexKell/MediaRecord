package com.rex.mobile.recordacc;

import android.app.Application;

import com.zlw.main.recorderlib.RecordManager;

/**
 * author: rexkell
 * date: 2020/12/9
 * explain:
 */
public abstract class RecordAccApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RecordManager.getInstance().init(this, true);
    }
}
