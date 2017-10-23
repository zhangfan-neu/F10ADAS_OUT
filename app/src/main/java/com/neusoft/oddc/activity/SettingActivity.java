package com.neusoft.oddc.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.R;
import com.neusoft.oddc.ui.setting.EntitySettingGroup;
import com.neusoft.oddc.ui.setting.SettingRecyclerAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.listeners.OnGroupClickListener;
import com.neusoft.oddc.widget.recycler.DefaultDividerDecoration;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity {

    private static final String TAG = SettingActivity.class.getSimpleName();

    private List<EntitySettingGroup> entities;

    private OnGroupClickListener onGroupClickListener = new OnGroupClickListener() {
        @Override
        public boolean onGroupClick(int flatPos) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "OnGroupClickListener position = " + flatPos);
            }

            EntitySettingGroup entity = entities.get(flatPos);
            if (null == entity) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "onGroupClick null == entity");
                }
                return false;
            }
            Class clazz = entity.getClazz();
            if (null == clazz) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "onGroupClick null == clazz");
                }
                return false;
            }
            Intent intent = new Intent(SettingActivity.this, clazz);
            startActivity(intent);
            return false;

        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setCustomTitle(R.string.title_setting);

        initViews();
    }

    private void initViews() {
        entities = getSettingGroup();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.setting_expandable_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        SettingRecyclerAdapter adapter = new SettingRecyclerAdapter(entities, this);
        adapter.setOnGroupClickListener(onGroupClickListener);
        recyclerView.addItemDecoration(new DefaultDividerDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private List<EntitySettingGroup> getSettingGroup() {
        List<EntitySettingGroup> settingGroup = new ArrayList<>();
        List<String> childList = new ArrayList<>();
        // childList.add("sub-item1");
        // childList.add("sub-item2");
        // childList.add("sub-item3");
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item1), SettingUserProfileActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item2), SettingVehicleProfileActivity.class, childList));
        // settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item3), SettingAdasCalibrationActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item4), SettingAdasParametersActivity.class, childList));
        settingGroup.add(new EntitySettingGroup(getString(R.string.setting_group_item5), SettingDvrSettingActivity.class, childList));
        return settingGroup;
    }
}
