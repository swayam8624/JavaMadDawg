package com.singal.maddog.math;

/**
 * Common mathematical helpers.
 */
public class MathUtils {
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float lerp(float start, float end, float percent) {
        return start + percent * (end - start);
    }

    public static double lerp(double start, double end, double percent) {
        return start + percent * (end - start);
    }
}
