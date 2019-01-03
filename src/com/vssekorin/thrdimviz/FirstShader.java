package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Param.*;

public final class FirstShader implements Shader {

    private float[][] M;
    private float[][] Mit;
    private float[][] Mshadow;
    private float[][] uv = new float[2][3];
    private float[][] tri = new float[3][3];

    public FirstShader(final float[][] m, final float[][] mit, final float[][] mshadow) {
        this.M = m;
        this.Mit = mit;
        this.Mshadow = mshadow;
    }

    @Override
    public float[] vertex(final int a, final int b) {
        float[] uvs = Application.model.vt.get(Application.model.f.get(a)[b][1] - 1);
        uv[0][b] = uvs[0];
        uv[1][b] = uvs[1];
        float[] gl = Matrix.mulVect(
            Matrix.mul3(mxViewport, mxProjection, mxModelView),
            Vector.upLength(Application.model.v.get(Application.model.f.get(a)[b][0] - 1), 4, 1)
        );
        float[] divGl = Vector.div(gl, gl[3]);
        tri[0][b] = divGl[0];
        tri[1][b] = divGl[1];
        tri[2][b] = divGl[2];
        return gl;
    }

    @Override
    public Object[] fragment(final float[] a, final int b) {
        return new Object[]{true, 0x00ff00};
    }
}
