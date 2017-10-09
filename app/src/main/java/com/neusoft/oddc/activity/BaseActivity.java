package com.neusoft.oddc.activity;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.R;
import com.neusoft.oddc.fragment.LoadingFragment;

public class BaseActivity extends AppCompatActivity implements DialogInterface.OnDismissListener{

    private static final String TAG = BaseActivity.class.getSimpleName();

    private LoadingFragment loadingFragment;
    protected boolean isLoading = false;

    protected static int BASE_PERMISSIONS = 100;
    protected boolean needCheckPermission = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideActionBar();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.currentActivity = this;

        Log.d(TAG, "onResume needCheckPermission = " + needCheckPermission);

        if (needCheckPermission) {
            checkPermissions();
        }

    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA
                    }
                    , BASE_PERMISSIONS);
            needCheckPermission = false;
        } else {
            Log.d(TAG, "checkPermissions all permission granted ");
            onPermissionGranted();
        }
    }

    protected void onPermissionGranted() {
        needCheckPermission = false;
    }

    protected void setCustomTitle(@StringRes int strId) {
        TextView textView = (TextView) findViewById(R.id.custom_title_textview);
        if (null != textView) {
            textView.setText(strId);
        }
    }

    protected void hideActionBar() {
        try {
            ActionBar actionBar = getSupportActionBar();
            if (null != actionBar) {
                actionBar.hide();
            }
        } catch (Exception e) {
            Log.e(TAG, "hideActionBar error!");
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    private void enableHomeAsBack() {
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    protected void startFragment(@IdRes int containerViewId, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String tag = fragment.getClass().getSimpleName();
        Log.d(TAG, "BaseActivity startFragment tag = " + tag);
        fragmentTransaction.replace(containerViewId, fragment, tag);
        // fragmentTransaction.addToBackStack(null);
        if (!isFinishing()) {
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        needCheckPermission = true;
        if (BASE_PERMISSIONS == requestCode) {
            for (int i : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != i) {
                    Toast.makeText(this, R.string.main_camera_permission_hint, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            Log.d(TAG, "onRequestPermissionsResult requestCode = " + requestCode);
            // All permission are granted
            onPermissionGranted();
        }
    }

    public void showLoading() {
        if (loadingFragment != null && loadingFragment.getDialog() != null && loadingFragment.getDialog().isShowing()) {
            dismissLoading();
        }
        loadingFragment = new LoadingFragment();
        loadingFragment.show(getSupportFragmentManager(), LoadingFragment.class.getSimpleName());
        isLoading = true;
    }

    public void dismissLoading() {
        try {
            isLoading = false;
            loadingFragment.dismissAllowingStateLoss();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isLoading = false;
    }
}
