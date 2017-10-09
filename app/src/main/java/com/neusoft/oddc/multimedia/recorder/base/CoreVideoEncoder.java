package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;


/**
 * This class wraps up the core components used for surface-input video encoding.
 * <p/>
 * Once created, frames are fed to the input surface.  Remember to provide the presentation
 * time stamp, and always call drainEncoder() before swapBuffers() to ensure that the
 * producer side doesn't get backed up.
 * <p/>
 * This class is not thread-safe, with one exception: it is valid to use the input surface
 * on one thread, and drain the output on a different thread.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CoreVideoEncoder extends BaseEncoder {
    private static final String TAG = CoreVideoEncoder.class.getSimpleName();

    // TODO: these ought to be configurable as well
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30; // fps
    private static final int IFRAME_INTERVAL = 2; // seconds between I-frames

    private Surface inputSurface;

    private OnFileSizeChangedListener onFileSizeChangedListener;

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    public CoreVideoEncoder(int width, int height, int bitRate, Muxer muxer) {
        super();
        this.muxer = muxer;
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            format.setInteger(MediaFormat.KEY_PROFILE,MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
//            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel13);
//        }

        Log.d(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            Log.e(TAG, "Create video encoder error!");
        }
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = encoder.createInputSurface();
        encoder.start();

        trackIndex = -1;
    }

    /**
     * Returns the encoder's input surface.
     */
    public Surface getInputSurface() {
        return inputSurface;
    }

    @Override
    protected boolean isUsingSurfaceInputEncoder() {
        return true;
    }

    @Override
    protected void onFileSizeChanged(long fileSize) {
        if (null != onFileSizeChangedListener) {
            onFileSizeChangedListener.onFileSizeChanged(fileSize);
        }
    }

    interface OnFileSizeChangedListener {
        void onFileSizeChanged(long fileSize);
    }

    public void setOnFileSizeChangedListener(OnFileSizeChangedListener onFileSizeChangedListener) {
        this.onFileSizeChangedListener = onFileSizeChangedListener;
    }
}
