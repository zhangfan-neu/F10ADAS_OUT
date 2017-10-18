package com.neusoft.oddc.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * private void startCameraFragment() {
 * CameraFragment fragment = new CameraFragment();
 * FragmentManager fragmentManager = getSupportFragmentManager();
 * FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 * fragmentTransaction.replace(R.id.preview_activity_fragment_container, fragment, CameraFragment.class.getSimpleName());
 * // fragmentTransaction.addToBackStack(null);
 * if (!isFinishing()) {
 * fragmentTransaction.commitAllowingStateLoss();
 * }
 * }
 */
public class CameraFragment extends Fragment implements Camera.PreviewCallback {

    private static final String TAG = CameraFragment.class.getSimpleName();

    public CameraFragmentHandler handler = new CameraFragmentHandler(this);

    private static final int MSG_CAMERA_PREVIEW_STARTED = 1;
    private static final int MSG_CHECK_RECORDER_TIME = 2;

    private Button recordButton;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onCreateView");
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onViewCreated");
        initViews(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onResume");

        isActivityPresent = true;

        cameraHolder.openCamera(this);
        recorder.onHostActivityResumed();

        if (null != handler) {
            handler.sendEmptyMessageDelayed(MSG_CHECK_RECORDER_TIME, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onPause");

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
    public void onStop() {
        super.onStop();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onDetach");
    }

    private void initViews(View view) {
        recordButton = (Button) view.findViewById(R.id.camera_record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickRecordButton();
            }
        });
        previewSurfaceView = (GLSurfaceView) view.findViewById(R.id.camera_glsurface);
        customTrailView = (CustomTrailView) view.findViewById(R.id.custom_trail_view);

        initRecorder();
        setDefaultOrientation();
    }

    private void updateCameraPrepared(boolean isPrepared) {
        // Log.d(TAG, "startup trace -- > updateCameraPrepared : time -- " + (System.currentTimeMillis() - openTime));
        Log.d(TAG, "camera trace -- > updateCameraPrepared : isPrepared = " + isPrepared);
        isCameraPrepared = isPrepared;
    }

    private void handleClickRecordButton() {
        if (null == recorder) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "No available recorder !!!");
            }
            return;
        }
        if (!isCameraPrepared || !isRecorderInitialized || !isActivityPresent) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Camera is not prepared, can't handleClickRecordButton.");
            }
            return;
        }
        if (recorder.isRecording()) {
            stopRecording();
            recordButton.setText("Record");
        } else {
            startRecording();
            recordButton.setText("Stop");
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


    private void setDefaultOrientation() {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activityOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

            cameraHolder.setDisplayOrientation(activityOrientation);

            requestActivityOrientation(activityOrientation);
            recorder.setDisplayOrientation(activityOrientation);
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


    private void requestActivityOrientation(int orientationType) {
        Log.d(TAG, "orentation trace -- > requestActivityOrientation : orientationType = " + orientationType
                + ", ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE = " + ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        activityOrientation = orientationType;
        getActivity().setRequestedOrientation(orientationType);
        updatePreviewDisplaySize();
    }

    private void onCameraPreviewStarted(boolean sizeChanged) {
        updatePreviewDisplaySize();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updatePreviewDisplaySize() {
        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        int windowWidth = metric.widthPixels;
        int windowHeight = metric.heightPixels;

        Log.d(TAG, "orentation trace -- > updatePreviewDisplaySize : windowWidth = " + windowWidth
                + ", windowHeight = " + windowHeight);

        int videoWidth = recorderSession.getVideoWidth();
        int videoHeight = recorderSession.getVideoHeight();

        if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == activityOrientation
                || ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == activityOrientation) {
            videoWidth = recorderSession.getVideoWidth();
            videoHeight = recorderSession.getVideoHeight();
            if (windowHeight > windowWidth) {
                int temp = windowHeight;
                windowHeight = windowWidth;
                windowWidth = temp;
            }
        } else if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == activityOrientation
                || ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT == activityOrientation) {
            videoWidth = recorderSession.getVideoHeight();
            videoHeight = recorderSession.getVideoWidth();
            if (windowHeight < windowWidth) {
                int temp = windowHeight;
                windowHeight = windowWidth;
                windowWidth = temp;
            }
        }

        float windowRadio = ((float) windowWidth) / windowHeight;
        float videoRadio = ((float) videoWidth) / videoHeight;

        int displayWidth = windowWidth;
        int displayHeight = windowHeight;

        if (windowRadio >= videoRadio) {
            displayWidth = (int) (windowHeight * videoRadio);
        } else {
            displayHeight = (int) (windowWidth / videoRadio);
        }

        Log.d(TAG, "orentation trace -- > updatePreviewDisplaySize : displayWidth = " + displayWidth
                + ", displayHeight = " + displayHeight);

       /*
       RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.previewSurfaceView.getLayoutParams();
        layoutParams.width = (displayWidth / 2) * 2;
        layoutParams.height = (displayHeight / 2) * 2;

        if (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE == activityOrientation) {
            layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == activityOrientation) {
            layoutParams.removeRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT == activityOrientation
                || ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT == activityOrientation) {
            layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        this.previewSurfaceView.setLayoutParams(layoutParams);
        */
        previewSurfaceView.setVisibility(View.VISIBLE);
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

    private void onFileSizeChanged(long fileSize) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > onFileSizeChanged fileSize = " + fileSize);
            Log.d(TAG, "recorder State trace -- > onFileSizeChanged Constants.RECORD_VIDEO_MAX_FILE_SIZE = " + Constants.RECORD_VIDEO_MAX_FILE_SIZE);
        }
        if (fileSize > Constants.RECORD_VIDEO_MAX_FILE_SIZE) {
            needRestartRecord = true;
            stopRecording();
        }
    }

    private void handleRecorderStart() {
        recorder.printState();

        if (needRestartRecord) {
            startRecording();
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


    private void onRecorderUnprepared() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- > onRecorderUnprepared: enter ");
        }
        isRecorderInitialized = false;
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

    private void onCameraOpened(CameraProperty cameraProperty) {
        // Log.d(TAG, "startup trace -- > onCameraOpened : time -- " + (System.currentTimeMillis() - openTime));
        cameraHolder.configCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "CameraFragment onPreviewFrame data lenth = " + data.length);
    }

    private static class RecorderStateListenerImpl implements RecorderStateListener {

        private WeakReference<CameraFragment> owner = null;

        public RecorderStateListenerImpl(CameraFragment activity) {
            owner = new WeakReference<CameraFragment>(activity);
        }

        @Override
        public void onRecorderPrepared() {
            Log.d(TAG, "recorder event trace -- > onRecorderPrepared : enter");
            if (null != owner) {
                final CameraFragment fragment = owner.get();
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
                CameraFragment activity = owner.get();
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
                CameraFragment activity = owner.get();
                if (null != activity) {
                    activity.onRecorderStopped(fps);
                }
            }
        }

        @Override
        public void onRecorderError(final int errorcode, Object data) {
            final CameraFragment activity = owner.get();
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
            final CameraFragment activity = owner.get();
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
        private WeakReference<CameraFragment> activityRef;

        public CameraStateChangedListenerImpl(CameraFragment activity) {
            this.activityRef = new WeakReference<CameraFragment>(activity);
        }

        @Override
        public void onCameraOpened(final CameraProperty cameraProperty) {
            Log.d(TAG, "camera trace -- > onCameraOpened !!!");

            final CameraFragment activity = activityRef.get();
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

            final CameraFragment activity = activityRef.get();
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

            final CameraFragment activity = activityRef.get();
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
            final CameraFragment activity = activityRef.get();
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

            final CameraFragment activity = activityRef.get();
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

    private static class CameraFragmentHandler extends WeakHandler<CameraFragment> {
        public CameraFragmentHandler(CameraFragment owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraFragment owner = getOwner();
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
