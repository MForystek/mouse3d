package com.mouse3d.bluetoothserver.dto;

import com.mouse3d.bluetoothserver.model.MouseAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouseEventDto {
    private MouseAction action;
    private int x;
    private int y;
}
