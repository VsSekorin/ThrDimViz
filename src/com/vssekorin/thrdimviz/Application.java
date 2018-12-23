package com.vssekorin.thrdimviz;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.vssekorin.thrdimviz.Param.*;

public final class Application {

    public static final Obj model = loadObj();

    public static void main(String[] args) {
        normalize(ligthDir);
        renderShadow();
        float[][] M = mxMul(mxViewport, mxProjection, mxModelView);
        renderImage();
    }

    private static float[][] mxMul(float[][] first, float[][] second, float[][] third) {
        return mxMul2(mxMul2(first, second), third);
    }

    private static float[][] mxMul2(float[][] first, float[][] second) {
        final float[][] result = {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += first[i][k] + second[k][j];
                }
            }
        }
        return result;
    }

    private static void renderShadow() {
        final BufferedImage sorry = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        lookat(ligthDir, center, up);
        viewport(size / 8, size / 8, size * 3 / 4, size * 3 / 4);
        projection(0);
        Shader second = new SecondShader();
        float[][] coords = new float[3][3];
        for (int i = 0; i < model.f.size(); i++) {
            coords[0] = second.vertex(i, 0);
            coords[1] = second.vertex(i, 1);
            coords[2] = second.vertex(i, 2);
            triangle(coords, second, sorry, bufferShadow);
        }
    }

    private static void projection(float coef) {
        identity(mxProjection);
        mxProjection[3][2] = coef;
    }

    private static void viewport(int x, int y, int w, int h) {
        identity(mxViewport);
        mxViewport[0][3] = x + w / 2.f;
        mxViewport[1][3] = y + h / 2.f;
        mxViewport[2][3] = depth / 2.f;
        mxViewport[0][0] = w / 2.f;
        mxViewport[1][1] = h / 2.f;
        mxViewport[2][2] = depth / 2.f;
    }

    private static void lookat(float[] eye, float[] center, float[] up) {
        final float[] z = normalize(Vector.sub(eye, center));
        final float[] x = normalize(cross(up, z));
        final float[] y = normalize(cross(z, x));
        identity(mxModelView);
        for (int i = 0; i < 3; i++) {
            mxModelView[0][i] = x[i];
            mxModelView[1][i] = y[i];
            mxModelView[2][i] = z[i];
            mxModelView[i][3] = -center[i];
        }
    }

    private static float[] cross(float[] first, float[] second) {
        return new float[]{
            first[1] * second[2] - first[2] * second[1],
            first[2] * second[0] - first[0] * second[2],
            first[0] * second[1] - first[1] * second[0]
        };
    }

    private static void identity(float[][] mx) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                mx[i][j] = i == j ? 1 : 0;
            }
        }
    }

    private static void renderImage() {
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        lookat(eye, center, up);
        viewport(size / 8, size / 8, size * 3 / 4, size * 3 / 4);
        projection(-1.f / norma(Vector.sub(eye, center)));
        Shader first = new Shader() {//TODO
            @Override
            public float[] vertex(int a, int b) {
                return new float[0];
            }

            @Override
            public Object[] fragment(float[] a, int b) {
                return new Object[2];
            }
        };
        float[][] coords = new float[3][3];
        for (int i = 0; i < model.f.size(); i++) {
            coords[0] = first.vertex(i, 0);
            coords[1] = first.vertex(i, 1);
            coords[2] = first.vertex(i, 2);
            triangle(coords, first, image, bufferZ);
        }
    }

    private static void triangle(float[][] coords, Shader shader, BufferedImage image, float[] buffer) {
        final float[] min = new float[]{Float.MAX_VALUE, Float.MAX_VALUE};
        final float[] max = new float[]{Float.MIN_VALUE, Float.MIN_VALUE};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                min[j] = Math.min(min[j], coords[i][j] / coords[i][3]);
                max[j] = Math.max(max[j], coords[i][j] / coords[i][3]);
            }
        }
        Integer color = 0;
        for (float x = min[0]; x < max[0]; x++) {
            for (float y = min[1]; y < max[1]; y++) {
                final float[] c = barycentric(
                    Vector.lowLength(Vector.div(coords[0], coords[0][3]), 2),
                    Vector.lowLength(Vector.div(coords[1], coords[1][3]), 2),
                    Vector.lowLength(Vector.div(coords[2], coords[2][3]), 2),
                    new float[]{x, y}
                );
                final float z = coords[0][2] * c[0] + coords[1][2] * c[1] + coords[2][2] * c[2];
                final float w = coords[0][3] * c[0] + coords[1][3] * c[1] + coords[2][3] * c[2];
                final int fd = (int) (z / w);
                if (c[0] < 0 || c[1] < 0 || c[2] < 0 || buffer[(int) (x + y * size)] > fd)
                    continue;
                Object[] discardAndColor = shader.fragment(c, color);
                if (!(boolean) discardAndColor[0]) {
                    buffer[(int) (x + y * size)] = fd;
                    image.setRGB((int) x, (int) y, (int) discardAndColor[1]);
                }
            }
        }
    }

    private static float[] barycentric(float[] a, float[] b, float[] c, float[] d) {
        final float[][] e = new float[2][3];
        for (int i = 0; i < 2; i++) {
            e[i][0] = c[i] - a[i];
            e[i][1] = b[i] - a[i];
            e[i][2] = a[i] - d[i];
        }
        float[] f = cross(e[0], e[1]);
        if (f[2] > 0.0001) {
            return new float[]{1.f - (f[0] + f[1]) / f[2], f[1] / f[2], f[0] / f[2]};
        } else {
            return new float[]{-1, -1, -1};
        }
    }

    private static float[] normalize(float[] vector) {
        final float norma = norma(vector);
        vector[0] *= 1 / norma;
        vector[1] *= 1 / norma;
        vector[2] *= 1 / norma;
        return vector;
    }

    private static float norma(float[] vector) {
        return vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2];
    }

    private static Obj loadObj() {
        final Obj obj = new Obj();
        try (Stream<String> objLines = Files.lines(Paths.get("african_head.obj"))) {
            objLines
                .map(String::trim)
                .filter(line -> !line.startsWith("#"))
                .map(line -> line.split("\\s+"))
                .forEach(array -> {
                    switch (array[0]) {
                        case "f":
                            String[] v1 = array[1].split("/");
                            String[] v2 = array[2].split("/");
                            String[] v3 = array[3].split("/");
                            obj.f.add(new int[][]{
                                {Integer.parseInt(v1[0]), Integer.parseInt(v1[1]), Integer.parseInt(v1[2])},
                                {Integer.parseInt(v2[0]), Integer.parseInt(v2[1]), Integer.parseInt(v2[2])},
                                {Integer.parseInt(v3[0]), Integer.parseInt(v3[1]), Integer.parseInt(v3[2])},
                            });
                            break;
                        case "v":
                            obj.v.add(new float[]{Float.parseFloat(array[1]), Float.parseFloat(array[2]), Float.parseFloat(array[3])});
                            break;
                        case "vn":
                            obj.vn.add(new float[]{Float.parseFloat(array[1]), Float.parseFloat(array[2]), Float.parseFloat(array[3])});
                            break;
                        case "vt":
                            obj.vt.add(new float[]{Float.parseFloat(array[1]), Float.parseFloat(array[2]), Float.parseFloat(array[3])});
                            break;
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
