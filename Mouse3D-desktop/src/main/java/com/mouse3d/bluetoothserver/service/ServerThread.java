package com.mouse3d.bluetoothserver.service;

import com.mouse3d.bluetoothserver.config.BluetoothServerConfig;
import com.mouse3d.bluetoothserver.exception.BluetoothException;
import com.mouse3d.bluetoothserver.exception.BluetoothTurnedOfException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ServerThread extends Thread {
    public static final String URL_PATTERN = "btspp://localhost:%s;name=%s";
    private final BluetoothServerConfig config;
    private final ScreenManager screenManager;
    private volatile boolean exit = false;

    @Override
    public void run() {
        initializeServer();
    }

    private void initializeServer() {
        System.setProperty("java.awt.headless", "false");
        try {
            var localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
            var url = generateUrl();
            var notifier = (StreamConnectionNotifier) Connector.open(url);
            log.info("Listening on: " + url);
            waitForClients(notifier);
        } catch (BluetoothStateException e) {
            throw new BluetoothTurnedOfException("Bluetooth is turned off");
        } catch (IOException e) {
            throw new BluetoothException("Cannot open connection");
        }
    }

    private String generateUrl() {
        return String.format(URL_PATTERN, config.getUuid(), config.getName());
    }

    private void waitForClients(StreamConnectionNotifier notifier) {
        while (!exit) {
            try {
                log.info("Waiting for clients to connect ...");
                var connection = notifier.acceptAndOpen();
                new Thread(new ClientThread(connection, screenManager)).start();
                Thread.sleep(500);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                exit = true;
            }
        }
        log.info("Terminated");
    }
    public void terminate() {
        exit = true;
    }
}
