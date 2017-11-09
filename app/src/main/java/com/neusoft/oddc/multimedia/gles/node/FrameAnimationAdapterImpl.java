package com.neusoft.oddc.multimedia.gles.node;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.widget.realtimedata.RealTimeDataDrawer;

public class FrameAnimationAdapterImpl implements FrameAnimationAdapter {
    private static final String TAG = FrameAnimationAdapterImpl.class.getSimpleName();

    private Context context;
    private int orgFrameWidth = 0;
    private int orgFrameHeight = 0;
    private int textureID = GlUtil.NO_TEXTURE;
    private RealTimeDataDrawer realTimeDataDrawer;

    public void setRenderADAS(boolean renderADAS) {
        this.renderADAS = renderADAS;
    }

    private boolean renderADAS = false;

    public FrameAnimationAdapterImpl(Context context, int orgFrameWidth, int orgFrameHeight) {
        this.context = context;
        this.orgFrameWidth = orgFrameWidth;
        this.orgFrameHeight = orgFrameHeight;
    }

    public void setRealTimeDataDrawer(RealTimeDataDrawer realTimeDataDrawer) {
        this.realTimeDataDrawer = realTimeDataDrawer;
    }

    @Override
    public Bitmap getFrameBitmap(int index) {
        Bitmap realDataBitmap;
        if (renderADAS) {
            if (null == realTimeDataDrawer) {
                realTimeDataDrawer = new RealTimeDataDrawer(context, orgFrameWidth, orgFrameHeight);
                Log.d(TAG, "getFrameBitmap realTimeDataDrawer = NULL");
            }
            realDataBitmap = realTimeDataDrawer.drawDataView();
        } else {
            realDataBitmap = Bitmap.createBitmap(orgFrameWidth, orgFrameHeight, Bitmap.Config.ARGB_8888);
            realDataBitmap.setHasAlpha(true);
            Canvas canvas = new Canvas(realDataBitmap);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }

        if (null != realDataBitmap && !realDataBitmap.isRecycled()) {
            return realDataBitmap;
        } else {
            Log.e(TAG, "getFrameBitmap bitmap = null");
        }
        return null;
    }

    @Override
    public int getFrameTexture(int index, int textureID) {
        Bitmap bitmap = getFrameBitmap(index);
        if (null != bitmap && !bitmap.isRecycled()) {
            return GlUtil.loadTexture(bitmap, textureID, false);
        } else {
            Log.e(TAG, "getFrameTexture bitmap = null");
        }
        return 0;
    }

    @Override
    public int getNextFrameTexture() {
        this.textureID = getFrameTexture(0, this.textureID);
        return this.textureID;
    }

    @Override
    public float[] getNextFrameTextureTransformMatrix() {
        float[] matrix = new float[16];
        android.opengl.Matrix.setIdentityM(matrix, 0);
        return matrix;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

}
