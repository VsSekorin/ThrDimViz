package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Param.*;

public final class SecondShader implements Shader {

    private final float[][] tr = new float[3][3];

    @Override
    public float[] vertex(int a, int b) {
        float[] gl = Vector.upLength(Application.model.v.get(Application.model.f.get(a)[b][0] - 1), 4, 1);
        gl = Matrix.mulVect(Matrix.mul3(mxViewport, mxProjection, mxModelView), gl);
        float[] glLow = Vector.lowLength(Vector.div(gl, gl[3]), 3);
        tr[0][b] = glLow[0];
        tr[1][b] = glLow[1];
        tr[2][b] = glLow[2];
        return gl;
    }

    @Override
    public Object[] fragment(float[] a, int b) {
        final Object[] result = new Object[2];
        result[0] = false;
        result[1] = a[0] * tr[2][0] + a[1] * tr[2][1] + a[2] * tr[2][2];
        return result;
    }
}
