package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.EglCore;
import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.multimedia.gles.IVideoFrameRender;
import com.neusoft.oddc.multimedia.gles.WindowSurface;
import com.neusoft.oddc.multimedia.gles.render.VideoFrameRender;
import com.neusoft.oddc.multimedia.recorder.Previewable;
import com.neusoft.oddc.multimedia.recorder.PreviewableStateChangedListener;
import com.neusoft.oddc.multimedia.recorder.Size;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RunnableVideoEncoder implements Runnable {
    private static final String TAG = RunnableVideoEncoder.class.getSimpleName();

    protected enum STATE {
        UNINITIALIZED,
        RESETTING,
        INITIALIZING,
        INITIALIZED,
        RECORDING,
        STOPPING,
        RELEASED;
    }

//    protected volatile STATE encoderState = STATE.UNINITIALIZED;

    protected static final int MSG_FRAME_AVAILABLE = 1;
    protected static final int MSG_SURFACE_CREATED = 2;
    protected static final int MSG_RELEASE = 3;
    protected static final int MSG_RESET = 4;
    protected static final int MSG_SET_DISPLAY_ORIENTATION = 5;

    protected static final int MSG_ACTIVITY_RESUME = 6;
    protected static final int MSG_ACTIVITY_PAUSE = 7;

    protected static final int MSG_START_RECORDING = 8;
    protected static final int MSG_STOP_RECORDING = 9;

    protected static final int MSG_CAMERA_PREPARED = 10;

    // ----- accessed exclusively by encoder thread -----
    protected WindowSurface inputWindowSurface;

    protected EglCore eglCore;
    protected IVideoFrameRender videoFrameRender;

    protected int textureId;
    protected CoreVideoEncoder videoEncoder;
    protected RecorderSession recorderSession;
    protected float[] transformMatrix = new float[16];
    protected float[] identityMatrix = new float[16];
    // ----- accessed by multiple threads -----
    protected volatile EncoderHandler handler;

    protected EglState eglState;
    protected final Object surfaceTextureLock = new Object();
    protected SurfaceTexture surfaceTexture;
    protected final Object recordFrameLock = new Object();
    protected boolean isEncoderPrepared;
    protected volatile boolean isRecording;
    //	private volatile boolean requestStopRecorder;
    protected final Object recordThreadStartingLock = new Object();
    protected boolean isRecordThreadRunning;
    protected GLSurfaceView displayView;

    protected boolean isHostActivityPresent = false;

    protected CameraEncoderStateChangedListener recorderStateChangedListener;
    protected int skipCount = 0;

    protected int screenOrentation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    protected long recoderStartTS4FPS = -1;
    protected long frameCount4FPS = 0;
    protected long fps = 0;

    protected Previewable camera;

    protected volatile boolean isCameraPrepared = false;
    protected volatile boolean isPreviewing = false;

    private long openTime = 0;

    public RunnableVideoEncoder(RecorderSession config, IVideoFrameRender frameRender,
                                CameraEncoderStateChangedListener recorderStateChangedListener,
                                Previewable camera) {
        openTime = System.currentTimeMillis();

        if (null == config || null == camera) {
            throw new IllegalArgumentException("recorder session can not be null.");
        }
        this.recorderStateChangedListener = recorderStateChangedListener;

        this.camera = camera;
        camera.setPreviewableStateChangedListener(previewableStateChangedListener);
        Matrix.setIdentityM(identityMatrix, 0);

//        encoderState = STATE.INITIALIZING;
//
//        Log.d(TAG, "cameraEncoderState trace -- > RunnableVideoEncoder: encoEderState = " + encoderState);

        if (null != frameRender) {
            videoFrameRender = frameRender;
        } else {
            videoFrameRender = new VideoFrameRender();
        }


        isEncoderPrepared = false;
        isRecording = false;
        //		requestStopRecorder = false;
        recorderSession = config;

        eglState = new EglState();
        startEncodingThread();

    }

    /********************
     * initializing operations
     ********************/
    private void startEncodingThread() {
        synchronized (recordThreadStartingLock) {
            if (isRecordThreadRunning) {
                return;
            }
            new Thread(this, RunnableVideoEncoder.class.getSimpleName()).start();
            while (!isRecordThreadRunning) {
                try {
                    recordThreadStartingLock.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        Log.d(TAG, "startup trace -- > startEncodingThread : time -- " + (System.currentTimeMillis() - openTime));
        synchronized (recordThreadStartingLock) {
            handler = new EncoderHandler(this);
            isRecordThreadRunning = true;
            recordThreadStartingLock.notify();
        }
        Looper.loop();

        synchronized (recordThreadStartingLock) {
            isRecordThreadRunning = false;
            handler = null;
            recordThreadStartingLock.notify();
        }
    }

    public void setPreviewDisplay(GLSurfaceView display) {
        display.setEGLContextClientVersion(EglCore.VERSION_GLES2);
        display.setRenderer(cameraSurfaceRender);
        display.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        display.setPreserveEGLContextOnPause(true);
        displayView = display;
    }

    /**
     * Handles for encoding thread event loop.
     */
    private static class EncoderHandler extends Handler {
        private WeakReference<RunnableVideoEncoder> encoderRef;

        public EncoderHandler(RunnableVideoEncoder encoder) {
            encoderRef = new WeakReference<RunnableVideoEncoder>(encoder);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            RunnableVideoEncoder encoder = encoderRef.get();
            if (encoder == null) {
                return;
            }

            switch (what) {
                case MSG_SURFACE_CREATED:
                    encoder.handleSurfaceCreated((Integer) obj);
                    break;
                case MSG_FRAME_AVAILABLE:
                    encoder.handleFrameAvailable((SurfaceTexture) obj);
                    break;
                case MSG_RELEASE:
                    encoder.handleRelease();
                    break;
                case MSG_RESET:
                    encoder.handleReset((RecorderSession) obj);
                    break;
                case MSG_SET_DISPLAY_ORIENTATION:
                    encoder.handleSetDisplayOrientation(inputMessage.arg1);
                    break;
                case MSG_ACTIVITY_RESUME:
                    encoder.handleActivityResume();
                    break;
                case MSG_ACTIVITY_PAUSE:
                    encoder.handleActivityPause();
                    break;
                case MSG_STOP_RECORDING:
                    encoder.handleStopRecording();
                    break;
                case MSG_CAMERA_PREPARED:
                    encoder.handleCameraPrepared();
                    break;

                default:
                    encoder.handleMessageDefault(inputMessage);
            }
        }
    }

    protected void handleMessageDefault(Message inputMessage) {
        throw new IllegalStateException("Unexpected msg what=" + inputMessage.what);
    }

    /********************
     * operations on SurfaceTexture
     ********************/
    public SurfaceTexture getSurfaceTextureForDisplay() {
        synchronized (surfaceTextureLock) {
            return surfaceTexture;
        }
    }

    public boolean isSurfaceTextureReadyForDisplay() {
        synchronized (surfaceTextureLock) {
            return surfaceTexture != null;
        }
    }

    /******************** Recorder operations from user ********************/

    /**
     * Hook for Host Activity's onPause()
     * Called on UI thread
     */
    public void onHostActivityPaused() {
        synchronized (recordFrameLock) {
            isHostActivityPresent = false;

            Log.d(TAG, "recorder life trace -- > onHostActivityPaused: isRecording = " + isRecording);

            // Pause the GLSurfaceView's Renderer thread
            if (displayView != null)
                displayView.onPause();
            // Release camera if we're not recording
            if (!isRecording && isSurfaceTextureReadyForDisplay()) {

//                Log.d(TAG, "camera trace -- > onHostActivityPaused: cameraHolder is null ? "
//                        + (cameraHolder == null));

                handler.removeMessages(MSG_ACTIVITY_PAUSE);
                handler.removeMessages(MSG_ACTIVITY_RESUME);

                handler.sendEmptyMessage(MSG_ACTIVITY_PAUSE);
            }
        }
    }

    /**
     * Hook for Host Activity's onResume()
     * Called on UI thread
     */
    public void onHostActivityResumed() {
        synchronized (recordFrameLock) {
            isHostActivityPresent = true;
            Log.d(TAG, "recorder life trace -- > onHostActivityResumed: isRecording = " + isRecording);
            // Resume the GLSurfaceView's Renderer thread
            if (displayView != null)
                displayView.onResume();
            // Re-open camera if we're not recording and the SurfaceTexture has already been created
            if (!isRecording && surfaceTexture != null) {

//                Log.d(TAG, "camera trace -- > onHostActivityResumed: cameraHolder is null ? "
//                        + (cameraHolder == null));

                handler.removeMessages(MSG_ACTIVITY_PAUSE);
                handler.removeMessages(MSG_ACTIVITY_RESUME);

                handler.sendEmptyMessage(MSG_ACTIVITY_RESUME);
            }
        }
    }

    public void reset(RecorderSession config) {


        Log.d(TAG, "recorder life trace -- > reset: enter !!!");
//        if (encoderState != STATE.UNINITIALIZED) {
//            Log.d(TAG, "recorder life trace -- > reset: encoderState = " + encoderState);
//            throw new IllegalArgumentException("reset called in invalid state");
//        }
//        encoderState = STATE.RESETTING;
//        Log.d(TAG, "cameraEncoderState trace -- > reset: encoEderState = " + encoderState);

        if (!isRecordThreadRunning) {
            throw new IllegalArgumentException("encoder thread is shutdown!");
        }

        handler.sendMessage(handler.obtainMessage(MSG_RESET, config));
    }

    public void startRecording() {

        Log.d(TAG, "recorder life trace -- > startRecording: enter !!!");

        if (!isEncoderPrepared) {
            throw new IllegalArgumentException("encoder thread is shutdown!");
        }

//        if (encoderState != STATE.INITIALIZED) {
//            return;
//        }
        synchronized (recordFrameLock) {
            isRecording = true;
//            encoderState = STATE.RECORDING;
//            Log.d(TAG, "cameraEncoderState trace -- > startRecording: encoEderState = " + encoderState);
        }
    }

    public void stopRecording() {
        Log.d(TAG, "recorder life trace -- > stopRecording: enter !!!");

//        if (encoderState != STATE.RECORDING)
//            throw new IllegalStateException("StopRecording called in invalid state !! encoderState = " + encoderState);


        if (isRecording) {
            handler.sendEmptyMessage(MSG_STOP_RECORDING);
        }
    }

    public void release() {
        Log.d(TAG, "encoder life trace -- > release: enter !!! ");

        if (isRecordThreadRunning) {
            handler.sendMessage(handler.obtainMessage(MSG_RELEASE));
        }
    }

    /********************
     * Camera operations from user
     ********************/

    public void setDisplayOrientation(int orientation) {
        handler.removeMessages(MSG_SET_DISPLAY_ORIENTATION);
//        if (cameraHolder == null) {
//            throw new RuntimeException("cameraHolder is unexpected released.");
//        }
        Message msg = handler.obtainMessage(MSG_SET_DISPLAY_ORIENTATION);
        msg.arg1 = orientation;
        msg.sendToTarget();
    }

    /********************
     * Camera operations run on encoding thread
     ********************/

    protected void startCameraPreview() {

        if (isCameraPrepared) {
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
            isPreviewing = true;
        }


    }

    protected void stopCameraPreview() {
        if (isCameraPrepared) {

            if (isPreviewing) {
                camera.stopPreview();
            }
        }
        isPreviewing = false;
    }

    public void handleSetDisplayOrientation(int orientation) {

        Log.d(TAG, "orientation trace -- > handleSetDisplayOrientation: "
                + ", orientation = " + orientation + ", isRecording = " + isRecording + ", isEncoderPrepared = " + isEncoderPrepared);

//        if (encoderState == STATE.UNINITIALIZED) {
//            return;
//        }

        if (!isRecordThreadRunning) {
            throw new IllegalArgumentException("encoder thread is shutdown!");
        }

        this.screenOrentation = orientation;

        if (recorderSession.useOrientationHintInMuxer()) {
            if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == orientation || ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == orientation) {
                recorderSession.setOrientationHint(0);
            } else {
                recorderSession.setOrientationHint(90);
            }
        } else {
            if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == orientation || ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == orientation) {
                recorderSession.setOrientationHint(0);
            } else {
                recorderSession.setOrientationHint(0);
            }
        }
        if (!isRecording && isEncoderPrepared) {
//            requestPrepareEncoder();
            prepareEncoder();
        }
    }

    /**
     * OnFrameAvailableListener
     * <p/>
     * Notify when frame from camera is ready.
     */
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//			Log.d(TAG, "render trace -- > onFrameAvailable: enter !!!  ");
            handler.sendMessage(handler.obtainMessage(MSG_FRAME_AVAILABLE, surfaceTexture));
        }
    };

    /********************
     * methods run on encoding thread
     ********************/

    private void handleSurfaceCreated(int textureId) {
        synchronized (surfaceTextureLock) {
            Log.d(TAG, "recorder life trace -- > handleSurfaceCreated: surfaceTexture is null ? "
                    + (surfaceTexture != null));

            if (surfaceTexture != null) {
                // TODO test
                releaseSurfaceTexture();
                releaseEncoder();
            }

            initSurfaceTexture(textureId);
            requestPrepareEncoder();

        }
    }

    private void handleCameraPrepared() {
        requestPrepareEncoder();
    }

    private void requestPrepareEncoder() {
        if (!isCameraPrepared || !isHostActivityPresent || null == surfaceTexture) {
            return;
        }
        startCameraPreview();
        prepareEncoder();
    }

    private void initSurfaceTexture(int textureId) {
        this.textureId = textureId;
        surfaceTexture = new SurfaceTexture(this.textureId);
        surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
    }

    private void releaseSurfaceTexture() {
        if (null != surfaceTexture) {
            surfaceTexture.setOnFrameAvailableListener(null);
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    private void handleActivityResume() {
        isHostActivityPresent = true;

        Log.d(TAG, "camera trace -- > handleActivityResume: encoderState = ");

//        if (encoderState == STATE.INITIALIZING) {
//            requestPrepareEncoder();
//        }

        if (!isEncoderPrepared) {
            requestPrepareEncoder();
        }

    }

    private void handleStopRecording() {
        Log.d(TAG, "recorder life trace -- > handleStopRecording: requestStopRecorder");

        try {
            if (frameCount4FPS > 50) {
                Log.d(TAG, "fps trace -- > handleStopRecording: video duration =  " + fps + ", frameCount4FPS = "
                        + frameCount4FPS);

                if (null != recorderStateChangedListener) {
                    recorderStateChangedListener.onCheckFPS(fps);
                }
            }
        } catch (Throwable e) {
            // do nothing
        }
        fps = 0;
        recoderStartTS4FPS = -1;
        frameCount4FPS = 0;

        videoEncoder.signalEndOfStream();
        videoEncoder.drainEncoder(true);
        isRecording = false;
//        encoderState = STATE.UNINITIALIZED;

        releaseEncoder();
        if (!isHostActivityPresent) {
            stopCameraPreview();
        }

//        Log.d(TAG, "cameraEncoderState trace -- > handleFrameAvailable: encoEderState = " + encoderState);
    }

    private void handleActivityPause() {
        isHostActivityPresent = false;

        Log.d(TAG, "camera trace -- > handleActivityPause: enter !!!");

//		if (encoderState == STATE.INITIALIZED || encoderState == STATE.UNINITIALIZED) {
//			releaseCamera();
//			releaseEncoder();
//		}

        if (!isRecording) {
            stopCameraPreview();
            releaseEncoder();
        }
    }

    private void prepareEncoder() {

        try {
            Log.d(TAG, "encoder life trace -- > prepareEncoder: enter !!! videoEncoder is null ? "
                    + (null == videoEncoder));

            if (null != videoEncoder) {
                releaseEncoder();
            }

            Log.d(TAG, "startup trace -- > prepareEncoder : time -- " + (System.currentTimeMillis() - openTime));

            int videoWidth = 0;
            int videoHeight = 0;

            if (recorderSession.useOrientationHintInMuxer()) {
                videoWidth = recorderSession.getVideoWidth();
                videoHeight = recorderSession.getVideoHeight();
            } else if (this.screenOrentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || this.screenOrentation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                videoWidth = recorderSession.getVideoWidth();
                videoHeight = recorderSession.getVideoHeight();
            } else {
                videoWidth = recorderSession.getVideoHeight();
                videoHeight = recorderSession.getVideoWidth();
            }

            Log.d(TAG, "orientation trace -- > prepareEncoder: videoWidth = " + videoWidth + ", videoHeight = "
                    + videoHeight);

            videoEncoder = new CoreVideoEncoder(videoWidth, videoHeight, recorderSession.getVideoBitrate(), recorderSession.getMuxer());
            videoEncoder.reset();
            videoEncoder.setOnFileSizeChangedListener(new CoreVideoEncoder.OnFileSizeChangedListener() {
                @Override
                public void onFileSizeChanged(long fileSize) {
                    if (null != recorderStateChangedListener) {
                        recorderStateChangedListener.onFileSizeChanged(fileSize);
                    }
                }
            });
            if (eglCore == null) {
                // This is the first prepare called for this RunnableVideoEncoder instance
                eglCore = new EglCore(eglState.eglContext, EglCore.FLAG_RECORDABLE);
            }
            if (inputWindowSurface != null)
                inputWindowSurface.release();
            inputWindowSurface = new WindowSurface(eglCore, videoEncoder.getInputSurface());
            inputWindowSurface.makeCurrent();


            prepareVideoEcodeFrame();
            isEncoderPrepared = true;
            GLES20.glViewport(0, 0, videoWidth, videoHeight);
//            encoderState = STATE.INITIALIZED;

            if (null != recorderStateChangedListener) {
                recorderStateChangedListener.onRecorderInitialized();
            }
        } catch (Throwable t) {
            Log.e(TAG, "failed to create video encoder !!! ");

            if (null != recorderStateChangedListener) {
                recorderStateChangedListener.onCameraEncoderError();
            }
        }
    }

    protected void prepareVideoEcodeFrame() {
        Log.d(TAG, "render trace -- > prepareVideoEcodeFrame:  enter !!! ");
        if (videoFrameRender.isInitialized()) {
            videoFrameRender.release();
        }

        Size inputsize = camera.getPreviewSize();
        int inputwidth = inputsize.width;
        int inputheight = inputsize.height;
        if (this.screenOrentation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && this.screenOrentation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            inputwidth = inputsize.height;
            inputheight = inputsize.width;
        }

        videoFrameRender.updateInputSize(inputwidth, inputheight);


        int videoWidth = 0;
        int videoHeight = 0;

        if (this.screenOrentation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || this.screenOrentation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            videoWidth = recorderSession.getVideoWidth();
            videoHeight = recorderSession.getVideoHeight();
        } else {
            videoWidth = recorderSession.getVideoHeight();
            videoHeight = recorderSession.getVideoWidth();
        }

        videoFrameRender.updateOutputSize(videoWidth, videoHeight);
        videoFrameRender.setOrientationHint(-recorderSession.getOrientationHint());
        videoFrameRender.init();
    }

    private void releaseEncoder() {
        Log.d(TAG, "encoder life trace -- > releaseEncoder: enter !!!");
        Log.d(TAG, "render trace -- > releaseEncoder:  isEncoderPrepared =  " + isEncoderPrepared);

        if (isEncoderPrepared) {

            Log.d(TAG, "startup trace -- > releaseEncoder : time -- " + (System.currentTimeMillis() - openTime));

            isEncoderPrepared = false;
            if (null != videoEncoder) {
                videoEncoder.release();
                videoEncoder = null;
            }

            if (inputWindowSurface != null) {
                inputWindowSurface.release();
                inputWindowSurface = null;
            }

            if (null != videoFrameRender && videoFrameRender.isInitialized()) {
                videoFrameRender.release();
            }

//            if (encoderState == STATE.INITIALIZED) {
//                encoderState = STATE.INITIALIZING;
//            }

            if (null != recorderStateChangedListener) {
                recorderStateChangedListener.onRecorderStopped();
            }
        }
    }

    private void releaseEGL() {
        if (eglCore != null) {
            eglCore.release();
            eglCore = null;
        }
    }

    private void handleFrameAvailable(SurfaceTexture surfaceTexture) {

//		Log.d(TAG, "camera trace -- > handleFrameAvailable: encoderState = " + encoderState);

        try {
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(transformMatrix);

        } catch (Exception e) {
            Log.e(TAG, "failed to update texture !!!");
        }

        if (null != videoFrameRender && videoFrameRender.isInitialized()) {
            videoFrameRender.renderInput(textureId, transformMatrix);
            onPostDrawPreviewFrame(videoFrameRender.getInputFrameTexture(), identityMatrix);
        }

        synchronized (recordFrameLock) {
            if (!isSurfaceTextureReadyForDisplay()) {
                return;
            }
//			Log.d(TAG, "fps trace -- > handleFrameAvailable: start  ");
            if (isRecording && skipCount > 0) {
                --skipCount;
            } else if (isRecording) {

                if (recoderStartTS4FPS < 0) {
                    Log.d(TAG, "fps trace -- > handleFrameAvailable: start  ");
                    recoderStartTS4FPS = System.currentTimeMillis();
                    frameCount4FPS = 0;
                    fps = 0;
                }

                videoEncoder.drainEncoder(false);

                videoFrameRender.renderOutputWithOrientationHint();

//				videoFrameRender.renderOutput();


                inputWindowSurface.setPresentationTime(this.surfaceTexture.getTimestamp());
                inputWindowSurface.swapBuffers();

                ++frameCount4FPS;
                fps = (frameCount4FPS * 1000) / (System.currentTimeMillis() - recoderStartTS4FPS);
            }
        }

        // Signal GLSurfaceView to render
        displayView.requestRender();
    }

    private void handleReset(RecorderSession config) {

        Log.d(TAG, "recorder life trace -- > handleReset: enter!!!");

//        if (encoderState != STATE.RESETTING) {
//            throw new IllegalStateException("handleRelease called in invalid state");
//        }


        skipCount = 2;
        isRecording = false;
        recoderStartTS4FPS = -1;
        frameCount4FPS = 0;
        fps = 0;

        if (null != recorderSession) {
            recorderSession.release();
        }

        recorderSession = config;
        // Make display EGLContext current
        eglState.makeSavedStateCurrent();
//        encoderState = STATE.INITIALIZING;

        requestPrepareEncoder();
//        Log.d(TAG, "cameraEncoderState trace -- > handleReset: encoEderState = ");

    }

    private void handleRelease() {

        Log.d(TAG, "recorder life trace -- > handleRelease: enter !!!");

        if (null != videoEncoder) {
            releaseEncoder();
        }

        shutdown();
//        encoderState = STATE.RELEASED;
//        Log.d(TAG, "cameraEncoderState trace -- > handleRelease: encoEderState = " + encoderState);
    }

    /**
     * release everything.
     */
    protected void shutdown() {
//        releaseCamera();
        stopCameraPreview();
        releaseSurfaceTexture();
        releaseEGL();
        if (null != recorderSession) {
            recorderSession.release();
        }
        Looper.myLooper().quit();
    }

    /**
     * GLSurfaceView.Renderer  all methods run on GL render thread.
     */
    private GLSurfaceView.Renderer cameraSurfaceRender = new GLSurfaceView.Renderer() {

        private int textureId = -1;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "startup trace -- > onSurfaceCreated : time -- " + (System.currentTimeMillis() - openTime));
            textureId = GlUtil.createExtTextureObject();
            synchronized (recordThreadStartingLock) {
                if (isRecordThreadRunning) {
                    eglState.saveEGLState();
                    handler.sendMessage(handler.obtainMessage(MSG_SURFACE_CREATED, textureId));
                    onPostSurfaceCreated(eglState.eglContext, textureId);
                }
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "orentation trace -- > onSurfaceChanged : left = " + width + ", height = " + height);
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (RunnableVideoEncoder.this.isSurfaceTextureReadyForDisplay()) {
                if (null != videoFrameRender && videoFrameRender.isInitialized()) {
                    videoFrameRender.renderOutput();

//					videoFrameRender.renderOutputWithOrientationHint();
                }
            }
        }
    };

    protected void onPostSurfaceCreated(EGLContext eglContext, int textureId) {

    }

    protected void onPostDrawPreviewFrame(int textureId, float[] texMatrix) {

    }

    private PreviewableStateChangedListener previewableStateChangedListener = new PreviewableStateChangedListener() {
        @Override
        public void onPrepared() {
            Log.d(TAG, "camera trace -- > onPrepared !!! ");
            isCameraPrepared = true;
            handler.sendEmptyMessage(MSG_CAMERA_PREPARED);
        }

        @Override
        public void onUnPrepared() {
            Log.d(TAG, "camera trace -- > onUnPrepared !!! ");
            isCameraPrepared = false;
        }
    };

    /********************
     * internal classes and interfaces
     ********************/

    private static class EglState {

        public EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
        public EGLSurface eglReadSurface = EGL14.EGL_NO_SURFACE;
        public EGLSurface eglDrawSurface = EGL14.EGL_NO_SURFACE;
        public EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;

        public void saveEGLState() {
            eglContext = EGL14.eglGetCurrentContext();
            eglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
            eglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
            eglDisplay = EGL14.eglGetCurrentDisplay();
        }

        public void makeSavedStateCurrent() {
            EGL14.eglMakeCurrent(eglDisplay, eglReadSurface, eglDrawSurface, eglContext);
        }

        public void makeNothingCurrent() {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
    }

    public interface CameraEncoderStateChangedListener {
        public void onRecorderInitialized();

        public void onRecorderStopped();

        public void onCameraEncoderError();

        public void onCheckFPS(long fps);

        public void onFileSizeChanged(long fileSize);
    }
}
