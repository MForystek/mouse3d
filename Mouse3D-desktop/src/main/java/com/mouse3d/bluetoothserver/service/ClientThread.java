package com.mouse3d.bluetoothserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mouse3d.model.MouseEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.microedition.io.StreamConnection;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ClientThread implements Runnable{

    private final StreamConnection mConnection;
    private final ScreenManager screenManager;
    private Robot robot;

    private static final int EXIT_COMMAND = -1;
    private static final int BUFFER_SIZE = 512;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int height;
    private int width;

    @Override
    public void run() {
        setResolution();
        initializeRobot();
        processClientRequests();
    }

    private void setResolution() {
        var graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        width = graphicsDevice.getDisplayMode().getWidth();
        height = graphicsDevice.getDisplayMode().getHeight();
    }

    private void initializeRobot() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void processClientRequests() {
        try (var inputStream = mConnection.openInputStream()) {
            log.info("InputStream was opened");

            var buffer = new byte[BUFFER_SIZE];
            int bytes;

            while (true) {
                bytes = inputStream.read(buffer);
                if (bytes == EXIT_COMMAND) {
                    log.info("Connection finished");
                    break;
                }
                var message = new String(buffer, 0, bytes);
                processRequest(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processRequest(String message) {
        try {
            var mouseEventDto = objectMapper.readValue(message, MouseEventDto.class);

            switch (mouseEventDto.getAction()) {
                case MOVE: {
                    System.out.println("RAW: " + mouseEventDto.getX() + ":" + mouseEventDto.getY());
                    System.out.println(scaleWidth(mouseEventDto.getX()) + ":" + scaleHeight(mouseEventDto.getY()));
                    robot.mouseMove(scaleWidth(mouseEventDto.getX()), scaleHeight(mouseEventDto.getY()));
                }
                    break;
                case SCROLL: {
                    robot.mouseWheel(mouseEventDto.getY());
                }
                    break;
                case LEFT_CLICK: {
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
                    break;
                case RIGHT_CLICK: {
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                }
                    break;
                case MIDDLE_CLICK: {
                    robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                    robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private int scaleWidth(int value) {
        return width * value / 100;
    }

    private int scaleHeight(int value) {
        return height * value / 100;
    }

}