package com.jasonhong.yoyu.presentation.showcase.sensor;

/**
 * Callback interface for tilt sensor updates.
 */
public interface TiltSensorListener {
    /**
     * Called when device tilt changes.
     *
     * @param pitch Normalized pitch (X-axis tilt, -1.0f to +1.0f)
     * @param roll  Normalized roll (Y-axis tilt, -1.0f to +1.0f)
     */
    void onTilt(float pitch, float roll);
}
