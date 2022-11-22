package com.example.mouse3d.sensors;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class SwipeListener implements View.OnTouchListener {
    GestureDetector gestureDetector;

    public SwipeListener(View view) {
        int threshold = 100;
        int velocity_threshold = 100;

        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float xDiff = e2.getX() - e1.getX();
                float yDiff = e2.getY() - e1.getY();
                try {
                    if (Math.abs(xDiff) < Math.abs(yDiff)) {
                        if (Math.abs(yDiff) > threshold && Math.abs(velocityY) > velocity_threshold) {
                            if (yDiff > 0) {
                                UserAction.SWIPE.value = -1;
                            } else {
                                UserAction.SWIPE.value = 1;
                            }
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        gestureDetector = new GestureDetector(listener);
        view.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //TODO without it middle click is not possible but with it swiping causes million middle clicks per second
        //v.callOnClick();
        return gestureDetector.onTouchEvent(event);
    }
}
