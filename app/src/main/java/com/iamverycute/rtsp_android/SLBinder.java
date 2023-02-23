package com.iamverycute.rtsp_android;

import android.os.Binder;

public class SLBinder extends Binder {
    private final SLService context;

    public SLBinder(SLService context) {
        this.context = context;
    }
    public SLService getContext(){
        return this.context;
    }
}
