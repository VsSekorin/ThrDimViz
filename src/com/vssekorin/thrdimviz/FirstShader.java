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
    public int fragment(float[] barycentric) {
        float[] sbc = mult(Mshadow, upLength(mult(triangle, barycentric), 4, 1));
        sbc = div(sbc, sbc[3]);
        int index = (int) sbc[0] + (int) sbc[1] * size;
        float shadow = 0.2f + 0.8f * (bufferShadow[index] < sbc[2] + 32.1f ? 1 : 0);
        float[] vuv = mult(uv, barycentric);
        float[] n = normalize(lowLength(mult(Mit, upLength(getNormals(vuv), 4, 1)), 3));
        float[] l = normalize(lowLength(mult(M, upLength(ligthDir, 4, 1)), 3));
        float[] r = normalize(sub(mult(n, dot(n, l) * 2.0f), l));
        float rzw = (float) Math.pow(Math.max(r[2], 0.0f), 150);
        float diff = Math.max(dot(n, l), 0.0f);
        int[] components = getComponents(texture.getRGB((int) (vuv[0] * 2844), 2844 - (int) (vuv[1] * 2844)));
        for (int i = 0; i < 3; i++) {
            components[i] = (int) Math.min(16 + components[i] * shadow * (1.1 * diff + 0.5 * rzw), 255);
        }
        return toColor(components);
    }

    private float[] getNormals(float[] vuv) {
        int[] components = getComponents(normals.getRGB((int) (vuv[0] * 2844), 2844-(int) (vuv[1] * 2844)));
        float[] normals = new float[3];
        for (int i = 0; i < 3; i++) {
            normals[i] = components[i] / 255.0f * 2.0f - 1.0f;
        }
        return normals;
    }

    private int[] getComponents(int rgb) {
        int value = -16777216 | rgb;
        return new int[]{ value >> 16 & 255, value >> 8 & 255, value & 255 };
    }

    private int toColor(int[] rgb) {
        return toColor(rgb[0], rgb[1], rgb[2]);
    }

    private int toColor(int r, int g, int b) {
        return 255 << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }
}
