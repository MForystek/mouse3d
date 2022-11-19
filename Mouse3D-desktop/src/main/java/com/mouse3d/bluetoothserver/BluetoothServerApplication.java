package com.mouse3d.bluetoothserver;

import com.mouse3d.bluetoothserver.config.BluetoothServerConfig;
import com.mouse3d.bluetoothserver.service.ServerThread;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BluetoothServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BluetoothServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(BluetoothServerConfig config) {
        return args -> new ServerThread(config).start();
    }
}
