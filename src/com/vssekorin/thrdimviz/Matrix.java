package com.vssekorin.thrdimviz;

public final class Matrix {

    public static float[][] mul3(float[][] first, float[][] second, float[][] third) {
        return multiply(multiply(first, second), third);
    }

    public static void identity(float[][] mx) {
        for (int i = 0; i < mx.length; i++) {
            for (int j = 0; j < mx[0].length; j++) {
                mx[i][j] = i == j ? 1 : 0;
            }
        }
    }

    public static float[] mulVect(float[][] matrix, float[] vector) {
        final float[] result = new float[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = 0;
            for (int j = 0; j < vector.length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    public static float determinant(float[][] matrix) {
        if (matrix.length != matrix[0].length)
            throw new IllegalStateException("invalid dimensions");

        if (matrix.length == 2)
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        float det = 0;
        for (int i = 0; i < matrix[0].length; i++)
            det += Math.pow(-1, i) * matrix[0][i]
                * determinant(minor(matrix, 0, i));
        return det;
    }

    public static float[][] inverse(float[][] matrix) {
        float[][] inverse = new float[matrix.length][matrix.length];

        // minors and cofactors
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                inverse[i][j] = (float) (Math.pow(-1, i + j)
                                    * determinant(minor(matrix, i, j)));

        // adjugate and determinant
        float det = 1.0f / determinant(matrix);
        for (int i = 0; i < inverse.length; i++) {
            for (int j = 0; j <= i; j++) {
                float temp = inverse[i][j];
                inverse[i][j] = inverse[j][i] * det;
                inverse[j][i] = temp * det;
            }
        }

        return inverse;
    }

    public static float[][] minor(float[][] matrix, int row, int column) {
        float[][] minor = new float[matrix.length - 1][matrix.length - 1];

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; i != row && j < matrix[i].length; j++)
                if (j != column)
                    minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
        return minor;
    }

    public static float[][] multiply(float[][] a, float[][] b) {
        if (a[0].length != b.length)
            throw new IllegalStateException("invalid dimensions");

        float[][] matrix = new float[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                float sum = 0;
                for (int k = 0; k < a[i].length; k++)
                    sum += a[i][k] * b[k][j];
                matrix[i][j] = sum;
            }
        }

        return matrix;
    }

    public static float[][] transpose(float[][] matrix) {
        float[][] transpose = new float[matrix[0].length][matrix.length];

        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                transpose[j][i] = matrix[i][j];
        return transpose;
    }
}
