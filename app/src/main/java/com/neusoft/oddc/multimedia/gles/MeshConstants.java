package com.neusoft.oddc.multimedia.gles;

public class MeshConstants {
    public static final int COORDS_COUNT_2 = 2;
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_SHORT = 2;


    public static final float RECTANGLE_COORDS[] = {
            -1.0f, -1.0f, // 0 bottom left
            1.0f, -1.0f, // 1 bottom right
            -1.0f, 1.0f, // 2 top left
            1.0f, 1.0f, // 3 top right
    };
    public static final float RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f, // 0 bottom left
            1.0f, 0.0f, // 1 bottom right
            0.0f, 1.0f, // 2 top left
            1.0f, 1.0f // 3 top right
    };
    public static final float RECTANGLE_TEX_COORDS_FLIP[] = {
            0.0f, 1.0f, // 0 bottom left
            1.0f, 1.0f, // 1 bottom right
            0.0f, 0.0f, // 2 top left
            1.0f, 0.0f // 3 top right
    };
}
