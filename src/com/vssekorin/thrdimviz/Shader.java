package com.vssekorin.thrdimviz;

public interface Shader {

    float[] vertex(int a, int b);

    Object[] fragment(float[] a, int b);
}