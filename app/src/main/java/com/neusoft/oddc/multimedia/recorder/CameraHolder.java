package com.neusoft.oddc.multimedia.recorder;

import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static android.hardware.Camera.CameraInfo;
import static android.hardware.Camera.getCameraInfo;
import static android.hardware.Camera.getNumberOfCameras;
import static com.neusoft.oddc.entity.Constants.DESIRED_FRAME_RATE;

/**
 * This is used to hold an android.hardware.Camera instance.
 * The open() and release() calls are similar to the ones in android.hardware.Camera.
 * The convenience is that we do not need to pass Camera between function
 * We can easily get camera in any function, and they are the same
 * So it is easy to operate and manage the camera
 */
public class CameraHolder implements Previewable {
    private static final String TAG = CameraHolder.class.getSimpleName();

    public static final int RUN_IN_CREATOR_THREAD = 0;
    public static final int RUN_IN_UI_THREAD = 1;
    public static final int RUN_IN_WORKER_THREAD = 2;

    private static final int SCREEN_ORIENTATION_DEGREE_PORTRAIT = 0;
    private static final int SCREEN_ORIENTATION_DEGREE_REVERSE_LANDSCAPE = 90;
    private static final int SCREEN_ORIENTATION_DEGREE_REVERSE_PORTRAIT = 180;
    private static final int SCREEN_ORIENTATION_DEGREE_LANDSCAPE = 270;

    private static final int MSG_OPEN_CAMERA = 0;
    private static final int MSG_CLOSE_CAMERA = 1;
    private static final int MSG_SET_PREVIEW_TEXTURE = 2;
    private static final int MSG_START_PREVIEW = 3;
    private static final int MSG_STOP_PREVIEW = 4;
    private static final int MSG_SET_DISPLAY_ORIENTATION = 8;
    private static final int MSG_CONFIG_CAMERA = 13;
    private static final int MSG_RELEASE = 9;

    private static final Size DEFAULT_PREVIEW_SIZE = new Size(1280, 720);

    private final Looper creatorLooper;
    private HandlerThread handlerThread;
    private CameraThreadHandler handler;

    private Camera.PreviewCallback previewCallback;

    private int threadMode = RUN_IN_CREATOR_THREAD;

    private static int CAMERA_COUNT = 0;

    private static final int NO_CAMERA = -1;
    private static int FRONT_CAMERA_ID = NO_CAMERA;
    private static int BACK_CAMERA_ID = NO_CAMERA;

    private Size previewSize;
    private int[] fpsRange;

    private Camera cameraDevice;
    private int cameraId = -1; // current camera id

    private int displayOrientationType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

    private CameraStateChangedListener cameraStateChangedListener;

    private final static int RESOLUTION_720_TOLERANCE = 10; // some device, 720p is not exactly equals to 720, may be more or less.
    private final static int DESIRED_MIN_VIDEO_FRAME_RATE = DESIRED_FRAME_RATE * 1000;
    private final static int DESIRED_RESOLUTION_720P_WIDTH = 1280;
    private final static int DESIRED_RESOLUTION_720P_HEIGHT = 720;
    private final static int DESIRED_RESOLUTION_480P_WIDTH = 640;
    private final static int DESIRED_RESOLUTION_480P_HEIGHT = 480;

    // CameraProperty
    private CameraProperty cameraProperty;

    private SurfaceTexture previewTexture;
    private PreviewableStateChangedListener previewableStateChangedListener;
    private volatile boolean isCameraPrepared = false;
    private volatile boolean isPreviewing = false;

    private volatile boolean sizeChanged = false;

    static {
        CAMERA_COUNT = getNumberOfCameras();
        CameraInfo[] cameraInfos = new CameraInfo[CAMERA_COUNT];
        for (int i = 0; i < CAMERA_COUNT; i++) {
            cameraInfos[i] = new CameraInfo();
            getCameraInfo(i, cameraInfos[i]);
        }
        // get the first (smallest) back and first front camera id
        for (int i = 0; i < CAMERA_COUNT; i++) {
            if (BACK_CAMERA_ID == -1
                    && cameraInfos[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                BACK_CAMERA_ID = i;
            } else if (FRONT_CAMERA_ID == -1
                    && cameraInfos[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                FRONT_CAMERA_ID = i;
            }
        }

    }

    private static class CameraThreadHandler extends Handler {

        private WeakReference<CameraHolder> cameraHolderWeakReference;

        public CameraThreadHandler(CameraHolder cameraHolder, Looper looper) {
            super(looper);
            cameraHolderWeakReference = new WeakReference<CameraHolder>(cameraHolder);
        }

        @Override
        public void handleMessage(Message msg) {

            CameraHolder cameraHolder = cameraHolderWeakReference.get();
            if (null == cameraHolder) {
                return;
            }

            switch (msg.what) {
                case MSG_OPEN_CAMERA:
                    cameraHolder.handleOpenCamera(msg.arg1);
                    break;
                case MSG_CLOSE_CAMERA:
                    cameraHolder.handleCloseCamera();
                    break;
                case MSG_SET_PREVIEW_TEXTURE:
                    cameraHolder.handleSetPreviewTexture((SurfaceTexture) msg.obj);
                    break;
                case MSG_START_PREVIEW:
                    cameraHolder.handleStartPreview();
                    break;
                case MSG_STOP_PREVIEW:
                    cameraHolder.handleStopPreview();
                    break;
                case MSG_SET_DISPLAY_ORIENTATION:
                    cameraHolder.handleSetDisplayOrientation(msg.arg1);
                    break;
                case MSG_CONFIG_CAMERA:
                    cameraHolder.handleConfigCamera();
                    break;
                case MSG_RELEASE:
                    cameraHolder.handleRelease();
                    break;
                default:
                    break;

            }

        }
    }

    public CameraHolder() {
        this(RUN_IN_CREATOR_THREAD);
    }

    public CameraHolder(int threadMode) {
        creatorLooper = Looper.myLooper();
        this.cameraId = BACK_CAMERA_ID;
        this.threadMode = threadMode;

        if (RUN_IN_UI_THREAD == threadMode) {
            handler = new CameraThreadHandler(this, Looper.getMainLooper());
        } else if (RUN_IN_CREATOR_THREAD == threadMode) {
            handler = new CameraThreadHandler(this, creatorLooper);
        } else {
            this.handlerThread = new HandlerThread(CameraHolder.TAG + "_" + System.currentTimeMillis());
            this.handlerThread.start();
            handler = new CameraThreadHandler(this, handlerThread.getLooper());
        }

    }

    public void openCamera(Camera.PreviewCallback previewCallback) {
        openCamera(this.cameraId);
        this.previewCallback = previewCallback;
    }

    public void openCamera(int cameraId) {
        handler.removeMessages(MSG_OPEN_CAMERA);
        Message msg = handler.obtainMessage(MSG_OPEN_CAMERA);
        msg.arg1 = cameraId;
        msg.sendToTarget();
    }

    public void closeCamera() {
        handler.removeMessages(MSG_CLOSE_CAMERA);
        Message msg = handler.obtainMessage(MSG_CLOSE_CAMERA);
        msg.sendToTarget();
    }

    @Override
    public void setPreviewableStateChangedListener(PreviewableStateChangedListener listener) {
        this.previewableStateChangedListener = listener;
        if (isCameraPrepared && null != previewableStateChangedListener) {
            previewableStateChangedListener.onPrepared();
        }
    }

    @Override
    public synchronized Size getPreviewSize() {
        return previewSize;
    }


    @Override
    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        handler.removeMessages(MSG_SET_PREVIEW_TEXTURE);
        Message msg = handler.obtainMessage(MSG_SET_PREVIEW_TEXTURE);
        msg.obj = surfaceTexture;
        msg.sendToTarget();
    }

    @Override
    public void startPreview() {
        handler.removeMessages(MSG_START_PREVIEW);
        Message msg = handler.obtainMessage(MSG_START_PREVIEW);
        msg.sendToTarget();
    }

    @Override
    public void stopPreview() {
        handler.removeMessages(MSG_STOP_PREVIEW);
        Message msg = handler.obtainMessage(MSG_STOP_PREVIEW);
        msg.sendToTarget();
    }

    public void setDisplayOrientation(int displayOrientationType) {
        handler.removeMessages(MSG_SET_DISPLAY_ORIENTATION);
        Message msg = handler.obtainMessage(MSG_SET_DISPLAY_ORIENTATION);
        msg.arg1 = displayOrientationType;
        msg.sendToTarget();
    }

    public void configCamera() {
        handler.removeMessages(MSG_CONFIG_CAMERA);
        Message msg = handler.obtainMessage(MSG_CONFIG_CAMERA);
        msg.sendToTarget();
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
        Message msg = handler.obtainMessage(MSG_RELEASE);
        msg.sendToTarget();
    }


    public synchronized void handleOpenCamera(int cameraId) {
        Log.d(TAG, "camera trace -- > handleOpenCamera !!! cameraId = " + cameraId);
        try {
            if (cameraDevice != null) {
                cameraDevice.release();
                cameraDevice = null;
            }
            this.cameraId = cameraId;
            cameraDevice = Camera.open(cameraId);
            if (null != previewCallback) {
                cameraDevice.setPreviewCallback(previewCallback);
            }
            dumpCameraParameters();

            if (null != this.cameraStateChangedListener) {
                cameraStateChangedListener.onCameraOpened(cameraProperty);
            }

            // TODO notify camera opened with camera informations

        } catch (Throwable e) {
            Log.e(TAG, "Open camera error!");
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }
    }

    public synchronized void handleCloseCamera() {
        Log.d(TAG, "camera trace -- > handleCloseCamera !!! null == cameraDevice ? " + (null == cameraDevice));
        if (null != cameraDevice) {

            isCameraPrepared = false;
            handleStopPreview();
            cameraDevice.setPreviewCallback(null);
            cameraDevice.setErrorCallback(null);
            // TODO release other callback
            try {
                cameraDevice.setPreviewTexture(null);
                cameraDevice.setPreviewDisplay(null);
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            }


            cameraProperty = null;
            cameraDevice.release();
            cameraDevice = null;
        }
        if (null != cameraStateChangedListener) {
            cameraStateChangedListener.onCameraReleased(cameraProperty);
        }
        if (null != previewableStateChangedListener) {
            previewableStateChangedListener.onUnPrepared();
        }
    }

    public synchronized void handleStartPreview() {
        Log.d(TAG, "camera trace -- > handleStartPreview !!! null == cameraDevice ? " + (null == cameraDevice));

        try {
            if (null == cameraDevice || !isCameraPrepared) {
                Log.d (TAG, "handleStartPreview : isCameraPrepared is " + isCameraPrepared);
                return;
            }
            cameraDevice.startPreview();
            isPreviewing = true;

            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onPreviewStarted(sizeChanged);
                sizeChanged = false;
            }

        } catch (Throwable e) {
            Log.e(TAG, "start preview error!");
            e.printStackTrace();
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }
    }

    public synchronized void handleStopPreview() {
        Log.d(TAG, "camera trace -- > handleStopPreview");
        try {
            if (null == cameraDevice) {
                return;
            }
            isPreviewing = false;
            cameraDevice.stopPreview();
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onPreviewStopped();
            }
        } catch (Throwable e) {
            Log.e(TAG, "start preview error!");
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }
    }

    public synchronized void handleSetDisplayOrientation(int orientationType) {
        Log.d(TAG, "camera trace -- > setDisplayOrientation in setCameraParameters: cameraId = " + cameraId
                + ", orientationType = " + orientationType);
        try {
            displayOrientationType = orientationType;

            if (null != cameraDevice) {
                Camera.Parameters cameraParameters = cameraDevice.getParameters();
                int targetorientation = getDisplayOrientation(orientation2Degree(displayOrientationType), cameraId);

                Log.d(TAG, "orientation trace -- > setDisplayOrientation in setDisplayOrientation: cameraId = " + cameraId
                        + ", orientation = " + targetorientation);
                cameraDevice.setDisplayOrientation(targetorientation);
                cameraDevice.setParameters(cameraParameters);
            }
        } catch (Throwable e) {
            Log.e(TAG, "setDisplayOrientation error!");
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }

//        updateCameraInfoForFD();
    }

    private synchronized void handleSetPreviewTexture(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "camera trace -- > handleSetPreviewTexture !!! null == cameraDevice ? " + (null == cameraDevice) +
                ", surfaceTexture is null ? " + (null == surfaceTexture));
        try {
            if (null == cameraDevice || !isCameraPrepared) {
                return;
            }
            this.previewTexture = surfaceTexture;

//            if (isPreviewing) {
//                handleStopPreview();
//            }
            cameraDevice.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            throw new RuntimeException("set preview surfaceTexture failed, message is that " + e.getMessage());
        }
    }

    private synchronized void handleConfigCamera() {
        Log.d(TAG, "camera trace -- > handleConfigCamera !!! null == cameraDevice ? " + (null == cameraDevice));

        if (null == cameraDevice) {
            Log.e(TAG, "config camera : camera is not opened !");
            return;
        }


        boolean failed = false;
        try {
            if (adjustPreviewSize() && adjustFPSRange()) {
                setCameraParameters();
            } else {
                failed = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, "failed to config camera parameters !");
            failed = true;
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }

        if (!failed) {
            sizeChanged = true;
            isCameraPrepared = true;

            if (null != this.cameraStateChangedListener) {
                cameraStateChangedListener.onCameraPrepared(cameraProperty);
            }

            if (null != previewableStateChangedListener) {
                previewableStateChangedListener.onPrepared();
            }

        } else {
            if (null != cameraStateChangedListener) {
                cameraStateChangedListener.onCameraError();
            }
        }


    }


    private void dumpCameraParameters() {

        if (null == cameraDevice) {
            Log.e(TAG, "camera trace -- > dumpCameraParameters : camera is null ");
            return;
        }

        Camera.Parameters cameraParameters = cameraDevice.getParameters();

        if (null == cameraParameters) {
            Log.e(TAG, "camera trace -- > dumpCameraParameters : camera is null ");
            return;
        }

        // Camera FOV
        float verticalViewAngle = cameraParameters.getVerticalViewAngle();
        float horizontalViewAngle = cameraParameters.getHorizontalViewAngle();
        double thetaV = Math.toRadians(verticalViewAngle);
        double thetaH = Math.toRadians(horizontalViewAngle);
        Log.d(TAG, "camera trace -- > dumpCameraParameters : verticalViewAngle = " + verticalViewAngle);
        Log.d(TAG, "camera trace -- > dumpCameraParameters : horizontalViewAngle = " + horizontalViewAngle);
        Log.d(TAG, "camera trace -- > dumpCameraParameters : thetaV = " + thetaV);
        Log.d(TAG, "camera trace -- > dumpCameraParameters : thetaH = " + thetaH);

        cameraProperty = new CameraProperty(cameraId);


        Log.d(TAG, "camera trace -- > dumpCameraParameters : camera id = " + cameraId);

        cameraProperty.supportedPreviewSizes = cameraParameters.getSupportedPreviewSizes();
        if (null != cameraProperty.supportedPreviewSizes) {
            for (int i = 0; i < cameraProperty.supportedPreviewSizes.size(); ++i) {
                Camera.Size size = cameraProperty.supportedPreviewSizes.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : previewSizes[" + i + "] = " +
                        "[" + size.width + ", " + size.height + "]  ");
            }
        }

        cameraProperty.supportedPictureSizes = cameraParameters.getSupportedPictureSizes();
        if (null != cameraProperty.supportedPictureSizes) {
            for (int i = 0; i < cameraProperty.supportedPictureSizes.size(); ++i) {
                Camera.Size size = cameraProperty.supportedPictureSizes.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : pictureSizes[" + i + "] = " +
                        "[" + size.width + ", " + size.height + "]  ");
            }
        }

        cameraProperty.supportedFPSRange = cameraParameters.getSupportedPreviewFpsRange();
        if (null != cameraProperty.supportedFPSRange) {
            for (int i = 0; i < cameraProperty.supportedFPSRange.size(); ++i) {
                int[] range = cameraProperty.supportedFPSRange.get(i);
                Log.d(TAG, "camera trace -- > dumpCameraParameters : fpsRange[" + i + "] = " +
                        "[" + range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + ", " + range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] + "]");
            }
        }


    }

    private boolean adjustPreviewSize() {

        int previewWidth = 0;
        int previewHeight = 0;

        if (null != cameraProperty && null != cameraProperty.supportedPreviewSizes) {
            for (int i = 0; i < cameraProperty.supportedPreviewSizes.size(); ++i) {
                Camera.Size size = cameraProperty.supportedPreviewSizes.get(i);
                if (size.width == DESIRED_RESOLUTION_720P_WIDTH && size.height == DESIRED_RESOLUTION_720P_HEIGHT) {
                    previewWidth = size.width;
                    previewHeight = size.height;
                    break;
                }
            }
        }

        if (previewWidth > 0 && previewHeight > 0) {
            previewSize = new Size(previewWidth, previewHeight);
            return true;
        }
        return false;
    }

    private boolean adjustFPSRange() {
        if (null == cameraDevice || null == cameraProperty || null == cameraProperty.supportedFPSRange) {
            return false;
        }

        int[] optimizeFpsRange = new int[]{0, 0};
        int maxFpsDelta = Integer.MAX_VALUE;
        int minFpsDelta = Integer.MAX_VALUE;

        for (int[] fps : cameraProperty.supportedFPSRange) {
            Log.d(TAG, "supported min/max Fps :" + fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + "/"
                    + fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
            int maxFps = fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int minFps = fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int delta = maxFps - DESIRED_MIN_VIDEO_FRAME_RATE;
            if ((maxFps == minFps) && delta >= 0 && delta < maxFpsDelta) {
                optimizeFpsRange = fps;
                maxFpsDelta = delta;
            }
        }

        if (optimizeFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] == 0) {
            Log.d(TAG, "Can not find fixed fps range equals to or larger than " + DESIRED_MIN_VIDEO_FRAME_RATE);
            maxFpsDelta = Integer.MAX_VALUE;
            for (int[] fps : cameraProperty.supportedFPSRange) {
                int maxFps = fps[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                int minFps = fps[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                int deltaMax = maxFps - DESIRED_MIN_VIDEO_FRAME_RATE;
                int deltaMin = minFps - DESIRED_MIN_VIDEO_FRAME_RATE;
                if ((Math.abs(deltaMax) <= Math.abs(maxFpsDelta)) && (Math.abs(deltaMin) <= Math.abs(minFpsDelta))) {
                    optimizeFpsRange = fps;
                    maxFpsDelta = deltaMax;
                    minFpsDelta = deltaMin;
                }
            }
        }

        Log.d(TAG, "target min/max fps is " + optimizeFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + "/"
                + optimizeFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        fpsRange = optimizeFpsRange;
        return true;
    }


    public void handleRelease() {
        handleStopPreview();
        handleCloseCamera();

        if (RUN_IN_UI_THREAD == threadMode) {

        } else if (RUN_IN_CREATOR_THREAD == threadMode) {

        } else {
            this.handlerThread.quit();
        }

    }

    public static int mZoomNumber = 20;

    private void setCameraParameters() {

        if (null == cameraDevice) {
            return;
        }

        Camera.Parameters cameraParameters = cameraDevice.getParameters();
        int orientation = getDisplayOrientation(orientation2Degree(displayOrientationType), cameraId);
        Log.d(TAG, "orientation trace -- > setDisplayOrientation in setCameraParameters: cameraId = " + cameraId
                + ", orientation = " + orientation);
        cameraDevice.setDisplayOrientation(orientation);
        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
        if (fpsRange != null && fpsRange.length >= 2 && fpsRange[0] > 0 && fpsRange[1] > 0) {
            cameraParameters.setPreviewFpsRange(fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        }

        Log.d(TAG, "camera trace -- > setCameraParameters : cameraId = " + cameraId
                + ", orientation = " + orientation + ", previewSize.width = " + previewSize.width
                + ", previewSize.height = " + previewSize.height + ", fpsrange = " + fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
                + ", " + fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        );

        // flashlight
        if (cameraProperty.flashSupported) {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }

        // focus
        if (cameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        if (cameraParameters.isZoomSupported()) {
            cameraParameters.setZoom( mZoomNumber );
            Log.d (TAG, "mZoomNumber is " + mZoomNumber);

        }
        Log.d(TAG, "cameraParameters.getFocalLength() is "+ cameraParameters.getFocalLength());

        cameraDevice.setParameters(cameraParameters);
    }


    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        CameraInfo info = new CameraInfo();
        getCameraInfo(cameraId, info);
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation - degrees + 360) % 360;
            result = (360 - result) % 360;
        } else {  // back-facing camera
            result = (info.orientation + degrees) % 360;
        }
        return result;
    }


    public static int getDefaultCameraId() {
        return BACK_CAMERA_ID;
    }

    public void setCameraStateChangedListener(CameraStateChangedListener cameraStateChangedListener) {
        this.cameraStateChangedListener = cameraStateChangedListener;
        if (null != this.cameraStateChangedListener && null != cameraProperty) {
            cameraStateChangedListener.onCameraOpened(cameraProperty);
        }
    }


    public static int orientation2Degree(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return SCREEN_ORIENTATION_DEGREE_PORTRAIT;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                return SCREEN_ORIENTATION_DEGREE_REVERSE_LANDSCAPE;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                return SCREEN_ORIENTATION_DEGREE_REVERSE_PORTRAIT;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return SCREEN_ORIENTATION_DEGREE_LANDSCAPE;
            default:
                return SCREEN_ORIENTATION_DEGREE_PORTRAIT;
        }
    }


}
