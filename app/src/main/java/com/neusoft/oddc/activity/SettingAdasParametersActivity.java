package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.R;
import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.db.dbentity.ADASParametersEntity;
import com.neusoft.oddc.db.gen.ADASParametersEntityDao;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.multimedia.recorder.fov.CameraFOV;
import com.neusoft.oddc.multimedia.recorder.fov.CameraFOVHelper;

import java.util.ArrayList;

public class SettingAdasParametersActivity extends BaseEdittableActivity {

    private static final String TAG = SettingAdasParametersActivity.class.getSimpleName();

    private EditText ap_gps_frequency_edittext;
    private EditText ap_accelerometer_sensitivity_edittext;
    private EditText ap_vehicle_length_edittext;
    private EditText ap_vehicle_width_edittext;
    private EditText ap_vehicle_height_edittext;
    private EditText ap_camear_fov_edittext;
    private EditText ap_wheel_base_edittext;
    private EditText ap_curb_weight_edittext;
    private EditText ap_camera_ht_from_ground_edittext;
    private EditText ap_camera_offset_from_center_edittext;
    private EditText ap_camera_dist_from_front_edittext;

    private Spinner ap_ld_sensor_sensitivity;
    private Spinner ap_fwd_col_sensitivity;
    private ArrayAdapter<String> ld_adapter;
    private ArrayAdapter<String> fwd_adapter;

    private ADASParametersEntityDao adasParametersEntityDao;
    private ADASParametersEntity entity;

    private ADASHelper ADASHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_adas_parameters_profile);

        setCustomTitle(R.string.setting_group_item4);

        initViews();

        ADASHelper = new ADASHelper();
        adasParametersEntityDao = ((MyApplication) getApplication()).getDaoSession().getADASParametersEntityDao();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isEditMode()) {
            getData();
        }

    }

    private void getData() {
        ArrayList<ADASParametersEntity> list = (ArrayList<ADASParametersEntity>) adasParametersEntityDao.queryBuilder()
                .where(ADASParametersEntityDao.Properties.Key_user.eq("")).list();
        if (null != list && list.size() > 0) {
            entity = list.get(0);
            updateEditTextUI(entity);
        } else {
            Log.d(TAG, "getData : no data");
            entity = null;
            // Set default values
            ap_ld_sensor_sensitivity.setSelection(Constants.DEFAULT_LDW_SENSITIVIY);
            ap_fwd_col_sensitivity.setSelection(Constants.DEFAULT_FCW_SENSITIVIY);
            ap_vehicle_length_edittext.setText("" + Constants.DEFAULT_CAR_LENGTH);
            ap_vehicle_width_edittext.setText("" + Constants.DEFAULT_CAR_WIDTH);
            ap_vehicle_height_edittext.setText("" + Constants.DEFAULT_CAR_HEIGHT);
            CameraFOV cameraFOV = CameraFOVHelper.get();
            ap_camear_fov_edittext.setText("H:" + cameraFOV.getHorizontalViewAngle() + "/" + "V:" + cameraFOV.getVerticalViewAngle());
            ap_camera_ht_from_ground_edittext.setText("" + Constants.DEFAULT_CAMERA_HEIGHT);
            ap_camera_offset_from_center_edittext.setText("" + Constants.DEFAULT_CAMERA_OFFSET);
            ap_camera_dist_from_front_edittext.setText("" + Constants.DEFAULT_CAMERA_DISTANCEFROMHEAD);

        }
    }


    private void initViews() {
        ap_gps_frequency_edittext = (EditText) findViewById(R.id.ap_gps_frequency_edittext);
        ap_accelerometer_sensitivity_edittext = (EditText) findViewById(R.id.ap_accelerometer_sensitivity_edittext);
        ap_vehicle_length_edittext = (EditText) findViewById(R.id.ap_vehicle_length_edittext);
        ap_vehicle_width_edittext = (EditText) findViewById(R.id.ap_vehicle_width_edittext);
        ap_vehicle_height_edittext = (EditText) findViewById(R.id.ap_vehicle_height_edittext);
        ap_camear_fov_edittext = (EditText) findViewById(R.id.ap_camera_fov_edittext);
        ap_wheel_base_edittext = (EditText) findViewById(R.id.ap_wheel_base_edittext);
        ap_curb_weight_edittext = (EditText) findViewById(R.id.ap_curb_weight_edittext);
        ap_camera_ht_from_ground_edittext = (EditText) findViewById(R.id.ap_camera_ht_from_ground_edittext);
        ap_camera_offset_from_center_edittext = (EditText) findViewById(R.id.ap_camera_offset_from_center_edittext);
        ap_camera_dist_from_front_edittext = (EditText) findViewById(R.id.ap_camera_dist_from_front_edittext);

        ap_ld_sensor_sensitivity = (Spinner) findViewById(R.id.ap_ld_sensor_sensitivity);
        ap_fwd_col_sensitivity = (Spinner) findViewById(R.id.ap_fwd_col_sensitivity);
        String[] sensitivity = getResources().getStringArray(R.array.sensitivity);
        ld_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_simple_string, sensitivity);
        ap_ld_sensor_sensitivity.setAdapter(ld_adapter);
        ap_ld_sensor_sensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected position = " + position);
                // entity.setLd_sensor_sensitivity(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ap_fwd_col_sensitivity.setAdapter(ld_adapter);
        ap_fwd_col_sensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected position = " + position);
                // entity.setFwd_col_sensitivity(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ap_ld_sensor_sensitivity.setEnabled(false);
        ap_fwd_col_sensitivity.setEnabled(false);

    }

    private void updateEditTextUI(@NonNull ADASParametersEntity entity) {
        ap_gps_frequency_edittext.setText(entity.getGps_frequency());
        ap_ld_sensor_sensitivity.setSelection(entity.getLd_sensor_sensitivity());
        ap_fwd_col_sensitivity.setSelection(entity.getFwd_col_sensitivity());
        ap_accelerometer_sensitivity_edittext.setText(entity.getAccelerometer_sensitivity());
        int length = entity.getVehicle_length();
        int width = entity.getVehicle_width();
        int height = entity.getVehicle_height();
        ap_vehicle_length_edittext.setText("" + length);
        ap_vehicle_width_edittext.setText("" + width);
        ap_vehicle_height_edittext.setText("" + height);
        ap_camear_fov_edittext.setText(entity.getCamera_fov());
        ap_wheel_base_edittext.setText(entity.getWheelbase());
        ap_curb_weight_edittext.setText(entity.getCurb_weight());
        ap_camera_ht_from_ground_edittext.setText("" + entity.getCamera_ht_from_ground());
        ap_camera_offset_from_center_edittext.setText("" + entity.getCamera_offset_from_center());
        ap_camera_dist_from_front_edittext.setText("" + entity.getCamera_dist_from_front());

    }

    @Override
    void OnStartEditMode() {
        enableEditText();
    }

    @Override
    void OnEndEditMode(boolean save) {
        disableEditText();

        if (save) {
            saveToDb();
        } else {
            getData();
        }
    }

    private void setADASConfigurations(ADASParametersEntity entity) {
        int lcar = entity.getVehicle_length();
        int wcar = entity.getVehicle_width();
        int hcar = entity.getVehicle_height();
        int hcam = entity.getCamera_ht_from_ground();
        int ocam = entity.getCamera_offset_from_center();
        int dcam = entity.getCamera_dist_from_front();
        // TODO This will cause crash, ADAS can't set below parameters twice?
        // ADASHelper.initADAS(lcar, wcar, hcar, hcam, ocam, dcam);

        int fwd_col_sensitivity = entity.getFwd_col_sensitivity();
        int ld_sensor_sensitivity = entity.getLd_sensor_sensitivity();
        ADASHelper.setupADAS(ld_sensor_sensitivity, fwd_col_sensitivity, 0);
    }


    private void saveToDb() {
        // TODO check input is legal
        String ap_gps_frequency = ap_gps_frequency_edittext.getText().toString();
        int ld_sensitivity = ap_ld_sensor_sensitivity.getSelectedItemPosition();
        int fwd_sensitivity = ap_fwd_col_sensitivity.getSelectedItemPosition();
        String ap_accelerometer_sensitivity = ap_accelerometer_sensitivity_edittext.getText().toString();
        String ap_vehicle_length = ap_vehicle_length_edittext.getText().toString();
        String ap_vehicle_width = ap_vehicle_width_edittext.getText().toString();
        String ap_vehicle_height = ap_vehicle_height_edittext.getText().toString();
        String ap_camear_fov = ap_camear_fov_edittext.getText().toString();
        String ap_wheel_base = ap_wheel_base_edittext.getText().toString();
        String ap_curb_weight = ap_curb_weight_edittext.getText().toString();
        String ap_camera_ht_from_ground = ap_camera_ht_from_ground_edittext.getText().toString();
        String ap_camera_offset_from_center = ap_camera_offset_from_center_edittext.getText().toString();
        String ap_camera_dist_from_front = ap_camera_dist_from_front_edittext.getText().toString();


        boolean isInsert = false;
        if (null == entity) {
            entity = new ADASParametersEntity();
            isInsert = true;
        }
        entity.setKey_user("");
        entity.setGps_frequency(ap_gps_frequency);
        entity.setLd_sensor_sensitivity(ld_sensitivity);
        entity.setFwd_col_sensitivity(fwd_sensitivity);
        entity.setAccelerometer_sensitivity(ap_accelerometer_sensitivity);
        entity.setVehicle_length(Integer.parseInt(ap_vehicle_length));
        entity.setVehicle_width(Integer.parseInt(ap_vehicle_width));
        entity.setVehicle_height(Integer.parseInt(ap_vehicle_height));
        entity.setCamera_fov(ap_camear_fov);
        entity.setWheelbase(ap_wheel_base);
        entity.setCurb_weight(ap_curb_weight);
        entity.setCamera_ht_from_ground(Integer.parseInt(ap_camera_ht_from_ground));
        entity.setCamera_offset_from_center(Integer.parseInt(ap_camera_offset_from_center));
        entity.setCamera_dist_from_front(Integer.parseInt(ap_camera_dist_from_front));

        if (isInsert) {
            adasParametersEntityDao.insert(entity);
        } else {
            adasParametersEntityDao.update(entity);
        }

        setADASConfigurations(entity);
    }

    private void enableEditText() {
        ap_gps_frequency_edittext.setEnabled(true);
        ap_ld_sensor_sensitivity.setEnabled(true);
        ap_fwd_col_sensitivity.setEnabled(true);
        ap_accelerometer_sensitivity_edittext.setEnabled(true);
        ap_vehicle_length_edittext.setEnabled(true);
        ap_vehicle_width_edittext.setEnabled(true);
        ap_vehicle_height_edittext.setEnabled(true);
        ap_camear_fov_edittext.setEnabled(true);
        ap_wheel_base_edittext.setEnabled(true);
        ap_curb_weight_edittext.setEnabled(true);
        ap_camera_ht_from_ground_edittext.setEnabled(true);
        ap_camera_offset_from_center_edittext.setEnabled(true);
        ap_camera_dist_from_front_edittext.setEnabled(true);

    }

    private void disableEditText() {
        ap_gps_frequency_edittext.setEnabled(false);
        ap_ld_sensor_sensitivity.setEnabled(false);
        ap_fwd_col_sensitivity.setEnabled(false);
        ap_accelerometer_sensitivity_edittext.setEnabled(false);
        ap_vehicle_length_edittext.setEnabled(false);
        ap_vehicle_width_edittext.setEnabled(false);
        ap_vehicle_height_edittext.setEnabled(false);
        ap_camear_fov_edittext.setEnabled(false);
        ap_wheel_base_edittext.setEnabled(false);
        ap_curb_weight_edittext.setEnabled(false);
        ap_camera_ht_from_ground_edittext.setEnabled(false);
        ap_camera_offset_from_center_edittext.setEnabled(false);
        ap_camera_dist_from_front_edittext.setEnabled(false);

    }


}
