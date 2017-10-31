package com.neusoft.oddc.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.R;
import com.neusoft.oddc.db.dbentity.VinOptionEntity;
import com.neusoft.oddc.db.gen.VinOptionEntityDao;
import com.neusoft.oddc.oddc.utilities.Utilities;

import java.util.ArrayList;

public class SettingVinOptionsActivity extends BaseActivity implements View.OnClickListener
{
    private VinOptionEntity entity;
    private TextView textView;
    private VinOptionEntityDao vinOptionEntityDao;
    private Spinner spinner;
    private int vinOption;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_vin_options);

        vinOptionEntityDao = ((MyApplication) getApplication()).getDaoSession().getVinOptionEntityDao();

        setCustomTitle(R.string.title_vin_options);
        initBackButton();
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void updateOptionAndText(@NonNull final int position)
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                spinner.setSelection(position);

                switch (position)
                {
                    case 0:
                        setOptionText(getString(R.string.vin_option_default_txt));
                        break;
                    case 1:
                        setOptionText(getString(R.string.vin_option_obd2_txt));
                        break;
                    case 2:
                        setOptionText(getString(R.string.vin_option_vehicle_profile_txt));
                        break;
                    default:
                        break;
                }

                vinOption = position;
            }
        });
    }

    private void saveToDb()
    {
        boolean isInsert = false;
        if (null == entity) {
            entity = new VinOptionEntity();
            isInsert = true;
            vinOption = 0;
        }

        entity.setKey_user("");
        entity.setVinOption(vinOption);

        if (isInsert) {
            vinOptionEntityDao.insert(entity);
        } else {
            vinOptionEntityDao.update(entity);
        }
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

    private void setOptionText(String text)
    {
        if(textView != null)
        {
            textView.setText(text);
        }
    }

    private void initializeUI()
    {
        spinner = (Spinner)findViewById(R.id.optionSpinner);
        textView = (TextView)findViewById(R.id.optionText);

        entity = Utilities.getVinOption();
        if(entity == null)
        {
            saveToDb();
        }

        if(spinner != null)
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.vin_options, R.layout.spinner_item_simple_string);
            adapter.setDropDownViewResource(R.layout.spinner_item_simple_string);
            spinner.getBackground().setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    vinOption = position;
                    updateOptionAndText(position);
                    saveToDb();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });

            VinOptionEntity optionEntity = Utilities.getVinOption();
            if(optionEntity != null)
            {
                updateOptionAndText(optionEntity.getVinOption());
            }
        }
    }
}
