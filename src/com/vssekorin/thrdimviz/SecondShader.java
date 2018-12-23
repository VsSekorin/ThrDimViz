package com.vssekorin.thrdimviz;

public final class SecondShader implements Shader {

    private final float[][] tr = new float[4][4];

    @Override
    public float[] vertex(int a, int b) {
        float[] gl = Vector.upLength(Application.model.v.get(Application.model.f.get(a)[b][0]), 4, 1);

        return new float[0];
    }

    @Override
    public Object[] fragment(float[] a, int b) {
        return new Object[0];
    }
}
