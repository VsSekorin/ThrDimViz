package com.vssekorin.thrdimviz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.vssekorin.thrdimviz.Geometry.*;
import static com.vssekorin.thrdimviz.Param.*;

public final class Application {

    public static Obj model;

    public static BufferedImage texture;

    public static BufferedImage normals;

    public static void main(String[] args) throws IOException {
        model = Obj.load();
        texture = ImageIO.read(new File("texture.jpg"));
        normals = ImageIO.read(new File("normals.jpg"));
        for (int i = 0; i < size * size; i++) {
            bufferZ[i] = -Float.MAX_VALUE;
            bufferShadow[i] = -Float.MAX_VALUE;
        }
        normalize(ligthDir);
        renderShadow();
        renderImage();
    }

    private static void renderShadow() throws IOException {
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
        ImageIO.write(sorry, "jpeg", new File("sorry.jpeg"));
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
        final float[] z = normalize(sub(eye, center));
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

    private static void renderImage() throws IOException {
        float[][] M = mult(mxViewport, mxProjection, mxModelView);
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        lookat(eye, center, up);
        viewport(size / 8, size / 8, size * 3 / 4, size * 3 / 4);
        projection(-1.f / norma(sub(eye, center)));
        float[][] mit = transpose(inverse(mult(mxProjection, mxModelView)));// or inverse(transpose(...))
        float[][] mshadow = mult(M, inverse(mult(mxViewport, mxProjection, mxModelView)));
        final Shader shader = new FirstShader(mxModelView, mit, mshadow);
        float[][] coords = new float[3][4];
        for (int i = 0; i < model.f.size(); i++) {
            coords[0] = shader.vertex(i, 0);
            coords[1] = shader.vertex(i, 1);
            coords[2] = shader.vertex(i, 2);
            triangle(coords, shader, image, bufferZ);
        }
        flipVertically(image);
        ImageIO.write(image, "jpeg", new File("image.jpeg"));
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
        for (int x = (int)min[0]; x < max[0]; x++) {
            for (int y = (int)min[1]; y < max[1]; y++) {
                final float[] br = barycentric(
                    lowLength(div(coords[0], coords[0][3]), 2),
                    lowLength(div(coords[1], coords[1][3]), 2),
                    lowLength(div(coords[2], coords[2][3]), 2),
                    new float[]{x, y}
                );
                final float z = coords[0][2] * br[0] + coords[1][2] * br[1] + coords[2][2] * br[2];
                final float w = coords[0][3] * br[0] + coords[1][3] * br[1] + coords[2][3] * br[2];
                final int fd = (int) (z / w);
                if (br[0] < 0 || br[1] < 0 || br[2] < 0 || buffer[x + y * size] > fd) {
                    continue;
                }
                if (x < 1000 && y < 1000 && x > 0 && y > 0) { //REMOVE!!!
                    buffer[x + y * size] = fd;
                    image.setRGB(x, y, shader.fragment(br));
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
        if (Math.abs(f[2]) > 0.0001) {
            return new float[]{1.f - (f[0] + f[1]) / f[2], f[1] / f[2], f[0] / f[2]};
        } else {
            return new float[]{-1, -1, -1};
        }
    }
}
