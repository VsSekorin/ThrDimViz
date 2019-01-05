package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Geometry.*;
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
        float[] gl = Geometry.mult(
            mult(mxViewport, mxProjection, mxModelView),
            upLength(Application.model.v.get(Application.model.f.get(a)[b][0] - 1), 4, 1)
        );
        float[] divGl = div(gl, gl[3]);
        tri[0][b] = divGl[0];
        tri[1][b] = divGl[1];
        tri[2][b] = divGl[2];
        return gl;
    }

    @Override
    public Object[] fragment(final float[] a, final int b) {
        Object[] result = new Object[2];
        result[0] = false;
        float[] sbp = Geometry.mult(Mshadow, upLength(Geometry.mult(tri, a), 4, 1));
        sbp = div(sbp, sbp[3]);
        int idx = (int)(sbp[0] + 0.5f) + (int) (sbp[1] + 0.5f) * size;
        float shadow = 0.3f + 0.7f * (bufferShadow[idx] < sbp[2] + 123.45 ? 1 : 0);
        float[] vUV = Geometry.mult(uv, a);
        int rgb = Application.texture.getRGB((int) (vUV[0] * 2844), (int) (vUV[1] * 2844));
        int[] components = getComponents(rgb);
        result[1] = toColor((int)(components[0] * shadow), (int)(components[1] * shadow), (int)(components[2] * shadow));
        return result;
    }

    private int[] getComponents(int color) {
        int[] components = new int[3];
        components[2] = color % 256;
        components[1] = (color / 256) % 256;
        components[0] = (color /256 / 256) % 256;
        return components;
    }

    private int toColor(int r, int g, int b) {
        return 256 * 256 * r + 256 * g + b;
    }
}
