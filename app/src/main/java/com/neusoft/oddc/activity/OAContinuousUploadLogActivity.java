package com.neusoft.oddc.activity;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.R;
import com.neusoft.oddc.adapter.OAContinuousUploadLogSpinnerAdapter;
import com.neusoft.oddc.oddc.model.DataPackageType;
import com.neusoft.oddc.oddc.neusoft.LogData;
import com.neusoft.oddc.ui.continous.ContinousRecyclerAdapter;
import com.neusoft.oddc.ui.continous.EntityContinousGroup;
import com.neusoft.oddc.widget.DataConverter;
import com.neusoft.oddc.widget.recycler.DefaultDividerDecoration;

import java.util.ArrayList;
import java.util.List;

public class OAContinuousUploadLogActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = OAContinuousUploadLogActivity.class.getSimpleName();

    private AppCompatSpinner supplierSpinner;
    private OAContinuousUploadLogSpinnerAdapter oaContinuousUploadLogSpinnerAdapter;

    public static final Intent createIntent(Context context) {
        Intent intent = new Intent(context, OAContinuousUploadLogActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oa_continuous_upload_log);
        setCustomTitle(R.string.title_continuous_upload);

        supplierSpinner = (AppCompatSpinner) findViewById(R.id.continuous_log_type_spinner);
        oaContinuousUploadLogSpinnerAdapter = new OAContinuousUploadLogSpinnerAdapter(this);
        supplierSpinner.setAdapter(oaContinuousUploadLogSpinnerAdapter);
        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DataPackageType dataPackageType = (DataPackageType) parent.getSelectedItem();
                getData(dataPackageType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        supplierSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isLoading) {
                    Toast.makeText(getApplicationContext(), R.string.oa_continuous_upload_log_data_loading, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.back);
            button.setOnClickListener(this);
        }

        getData(DataPackageType.CONTINUOUS);
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

    private void initViews(List<EntityContinousGroup> entities) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.oa_continuous_upload_log_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ContinousRecyclerAdapter adapter = new ContinousRecyclerAdapter(entities, this);
        recyclerView.addItemDecoration(new DefaultDividerDecoration(this));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        // adapter.setOnGroupClickListener(onGroupClickListener);
    }

    private void getData(DataPackageType dataPackageType) {
        new GetDataTask().execute(dataPackageType);
    }


    private class GetDataTask extends AsyncTask<DataPackageType, Integer, List<EntityContinousGroup>> {

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected List<EntityContinousGroup> doInBackground(DataPackageType... params) {
            Log.d(TAG, "getData DataPackageType = " + params[0]);
            NeusoftHandler neusoftHandler = new NeusoftHandler();
            ArrayList<LogData> logDatas = neusoftHandler.getLogList(params[0]);
            List<EntityContinousGroup> entities = DataConverter.logData2Group(logDatas);
            return entities;
        }

        @Override
        protected void onPostExecute(List<EntityContinousGroup> entities) {
            if (null == entities || entities.size() == 0) {
                // entities = DataConverter.makeFadeContinuousGroupData();
                entities = new ArrayList<>();
            }
            initViews(entities);
            dismissLoading();


        }
    }

}
