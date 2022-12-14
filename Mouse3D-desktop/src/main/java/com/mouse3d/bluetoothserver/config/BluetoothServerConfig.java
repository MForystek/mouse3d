package com.mouse3d.bluetoothserver.config;

import lombok.Data;

import javax.bluetooth.UUID;


@Data
public class BluetoothServerConfig {
    private String name;
    private UUID uuid;

    public void setUuid(String string) {
        this.uuid = new UUID(string, false);
    }
}
