package com.mouse3d.bluetoothserver.gui;

import com.mouse3d.bluetoothserver.config.BluetoothServerConfig;
import com.mouse3d.bluetoothserver.exception.BluetoothException;
import com.mouse3d.bluetoothserver.exception.BluetoothTurnedOfException;
import com.mouse3d.bluetoothserver.service.ScreenManager;
import com.mouse3d.bluetoothserver.service.ServerThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {
    private JButton startServerButton;
    private JPanel mainPanel;
    private JButton stopServerButton;
    private JList monitorList;
    private ServerThread serverThread;
    private ScreenManager screenManager;

    public MainWindow() {
        super("Mouse3D");
        setContentPane(this.mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(500, 500));
        setVisible(true);
        initMonitorList();
        screenManager = new ScreenManager();
        screenManager.setGraphicsDevice(getDefaultGraphicsDevice());
        addListeners();
    }

    public static void main(String [] args) {
        new MainWindow();
    }

    private void addListeners() {
        startServerButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                restartServer();
            }
        });
        stopServerButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                stopServer();
            }
        });
        monitorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                var list = (JList) mouseEvent.getSource();
                int index = list.locationToIndex(mouseEvent.getPoint());
                screenManager.setGraphicsDevice(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[index]);
            }
        });
    }

    private void restartServer() {
        stopServer();

        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread th, Throwable ex) {
                if (ex instanceof BluetoothTurnedOfException) {
                    JOptionPane.showMessageDialog(null, "Turn off bluetooth first or make it discoverable");
                } else if (ex instanceof BluetoothException) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        };

        serverThread = new ServerThread(buildConfig(), screenManager);
        serverThread.setUncaughtExceptionHandler(handler);
        serverThread.start();
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private void initMonitorList() {
        var defaultListModel = new DefaultListModel<GraphicsDevice>();
        for (var graphicsDevice: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            defaultListModel.addElement(graphicsDevice);
        }
        monitorList.setModel(defaultListModel);
    }

    private GraphicsDevice getDefaultGraphicsDevice() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    private BluetoothServerConfig buildConfig() {
        var bluetoothServerConfig = new BluetoothServerConfig();
        bluetoothServerConfig.setName("SimpleBluetoothServer");
        bluetoothServerConfig.setUuid("0000110100001000800000805F9B34FB");
        return bluetoothServerConfig;
    }
}
