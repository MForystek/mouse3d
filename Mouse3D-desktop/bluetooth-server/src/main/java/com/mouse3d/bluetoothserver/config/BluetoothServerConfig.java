package com.mouse3d.bluetoothserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.bluetooth.UUID;


@ConfigurationProperties(prefix = "bluetooth-server")
@Component
@Data
public class BluetoothServerConfig {
    private String name;
    private UUID uuid;

    public void setUuid(String string) {
        this.uuid = new UUID(string, false);
    }
}
