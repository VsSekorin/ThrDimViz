package com.vssekorin.thrdimviz;

public final class Vector {
    private Vector() {
    }

    public static float[] sub(float[] first, float[] second) {
        final float[] result = new float[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] - second[i];
        }
        return result;
    }

    public static float[] div(float[] vector, float number) {
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= number;
        }
        return vector;
    }

    public static float[] upLength(float[] vector, int newLength, float value) {
        final float[] result = new float[newLength];
        for (int i = 0; i < newLength; i++) {
            result[i] = i < vector.length ? vector[i] : value;
        }
        return result;
    }

    public static float[] lowLength(float[] vector, int newLength) {
        final float[] result = new float[newLength];
        for (int i = 0; i < newLength; i++) {
            result[i] = vector[i];
        }
        return result;
    }
}
