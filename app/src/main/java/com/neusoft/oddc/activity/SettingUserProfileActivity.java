package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.R;
import com.neusoft.oddc.db.dbentity.UserProfileEntity;
import com.neusoft.oddc.db.gen.UserProfileEntityDao;

import java.util.ArrayList;

public class SettingUserProfileActivity extends BaseEdittableActivity {

    private static final String TAG = SettingActivity.class.getSimpleName();

    private EditText up_user_name_edittext;
    private EditText up_first_name_edittext;
    private EditText up_last_name_edittext;
    private EditText up_phone_no_edittext;
    private EditText up_email_edittext;
    private EditText up_driver_license_no_edittext;

    private UserProfileEntityDao userProfileEntityDao;
    private UserProfileEntity entity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_user_profile);

        setCustomTitle(R.string.setting_group_item1);

        initViews();

        userProfileEntityDao = ((MyApplication) getApplication()).getDaoSession().getUserProfileEntityDao();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isEditMode()) {
            getUserProfile();
        }

    }

    private void getUserProfile() {
        ArrayList<UserProfileEntity> list = (ArrayList<UserProfileEntity>) userProfileEntityDao.queryBuilder()
                .where(UserProfileEntityDao.Properties.Key_user.eq("")).list();
        if (null != list && list.size() > 0) {
            entity = list.get(0);
            updateUserProfileUI(entity);
        } else {
            Log.d(TAG, "getUserProfile : no data");
            entity = null;
        }
    }

    private void updateUserProfileUI(@NonNull UserProfileEntity entity) {
        up_user_name_edittext.setText(entity.getUsername());
        up_first_name_edittext.setText(entity.getFirstname());
        up_last_name_edittext.setText(entity.getLastname());
        up_phone_no_edittext.setText(entity.getPhoneno());
        up_email_edittext.setText(entity.getEmail());
        up_driver_license_no_edittext.setText(entity.getDriverlicenseno());
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
            getUserProfile();
        }
    }

    private void saveToDb() {
        // TODO check input is legal
        String username = up_user_name_edittext.getText().toString();
        String firstname = up_first_name_edittext.getText().toString();
        String lastname = up_last_name_edittext.getText().toString();
        String phoneno = up_phone_no_edittext.getText().toString();
        String email = up_email_edittext.getText().toString();
        String driverlicenseno = up_driver_license_no_edittext.getText().toString();
        boolean isInsert = false;
        if (null == entity) {
            entity = new UserProfileEntity();
            isInsert = true;
        }
        entity.setKey_user("");
        entity.setUsername(username);
        entity.setFirstname(firstname);
        entity.setLastname(lastname);
        entity.setPhoneno(phoneno);
        entity.setEmail(email);
        entity.setDriverlicenseno(driverlicenseno);
        if (isInsert) {
            userProfileEntityDao.insert(entity);
        } else {
            userProfileEntityDao.update(entity);
        }
    }

    private void initViews() {
        up_user_name_edittext = (EditText) findViewById(R.id.up_user_name_edittext);
        up_first_name_edittext = (EditText) findViewById(R.id.up_first_name_edittext);
        up_last_name_edittext = (EditText) findViewById(R.id.up_last_name_edittext);
        up_phone_no_edittext = (EditText) findViewById(R.id.up_phone_no_edittext);
        up_email_edittext = (EditText) findViewById(R.id.up_email_edittext);
        up_driver_license_no_edittext = (EditText) findViewById(R.id.up_driver_license_no_edittext);
    }

    private void enableEditText() {
        up_user_name_edittext.setEnabled(true);
        // showSoftKeyBoard(up_user_name_edittext);
        up_first_name_edittext.setEnabled(true);
        up_last_name_edittext.setEnabled(true);
        up_phone_no_edittext.setEnabled(true);
        up_email_edittext.setEnabled(true);
        up_driver_license_no_edittext.setEnabled(true);
    }

    private void disableEditText() {
        up_user_name_edittext.setEnabled(false);
        up_first_name_edittext.setEnabled(false);
        up_last_name_edittext.setEnabled(false);
        up_phone_no_edittext.setEnabled(false);
        up_email_edittext.setEnabled(false);
        up_driver_license_no_edittext.setEnabled(false);
    }


}

