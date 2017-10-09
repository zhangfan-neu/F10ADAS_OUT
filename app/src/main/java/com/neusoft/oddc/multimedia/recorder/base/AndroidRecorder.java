package com.neusoft.oddc.multimedia.recorder.base;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.IVideoFrameRender;
import com.neusoft.oddc.multimedia.recorder.Previewable;
import com.neusoft.oddc.multimedia.recorder.RecorderStateListener;

import java.lang.ref.WeakReference;

/**
 * Wrapper of Video encoder and Audio encoder.
 */
public class AndroidRecorder {

    private static final String TAG = AndroidRecorder.class.getSimpleName();

    public static final int RECORDER_ERROR_CAMERA_ENCODER = 1;
    public static final int RECORDER_ERROR_MICRPPHONE = 2;
    public static final int RECORDER_ERROR_MICRPPHONE_ENCODER = 3;

    public static final long NO_VIDEO = -1;

    protected enum STATE {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED,
        STARTING,
        RECORDING,
        STOPPING
    }

    protected volatile STATE recorderState = STATE.UNINITIALIZED;

    protected RecorderSession recorderSession;
    protected RunnableVideoEncoder cameraEncoder;
    protected RunnableAudioEncoder microphoneEncoder;

    protected RunnableVideoEncoder.CameraEncoderStateChangedListener cameraEncoderStateChangedListener = null;
    protected RunnableAudioEncoder.MicrophoneEncoderStateListener microphoneEncoderStateListener = null;

    protected RecorderStateListener recorderStateListener = null;

    protected Muxer.MuxerStateListener muxerStateListener = null;

    protected volatile boolean isMicphonePrepared = false;
    protected volatile boolean isVideoEncoderPrepared = false;
    protected volatile boolean isAudioEncoderPrepared = false;
    protected volatile boolean isMuxerStarted = false;
    protected volatile long videoStartTS = NO_VIDEO;
    protected volatile long videoEndTS = NO_VIDEO;

    protected long fps = -1;

    public AndroidRecorder(RecorderSession session, IVideoFrameRender frameRender,
                           RecorderStateListener recorderStateListener, Previewable camera) {

        this.recorderState = STATE.INITIALIZING;

        this.recorderStateListener = recorderStateListener;
        createCameraEncoder(session, frameRender, camera);
        createMicrophoneEncoder(session);
        this.recorderSession = session;
        this.videoStartTS = NO_VIDEO;
        this.videoEndTS = NO_VIDEO;
        this.fps = -1;

        Muxer muxer = recorderSession.getMuxer();
        muxerStateListener = new MuxerStateChangedListener(this);
        muxer.setMuxerListener(muxerStateListener);
    }

    protected void createCameraEncoder(RecorderSession session, IVideoFrameRender frameRender, Previewable camera) {
        this.cameraEncoderStateChangedListener = new CameraEncoderStateChangedListenerImpl(this);
        this.cameraEncoder = new RunnableVideoEncoder(session, frameRender, cameraEncoderStateChangedListener, camera);
    }

    protected void createMicrophoneEncoder(RecorderSession session) {
        this.microphoneEncoderStateListener = new AudioStateListener(this);
        this.microphoneEncoder = new RunnableAudioEncoder(session, this.microphoneEncoderStateListener);
    }

    public synchronized void startRecording() {

        Log.d(TAG, "recorder event trace -- > startRecording : recorderState = " + recorderState);

        if (recorderState != STATE.INITIALIZED) {
            throw new IllegalStateException("startRecording called in invalid state !! recorderState = "
                    + recorderState);
        }

        this.recorderState = STATE.STARTING;
        cameraEncoder.startRecording();
        microphoneEncoder.startRecording();
    }

    public synchronized void stopRecording() {

        Log.d(TAG, "recorder event trace -- > stopRecording : recorderState = " + recorderState);

        if (recorderState != STATE.STARTING && recorderState != STATE.RECORDING) {
            throw new IllegalStateException("StopRecording called in invalid state !! recorderState = "
                    + recorderState);
        }

//        isMicphonePrepared = false;
//        isVideoEncoderPrepared = false;
//        isAudioEncoderPrepared = false;
//        isMuxerStarted = false;

        this.recorderState = STATE.STOPPING;
        cameraEncoder.stopRecording();
        microphoneEncoder.stopRecording();
    }

    public synchronized boolean isRecording() {
        return this.recorderState == STATE.STARTING || this.recorderState == STATE.RECORDING;
    }

    public void release() {
        if (isRecording()) {
            stopRecording();
        }
        cameraEncoder.release();
        microphoneEncoder.release();
    }

    public synchronized void reset(RecorderSession config) {

        Log.d(TAG, "recorder event trace -- > reset : recorderState = " + recorderState);

//        if (recorderState != STATE.UNINITIALIZED) {
//            throw new IllegalStateException("StopRecording called in invalid state !! recorderState = "
//                    + recorderState);
//        }

        this.recorderState = STATE.INITIALIZING;
        this.cameraEncoder.reset(config);
        this.microphoneEncoder.reset(config);
        this.recorderSession = config;
        this.videoStartTS = NO_VIDEO;
        this.videoEndTS = NO_VIDEO;
        this.fps = -1;

        Muxer muxer = recorderSession.getMuxer();
        muxerStateListener = new MuxerStateChangedListener(this);
        muxer.setMuxerListener(muxerStateListener);
    }

    public void setPreviewDisplay(GLSurfaceView glSurfaceView) {
        if (null != cameraEncoder) {
            cameraEncoder.setPreviewDisplay(glSurfaceView);
        }
    }

    public void setDisplayOrientation(int orientation) {
        cameraEncoder.setDisplayOrientation(orientation);
    }

    public void onHostActivityPaused() {

        Log.d(TAG, "recorder event trace -- > onHostActivityPaused : recorderState = " + recorderState);

        cameraEncoder.onHostActivityPaused();
        microphoneEncoder.onHostActivityPaused();
    }

    public void onHostActivityResumed() {

        Log.d(TAG, "recorder event trace -- > onHostActivityResumed : recorderState = " + recorderState);

        cameraEncoder.onHostActivityResumed();
        microphoneEncoder.onHostActivityResumed();
    }

    private synchronized void onCameraEncoderInit() {
        Log.d(TAG, "recorder event trace -- > onCameraEncoderInit going to check recorder prepared!");
        if (this.recorderState != STATE.STOPPING) {
            this.isVideoEncoderPrepared = true;
            checkRecorderPrepared();
        }

    }

    private synchronized void onCameraEncoderStopped() {
        this.isVideoEncoderPrepared = false;
        if (recorderState == STATE.STOPPING) {
            checkRecorderStopped();
        } else {
            Log.d(TAG, "recorder event trace -- > onCameraEncoderStopped going to check recorder prepared!");
            checkRecorderPrepared();
        }

    }

    private synchronized void onCameraEncoderError() {
        if (null != recorderStateListener) {
            recorderStateListener.onRecorderError(RECORDER_ERROR_CAMERA_ENCODER, null);
        }

    }

    private synchronized void onCheckFPS(long fps) {
        this.fps = fps;
    }

    private synchronized void onFileSizeChanged(long fileSize) {
        if (null != recorderStateListener) {
            recorderStateListener.onFileSizeChanged(fileSize);
        }
    }

    private synchronized void onMuxerStarted(long ts) {
        this.isMuxerStarted = true;
        this.videoStartTS = ts;

        if (this.recorderState == STATE.STARTING) {
            this.recorderState = STATE.RECORDING;
            if (null != recorderStateListener) {
                recorderStateListener.onStartRecording(ts);
            }
        }
    }

    private synchronized void onMuxerStopped(long ts) {

        this.isMuxerStarted = false;
        this.videoEndTS = ts;

        checkRecorderStopped();

    }

    private synchronized void checkRecorderStopped() {
        Log.d(TAG, "recorder event trace -- > checkRecorderStopped : isAudioEncoderPrepared = "
                + isAudioEncoderPrepared
                + ", isVideoEncoderPrepared = " + isVideoEncoderPrepared
                + ", isMuxerStarted = " + isMuxerStarted
                + ", videoStartTS = " + videoStartTS
                + ", recorderState = " + recorderState);

        if (!isAudioEncoderPrepared && !isVideoEncoderPrepared
                && !isMuxerStarted) {
            if (this.recorderState == STATE.STOPPING) {
                this.recorderState = STATE.UNINITIALIZED;
                if (null != recorderStateListener) {

                    long ts = videoStartTS < 0l ? NO_VIDEO : videoEndTS;

                    Log.d(TAG, "recorder event trace -- > checkRecorderStopped : ts = " + ts);

                    recorderStateListener.onStopRecording(ts, fps);
                }
            } else {
                this.recorderState = STATE.INITIALIZING;
            }
        }
    }

    public void printState() {
        Log.d(TAG, "aaaaaa recorder State trace -- > checkRecorderPrepared : isVideoEncoderPrepared = "
                + isVideoEncoderPrepared
                + ", isMicphonePrepared = " + isMicphonePrepared
                + ", isAudioEncoderPrepared = " + isAudioEncoderPrepared
                + ", recorderState = " + recorderState);
    }

    private synchronized void checkRecorderPrepared() {

        Log.d(TAG, "recorder event trace -- > checkRecorderPrepared : isVideoEncoderPrepared = "
                + isVideoEncoderPrepared
                + ", isMicphonePrepared = " + isMicphonePrepared
                + ", isAudioEncoderPrepared = " + isAudioEncoderPrepared
                + ", recorderState = " + recorderState);

        Log.d(TAG, "recorder State trace -- > checkRecorderPrepared : isVideoEncoderPrepared = "
                + isVideoEncoderPrepared
                + ", isMicphonePrepared = " + isMicphonePrepared
                + ", isAudioEncoderPrepared = " + isAudioEncoderPrepared
                + ", recorderState = " + recorderState);

        if (isVideoEncoderPrepared
                && isMicphonePrepared
                && isAudioEncoderPrepared) {

            if (this.recorderState == STATE.INITIALIZING) {
                this.recorderState = STATE.INITIALIZED;

                if (this.recorderState == STATE.INITIALIZED && null != recorderStateListener) {
                    recorderStateListener.onRecorderPrepared();
                }
            }
        } else {
            if (this.recorderState == STATE.INITIALIZED) {
                this.recorderState = STATE.INITIALIZING;

                if (null != recorderStateListener) {
                    recorderStateListener.onRecorderUnprepared();
                }
            }
        }
    }

    private synchronized void onMicPrepared() {

        this.isMicphonePrepared = true;
        Log.d(TAG, "recorder event trace -- > onMicPrepared going to check recorder prepared!");
        checkRecorderPrepared();
    }

    private synchronized void onMicReleased() {
        this.isMicphonePrepared = false;
        Log.d(TAG, "recorder event trace -- > onMicReleased going to check recorder prepared!");
        checkRecorderPrepared();
    }

    private synchronized void onMicrophoneEncoderInit() {
        if (this.recorderState != STATE.STOPPING) {
            this.isAudioEncoderPrepared = true;
            Log.d(TAG, "recorder event trace -- > onMicrophoneEncoderInit going to check recorder prepared!");
            checkRecorderPrepared();
        }
    }

    private synchronized void onMicrophoneError() {
        if (null != recorderStateListener) {
            recorderStateListener.onRecorderError(RECORDER_ERROR_MICRPPHONE, null);
        }
    }

    private synchronized void onMicrophoneEncoderStopped() {
        this.isAudioEncoderPrepared = false;
        if (recorderState == STATE.STOPPING) {
            checkRecorderStopped();
        } else {
            Log.d(TAG, "recorder event trace -- > onMicrophoneEncoderStopped going to check recorder prepared!");
            checkRecorderPrepared();
        }
    }

    private synchronized void onAudioEncoderError() {
        if (null != recorderStateListener) {
            recorderStateListener.onRecorderError(RECORDER_ERROR_MICRPPHONE_ENCODER, null);
        }
    }

    private static class MuxerStateChangedListener implements Muxer.MuxerStateListener {

        private WeakReference<AndroidRecorder> recorderRef;

        public MuxerStateChangedListener(AndroidRecorder recorder) {
            this.recorderRef = new WeakReference<AndroidRecorder>(recorder);
        }

        @Override
        public void onMuxerStarted(long ts) {
            Log.d(TAG, "recorder event trace -- > video.onMuxerStarted : ts = " + ts);

            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMuxerStarted(ts);
        }

        @Override
        public void onMuxerStopped(long ts) {
            Log.d(TAG, "recorder event trace -- > video.onMuxerStopped : ts = " + ts);

            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMuxerStopped(ts);
        }
    }

    protected static class CameraEncoderStateChangedListenerImpl
            implements RunnableVideoEncoder.CameraEncoderStateChangedListener {
        private WeakReference<AndroidRecorder> recorderRef = null;

        public CameraEncoderStateChangedListenerImpl(AndroidRecorder recorder) {
            recorderRef = new WeakReference<AndroidRecorder>(recorder);
        }

        @Override
        public void onRecorderInitialized() {
            Log.d(TAG, "recorder event trace -- > video.onRecorderInitialized : camera encoder prepared !!!");
            if (null != recorderRef) {
                AndroidRecorder recorder = recorderRef.get();
                if (null != recorder) {
                    recorder.onCameraEncoderInit();
                }
            }
        }

        @Override
        public void onRecorderStopped() {
            Log.d(TAG, "recorder event trace -- > video.onRecorderStopped : camera encoder stopped !!!");
            if (null != recorderRef) {
                AndroidRecorder recorder = recorderRef.get();
                if (null != recorder) {
                    recorder.onCameraEncoderStopped();
                }
            }
        }

        @Override
        public void onCameraEncoderError() {
            Log.d(TAG, "recorder event trace -- > video.onCameraEncoderError  !!!");
            if (null != recorderRef) {
                AndroidRecorder recorder = recorderRef.get();
                if (null != recorder) {
                    recorder.onCameraEncoderError();
                }
            }
        }

        @Override
        public void onCheckFPS(long fps) {
            Log.d(TAG, "recorder event trace -- > video.onCheckFPS :   fps = " + fps);
            if (null != recorderRef) {
                AndroidRecorder recorder = recorderRef.get();
                if (null != recorder) {
                    recorder.onCheckFPS(fps);
                }
            }
        }

        @Override
        public void onFileSizeChanged(long fileSize) {
            // Log.d(TAG, "recorder event trace -- > video.onFileSizeChanged : fileSize = " + fileSize);
            if (null != recorderRef) {
                AndroidRecorder recorder = recorderRef.get();
                if (null != recorder) {
                    recorder.onFileSizeChanged(fileSize);
                }
            }
        }
    }

    private static class AudioStateListener implements RunnableAudioEncoder.MicrophoneEncoderStateListener {
        private WeakReference<AndroidRecorder> recorderRef = null;

        public AudioStateListener(AndroidRecorder recorder) {
            recorderRef = new WeakReference<AndroidRecorder>(recorder);
        }

        @Override
        public void onMicrophonePrepared() {
            Log.d(TAG, "recorder event trace -- > audio.onMicrophonePrepared : enter !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMicPrepared();
        }

        @Override
        public void onMicrophoneReleased() {
            Log.d(TAG, "recorder event trace -- > audio.onMicrophoneReleased : enter !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMicReleased();
        }

        @Override
        public void onMicrophoneError() {
            Log.d(TAG, "recorder event trace -- > audio.onMicrophoneError !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMicrophoneError();
        }

        @Override
        public void onRecorderPrepared() {
            Log.d(TAG, "recorder event trace -- > audio.onRecorderPrepared : microphone encoder prepared !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMicrophoneEncoderInit();
        }

        @Override
        public void onRecorderStoped() {
            Log.d(TAG, "recorder event trace -- > audio.onRecorderStoped : microphone encoder stopped !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onMicrophoneEncoderStopped();
        }

        @Override
        public void onAudioEncoderError() {
            Log.d(TAG, "recorder event trace -- > audio.onAudioEncoderError !!!");
            final AndroidRecorder recorder = recorderRef.get();
            if (recorder == null) {
                return;
            }
            recorder.onAudioEncoderError();
        }
    }

}
