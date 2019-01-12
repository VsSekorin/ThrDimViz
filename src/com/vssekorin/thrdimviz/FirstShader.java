package com.vssekorin.thrdimviz;

import static com.vssekorin.thrdimviz.Application.*;
import static com.vssekorin.thrdimviz.Geometry.*;
import static com.vssekorin.thrdimviz.Param.*;

public final class FirstShader implements Shader {

    private float[][] M;
    private float[][] Mit;
    private float[][] Mshadow;
    private float[][] uv = new float[2][3];
    private float[][] triangle = new float[3][3];

    public FirstShader(final float[][] m, final float[][] mit, final float[][] mshadow) {
        this.M = m;
        this.Mit = mit;
        this.Mshadow = mshadow;
    }

    @Override
    public float[] vertex(final int a, final int b) {
        float[] uvs = model.vt.get(model.f.get(a)[b][1] - 1);
        uv[0][b] = uvs[0];
        uv[1][b] = uvs[1];
        float[] gl = mult(
            mult(mxViewport, mxProjection, mxModelView),
            upLength(model.v.get(model.f.get(a)[b][0] - 1), 4, 1)
        );
        float[] divGl = lowLength(div(gl, gl[3]), 3);
        triangle[0][b] = divGl[0];
        triangle[1][b] = divGl[1];
        triangle[2][b] = divGl[2];
        return gl;
    }

    @Override
    public Object[] fragment(float[] a, int b) {
        Object[] result = new Object[2];
        float[] sbc = mult(Mshadow, upLength(mult(triangle, a), 4, 1));
        sbc = div(sbc, sbc[3]);
        int index = (int) sbc[0] + (int) sbc[1] * size;
        float shadow = 0.2f + 0.8f * (bufferShadow[index] < sbc[2] ? 1 : 0);
        float[] vuv = mult(uv, a);
        float[] n = normalize(lowLength(mult(Mit, upLength(getNormals(vuv), 4, 1)), 3));
        float[] l = normalize(lowLength(mult(M, upLength(ligthDir, 4, 1)), 3));
        float[] r = normalize(sub(mult(n, dot(n, l) * 2.0f), l));
        float rzw = (float) Math.pow(Math.max(r[2], 0.0f), 10000);
        float diff = Math.max(dot(n, l), 0.0f);
        int[] c = getComponents(texture.getRGB((int) (vuv[0] * 2844), (int) (vuv[1] * 2844)));
        int[] color = new int[3];
        for (int i = 0; i < 3; i++) {
            color[i] = (int) Math.min(c[i] * shadow * (diff + 0.6 * rzw), 255);
        }
        result[0] = false;
        result[1] = toColor(color[0], color[1], color[2]);
        return result;
    }

    private float[] getNormals(float[] vuv) {
        int[] components = getComponents(normals.getRGB((int) (vuv[0] * 2844), (int) (vuv[1] * 2844)));
        float[] normals = new float[3];
        for (int i = 0; i < 3; i++) {
//            normals[2-i] = components[i] / 255.0f * 2.0f - 1.0f;
            normals[i] = components[i] / 255.0f * 2.0f - 1.0f;
        }
        return normals;
    }

    private int[] getComponents(int rgb) {
        int value = -16777216 | rgb;
        return new int[]{ value >> 16 & 255, value >> 8 & 255, value & 255 };
    }

    private int toColor(int r, int g, int b) {
        return 255 << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }
}
