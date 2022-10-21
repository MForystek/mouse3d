package com.example.mouse3d;

public enum MessageConstants {
    MESSAGE_READ(0),
    MESSAGE_WRITE(1),
    MESSAGE_TOAST(2);

    private final int value;

    MessageConstants(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
