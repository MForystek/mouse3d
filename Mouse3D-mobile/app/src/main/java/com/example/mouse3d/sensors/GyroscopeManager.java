package com.example.mouse3d.sensors;

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
    private final float[] rotationMatrix;
    private final float[] currentOrientation;
    private List<Float> relativeDirections;
    private List<Float> currentDirections;
    private float[] pastOrientationsX;
    private float[] pastOrientationsY;


    public GyroscopeManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        relativeDirections = List.of(0f, 0f, 0f);
        currentOrientation = new float[3];
        rotationMatrix = new float[9];

        pastOrientationsX = new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f};
        pastOrientationsY = new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f};

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                SensorManager.getOrientation(rotationMatrix, currentOrientation);
                for (int i = 0; i < currentOrientation.length; i++) {
                    currentOrientation[i] = (float) Math.toDegrees(currentOrientation[i]);
                }
                currentDirections = List.of(currentOrientation[0], currentOrientation[1], currentOrientation[2]);

                float x = currentOrientation[0] - relativeDirections.get(0);
                x = ensureCorrectRangeOfX(x);
                float y = currentOrientation[1] - relativeDirections.get(1);
                y = ensureCorrectRangeOfY(y);
                float z = 0.0f;

                float intermX = smoothTheValue(x, getMeanArrayValue(pastOrientationsX));
                float intermY = smoothTheValue(y, getMeanArrayValue(pastOrientationsY));

                reloadTheArray(pastOrientationsX);
                pastOrientationsX[0] = x;
                reloadTheArray(pastOrientationsY);
                pastOrientationsY[0] = y;

                float finalX = getMeanArrayValue(pastOrientationsX);
                float finalY = getMeanArrayValue(pastOrientationsY);

                if (listener != null) {
                    listener.onRotation(finalX, finalY, z);
                    System.out.println(finalX + " " + finalY);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private float ensureCorrectRangeOfX(float x) {
        if (x > 180) x = -360 + x;
        if (x <= -180) x = -360 - x;
        return x;
    }

    private float ensureCorrectRangeOfY(float y) {
        if (y > 180) y = -360 + y;
        if (y <= -180) y = 360 + y;
        return y;
    }

    private float smoothTheValue(float value, float mean) {
        float maxPercent = 0.30f;
        value = (float) Math.min(value, mean + maxPercent*mean);
        value = (float) Math.max(value, mean - maxPercent*mean);
        return value;
    }

    private void reloadTheArray(float[] array) {
        for (int i = 0; i < array.length-1; i++) {
            array[i+1] = array[i];
        }
        array[0] = 0.0f;
    }

    private float getMeanArrayValue(float[] array) {
        float result = 0f;
        for (float v : array) {
            result += v;
        }
        return result / array.length;
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
