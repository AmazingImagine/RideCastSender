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
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import java.util.*;

public class InputService extends AccessibilityService {
    final int LIFT_DOWN = 9;
    final int LIFT_MOVE=8;
    final int LIFT_UP=10;
    final int RIGHT_UP=18;
    final int WHEEL_BUTTON_DOWN=33;
    final int WHEEL_BUTTON_UP=34;
    final int WHEEL_DOWN=523331;
    final int WHEEL_UP=963;
    final int WHEEL_STEP=120;
    final long WHEEL_DURATION=50L;
    final long  LONG_TAP_DELAY=200L;
    private String logTag = "input service";
    private Boolean leftIsDown = false;
    private Path touchPath = new Path();
    private long lastTouchGestureStartTime = 0L;
    private int mouseX = 0;
    private int mouseY = 0;
    private Timer timer = new Timer();
    private TimerTask recentActionTask = null;
    private LinkedList <GestureDescription> wheelActionsQueue = new LinkedList <GestureDescription> ();
    private Boolean isWheelActionsPolling = false;
    private Boolean isWaitingLongPress = false;

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
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(_x, _y);
        p.lineTo(_x+10, _y+10);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 10L, 200L));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
        Log.e(logTag, "isDispatched:"+isDispatched);
        //Toast.makeText(FingerprintService.this, "Was it dispatched? " + isDispatched, Toast.LENGTH_SHORT).show();

//        touchPath = new Path();
//        touchPath.moveTo(500.0f, 100.0f);
//        touchPath.lineTo(300.0f, 110.0f);
//        lastTouchGestureStartTime = System.currentTimeMillis();
//        try {
//            touchPath.lineTo(10.0f, 100.0f);
//            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
//                    touchPath,
//                    0,
//                    1
//            );
//            GestureDescription.Builder builder = new GestureDescription.Builder();
//            builder.addStroke(stroke);
//            Log.d(logTag, "end gesture x:$x y:$y time:$duration");
//            dispatchGesture(builder.build(), null, null);
//        } catch (Exception e){
//            Log.e(logTag, "endGesture error:$e");
//        }

//        float x = max(0.0f, _x);
//        float y = max(0.0f, _y);
//
//        if (mask == 0 || mask == LIFT_MOVE) {
//            int oldX = mouseX;
//            int oldY = mouseY;
//            mouseX = (int) (x * screenScale);
//            mouseY = (int) (y * screenScale);
//            if (isWaitingLongPress) {
//                int delta = abs(oldX - mouseX) + abs(oldY - mouseY);
//                Log.d(logTag, "delta:$delta");
//                if (delta > 8) {
//                    isWaitingLongPress = false;
//                }
//            }
//        }
//
//        // left button down ,was up
//        if (mask == LIFT_DOWN) {
//            isWaitingLongPress = true;
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    if (isWaitingLongPress) {
//                        isWaitingLongPress = false;
//                        leftIsDown = false;
//                        endGesture(mouseX, mouseY);
//                    }
//                }
//            },LONG_TAP_DELAY * 4);
//
//            leftIsDown = true;
//            startGesture(mouseX, mouseY);
//            return;
//        }
//
//        // left down ,was down
//        if (leftIsDown) {
//            continueGesture(mouseX, mouseY);
//        }
//
//        // left up ,was down
//        if (mask == LIFT_UP) {
//            if (leftIsDown) {
//                leftIsDown = false;
//                isWaitingLongPress = false;
//                endGesture(mouseX, mouseY);
//                return;
//            }
//        }
//
//        if (mask == RIGHT_UP) {
//            performGlobalAction(GLOBAL_ACTION_BACK);
//            return;
//        }
////////////////////////////////////////////////////////////////////////////////
        // long WHEEL_BUTTON_DOWN -> GLOBAL_ACTION_RECENTS
//        if (mask == WHEEL_BUTTON_DOWN) {
//            timer.purge();
//            recentActionTask = new TimerTask() {
//                @Override
//                public void run() {
//                    performGlobalAction(GLOBAL_ACTION_RECENTS);
//                    recentActionTask = null;
//                }
//            };
//            timer.schedule(recentActionTask, LONG_TAP_DELAY);
//        }
//
//        // wheel button up
//        if (mask == WHEEL_BUTTON_UP) {
//            if (recentActionTask != null) {
//                recentActionTask.cancel();
//                performGlobalAction(GLOBAL_ACTION_HOME);
//            }
//            return;
//        }
//
//        if (mask == WHEEL_DOWN) {
//            if (mouseY < WHEEL_STEP) {
//                return;
//            }
//            Path path = new Path();
//            path.moveTo(mouseX, mouseY);
//            path.lineTo(mouseX, mouseY - WHEEL_STEP);
//            GestureDescription.StrokeDescription stroke = new  GestureDescription.StrokeDescription(
//                    path,
//                    0,
//                    WHEEL_DURATION
//            );
//            GestureDescription.Builder builder = new GestureDescription.Builder();
//            builder.addStroke(stroke);
//            wheelActionsQueue.offer(builder.build());
//            consumeWheelActions();
//        }
//
//        if (mask == WHEEL_UP) {
//            if (mouseY < WHEEL_STEP) {
//                return;
//            }
//            Path path = new Path();
//            path.moveTo(mouseX, mouseY);
//            path.lineTo(mouseX, (mouseY + WHEEL_STEP));
//            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
//                    path,
//                    0,
//                    WHEEL_DURATION
//            );
//            GestureDescription.Builder builder = new GestureDescription.Builder();
//            builder.addStroke(stroke);
//            wheelActionsQueue.offer(builder.build());
//            consumeWheelActions();
//        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void consumeWheelActions() {
        if (isWheelActionsPolling) {
            return;
        } else {
            isWheelActionsPolling = true;
        }

        GestureDescription gd = wheelActionsQueue.poll();
        if(gd!=null){
            dispatchGesture(gd, null, null);
            timer.purge();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isWheelActionsPolling = false;
                    consumeWheelActions();
                }
            },WHEEL_DURATION + 10);
        }
        else {
            isWheelActionsPolling = false;
            return;
        }
    }

    private void startGesture(int x, int y) {
        touchPath = new Path();
        touchPath.moveTo(x, y);
        lastTouchGestureStartTime = System.currentTimeMillis();
    }

    private void continueGesture(int x, int y) {
        touchPath.lineTo(x, y);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private void endGesture(int x, int y) {
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

    @Override
    public void  onServiceConnected() {
        super.onServiceConnected();
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
