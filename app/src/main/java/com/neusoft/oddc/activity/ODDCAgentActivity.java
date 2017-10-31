package com.neusoft.oddc.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.R;
import com.neusoft.oddc.ui.oddcagent.OddcAgentRecyclerAdapter;
import com.neusoft.oddc.ui.setting.EntitySettingGroup;
import com.neusoft.oddc.widget.expandablerecycler.common.listeners.OnGroupClickListener;
import com.neusoft.oddc.widget.recycler.DefaultDividerDecoration;

import java.util.ArrayList;
import java.util.List;

public class ODDCAgentActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ODDCAgentActivity.class.getSimpleName();

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
            Intent intent = new Intent(ODDCAgentActivity.this, clazz);
            startActivity(intent);
            return false;

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oddc_agent);
        setCustomTitle(R.string.title_oddc_agent);

        initViews();
        initBackButton();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_title_left_button:
                finish();
                break;
            default:
                break;
        }
    }

    private void initViews() {
        entities = getSettingGroup();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.oddc_agent_expandable_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        OddcAgentRecyclerAdapter adapter = new OddcAgentRecyclerAdapter(entities, this);
        recyclerView.addItemDecoration(new DefaultDividerDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnGroupClickListener(onGroupClickListener);
    }

    private void initBackButton()
    {
        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.back);
            button.setOnClickListener(this);
        }
    }

    private List<EntitySettingGroup> getSettingGroup() {
        List<EntitySettingGroup> settingGroup = new ArrayList<>();
        List<String> childList = new ArrayList<>();
        // childList.add("sub-item1");
        // childList.add("sub-item2");
        // childList.add("sub-item3");
        settingGroup.add(new EntitySettingGroup(getString(R.string.oddc_agent_item1), OAContinuousUploadLogActivity.class, childList));
        // settingGroup.add(new EntitySettingGroup(getString(R.string.oddc_agent_item2), OAEventUploadLogActivity.class, childList));
        // settingGroup.add(new EntitySettingGroup(getString(R.string.oddc_agent_item3), OAOnDemandUploadLogActivity.class, childList));
        // settingGroup.add(new EntitySettingGroup(getString(R.string.oddc_agent_item4), OADataCollectionPropertyActivity.class, childList));
        return settingGroup;
    }
}
