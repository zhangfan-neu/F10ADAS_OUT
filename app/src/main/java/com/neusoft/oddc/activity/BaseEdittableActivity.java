package com.neusoft.oddc.activity;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.neusoft.oddc.R;

public abstract class BaseEdittableActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = BaseEdittableActivity.class.getSimpleName();

    private boolean editMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCustomTitle();
        initBottomButtons();
    }

    private void initBottomButtons() {
        Button bottomLeftBtn = (Button) findViewById(R.id.custom_editmode_bottom_left_button);
        Button bottomRightBtn = (Button) findViewById(R.id.custom_editmode_bottom_right_button);
        if (null != bottomLeftBtn) {
            bottomLeftBtn.setOnClickListener(this);
        }
        if (null != bottomRightBtn) {
            bottomRightBtn.setOnClickListener(this);
        }
    }

    private void initCustomTitle() {
        initCustomTitleButton(-1, this, true);
        initCustomTitleButton(-1, this, false);
    }

    protected void initCustomTitleButton(@StringRes int resId, View.OnClickListener onClickListener, boolean isLeft) {
        Button button;
        if (isLeft) {
            button = (Button) findViewById(R.id.custom_title_left_button);
        } else {
            button = (Button) findViewById(R.id.custom_title_right_button);
        }
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            if (resId > 0) {
                button.setText(resId);
            }
            if (null != onClickListener) {
                button.setOnClickListener(onClickListener);
            }
        }
    }


    protected void showTitleLeftButtons() {
        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
        }
    }

    protected void showTitleRightButtons() {
        Button button = (Button) findViewById(R.id.custom_title_right_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
        }
    }

    protected void showAllTitleButtons() {
        showTitleLeftButtons();
        showTitleRightButtons();
    }


    private void hideTitleLeftButtons() {
        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.GONE);
        }
    }

    private void hideTitleRightButton() {
        Button button = (Button) findViewById(R.id.custom_title_right_button);
        if (null != button) {
            button.setVisibility(View.GONE);
        }
    }

    protected void hideAllTitleButtons() {
        hideTitleLeftButtons();
        hideTitleRightButton();
    }

    protected void hideBottomButtons() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.custom_editmode_bottom_button_layout);
        if (null != layout) {
            layout.setVisibility(View.GONE);
        }
    }

    protected void showBottomButtons() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.custom_editmode_bottom_button_layout);
        if (null != layout) {
            layout.setVisibility(View.VISIBLE);
        }
    }

    protected void showSoftKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.custom_title_left_button:
                onTitleLeftBtnClicked();
                break;
            case R.id.custom_title_right_button:
                onTitleRightBtnClicked();
                break;
            case R.id.custom_editmode_bottom_left_button:
                onBottomLeftBtnClicked();
                break;
            case R.id.custom_editmode_bottom_right_button:
                onBottomRightBtnClicked();
                break;
            default:
                break;
        }
    }

    private void onBottomRightBtnClicked() {
        exitEditMode();
        OnEndEditMode(true);
    }

    private void onBottomLeftBtnClicked() {
        exitEditMode();
        OnEndEditMode(false);
    }

    private void onTitleRightBtnClicked() {
        startEditMode();
        OnStartEditMode();
    }

    private void startEditMode() {
        editMode = true;
        hideAllTitleButtons();
        showBottomButtons();
    }

    private void exitEditMode() {
        editMode = false;
        showAllTitleButtons();
        hideBottomButtons();
    }

    private void onTitleLeftBtnClicked() {
        finish();
    }

    abstract void OnStartEditMode();

    abstract void OnEndEditMode(boolean save);

    protected boolean isEditMode() {
        return editMode;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
