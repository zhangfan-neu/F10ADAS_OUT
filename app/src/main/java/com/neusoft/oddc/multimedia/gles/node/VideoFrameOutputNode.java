package com.neusoft.oddc.multimedia.gles.node;


import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.multimedia.gles.MeshConstants;
import com.neusoft.oddc.multimedia.gles.program.SimpleProgram;

public class VideoFrameOutputNode extends RectNode {
    public VideoFrameOutputNode() {
        glProgram = new SimpleProgram();
    }

    public VideoFrameOutputNode(boolean flip) {
        glProgram = new SimpleProgram();

        if (flip) {
            texCoordArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_TEX_COORDS_FLIP);
        } else {
            texCoordArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_TEX_COORDS);
        }
    }
}
