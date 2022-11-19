package com.mouse3d.bluetoothserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mouse3d.model.MouseAction;
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
    private Robot robot;

    private static final int EXIT_COMMAND = -1;
    private static final int BUFFER_SIZE = 512;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void run() {
        initializeRobot();
        processClientRequests();
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

            if (mouseEventDto.getAction().equals(MouseAction.LEFT_CLICK)) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } else if (mouseEventDto.getAction().equals(MouseAction.MIDDLE_CLICK)) {
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
            } else if (mouseEventDto.getAction().equals(MouseAction.RIGHT_CLICK)) {
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            } else if (mouseEventDto.getAction().equals(MouseAction.SCROLL)) {
                robot.mouseWheel(mouseEventDto.getY());
            } else if (mouseEventDto.getAction().equals(MouseAction.MOVE)) {
                Point location = MouseInfo.getPointerInfo().getLocation();
                robot.mouseMove(location.x + mouseEventDto.getX(), location.y + mouseEventDto.getY());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}