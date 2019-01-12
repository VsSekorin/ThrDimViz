package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Geometry.*;
import static com.vssekorin.thrdimviz.Param.*;

public final class SecondShader implements Shader {

    private final float[][] triangle = new float[3][3];

    @Override
    public float[] vertex(int a, int b) {
        float[] gl = upLength(Application.model.v.get(Application.model.f.get(a)[b][0] - 1), 4, 1);
        gl = mult(mult(mxViewport, mxProjection, mxModelView), gl);
        float[] glLow = lowLength(div(gl, gl[3]), 3);
        triangle[0][b] = glLow[0];
        triangle[1][b] = glLow[1];
        triangle[2][b] = glLow[2];
        return gl;
    }

    @Override
    public Object[] fragment(float[] a, int b) {
        final Object[] result = new Object[2];
        float[] vect = mult(triangle, a);
        float v = vect[2] / depth;
        float intensity = (v > 1.f ? 1.f : (v < 0.f ? 0.f : v));
        result[0] = false;
        int component = (int)(255 * intensity);
        result[1] = toColor(component, component, component);
        return result;
    }

    private int toColor(int r, int g, int b) {
        return 255 << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }
}
