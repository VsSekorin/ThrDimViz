package com.vssekorin.thrdimviz;

public interface Shader {

    float[] vertex(int a, int b);

    int fragment(float[] barycentric);
}