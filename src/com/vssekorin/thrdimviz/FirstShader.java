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
        float[] gl = mult(
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
        float[] sbc = mult(Mshadow, upLength(mult(tri, a), 4, 1)); //Координаты в bufferShadow
        sbc = div(sbc, sbc[3]);
        int shadow = bufferShadow[(int)sbc[0] + (int)sbc[1] * size] < sbc[2] ? 1 : 0; //Сравнение z координаты со значением из bufferShadow
        float[] vUV = mult(uv, a); //интерполяция uv
        float[] n = normalize(lowLength(mult(Mit, upLength(getNormal(vUV), 4, 1)), 3)); //нормаль
        float[] l = normalize(lowLength(mult(M, upLength(ligthDir, 4, 1)), 3)); //свет
        float[] r = normalize(sub(mult(n, dot(n, l) * 2.0f), l));
        double rzw = Math.pow(r[2], 10000); //Возведение в некоторую большую степень
        float diff = dot(n, l); // Чтобы убрать красный цвет с картинки надо делать Math.max с 0, но возможно, он и должен быть.
        int[] color = getComponents(Application.texture.getRGB((int) (vUV[0] * 2844), (int) (vUV[1] * 2844))); //Компоненты цвета из текстуры
        for (int i = 0; i < 3; i++) {
            color[i] = Math.min((int)(color[i] * shadow * (0.5f * diff + 0.5f * rzw)), 255); //От коэффицеинтов ничего не меняется. Формула неправильная с вероятностью 90%.
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
