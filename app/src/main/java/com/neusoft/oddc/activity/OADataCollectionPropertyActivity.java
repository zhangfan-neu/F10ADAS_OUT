package com.neusoft.oddc.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.neusoft.oddc.R;

public class OADataCollectionPropertyActivity extends BaseActivity {

    public static final Intent createIntent(Context context) {
        Intent intent = new Intent(context, OADataCollectionPropertyActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oa_data_collection_property);
        setCustomTitle(R.string.title_property);

        initViews();
    }

    private void initViews() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
