package com.neusoft.oddc.multimedia.gles.node;

import android.opengl.GLES20;

import com.neusoft.oddc.multimedia.gles.program.GLBlendable;
import com.neusoft.oddc.multimedia.gles.program.GLProgram;


public class SimpleBlendingRectNode extends RectNode {

    protected int blendingSrcFactor = GLES20.GL_SRC_ALPHA;
    protected int blendingDestFactor = GLES20.GL_ONE;


    private GLBlendable blendingProgram;

    public SimpleBlendingRectNode(GLProgram program) {
        super(program);

        if (program instanceof GLBlendable) {
            blendingProgram = (GLBlendable) program;
        }

    }

    public void setBlendingFunction(int src, int dest) {
        this.blendingSrcFactor = src;
        this.blendingDestFactor = dest;
        if (null != blendingProgram) {
            blendingProgram.setBlendingFunction(blendingSrcFactor, blendingDestFactor);
        }
    }

}
