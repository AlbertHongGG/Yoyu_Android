package com.jasonhong.yoyu.presentation.showcase.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

/**
 * Controller managing device gyroscope and rotation sensor:
 * - Computes smooth device tilt angles (pitch and roll).
 * - Applies a 1st-order low-pass filter to eliminate sensor jitter and hand tremors.
 * - Supports seamless pause/resume when user interacts via touch gestures.
 */
public class GyroscopeTiltController implements SensorEventListener {

    private static final float FILTER_ALPHA = 0.82f;
    private static final float MAX_ANGLE_RAD = (float) Math.toRadians(35.0); // 35 degrees normalized to 1.0

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private TiltSensorListener listener;

    private boolean isRunning = false;
    private boolean isPaused = false;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float smoothedPitch = 0f;
    private float smoothedRoll = 0f;

    public GyroscopeTiltController(@NonNull Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = null;
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (sensor == null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            }
            if (sensor == null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }
        rotationSensor = sensor;
    }

    public void setListener(TiltSensorListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (!isRunning && sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isRunning = false;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isPaused || listener == null) return;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            // pitch (around X-axis) and roll (around Y-axis)
            float rawPitch = orientationAngles[1];
            float rawRoll = orientationAngles[2];

            // Normalize to [-1.0, 1.0]
            float normPitch = clamp(rawPitch / MAX_ANGLE_RAD, -1.0f, 1.0f);
            float normRoll = clamp(rawRoll / MAX_ANGLE_RAD, -1.0f, 1.0f);

            // Low-pass filter for smooth motion
            smoothedPitch = FILTER_ALPHA * smoothedPitch + (1.0f - FILTER_ALPHA) * normPitch;
            smoothedRoll = FILTER_ALPHA * smoothedRoll + (1.0f - FILTER_ALPHA) * normRoll;

            listener.onTilt(smoothedPitch, smoothedRoll);
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float normRoll = clamp(event.values[0] / 9.8f, -1.0f, 1.0f);
            float normPitch = clamp(event.values[1] / 9.8f, -1.0f, 1.0f);

            smoothedPitch = FILTER_ALPHA * smoothedPitch + (1.0f - FILTER_ALPHA) * normPitch;
            smoothedRoll = FILTER_ALPHA * smoothedRoll + (1.0f - FILTER_ALPHA) * normRoll;

            listener.onTilt(smoothedPitch, smoothedRoll);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
