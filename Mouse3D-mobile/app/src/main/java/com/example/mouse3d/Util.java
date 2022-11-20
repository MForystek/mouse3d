package com.example.mouse3d;

import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class Util {
    public static void hideNavigationBar(Window window) {
        window.setDecorFitsSystemWindows(false);
        WindowInsetsController controller = window.getInsetsController();
        if (controller == null) return;
        controller.hide(WindowInsets.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
