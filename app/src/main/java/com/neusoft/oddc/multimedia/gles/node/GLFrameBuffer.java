package com.neusoft.oddc.multimedia.gles.node;

public interface GLFrameBuffer extends GLNode {

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public void setOutputSize(int width, int height);

    public int getTextureID();
}
