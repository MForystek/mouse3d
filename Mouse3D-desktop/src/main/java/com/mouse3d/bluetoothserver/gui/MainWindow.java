package com.mouse3d.bluetoothserver.gui;

import com.mouse3d.bluetoothserver.config.BluetoothServerConfig;
import com.mouse3d.bluetoothserver.exception.BluetoothException;
import com.mouse3d.bluetoothserver.exception.BluetoothTurnedOfException;
import com.mouse3d.bluetoothserver.service.ServerThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {
    private JButton startServerButton;
    private JPanel mainPanel;
    private JButton stopServerButton;
    private ServerThread serverThread;

    public static void main(String [] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new MainWindow();
    }

    public MainWindow() {
        super("Mouse3D");
        setContentPane(this.mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int windowWidth = 250;
        int windowHeight = (windowWidth / 16) * 10;
        setSize(new Dimension(windowWidth, windowHeight));
        setResizable(false);
        setVisible(true);
        addListeners();
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

        serverThread = new ServerThread(buildConfig());
        serverThread.setUncaughtExceptionHandler(handler);
        serverThread.start();
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private BluetoothServerConfig buildConfig() {
        var bluetoothServerConfig = new BluetoothServerConfig();
        bluetoothServerConfig.setName("SimpleBluetoothServer");
        bluetoothServerConfig.setUuid("0000110100001000800000805F9B34FB");
        return bluetoothServerConfig;
    }
}
