package com.mouse3d.bluetoothserver.service;

import com.mouse3d.bluetoothserver.config.BluetoothServerConfig;
import lombok.extern.slf4j.Slf4j;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;

@Slf4j
public class ServerThread extends Thread {
    private final BluetoothServerConfig config;
    public static final String URL_PATTERN = "btspp://localhost:%s;name=%s";

    public ServerThread(BluetoothServerConfig config) {
        this.config = config;
    }

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
            var notifier = (StreamConnectionNotifier)Connector.open(url);

            log.info("Listening on: " + url);

            waitForClients(notifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateUrl() {
        return String.format(URL_PATTERN, config.getUuid(), config.getName());
    }

    private void waitForClients(StreamConnectionNotifier notifier) {
        while (true) {
            try {
                log.info("Waiting for clients to connect ...");
                var connection = notifier.acceptAndOpen();
                new Thread(new ClientThread(connection)).start();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
