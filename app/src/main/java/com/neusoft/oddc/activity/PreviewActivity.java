package com.neusoft.oddc.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.neusoft.adas.DasEgoStatus;
import com.neusoft.adas.DasEngine;
import com.neusoft.adas.DasEvents;
import com.neusoft.adas.DasLaneMarkings;
import com.neusoft.adas.DasTrafficEnvironment;
import com.neusoft.adas.DasVehicles;
import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.R;
import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.fragment.RealTimeContinuousDataFragment;
import com.neusoft.oddc.fragment.RealTimeDataFragment;
import com.neusoft.oddc.multimedia.gles.node.FrameAnimationAdapterImpl;
import com.neusoft.oddc.multimedia.gles.node.FrameAnimationNode;
import com.neusoft.oddc.multimedia.gles.render.OverLayVideoFrameRender;
import com.neusoft.oddc.multimedia.gles.render.VideoFrameRender;
import com.neusoft.oddc.multimedia.recorder.CameraHolder;
import com.neusoft.oddc.multimedia.recorder.CameraProperty;
import com.neusoft.oddc.multimedia.recorder.CameraStateChangedListener;
import com.neusoft.oddc.multimedia.recorder.RecorderStateListener;
import com.neusoft.oddc.multimedia.recorder.base.AndroidRecorder;
import com.neusoft.oddc.multimedia.recorder.base.RecorderSession;
import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.neusoft.JobManager;
import com.neusoft.oddc.ui.CustomTrailView;
import com.neusoft.oddc.widget.DateHelper;
import com.neusoft.oddc.widget.PreferencesUtils;
import com.neusoft.oddc.widget.Utils;
import com.neusoft.oddc.widget.WeakHandler;
import com.neusoft.oddc.widget.eventbus.EventStopDataCollection;
import com.neusoft.oddc.widget.realtimedata.RealTimeDataDrawer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.neusoft.oddc.entity.Constants.TRACE_LIFECYCLE_FOR_CAMERA;

public class PreviewActivity extends BaseActivity implements Camera.PreviewCallback, View.OnClickListener {

    private static final String TAG = PreviewActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private Button mainBtn;
    private RealTimeDataFragment realTimeDataFragment;
    // For debug
    private Button debugBtn;
    private View continuousDataLayout;
    private RealTimeContinuousDataFragment realTimeContinuousDataFragment;

    private ImageView custom_real_data_view;
    private RealTimeDataDrawer realTimeDataDrawer;
    private FrameAnimationAdapterImpl animationAdapter;
    private boolean renderMode = true;

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
    private boolean needReset = false;
    private boolean isCameraPrepared = false;
    private boolean needRestartRecord = true;
    private long startRecordTime = -1;
    private CustomTrailView customTrailView;
    private String fileOutputPath = "";
    private NeusoftHandler nsfh;
    private ADASHelper adasHelper;

    Paint paint;
    Canvas canvas;
    private Bitmap bitmap;
    private Button jobButton;
    ImageView recordingIcon;

    private int screenWidth;
    private int screenHeight;

    public static PreviewActivity getInstance()
    {
        return instance;
    }

    private static PreviewActivity instance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onCreate");

        instance = this;

        // setTitle(R.string.title_dvr_adas);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_preview);
        Context context = getApplicationContext();
        nsfh = new NeusoftHandler();
        adasHelper = new ADASHelper();

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        screenWidth = 2220;
        Log.d(TAG + "Jiehunt", "screenWidth is " + screenWidth + "screenHeight is " + screenHeight);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onStart");

        // Register EventBus
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onResume");

        adasHelper.restartobd2thread();
        isActivityPresent = true;
        cameraHolder.openCamera(this);
        if (needReset) {
            resetRecorderSession();
            needReset = false;
        }
        recorder.onHostActivityResumed();
        if (null != handler) {
            Log.d(TAG, "handler start to check recorder time ");
            handler.sendEmptyMessageDelayed(MSG_CHECK_RECORDER_TIME, 1000);
        }
    }


    @Override
    protected void onPause() {
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onPause");

        if (recorder.isRecording()) {
            needRestartRecord = true;
            stopRecording();
            adasHelper.stopobd2thread();
        }
        cameraHolder.closeCamera();
        recorder.onHostActivityPaused();
        isActivityPresent = false;
        if (null != handler) {
            handler.removeMessages(MSG_CHECK_RECORDER_TIME);
        }

        super.onPause();
    }


    @Override
    protected void onStop() {
        adasHelper.stopobd2thread();

        // Unregister EventBus
        EventBus.getDefault().unregister(this);

        super.onStop();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onStop");
    }


    @Override
    protected void onDestroy() {
        adasHelper.stopobd2thread();
        super.onDestroy();
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onDestroy");
    }

    private int leftDis = 0;
    private int rightDis = 0;
    private int yDistance = 0;
    boolean leftevent = false;
    boolean rightevent = false;
    boolean ttcevent = false;
    boolean gShockEvent = false;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, TRACE_LIFECYCLE_FOR_CAMERA + "PreviewActivity onPreviewFrame data length = " + data.length);

        // ADAS
        DasEvents dasEvents = null;
        DasTrafficEnvironment dasTrafficEnvironment = null;
        if (MyApplication.ADAS_OK) {
            DasEgoStatus dasEgoStatus = new DasEgoStatus();
            dasEgoStatus.setTimestamp(System.currentTimeMillis());

            dasEgoStatus.setModuleRunTable(3);
//            Log.d("Jiehunt", "dasEgoStatus.getModuleRunTable() is " + dasEgoStatus.getModuleRunTable());
            dasEgoStatus.setSpeedStatus(2);
            int spd = adasHelper.getspd();
            // Used for testing FCW Event
            // spd = 50 ;
            dasEgoStatus.setSpeed(spd);


//            dasEgoStatus.setSpeedStatus();
//            dasEgoStatus.setSpeed();
//            dasEgoStatus.setSteeringStatus();
//            dasEgoStatus.setSteeringAngle();
//            dasEgoStatus.setAccelerationStatus();
//            dasEgoStatus.setCarLampStatus();
//            dasEgoStatus.setWindscreenWiperStatus();
//            dasEgoStatus.setWeatherCondition();
//            dasEgoStatus.setLightCondition();

            int res;
            res = DasEngine.process(data, dasEgoStatus);
            Log.d(TAG, "ADAS DasEngine.process res is  " + res);

            // ADAS Get results
            dasEvents = new DasEvents();
            dasTrafficEnvironment = new DasTrafficEnvironment();

            res = DasEngine.getResults(dasEvents, dasTrafficEnvironment);
//            Log.d(TAG, "ADAS DasEngine.getResults res is "+ res);

            if (null != customTrailView) {
                customTrailView.setDasTrafficEnvironment(dasTrafficEnvironment);
            }

            Context context = getApplicationContext();
            if (dasEvents.parseEventCode(dasEvents.OFF_ROAD_LEFT_SOLID_EVENT) || dasEvents.parseEventCode(dasEvents.OFF_ROAD_LEFT_DASH_EVENT)) {
                Log.d(TAG, "ADAS Event OFF_ROAD_LEFT_EVENT");
                Toast.makeText(context, "OFF_ROAD_LEFT_SOLID_EVENT", Toast.LENGTH_LONG).show();
                leftevent = true;
            } else {
                leftevent = false;
            }
            if (dasEvents.parseEventCode(dasEvents.OFF_ROAD_RIGHT_SOLID_EVENT) || dasEvents.parseEventCode(dasEvents.OFF_ROAD_RIGHT_DASH_EVENT)) {
                Log.d(TAG, "ADAS Event OFF_ROAD_RIGHT_EVENT");
                Toast.makeText(context, "OFF_ROAD_RIGHT_EVENT", Toast.LENGTH_LONG).show();
                rightevent = true;
            } else {
                rightevent = false;
            }
            if (dasEvents.parseEventCode(dasEvents.FORWARD_TTC_COLLISION_EVENT) || dasEvents.parseEventCode(dasEvents.FORWARD_HEADWAY_COLLISION_EVENT)) {
                Log.d(TAG, "ADAS Event FORWARD_TTC_COLLISION_EVENT");
                Toast.makeText(context, "FORWARD_TTC_COLLISION_EVENT", Toast.LENGTH_LONG).show();
                ttcevent = true;
            } else {
                ttcevent = false;
            }
        }

        // ODDC
        if (NeusoftHandler.isOddcOk) {
            String filename = fileOutputPath.substring(fileOutputPath.lastIndexOf("/") + 1, fileOutputPath.length());
            double accelerationX = adasHelper.getAccelerometerX();
            double accelerationY = adasHelper.getAccelerometerY();
            double accelerationZ = adasHelper.getAccelerometerZ();

            ContinuousData continuousData = nsfh.mkContinuousData(filename, accelerationX, accelerationY, accelerationZ);

            if(continuousData == null)
            {
                return;
            }

            // G Force Event
            double gx = accelerationX / 9.81;
            double gy = accelerationY / 9.81;
            double gz = accelerationZ / 9.81;
            double gForce = Math.sqrt((gx * gx) + (gy * gy) + (gz * gz));
            //continuousData.gShockEventValue = gForce;
            Log.d(TAG, "gForce value = " + gForce);
            if (gForce > Constants.G_THRESH_VALUE) {
                continuousData.gShockEvent = true;
                gShockEvent = true;
            } else {
                gShockEvent = false;
            }
            continuousData.gShockEventThreshold = Constants.G_THRESH_VALUE;

            // ADAS related
            if (null != dasTrafficEnvironment) {
                DasLaneMarkings dasLaneMarkings = dasTrafficEnvironment.getLaneMarkings();
//                long ldwTimestamp = dasLaneMarkings.getTimestamp();
                DasLaneMarkings.DasEgoLane dasEgoLane = dasLaneMarkings.getDasEgoLane();
                leftDis = dasEgoLane.getLeftDis();
                continuousData.ldwDistanceToLeftLane = leftDis;
                rightDis = dasEgoLane.getRightDis();
                continuousData.ldwDistanceToRightLane = rightDis;
                if (leftevent || rightevent) continuousData.ldwEvent = true;

                /*if (leftevnet) {
                    continuousData.ldwEventType = 1;
                } else if (rightevnet) {
                    continuousData.ldwEventType = 2;
                }*/

                DasVehicles dasVehicles = dasTrafficEnvironment.getVehicles();
                if (dasVehicles.getNums() > 0) {
                    for (int i = 0; i < dasVehicles.getNums(); i++) {
                        DasVehicles.DasVehicle dasVehicle = dasVehicles.getVehicleByIndex(i);
                        if (null != dasVehicle) {
                            int x = dasVehicle.getXDistance();
                            if (Math.abs(x) < 1700) {
                                yDistance = dasVehicle.getYDistance();
                                if (yDistance < 120 * 1000) {
                                    continuousData.fcwDistanceToFV = yDistance;
                                    continuousData.fcwExistFV = true;
                                    continuousData.fcwRelativeSpeedToFV = dasVehicle.getLongVelocity();
                                    break;
                                } else {
                                    yDistance = 0;
                                    continuousData.fcwDistanceToFV = yDistance;
                                }
                            }
                        }
                    }
                }
                if (ttcevent) {
                    continuousData.fcwCutIn = true;
                    continuousData.fcwEvent = true;
                }
            }

            if (BuildConfig.DEBUG) {
                if (null != realTimeContinuousDataFragment) {
                    realTimeContinuousDataFragment.updateUI(continuousData);
                }
            }

            boolean continueRecording = nsfh.postContinuousData(continuousData);
            // Log.d(TAG, "oddc trace -> continueRecording = " + continueRecording);
            if (!continueRecording) {
                stopRecording();
                needRestartRecord = false;
            }
        }

        if (null != realTimeDataDrawer) {
            String spdStr = "" + adasHelper.getspd();
            String vechicleIDStr = ADASHelper.getvin();
            String latitudeStr = "";
            String longitudeStr = "";
            Location location = adasHelper.getCoarseLocation();
            if (null != location) {
                latitudeStr = Utils.formatValueToStr(location.getLatitude());
                longitudeStr = Utils.formatValueToStr(location.getLongitude());
            }
            String accelXStr = Utils.formatValueToStr(adasHelper.getAccelerometerX());
            String accelYStr = Utils.formatValueToStr(adasHelper.getAccelerometerY());
            String accelZStr = Utils.formatValueToStr(adasHelper.getAccelerometerZ());
            realTimeDataDrawer.setTopInfoStrings(spdStr, vechicleIDStr, latitudeStr, longitudeStr,
                    accelXStr, accelYStr, accelZStr);
            realTimeDataDrawer.setDasTrafficEnvironment(dasTrafficEnvironment);


            String recTimeStr = DateHelper.getEventTime(startRecordTime);
            Log.d(TAG, "recTimeStr = " + recTimeStr);
            if (ttcevent) {
                realTimeDataDrawer.addEvents(getString(R.string.rtdd_date_event3) + recTimeStr);
            }
            if (leftevent) {
                realTimeDataDrawer.addEvents(getString(R.string.rtdd_date_event1) + recTimeStr);
            }
            if (rightevent) {
                realTimeDataDrawer.addEvents(getString(R.string.rtdd_date_event2) + recTimeStr);
            }
            if (gShockEvent) {
                realTimeDataDrawer.addEvents(getString(R.string.rtdd_date_event4) + recTimeStr);
            }
        }


        if (renderMode) {
            if (null != custom_real_data_view) {
                custom_real_data_view.setImageBitmap(null);
                custom_real_data_view.setVisibility(View.GONE);
            }
            if (null != animationAdapter) {
                animationAdapter.setRenderADAS(true);
                animationAdapter.setRealTimeDataDrawer(realTimeDataDrawer);
            }
        } else {
            if (null != custom_real_data_view) {
                custom_real_data_view.setVisibility(View.VISIBLE);
                Bitmap realTimeDataBitmap = realTimeDataDrawer.drawDataView();
                if (null != realTimeDataBitmap && !realTimeDataBitmap.isRecycled()) {
                    custom_real_data_view.setImageBitmap(realTimeDataBitmap);
                }
            }
            if (null != animationAdapter) {
                animationAdapter.setRenderADAS(false);
            }
        }

    }

    private static String getTimestamp(){
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS Z" );
        Date date = new Date();
        return dateFormat.format(date);
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
            case R.id.preview_activity_debug_btn:
                try {
                    drawerLayout.openDrawer(Gravity.START);
                    drawerLayout.openDrawer(Gravity.END);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.preview_activity_job_btn:
                JobManager jobManager = JobManager.getInstance();
                if(recorder != null && !recorder.isRecording())
                {
                    if(jobManager != null)
                    {
                        jobManager.setAdasEnabled(true);
                    }
                    beginRecording();
                }
                else
                {
                    if(jobManager != null)
                    {
                        jobManager.setAdasEnabled(false);
                    }
                    endRecording();
                }
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
        renderMode =  PreferencesUtils.getBoolean(this, SettingDvrSettingActivity.KEY_PREF_RENDER_OVERLAY, true);
        realTimeDataDrawer = new RealTimeDataDrawer(PreviewActivity.this, screenWidth, screenHeight);
        custom_real_data_view = (ImageView) findViewById(R.id.custom_real_data_view);

        realTimeDataFragment = (RealTimeDataFragment) getSupportFragmentManager().findFragmentById(R.id.preview_activity_fragment_real_time_data);

        // For camera
        previewSurfaceView = (GLSurfaceView) findViewById(R.id.camera_glsurface);
        customTrailView = (CustomTrailView) findViewById(R.id.custom_trail_view);

        mainBtn = (Button) findViewById(R.id.preview_activity_back_btn);
        mainBtn.setOnClickListener(this);

        recordingIcon = (ImageView) findViewById(R.id.recordingIcon);
        if(recordingIcon != null)
        {
            recordingIcon.setVisibility(View.GONE);
        }

        initRecorder();
        setDefaultOrientation();

        // For debug
        if (BuildConfig.DEBUG)
        {
            debugBtn = (Button) findViewById(R.id.preview_activity_debug_btn);
            debugBtn.setVisibility(View.VISIBLE);
            debugBtn.setOnClickListener(this);
            continuousDataLayout = findViewById(R.id.preview_activity_fragment_real_time_continuous_data_layout);
            continuousDataLayout.setVisibility(View.VISIBLE);
            realTimeContinuousDataFragment = (RealTimeContinuousDataFragment) getSupportFragmentManager().findFragmentById(R.id.preview_activity_fragment_real_time_continuous_data);

            //NOTE: Disabling start/stop button because we require a sessionID which can only be obtained
            //      with a valid connection to the server.  Forcing the ADAS/DVR system to start can introduce
            //      bad data.
//            jobButton = (Button)findViewById(R.id.preview_activity_job_btn);
//            if(jobButton != null)
//            {
//                jobButton.setVisibility(View.VISIBLE);
//                if(recorder != null && !recorder.isRecording())
//                {
//                    jobButton.setText(getString(R.string.start));
//                }
//                else
//                {
//                    jobButton.setText(getString(R.string.stop));
//                }
//
//                jobButton.setOnClickListener(this);
//            }
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.preview_activity_drawerlayout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

/*
        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
*/
    }


    private void initRecorder() {
        if (null != recorder) {
            recorder.release();
            recorder = null;
        }

        recorderSession = new RecorderSession();
        fileOutputPath = recorderSession.getOutputPath();
        recorderSession.setOutputVideoSize(RecorderSession.VIDEO_WIDTH_720P, RecorderSession.VIDEO_HEIGHT_720P);

        cameraHolder = new CameraHolder(CameraHolder.RUN_IN_UI_THREAD);
        OverLayVideoFrameRender render = new OverLayVideoFrameRender();
        animationAdapter = new FrameAnimationAdapterImpl(this, screenWidth, screenHeight);
        FrameAnimationNode animationNode = new FrameAnimationNode();
        animationNode.setFrameAnimationAdapter(animationAdapter);
        render.addOverlay(animationNode);
        recorder = new AndroidRecorder(recorderSession, render, new RecorderStateListenerImpl(this), cameraHolder);

        cameraStateChangedListener = new CameraStateChangedListenerImpl(this);
        cameraHolder.setCameraStateChangedListener(cameraStateChangedListener);


        recorder.setPreviewDisplay(previewSurfaceView);
    }

    private void setDefaultOrientation() {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activityOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            Log.d(TAG, "recorder State trace -- > setDefaultOrientation: activityOrientation = " + activityOrientation);
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

        JobManager jobManager = JobManager.getInstance();
        if(jobManager != null && jobManager.isAdasEnabled())
        {
            beginRecording();
        }
    }

    public void beginRecording()
    {
        JobManager jobManager = JobManager.getInstance();

//        if(BuildConfig.DEBUG)
//        {
//            //NOTE: For testing purposes...We will not check for ADAS enabled because we might want to trigger this with the job button.
//        }
//        else
//        {
//            if(jobManager != null && !jobManager.isAdasEnabled())
//            {
//                return;
//            }
//        }

        if(jobManager != null && !jobManager.isAdasEnabled())
        {
            return;
        }

        if(recorder != null && !recorder.isRecording())
        {
            needRestartRecord = true;
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

        showRecordingIcon();
    }

    private void showRecordingIcon()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                if(jobButton != null)
                {
                    jobButton.setText(getString(R.string.stop));
                }

                if(recordingIcon != null)
                {
                    recordingIcon.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void hideRecordingIcon()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                if(jobButton != null)
                {
                    jobButton.setText(getString(R.string.start));
                }

                if(recordingIcon != null)
                {
                    recordingIcon.setVisibility(View.GONE);
                }
            }
        });
    }

    public void endRecording()
    {
        if(recorder != null && !recorder.isRecording())
        {
            return;
        }

        needRestartRecord = true;
        stopRecording();
        hideRecordingIcon();
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
        adasHelper.restartobd2thread();
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
        Log.d(TAG, "fps trace -- > handleRecorderStopped: isActivityPresent =  " + isActivityPresent);
        if (isActivityPresent) {
            resetRecorderSession();
        } else {
            needReset = true;
        }
    }

    private void resetRecorderSession() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "recorder State trace -- >resetRecorderSession");
        }
        startRecordTime = -1;
        cameraHolder.setDisplayOrientation(activityOrientation);
        recorderSession = new RecorderSession();
        fileOutputPath = recorderSession.getOutputPath();
        recorderSession.setOutputVideoSize(RecorderSession.VIDEO_WIDTH_720P, RecorderSession.VIDEO_HEIGHT_720P);
        recorder.reset(recorderSession);
    }

    private void onFileSizeChanged(long fileSize) {
        if (BuildConfig.DEBUG) {
            // Log.d(TAG, "recorder State trace -- > onFileSizeChanged fileSize = " + fileSize);
            // Log.d(TAG, "recorder State trace -- > onFileSizeChanged Constants.RECORD_VIDEO_MAX_FILE_SIZE = " + Constants.RECORD_VIDEO_MAX_FILE_SIZE);
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
            adasHelper.stopobd2thread();
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
        // Log.d(TAG, "recorder State trace -- > checkRecordTime startRecordTime = " + startRecordTime);
        if (startRecordTime > 0) {
            long duration = System.currentTimeMillis() - startRecordTime;
            if (BuildConfig.DEBUG) {
                // Log.d(TAG, "recorder State trace -- > checkRecordTime duration = " + duration);
            }
            if (duration > Constants.RECORD_VIDEO_MAX_DURATION) {
                needRestartRecord = true;
                stopRecording();
            } else {
                if (null != realTimeDataFragment && !isFinishing()) {
                    realTimeDataFragment.updateTimeUI();
                    realTimeDataFragment.updateRECTime("" + (duration / 1000));

                    Location location = adasHelper.getCoarseLocation();
                    if (null != location) {
                        double mLongitude = location.getLongitude();
                        double mLatitude = location.getLatitude();
                        realTimeDataFragment.updateLatitudeAndLongitude(String.format("%.6f", mLongitude), String.format("%.6f", mLatitude));
                    }


                    float[] mMagnetic = adasHelper.getmMagnetic();
                    float[] mGravity = adasHelper.getmGravity();
                    if (null != mMagnetic && null != mGravity) {
                        realTimeDataFragment.updateGSensor(String.format("%.1f", mGravity[0]), String.format("%.1f", mGravity[1]), String.format("%.1f", mGravity[2]));
//                        realTimeDataFragment.updateMagnetic(String.format("%.0f", mMagnetic[0]), String.format("%.0f", mMagnetic[1]), String.format("%.0f", mMagnetic[2]));
                    }

                    float x = adasHelper.getAccelerometerX();
                    float y = adasHelper.getAccelerometerY();
                    float z = adasHelper.getAccelerometerZ();
//                    realTimeDataFragment.updateGSensor(String.valueOf(x),String.valueOf(y),String.valueOf(z));

                    int spd = adasHelper.getspd();
                    realTimeDataFragment.updateSpeed(String.format("%d", spd));

                    realTimeDataFragment.updateLanDistance(leftDis + " mm", rightDis + " mm");
                    realTimeDataFragment.updateVehicleDistance(yDistance / 1000 + " m");
                }
            }
        }
    }

    private static class RecorderStateListenerImpl implements RecorderStateListener {

        private WeakReference<PreviewActivity> owner = null;

        public RecorderStateListenerImpl(PreviewActivity activity) {
            owner = new WeakReference<PreviewActivity>(activity);
        }

        @Override
        public void onRecorderPrepared() {
            Log.d(TAG, "recorder event trace -- > onRecorderPrepared : enter");
            if (null != owner) {
                final PreviewActivity fragment = owner.get();
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
                PreviewActivity activity = owner.get();
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
                PreviewActivity activity = owner.get();
                if (null != activity) {
                    activity.onRecorderStopped(fps);
                }
            }
        }

        @Override
        public void onRecorderError(final int errorcode, Object data) {
            final PreviewActivity activity = owner.get();
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
            final PreviewActivity activity = owner.get();
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
        private WeakReference<PreviewActivity> activityRef;

        public CameraStateChangedListenerImpl(PreviewActivity activity) {
            this.activityRef = new WeakReference<PreviewActivity>(activity);
        }

        @Override
        public void onCameraOpened(final CameraProperty cameraProperty) {
            Log.d(TAG, "camera trace -- > onCameraOpened !!!");

            final PreviewActivity activity = activityRef.get();
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

            final PreviewActivity activity = activityRef.get();
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

            final PreviewActivity activity = activityRef.get();
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
            final PreviewActivity activity = activityRef.get();
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

            final PreviewActivity activity = activityRef.get();
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

    private static class PreviewActivityHandler extends WeakHandler<PreviewActivity> {
        public PreviewActivityHandler(PreviewActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            PreviewActivity owner = getOwner();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventStopDataCollection eventStopDataCollection) {
        finish();
    }

}
