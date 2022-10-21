package com.example.mouse3d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.Size;

import java.util.List;

public class GyroscopeManager {

    public interface Listener {
        void onRotation(float rx, float ry, float rz);
    }

    private Listener listener;
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private final SensorEventListener sensorEventListener;
    private List<Float> relativeDirections;
    private List<Float> currentDirections;

    @SuppressWarnings("deprecation")
    GyroscopeManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        relativeDirections = List.of(0f, 0f, 0f);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (listener != null) {
                    listener.onRotation(
                            sensorEvent.values[0] - relativeDirections.get(0),
                            sensorEvent.values[1] - relativeDirections.get(1),
                            sensorEvent.values[2] - relativeDirections.get(2));
                    currentDirections = List.of(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setRelativeDirections(@Size(min = 3, max = 3) List<Float> list) {
        this.relativeDirections = list.subList(0, 3);
    }

    public List<Float> getCurrentDirections() {
        return currentDirections;
    }

    public void register() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(sensorEventListener);
    }
}
