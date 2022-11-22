package com.example.mouse3d;

import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Utils {
    public static void hideNavigationBar(Window window) {
        window.setDecorFitsSystemWindows(false);
        WindowInsetsController controller = window.getInsetsController();
        if (controller == null) return;
        controller.hide(WindowInsets.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public static void exitApplicationDisplayingToastWithMessage(AppCompatActivity context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        android.os.Process.killProcess(android.os.Process.myPid());
        context.finish();
    }
}
