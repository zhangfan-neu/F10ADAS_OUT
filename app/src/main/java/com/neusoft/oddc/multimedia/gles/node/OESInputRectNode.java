package com.neusoft.oddc.multimedia.gles.node;


import com.neusoft.oddc.multimedia.gles.cropper.BaseTextureCropper;
import com.neusoft.oddc.multimedia.gles.program.OESInputProgram;

public class OESInputRectNode extends RectNode {
    private static final String TAG = OESInputRectNode.class.getSimpleName();

    public OESInputRectNode(BaseTextureCropper corpper) {
        super(new OESInputProgram(), corpper);
    }

    public void setFrameTexture(int textureId, float[] texMatrix) {
        this.textureId = textureId;
        this.texMatrix = texMatrix;
    }
}
