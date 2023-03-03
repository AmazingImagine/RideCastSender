package com.iamverycute.AccessibilityService;

/**
 * Handle remote input and dispatch android gesture
 * <p>
 * Inspired by [droidVNC-NG] https://github.com/bk138/droidVNC-NG
 */
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.iamverycute.rtsp_android.MainActivity;

import java.util.*;

public class InputService extends AccessibilityService {
    final int ACTION_DOWN = 0;
    final int ACTION_MOVE = 1;
    final int ACTION_UP = 2;
    final int ACTION_HOME = 3;
    final int ACTION_BACK = 4;
    final int ACTION_RECENTS = 5;

    private String logTag = "input service";
    private Path touchPath = new Path();
    private int  x_screen_size = 0;
    private int  y_screen_size = 0;
    private long lastTouchGestureStartTime;
    private boolean leftIsDown = false;


    private static InputService  instance_;
    public static InputService getInstance(){
        return instance_;
    }

    private float screenScale = 1.0F;
    public void SetScreenScale(float scale){
        screenScale = scale;
    }
    @RequiresApi(Build.VERSION_CODES.N)
    public void onMouseInput(int mask, float _x, float _y) {
        float xPos = _x * x_screen_size;
        float yPos = _y * y_screen_size;
        switch (mask){
            case ACTION_DOWN:
            {
                leftIsDown = true;
                startGesture(xPos, yPos);
            }
            break;
            case ACTION_MOVE:
            {
                if(leftIsDown)
                    continueGesture(xPos, yPos);
            }
            break;
            case ACTION_UP:
            {
                if(leftIsDown){
                    leftIsDown = false;
                    endGesture(xPos, yPos);
                }
            }
            break;
            case ACTION_HOME:
            {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
            break;
            case ACTION_BACK:
            {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
            break;
            case ACTION_RECENTS:
            {
                performGlobalAction(GLOBAL_ACTION_RECENTS);
            }
            break;
        }
    }


    private void startGesture(float x, float y) {
        touchPath = new Path();
        touchPath.moveTo(x, y);
        lastTouchGestureStartTime = System.currentTimeMillis();
    }

    private void continueGesture(float x, float y) {
        touchPath.lineTo(x, y);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void endGesture(float x, float y) {
        try {
            touchPath.lineTo(x, y);
            var duration = System.currentTimeMillis() - lastTouchGestureStartTime;
            if (duration <= 0) {
                duration = 1;
            }
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
                    touchPath,
                    0,
                    duration
            );
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(stroke);
            Log.d(logTag, "end gesture x:$x y:$y time:$duration");
            dispatchGesture(builder.build(), null, null);
        } catch (Exception e){
            Log.e(logTag, "endGesture error:$e");
        }
    }

    private void getScreenSize(){
        WindowManager windowManager = MainActivity.getInstance().getWindow().getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        //屏幕实际宽度（像素个数）
        x_screen_size = metrics.widthPixels;
        //屏幕实际高度（像素个数）
        y_screen_size = metrics.heightPixels;
    }


    @Override
    public void  onServiceConnected() {
        super.onServiceConnected();
        getScreenSize();
        instance_ = this;
        Log.d(logTag, "onServiceConnected!");
    }

    @Override
    public void onDestroy(){
        instance_ = null;
        super.onDestroy();
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }
}
