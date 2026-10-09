package com.jasonhong.yoyu.domain.repository;

/**
 * Callback contract for monitoring granular execution progress of batch operations.
 */
@FunctionalInterface
public interface BatchProgressListener {
    /**
     * Invoked when a slice/chunk of cards completes validation.
     *
     * @param processed Number of cards evaluated so far.
     * @param total     Total number of cards requested.
     * @param message   Human-readable status description.
     */
    void onProgress(int processed, int total, String message);
}
