package com.vssekorin.thrdimviz;

public final class Param {

    private Param() {
    }

    public static final int size = 1000;
    public static final float[] ligthDir = {1, 1, 0};
    public static final float[] eye = {1, 2, 3};
    public static final float[] center = {0, 0, 0};
    public static final float[] up = {0, 1, 0};
    public static final float[] bufferZ = new float[1000000];
    public static final float[] bufferShadow = new float[1000000];
    public static final float[][] mxModelView = new float[4][4];
    public static final float[][] mxViewport = new float[4][4];
    public static final float[][] mxProjection = new float[4][4];
    public static final float depth = 2000;
}
