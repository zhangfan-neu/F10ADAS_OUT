package com.neusoft.oddc.multimedia.recorder;

import android.graphics.SurfaceTexture;

public interface Previewable {

    void startPreview();

    void stopPreview();

    void setPreviewTexture(SurfaceTexture surfaceTexture);

    Size getPreviewSize();

    void setPreviewableStateChangedListener(PreviewableStateChangedListener listener);

}
