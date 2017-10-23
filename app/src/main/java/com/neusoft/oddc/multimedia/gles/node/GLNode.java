package com.neusoft.oddc.multimedia.gles.node;

public interface GLNode {

    public void init();

    public void release();

    public int draw();

    public GLNode getParent();

    public void setFullFrameSize(int width, int height);

    public int getFullFrameWidth();

    public int getFullFrameHeight();

    public boolean isInitialized();

    public void setRotation(float rotation);

    public float getRotation();
}
