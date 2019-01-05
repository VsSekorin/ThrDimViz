package com.vssekorin.thrdimviz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.vssekorin.thrdimviz.Param.*;
public final class Application {

    public static Obj model;

    public static BufferedImage texture;

    public static void main(String[] args) throws IOException {
        model = loadObj();
        texture = ImageIO.read(new File("texture.jpg"));
        for (int i = 0; i < size * size; i++) {
            bufferZ[i] = 0;
            bufferShadow[i] = 0;
        }
        Vector.normalize(ligthDir);
        renderShadow();
        renderImage();
    }

    private static void renderShadow() {
        final BufferedImage sorry = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        lookat(ligthDir, center, up);
        viewport(size / 8, size / 8, size * 3 / 4, size * 3 / 4);
        projection(0);
        Shader second = new SecondShader();
        float[][] coords = new float[3][4];
        for (int i = 0; i < model.f.size(); i++) {
            coords[0] = second.vertex(i, 0);
            coords[1] = second.vertex(i, 1);
            coords[2] = second.vertex(i, 2);
            triangle(coords, second, sorry, bufferShadow);
        }
        flipVertically(sorry);
        try {
            ImageIO.write(sorry, "jpeg", new File("sorry.jpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void projection(float coef) {
        Matrix.identity(mxProjection);
        mxProjection[3][2] = coef;
    }

    private static void viewport(int x, int y, int w, int h) {
        Matrix.identity(mxViewport);
        mxViewport[0][3] = x + w / 2.f;
        mxViewport[1][3] = y + h / 2.f;
        mxViewport[2][3] = depth / 2.f;
        mxViewport[0][0] = w / 2.f;
        mxViewport[1][1] = h / 2.f;
        mxViewport[2][2] = depth / 2.f;
    }

    private static void lookat(float[] eye, float[] center, float[] up) {
        final float[] z = Vector.normalize(Vector.sub(eye, center));
        final float[] x = Vector.normalize(cross(up, z));
        final float[] y = Vector.normalize(cross(z, x));
        Matrix.identity(mxModelView);
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

    private static void renderImage() {
        float[][] M = Matrix.mul3(mxViewport, mxProjection, mxModelView);
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        lookat(eye, center, up);
        viewport(size / 8, size / 8, size * 3 / 4, size * 3 / 4);
        projection(-1.f / Vector.norma(Vector.sub(eye, center)));
        float[][] mit = Matrix.transpose(Matrix.inverse(Matrix.multiply(mxProjection, mxModelView)));
        float[][] mshadow = Matrix.multiply(M, Matrix.inverse(Matrix.mul3(mxViewport, mxProjection, mxModelView)));
        final Shader shader = new FirstShader(mxModelView, mit, mshadow);
        float[][] coords = new float[3][4];
        for (int i = 0; i < model.f.size(); i++) {
            coords[0] = shader.vertex(i, 0);
            coords[1] = shader.vertex(i, 1);
            coords[2] = shader.vertex(i, 2);
            triangle(coords, shader, image, bufferZ);
        }
        flipVertically(image);
        try {
            ImageIO.write(image, "jpeg", new File("image.jpeg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flipVertically(final BufferedImage image) {
        for (int i = 1; i < size; i++) {
            for (int j = 1; j < size / 2; j++) {
                int rgb = image.getRGB(i, j);
                image.setRGB(i, j, image.getRGB(i, size - j));
                image.setRGB(i, size - j, rgb);
            }
        }
    }

    private static void triangle(float[][] coords, Shader shader, BufferedImage image, float[] buffer) {
        final float[] min = new float[]{Float.MAX_VALUE, Float.MAX_VALUE};
        final float[] max = new float[]{-Float.MAX_VALUE, -Float.MAX_VALUE};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                min[j] = Math.min(min[j], coords[i][j] / coords[i][3]);
                max[j] = Math.max(max[j], coords[i][j] / coords[i][3]);
            }
        }
        int color = 0;
        for (int x = (int)min[0]; x < max[0]; x++) {
            for (int y = (int)min[1]; y < max[1]; y++) {
                final float[] c = barycentric(
                    Vector.lowLength(Vector.div(coords[0], coords[0][3]), 2),
                    Vector.lowLength(Vector.div(coords[1], coords[1][3]), 2),
                    Vector.lowLength(Vector.div(coords[2], coords[2][3]), 2),
                    new float[]{x, y}
                );
                final float z = coords[0][2] * c[0] + coords[1][2] * c[1] + coords[2][2] * c[2];
                final float w = coords[0][3] * c[0] + coords[1][3] * c[1] + coords[2][3] * c[2];
                final int fd = (int) (z / w);
                if (c[0] < 0 || c[1] < 0 || c[2] < 0 || buffer[x + y * size] > fd) {
                    continue;
                }
                Object[] discardAndColor = shader.fragment(c, color);
                if (!(boolean) discardAndColor[0] && x < 1000 && y < 1000 && x > 0 && y > 0) {
                    buffer[x + y * size] = fd;
                    image.setRGB(x, y, (int)discardAndColor[1]);
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
