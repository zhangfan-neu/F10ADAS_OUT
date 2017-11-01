package com.neusoft.oddc.multimedia.gles.node;

import android.util.Log;

import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.multimedia.gles.MeshConstants;
import com.neusoft.oddc.multimedia.gles.program.BlendingProgram;

public class FrameAnimationNode extends RectNode {
    private static final String TAG = FrameAnimationNode.class.getSimpleName();

    private boolean isfinished = false;
    private FrameAnimationAdapter frameAdapter;
    private boolean layoutChanged = true;

    public FrameAnimationNode() {
        super(new BlendingProgram());
        texCoordArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_TEX_COORDS_FLIP);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public int draw() {
        pendingTasks.runPendingOnDrawTasks();
        if (isfinished) {
            return 0;
        } else {
            updateTexture();
        }
        adjustLayout();
        return super.draw();
    }

    public void setFrameAnimationAdapter(FrameAnimationAdapter adapter) {
        this.frameAdapter = adapter;
    }

    private void updateTexture() {
        if (null != frameAdapter && !frameAdapter.isFinished()) {
            textureId = frameAdapter.getNextFrameTexture();
            this.setTextMatrix(frameAdapter.getNextFrameTextureTransformMatrix());
        } else {
            isfinished = true;
        }
    }

    private void adjustLayout() {
        if (!layoutChanged) {
            return;
        }
        updateMVP();
        Log.d(TAG, "layout trace -- > adjustLayout : " + ", fullFrameWidth = " + fullFrameWidth + ", fullFrameHeight = " + fullFrameHeight
                + ", top = " + top + ", left = " + left + ", right = " + right + ", bottom = " + bottom);
        layoutChanged = false;
    }


    @Override
    public void setFullFrameSize(int width, int height) {
        super.setFullFrameSize(width, height);
        layoutChanged = true;
        Log.d(TAG, "layout trace -- > setFullFrameSize : width = " + fullFrameWidth + ", height = " + fullFrameHeight);
    }
}
