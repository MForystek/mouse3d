package com.example.mouse3d.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.Size;

import java.util.List;

public class WorkPls {

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
    public WorkPls(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        relativeDirections = List.of(0f, 0f, 0f);
        sensorEventListener = new SensorEventListener() {
            float[] mGravity;
            float[] mGeomagnetic;
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = event.values;
                if (mGravity != null && mGeomagnetic != null) {
                    float[] r = new float[9];
                    float[] i = new float[9];
                    boolean success = SensorManager.getRotationMatrix(r, i, mGravity, mGeomagnetic);
                    if (success) {
                        float[] orientation = new float[3];
                        SensorManager.getOrientation(r, orientation);
                        if (listener != null) {
                            listener.onRotation(orientation[0], orientation[1], orientation[2]);
                        }
                    }
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
