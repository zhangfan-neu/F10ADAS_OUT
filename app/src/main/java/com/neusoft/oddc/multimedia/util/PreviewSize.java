package com.neusoft.oddc.multimedia.util;


public class PreviewSize {

    public int width;
    public int height;

    public PreviewSize(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreviewSize)) {
            return false;
        }
        PreviewSize s = (PreviewSize) obj;
        return width == s.width && height == s.height;
    }

    @Override
    public int hashCode() {
        return width * 32713 + height;
    }

}
