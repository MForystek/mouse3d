package com.mouse3d.bluetoothserver;

import java.awt.*;
import java.util.Random;

public class MouseMover {
    public static final int SECONDS = 3000;
    public static final int MAX_Y = 1080;
    public static final int MAX_X = 1920;

    public static void main(String... args) throws Exception {
        Robot robot = new Robot();
        Random random = new Random();
        while (true) {
            var x = MAX_X + 2560;
            var y =  random.nextInt(MAX_Y);
            System.out.println(x + " : " + y);
            robot.mouseMove(x, y);
            Thread.sleep(SECONDS);
        }
    }
}