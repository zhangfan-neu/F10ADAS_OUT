package com.neusoft.oddc.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.neusoft.adas.DasEngine;
import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.R;
import com.neusoft.oddc.adapter.MainButtonsAdapter;
import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.db.dbentity.ADASParametersEntity;
import com.neusoft.oddc.db.dbentity.UserProfileEntity;
import com.neusoft.oddc.db.dbentity.VehicleProfileEntity;
import com.neusoft.oddc.db.gen.ADASParametersEntityDao;
import com.neusoft.oddc.db.gen.UserProfileEntityDao;
import com.neusoft.oddc.db.gen.VehicleProfileEntityDao;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.entity.EntityMainButton;
import com.neusoft.oddc.fragment.ErrorDialogFragment;
import com.neusoft.oddc.widget.FileUtil;
import com.neusoft.oddc.widget.PropertyUtil;
import com.neusoft.oddc.widget.StorageUtil;
import com.neusoft.oddc.widget.eventbus.EventStartDataCollection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static int RECORD_PERMISSIONS = BASE_PERMISSIONS + 1;

    private RecyclerView buttonsRecyclerView;
    private MainButtonsAdapter mainButtonsAdapter;

    private TextView main_status_param1; // username
    private TextView main_status_param2; // current vehicle
    private TextView main_status_param5; // number of events
    private TextView main_status_param6; // number of on demand updates
    private TextView main_status_param7; // server connection

    private VehicleProfileEntityDao vehicleProfileEntityDao;
    private UserProfileEntityDao userProfileEntityDao;
    private ADASParametersEntityDao adasParametersEntityDao;

    private int vehicleLength = Constants.DEFAULT_CAR_LENGTH;
    private int vehicleWidth = Constants.DEFAULT_CAR_WIDTH;
    private int vehicleHeight = Constants.DEFAULT_CAR_HEIGHT;

    private NeusoftHandler nsfh;
    private ADASHelper adasHelper;

    private MainButtonsAdapter.OnRecyclerViewItemClickListener mainButtonClickListener = new MainButtonsAdapter.OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(View view, EntityMainButton entity) {
            Class clazz = entity.getClazz();
            if (null != clazz) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "clazz =" + clazz);
                }
                if (PreviewActivity.class == clazz) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "PreviewActivity.class == clazz");
                    }
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RECORD_PERMISSIONS);
                        return;
                    } else {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Camera permission granted");
                        }
                    }
                    //  Check storage
                    if (!StorageUtil.enoughSpace()) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "No enough space!");
                        }
                        Toast.makeText(getApplicationContext(), R.string.main_camera_no_space, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Intent intent = new Intent(MainActivity.this, clazz);
                startActivity(intent);
            } else {
                // do nothing
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "MainActivity onCreate");

        setContentView(R.layout.activity_main);
        setCustomTitle(R.string.title_main);

        initViews();

        userProfileEntityDao = ((MyApplication) getApplication()).getDaoSession().getUserProfileEntityDao();
        vehicleProfileEntityDao = ((MyApplication) getApplication()).getDaoSession().getVehicleProfileEntityDao();
        adasParametersEntityDao = ((MyApplication) getApplication()).getDaoSession().getADASParametersEntityDao();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!needCheckPermission) {
            initData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register EventBus
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        // Unregister EventBus
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();

        // init config property
        PropertyUtil.initCommonProperties();

        initData();


        Context context = getApplicationContext();
        // init ADASHelper
        adasHelper = new ADASHelper(context);
        adasHelper.init(context);
        Log.d("Jiehunt", adasHelper.getvin());

        // init NeusoftHandler
        nsfh = new NeusoftHandler(context);
        nsfh.init(context);


        // init ODDC
        nsfh.startupOddcClass();
        Log.d(TAG, "oddc trace -> initODDC isOddcOk = " + NeusoftHandler.isOddcOk);

        // release ADAS before init
        // int releaseADASResult = nsfh.releaseADAS();
        // Log.d(TAG, "adas trace -> releaseADASResult = " + releaseADASResult);
        // init ADAS
        String key = FileUtil.getADASKey();
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, R.string.main_adas_key_ng, Toast.LENGTH_LONG).show();
        }

        int result = adasHelper.prepareADAS(key);
        if (result == 0) {

            // setup ADAS
            ADASParametersEntity entity = null;
            ADASParametersEntityDao adasParametersEntityDao = ((MyApplication) getApplicationContext()).getDaoSession().getADASParametersEntityDao();
            if (null != adasParametersEntityDao) {
                ArrayList<ADASParametersEntity> list = (ArrayList<ADASParametersEntity>) adasParametersEntityDao.queryBuilder()
                        .where(ADASParametersEntityDao.Properties.Key_user.eq("")).list();
                if (null != list && list.size() > 0) {
                    entity = list.get(0);
                }
            }

            if (null != entity) {
                result = adasHelper.initADAS(entity.getVehicle_length(),
                        entity.getVehicle_width(), entity.getVehicle_height(),
                        entity.getCamera_ht_from_ground(), entity.getCamera_offset_from_center(),
                        entity.getCamera_dist_from_front());
            } else {
                result = adasHelper.initADAS(Constants.DEFAULT_CAR_LENGTH,
                        Constants.DEFAULT_CAR_WIDTH, Constants.DEFAULT_CAR_HEIGHT,
                        Constants.DEFAULT_CAMERA_HEIGHT, Constants.DEFAULT_CAMERA_OFFSET,
                        Constants.DEFAULT_CAMERA_DISTANCEFROMHEAD);
            }

            if (0 == result) {
                MyApplication.ADAS_OK = true;
                Log.d(TAG, "ADAS version = " + DasEngine.getVersion());
                // get the spinner's setting to set here
                if (null != entity) {
                    adasHelper.setupADAS(entity.getLd_sensor_sensitivity(), entity.getFwd_col_sensitivity(), 0);
                } else {
                    adasHelper.setupADAS(Constants.DEFAULT_LDW_SENSITIVIY, Constants.DEFAULT_FCW_SENSITIVIY, 0);
                }
            }


        }

        if (result != 0) {
            MyApplication.ADAS_OK = false;
            Toast.makeText(this, R.string.main_adas_init_error, Toast.LENGTH_LONG).show();
        }
    }

    private void initData() {
        if (null != userProfileEntityDao) {
            ArrayList<UserProfileEntity> list = (ArrayList<UserProfileEntity>) userProfileEntityDao.queryBuilder()
                    .where(UserProfileEntityDao.Properties.Key_user.eq("")).list();
            if (null != list && list.size() > 0) {
                UserProfileEntity entity = list.get(0);
                if (null != main_status_param1) {
                    String username = entity.getUsername();
                    main_status_param1.setText(username);
                }
            } else {
                Log.d(TAG, "getUserProfile : no data");
            }
        }
        if (null != vehicleProfileEntityDao) {
            ArrayList<VehicleProfileEntity> list = (ArrayList<VehicleProfileEntity>) vehicleProfileEntityDao.queryBuilder()
                    .where(VehicleProfileEntityDao.Properties.Key_user.eq("")).list();
            if (null != list && list.size() > 0) {
                VehicleProfileEntity entity = list.get(0);
                if (null != main_status_param2) {
                    String vehicleStr = entity.getYear() + " " + entity.getBrand() + " " + entity.getModel();
                    main_status_param2.setText(vehicleStr);
                }

            } else {
                Log.d(TAG, "getVehicleProfile : no data");
            }
        }
        if (null != adasParametersEntityDao) {
            ArrayList<ADASParametersEntity> list = (ArrayList<ADASParametersEntity>) adasParametersEntityDao.queryBuilder()
                    .where(ADASParametersEntityDao.Properties.Key_user.eq("")).list();
            if (null != list && list.size() > 0) {
                ADASParametersEntity entity = list.get(0);
                vehicleLength = entity.getVehicle_length();
                vehicleWidth = entity.getVehicle_width();
                vehicleHeight = entity.getVehicle_height();
            } else {
                Log.d(TAG, "getADASParameters : no data");
            }
        }
    }

    private void initViews() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        buttonsRecyclerView = (RecyclerView) findViewById(R.id.main_button_recyclerview);
        buttonsRecyclerView.setLayoutManager(layoutManager);
        // buttonsRecyclerView.addItemDecoration(new DefaultDividerDecoration(this));
        mainButtonsAdapter = new MainButtonsAdapter(this);
        mainButtonsAdapter.setOnItemClickListener(mainButtonClickListener);
        buttonsRecyclerView.setAdapter(mainButtonsAdapter);

        initMainButtons();

        main_status_param1 = (TextView) findViewById(R.id.main_status_param1);
        main_status_param2 = (TextView) findViewById(R.id.main_status_param2);
        main_status_param5 = (TextView) findViewById(R.id.main_status_param5);
        main_status_param6 = (TextView) findViewById(R.id.main_status_param6);
        main_status_param7 = (TextView) findViewById(R.id.main_status_param7);
    }


    private void initMainButtons() {
        ArrayList<EntityMainButton> mainButtons = new ArrayList<>();
        mainButtons.add(new EntityMainButton(0, R.string.main_button_data_collection, PreviewActivity.class));
        mainButtons.add(new EntityMainButton(1, R.string.main_button_setting, SettingActivity.class));
        mainButtons.add(new EntityMainButton(2, R.string.main_button_oddc_agent, ODDCAgentActivity.class));
        mainButtons.add(new EntityMainButton(3, R.string.main_button_dvr_playback, DVRPlaybackActivity.class));
        // mainButtons.add(new EntityMainButton(4, R.string.main_button_schedules, SchedulesActivity.class));
        // mainButtons.add(new EntityMainButton(5, R.string.main_button_other, PreviewActivity1.class));
        mainButtonsAdapter.setEntity(mainButtons);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (RECORD_PERMISSIONS == requestCode) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    ErrorDialogFragment.newInstance(new ErrorDialogFragment.Callback() {
                        @Override
                        public void onOkClicked() {

                        }
                    }, getString(R.string.main_camera_permission_hint))
                            .show(getSupportFragmentManager(), ErrorDialogFragment.class.getSimpleName());
                    Toast.makeText(this, R.string.main_camera_permission_hint, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            // All permissions are granted
            Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MainActivity onDestroy");
        // ODDC
        if (null != nsfh) {
            boolean shutdown = nsfh.shutdownOddc();
            Log.d(TAG, "oddc trace -> reqShutdown = " + shutdown);
        }
        // release ADAS
        // if (null != nsfh) {
        //     int releaseADASResult = nsfh.releaseADAS();
        //     Log.d(TAG, "adas trace -> releaseADASResult = " + releaseADASResult);
        // }

        super.onDestroy();

        // For releasing ADAS
//        try {
//            System.exit(0);
//        }catch (Exception e){
//            if(BuildConfig.DEBUG){
//                e.printStackTrace();
//            }
//        }

    }

    private void setNumberOfEvent(String str) {
        if (null != main_status_param5) {
            main_status_param5.setText(str);
        }
    }

    private void setNumberOfOnDemandUpdates(String str) {
        if (null != main_status_param6) {
            main_status_param6.setText(str);
        }
    }

    private void setServerConnection(String str) {
        if (null != main_status_param7) {
            main_status_param7.setText(str);
        }
    }

    // for ODDC class
    public static int getFrameRate() {
        return 30;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventStartDataCollection eventStartDataCollection) {
        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
        startActivity(intent);
    }

}
