package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.R;
import com.neusoft.oddc.db.dbentity.VehicleProfileEntity;
import com.neusoft.oddc.db.gen.VehicleProfileEntityDao;
import com.neusoft.oddc.oddc.neusoft.OBDManager;

import java.util.ArrayList;

public class SettingVehicleProfileActivity extends BaseEdittableActivity {

    private static final String TAG = SettingVehicleProfileActivity.class.getSimpleName();

    private EditText vp_year_edittext;
    private EditText vp_brand_edittext;
    private EditText vp_model_edittext;
    private EditText vp_color_edittext;
    private EditText vp_mileage_edittext;
    private EditText vp_vin_edittext;

    private VehicleProfileEntityDao vehicleProfileEntityDao;
    private VehicleProfileEntity entity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_vehicle_profile);

        setCustomTitle(R.string.setting_group_item2);

        initViews();

        vehicleProfileEntityDao = ((MyApplication) getApplication()).getDaoSession().getVehicleProfileEntityDao();


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isEditMode()) {
            getData();
        }

    }

    private void getData() {
        ArrayList<VehicleProfileEntity> list = (ArrayList<VehicleProfileEntity>) vehicleProfileEntityDao.queryBuilder()
                .where(VehicleProfileEntityDao.Properties.Key_user.eq("")).list();
        if (null != list && list.size() > 0) {
            entity = list.get(0);
            updateEditTextUI(entity);
        } else {
            Log.d(TAG, "getData : no data");
            entity = null;

            // Set vin read from odb2
            String vin = OBDManager.getInstance().getVIN();
            Log.d(TAG, "VIN = " + vin);
            if (!TextUtils.isEmpty(vin)) {
                vp_vin_edittext.setText(vin);
            }
        }
    }

    private void initViews() {
        vp_year_edittext = (EditText) findViewById(R.id.vp_year_edittext);
        vp_brand_edittext = (EditText) findViewById(R.id.vp_brand_edittext);
        vp_model_edittext = (EditText) findViewById(R.id.vp_model_edittext);
        vp_color_edittext = (EditText) findViewById(R.id.vp_color_edittext);
        vp_mileage_edittext = (EditText) findViewById(R.id.vp_mileage_edittext);
        vp_vin_edittext = (EditText) findViewById(R.id.vp_vin_edittext);
    }

    private void updateEditTextUI(@NonNull VehicleProfileEntity entity) {
        vp_year_edittext.setText(entity.getYear());
        vp_brand_edittext.setText(entity.getBrand());
        vp_model_edittext.setText(entity.getModel());
        vp_color_edittext.setText(entity.getColor());
        vp_mileage_edittext.setText(entity.getMileage());
        String vin = OBDManager.getInstance().getVIN();
        Log.d(TAG, "VIN = " + vin);
        if (TextUtils.isEmpty(vin)) {
            vp_vin_edittext.setText(entity.getVin());
        } else {
            vp_vin_edittext.setText(vin);
        }
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

    private void saveToDb() {
        // TODO check input is legal
        String year = vp_year_edittext.getText().toString();
        String brand = vp_brand_edittext.getText().toString();
        String model = vp_model_edittext.getText().toString();
        String color = vp_color_edittext.getText().toString();
        String mileage = vp_mileage_edittext.getText().toString();

        boolean isInsert = false;
        if (null == entity) {
            entity = new VehicleProfileEntity();
            isInsert = true;
        }
        entity.setKey_user("");
        entity.setYear(year);
        entity.setBrand(brand);
        entity.setModel(model);
        entity.setColor(color);
        entity.setMileage(mileage);

        String vin = OBDManager.getInstance().getVIN();
        if (TextUtils.isEmpty(vin)) {
            vin = vp_vin_edittext.getText().toString();
        }
        entity.setVin(vin);

        if (isInsert) {
            vehicleProfileEntityDao.insert(entity);
        } else {
            vehicleProfileEntityDao.update(entity);
        }
    }

    private void enableEditText() {
        vp_year_edittext.setEnabled(true);
        vp_brand_edittext.setEnabled(true);
        vp_model_edittext.setEnabled(true);
        vp_color_edittext.setEnabled(true);
        vp_mileage_edittext.setEnabled(true);
        vp_vin_edittext.setEnabled(true);
    }

    private void disableEditText() {
        vp_year_edittext.setEnabled(false);
        vp_brand_edittext.setEnabled(false);
        vp_model_edittext.setEnabled(false);
        vp_color_edittext.setEnabled(false);
        vp_mileage_edittext.setEnabled(false);
        vp_vin_edittext.setEnabled(false);
    }

}
