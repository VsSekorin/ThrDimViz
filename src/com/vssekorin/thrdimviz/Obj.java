package com.vssekorin.thrdimviz;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Obj {
    public final List<int[][]> f = new ArrayList<>();
    public final List<float[]> v = new ArrayList<>();
    public final List<float[]> vn = new ArrayList<>();
    public final List<float[]> vt = new ArrayList<>();


    public static Obj load() {
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