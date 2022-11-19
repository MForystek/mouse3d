package com.example.mouse3d.sensors;

public enum UserAction {
    LEFT_CLICK(0),
    RIGHT_CLICK(0),
    MIDDLE_CLICK(0),
    SWIPE(0);

    public int value;

    UserAction(int value) {
        this.value = value;
    }
}
