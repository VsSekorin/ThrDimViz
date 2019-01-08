package com.vssekorin.thrdimviz;

import java.util.Arrays;

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
    public Object[] fragment(float[] a, int b) {
        Object[] result = new Object[2];
        float[] vUV = mult(uv, a);
        float[] n = normalize(lowLength(mult(Mit, upLength(getNormal(vUV), 4, 1)), 3));
        float[] l = normalize(lowLength(mult(M, upLength(ligthDir, 4, 1)), 3));
        float[] r = normalize(sub(mult(n, dot(n, l) * 2.0f), l));
        float rz = Math.max(r[2], 0.0f);
        float d = Math.max(dot(n, l), 0.0f);
        int[] color = getComponents(Application.texture.getRGB((int) (vUV[0] * 2844), (int) (vUV[1] * 2844)));
        for (int i = 0; i < 3; i++) {
            color[i] = Math.min(255, (int)(color[i] * (d + 0.6f * rz)));
        }
        result[0] = false;
        result[1] = toColor(color[0], color[1], color[2]);
        return result;
    }

    private float[] getNormal(float[] vuv) {
        float[] result = new float[3];
        int rgb = Application.normals.getRGB((int) (vuv[0] * 2844), (int) (vuv[1] * 2844));
        int[] components = getComponents(rgb);
        for (int i = 0; i < 3; i++) {
            result[i] = components[i] / 255.0f * 2.0f - 1.0f;
        }
        return result;
    }

    private int[] getComponents(int rgb) {
        int value = -16777216 | rgb;
        return new int[]{ value >> 16 & 255, value >> 8 & 255, value & 255 };
    }

    private int toColor(int r, int g, int b) {
        return 255 << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }
}
