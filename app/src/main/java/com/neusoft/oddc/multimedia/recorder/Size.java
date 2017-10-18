package com.neusoft.oddc.multimedia.recorder;


public class Size {

    public int width;
    public int height;

    public Size(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Size)) {
            return false;
        }
        Size s = (Size) obj;
        return width == s.width && height == s.height;
    }

    @Override
    public int hashCode() {
        return width * 32713 + height;
    }

}
