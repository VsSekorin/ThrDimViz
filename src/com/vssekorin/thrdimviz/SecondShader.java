package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Geometry.*;
import static com.vssekorin.thrdimviz.Param.*;

public final class SecondShader implements Shader {

    private final float[][] tr = new float[3][3];

    @Override
    public float[] vertex(int a, int b) {
        float[] gl = upLength(Application.model.v.get(Application.model.f.get(a)[b][0] - 1), 4, 1);
        gl = Geometry.mult(mult(mxViewport, mxProjection, mxModelView), gl);
        float[] glLow = lowLength(div(gl, gl[3]), 3);
        tr[0][b] = glLow[0];
        tr[1][b] = glLow[1];
        tr[2][b] = glLow[2];
        return gl;
    }

    @Override
    public Object[] fragment(float[] a, int b) {
        final Object[] result = new Object[2];
        float[] vect = Geometry.mult(tr, a);
        float v = vect[2] / depth;
        float intensity = (v > 1.f ? 1.f : (v < 0.f ? 0.f : v));
        result[0] = false;
        int component = (int)(255 * intensity);
        result[1] = toColor(component, component, component);
        return result;
    }

    private int toColor(int r, int g, int b) {
        return 256 * 256 * r + 256 * g + b;
    }
}
