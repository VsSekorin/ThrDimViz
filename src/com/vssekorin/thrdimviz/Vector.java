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
        float[] result = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] / number;
        }
        return result;
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

    public static float[] normalize(float[] vector) {
        final float norma = norma(vector);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] * 1 / norma;
        }
        return vector;
    }

    public static float norma(float[] vector) {
        return vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2];
    }
}
