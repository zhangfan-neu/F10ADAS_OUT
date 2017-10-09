package com.neusoft.oddc.activity;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.R;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.multimedia.gles.render.VideoFrameRender;
import com.neusoft.oddc.multimedia.recorder.CameraHolder;
import com.neusoft.oddc.multimedia.recorder.CameraProperty;
import com.neusoft.oddc.multimedia.recorder.CameraStateChangedListener;
import com.neusoft.oddc.multimedia.recorder.RecorderStateListener;
import com.neusoft.oddc.multimedia.recorder.base.AndroidRecorder;
import com.neusoft.oddc.multimedia.recorder.base.RecorderSession;
import com.neusoft.oddc.ui.CustomTrailView;
import com.neusoft.oddc.widget.WeakHandler;

import java.lang.ref.WeakReference;

import static com.neusoft.oddc.entity.Constants.TRACE_LIFECYCLE_FOR_CAMERA;

public class PreviewActivity1 extends BaseActivity implements Camera.PreviewCallback, View.OnClickListener {

    private static final String TAG = PreviewActivity1.class.getSimpleName();

    private Button mainBtn;

    // For camera
    public PreviewActivityHandler handler = new PreviewActivityHandler(this);
    private static final int MSG_CAMERA_PREVIEW_STARTED = 1;
    private static final int MSG_CHECK_RECORDER_TIME = 2;
    private CameraHolder cameraHolder;
    private GLSurfaceView previewSurfaceView;
    private AndroidRecorder recorder = null;
    private RecorderSession recorderSession = null;
    private CameraStateChangedListener cameraStateChangedListener;
    private int activityOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private volatile boolean isRecorderInitialized = false;
    private boolean isActivityPresent = false;
    private boolean isCameraPrepared = false;
    private boolean needRestartRecord = true;
    private long startRecordTime = -1;
    private CustomTrailView customTrailView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onCreate");

        // setTitle(R.string.title_dvr_adas);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_preview1);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onResume");

        isActivityPresent = true;
        cameraHolder.openCamera(this);
        recorder.onHostActivityResumed();
        if (null != handler) {
            handler.sendEmptyMessageDelayed(MSG_CHECK_RECORDER_TIME, 1000);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onPause");

        if (recorder.isRecording()) {
            stopRecording();
        }
        cameraHolder.closeCamera();
        recorder.onHostActivityPaused();
        isActivityPresent = false;
        if (null != handler) {
            handler.removeMessages(MSG_CHECK_RECORDER_TIME);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onStop");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onDestroy");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onPreviewFrame data lenth = " + data.length);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "PreviewActivity onTouchEvent event = " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.preview_activity_back_btn:
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initViews() {
        mainBtn = (Button) findViewById(R.id.preview_activity_back_btn);
        mainBtn.setOnClickListener(this);

        // For camera
        previewSurfaceView = (GLSurfaceView) findViewById(R.id.camera_glsurface);
        customTrailView = (CustomTrailView) findViewById(R.id.custom_trail_view);

        initRecorder();
        setDefaultOrientation();
    }

    private void initRecorder() {
        if (null != recorder) {
            recorder.release();
            recorder = null;
        }

        recorderSession = new RecorderSession();

        recorderSession.setOutputVideoSize(RecorderSession.VIDEO_WIDTH_720P, RecorderSession.VIDEO_HEIGHT_720P);


        cameraHolder = new CameraHolder(CameraHolder.RUN_IN_UI_THREAD);
        recorder = new AndroidRecorder(recorderSession, new VideoFrameRender(), new RecorderStateListenerImpl(this), cameraHolder);

        cameraStateChangedListener = new CameraStateChangedListenerImpl(this);
        cameraHolder.setCameraStateChangedListener(cameraStateChangedListener);


        recorder.setPreviewDisplay(previewSurfaceView);
    }

    private void setDefaultOrientation() {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activityOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            cameraHolder.setDisplayOrientation(activityOrientation);
            recorder.setDisplayOrientation(activityOrientation);
        }
    }

    private void onRecorderUnprepared() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > onRecorderUnprepared: enter ");
        }
        isRecorderInitialized = false;
    }


    private void onRecorderPrepared() {
        // Log.d(TAG, "startup trace -- > onRecorderPrepared : time -- " + (System.currentTimeMillis() - openTime));
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- >  onRecorderPrepared ");
        }
        isRecorderInitialized = true;

        if (null != handler) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleRecorderStart();
                }
            });
        }
    }

    private void handleRecorderStart() {
        recorder.printState();

        if (needRestartRecord) {
            startRecording();
        }
    }

    private void startRecording() {
        Log.d(TAG, "recorder life trace -- > startRecording: recorder.isRecording() ? " + recorder.isRecording());
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > startRecording: recorder.isRecording() ? " + recorder.isRecording()
                    + ", isRecorderInitialized = " + isRecorderInitialized);
        }

        if (!isActivityPresent || recorder.isRecording() || !isRecorderInitialized) {
            return;
        }
        doStartRecording();
    }

    private void doStartRecording() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder life trace -- > doStartRecording: recorder.isRecording() ? " + recorder.isRecording());
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > doStartRecording: recorder.isRecording() ? " + recorder.isRecording()
                    + ", isRecorderInitialized = " + isRecorderInitialized);
        }

        if (!isActivityPresent || recorder.isRecording() || !isRecorderInitialized) {
            return;
        }

        if (isActivityPresent && !recorder.isRecording()) {
            needRestartRecord = false;
            startRecordTime = System.currentTimeMillis();
            recorder.startRecording();
        }
    }

    private void onRecorderStopped(final long fps) {
        if (null != handler) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleRecorderStopped(fps);
                }
            });
        }
    }

    private void handleRecorderStopped(long fps) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "fps trace -- > handleRecorderStopped: video duration =  " + fps);
        }
//        long duration = stopRecordTime - startRecordTime;
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, "duration trace -- > handleRecorderStopped: video duration =  " + duration);
//        }
        if (isActivityPresent) {
            resetRecorderSession();
        }
    }

    private void resetRecorderSession() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- >resetRecorderSession");
        }
        startRecordTime = -1;
        cameraHolder.setDisplayOrientation(activityOrientation);
        recorderSession = new RecorderSession();
        recorderSession.setOutputVideoSize(RecorderSession.VIDEO_WIDTH_720P, RecorderSession.VIDEO_HEIGHT_720P);
        recorder.reset(recorderSession);
    }

    private void onFileSizeChanged(long fileSize) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > onFileSizeChanged fileSize = " + fileSize);
            Log.d(TAG, "recorder State trace -- > onFileSizeChanged Constants.RECORD_VIDEO_MAX_FILE_SIZE = " + Constants.RECORD_VIDEO_MAX_FILE_SIZE);
        }
        if (fileSize > Constants.RECORD_VIDEO_MAX_FILE_SIZE) {
            // needRestartRecord = true;
            // stopRecording();
        }
    }

    private void stopRecording() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder life trace -- > stopRecording: recorder.isRecording() ? " + recorder.isRecording());
        }

        if (recorder.isRecording()) {
            isRecorderInitialized = false;
            startRecordTime = -1;
            recorder.stopRecording();
        }
    }

    private void onCameraOpened(CameraProperty cameraProperty) {
        // Log.d(TAG, "startup trace -- > onCameraOpened : time -- " + (System.currentTimeMillis() - openTime));
        cameraHolder.configCamera();
    }

    private void updateCameraPrepared(boolean isPrepared) {
        // Log.d(TAG, "startup trace -- > updateCameraPrepared : time -- " + (System.currentTimeMillis() - openTime));
        Log.d(TAG, "camera trace -- > updateCameraPrepared : isPrepared = " + isPrepared);
        isCameraPrepared = isPrepared;
    }

    private void onCameraPreviewStarted(boolean sizeChanged) {
        // updatePreviewDisplaySize();
    }

    public void hideLoading() {
//        if (null != loadingView) {
//            loadingView.setVisibility(View.GONE);
//        }
    }

    public void checkRecordTime() {
        if (null != handler) {
            handler.sendEmptyMessageDelayed(MSG_CHECK_RECORDER_TIME, 1000);
        }
        if (startRecordTime > 0) {
            long duration = System.currentTimeMillis() - startRecordTime;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "recorder State trace -- > checkRecordTime duration = " + duration);
            }
            if (duration > Constants.RECORD_VIDEO_MAX_DURATION) {
                needRestartRecord = true;
                stopRecording();
            }
        }
    }

    private static class RecorderStateListenerImpl implements RecorderStateListener {

        private WeakReference<PreviewActivity1> owner = null;

        public RecorderStateListenerImpl(PreviewActivity1 activity) {
            owner = new WeakReference<PreviewActivity1>(activity);
        }

        @Override
        public void onRecorderPrepared() {
            Log.d(TAG, "recorder event trace -- > onRecorderPrepared : enter");
            if (null != owner) {
                final PreviewActivity1 fragment = owner.get();
                if (null != fragment) {

                    fragment.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            fragment.onRecorderPrepared();
                        }
                    });

                }
            }
        }

        @Override
        public void onRecorderUnprepared() {
            Log.d(TAG, "recorder event trace -- > onRecorderUnprepared : enter");

            if (null != owner) {
                PreviewActivity1 activity = owner.get();
                if (null != activity) {
                    activity.onRecorderUnprepared();
                }
            }
        }

        @Override
        public void onStartRecording(long ts) {
            Log.d(TAG, "recorder event trace -- > RSL onStartRecording : enter");
        }

        @Override
        public void onStopRecording(long ts, long fps) {
            Log.d(TAG, "recorder event trace -- > RSL onStopRecording : enter");
            if (null != owner) {
                PreviewActivity1 activity = owner.get();
                if (null != activity) {
                    activity.onRecorderStopped(fps);
                }
            }
        }

        @Override
        public void onRecorderError(final int errorcode, Object data) {
            final PreviewActivity1 activity = owner.get();
            if (activity == null) {
                return;
            }
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    //  activity.handleRecorderError(errorcode);
                }
            });
        }

        @Override
        public void onFileSizeChanged(final long fileSize) {
            final PreviewActivity1 activity = owner.get();
            if (activity == null) {
                return;
            }
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.onFileSizeChanged(fileSize);
                }
            });
        }
    }

    public static class CameraStateChangedListenerImpl implements CameraStateChangedListener {
        private WeakReference<PreviewActivity1> activityRef;

        public CameraStateChangedListenerImpl(PreviewActivity1 activity) {
            this.activityRef = new WeakReference<PreviewActivity1>(activity);
        }

        @Override
        public void onCameraOpened(final CameraProperty cameraProperty) {
            Log.d(TAG, "camera trace -- > onCameraOpened !!!");

            final PreviewActivity1 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Log.d(TAG, "recorder event trace -- > onCameraPrepared : cameraState = " + cameraProperty);
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.onCameraOpened(cameraProperty);
                }
            });
        }

        @Override
        public void onCameraPrepared(CameraProperty cameraProperty) {
            Log.d(TAG, "camera trace -- > onCameraPrepared !!!");

            final PreviewActivity1 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Log.d(TAG, "recorder event trace -- > onCameraPrepared : cameraState = " + cameraProperty);

            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.updateCameraPrepared(true);
                }
            });
        }

        @Override
        public void onCameraReleased(CameraProperty cameraProperty) {
            Log.d(TAG, "camera trace -- > onCameraReleased !!!");

            final PreviewActivity1 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.updateCameraPrepared(false);
                }
            });
        }

        @Override
        public void onPreviewStarted(final boolean sizeChanged) {
            final PreviewActivity1 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.onCameraPreviewStarted(sizeChanged);
                }
            });
        }

        @Override
        public void onPreviewStopped() {

        }

        @Override
        public void onCameraError() {

            Log.d(TAG, "recorder event trace -- > video.onCameraError : ");

            final PreviewActivity1 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.handler.post(new Runnable() {
                @Override
                public void run() {
//                    activity.handleRecorderError(0);
                }
            });
        }
    }

    private static class PreviewActivityHandler extends WeakHandler<PreviewActivity1> {
        public PreviewActivityHandler(PreviewActivity1 owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            PreviewActivity1 owner = getOwner();
            if (null == owner) {
                return;
            }
            switch (msg.what) {
                case MSG_CAMERA_PREVIEW_STARTED:
                    owner.hideLoading();
                    break;
                case MSG_CHECK_RECORDER_TIME:
                    owner.checkRecordTime();
                    break;
                default:
                    break;
            }
        }
    }

}
