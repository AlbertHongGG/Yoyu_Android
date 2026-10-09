package com.jasonhong.yoyu.presentation.showcase.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

/**
 * Controller managing device gyroscope and rotation sensor:
 * - Automatically calibrates to user's natural holding baseline orientation on start.
 * - Computes smooth relative tilt angles (pitch and roll) with calibrated sensitivity.
 * - Applies a tuned low-pass filter (alpha = 0.60) to eliminate jitter while keeping immediate tactile response.
 * - Leaky drift compensator slowly adapts to user posture changes.
 * - Supports seamless pause/resume when user interacts via touch gestures.
 */
public class GyroscopeTiltController implements SensorEventListener {

    private static final float FILTER_ALPHA = 0.60f;
    // Maximum natural wrist tilt angle (22 degrees) mapped to normalized 1.0 range
    private static final float MAX_TILT_RAD = (float) Math.toRadians(22.0);
    // Slow baseline adaptation rate (adapts over ~8-10 seconds)
    private static final float BASELINE_ADAPT_ALPHA = 0.002f;

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private TiltSensorListener listener;

    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean isCalibrated = false;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float basePitch = 0f;
    private float baseRoll = 0f;

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
            isCalibrated = false;
            smoothedPitch = 0f;
            smoothedRoll = 0f;
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isRunning = false;
            isCalibrated = false;
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void recalibrate() {
        isCalibrated = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isPaused || listener == null) return;

        float rawPitch;
        float rawRoll;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            rawPitch = orientationAngles[1];
            rawRoll = orientationAngles[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Normalize accelerometer/gravity vector to radians approximation
            rawRoll = (float) Math.asin(clamp(event.values[0] / 9.8f, -1.0f, 1.0f));
            rawPitch = (float) Math.asin(clamp(event.values[1] / 9.8f, -1.0f, 1.0f));
        } else {
            return;
        }

        // 1. Initial Reference Baseline Calibration:
        // Lock the user's initial natural holding posture as neutral (0, 0)
        if (!isCalibrated) {
            basePitch = rawPitch;
            baseRoll = rawRoll;
            isCalibrated = true;
            smoothedPitch = 0f;
            smoothedRoll = 0f;
            listener.onTilt(0f, 0f);
            return;
        }

        // 2. Slow baseline drift adaptation (prevents holding fatigue):
        basePitch = (1.0f - BASELINE_ADAPT_ALPHA) * basePitch + BASELINE_ADAPT_ALPHA * rawPitch;
        baseRoll = (1.0f - BASELINE_ADAPT_ALPHA) * baseRoll + BASELINE_ADAPT_ALPHA * rawRoll;

        // 3. Compute relative angle difference:
        // Tilting top of phone forward away from user increases rawPitch -> positive deltaPitch
        float deltaPitch = rawPitch - basePitch;
        // Tilting right edge of phone down decreases rawRoll -> negate so tilting right gives positive deltaRoll
        float deltaRoll = -(rawRoll - baseRoll);

        // 4. Normalize to [-1.0, 1.0]
        float normPitch = clamp(deltaPitch / MAX_TILT_RAD, -1.0f, 1.0f);
        float normRoll = clamp(deltaRoll / MAX_TILT_RAD, -1.0f, 1.0f);

        // 5. Responsive low-pass filter
        smoothedPitch = FILTER_ALPHA * smoothedPitch + (1.0f - FILTER_ALPHA) * normPitch;
        smoothedRoll = FILTER_ALPHA * smoothedRoll + (1.0f - FILTER_ALPHA) * normRoll;

        listener.onTilt(smoothedPitch, smoothedRoll);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}
