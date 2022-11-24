package com.mouse3d.bluetoothserver.service;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ScreenManager {
    private GraphicsDevice graphicsDevice;

    public int getWidth() {
        return graphicsDevice.getDisplayMode().getWidth();
    }

    public int getHeight() {
        return graphicsDevice.getDisplayMode().getHeight();
    }
}
