package com.neusoft.oddc.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;

public class ErrorDialogFragment extends DialogFragment {

    private static final String TAG = ErrorDialogFragment.class.getSimpleName();

    public static final String KEY_MESSAGE = "key_message";

    public interface Callback {
        void onOkClicked();
    }

    private Callback callback;
    private String message;

    public static ErrorDialogFragment newInstance(Callback callback, String msg) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.callback = callback;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MESSAGE, msg);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (null != bundle) {
            message = bundle.getString(KEY_MESSAGE, "");
            Log.d(TAG, "message = " + message);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_error_dialog_layout, null);
        TextView messageTextView = (TextView) view.findViewById(R.id.fragment_error_dialog_message_textview);
        messageTextView.setText(message);
        builder.setView(view).setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (null != callback) {
                            callback.onOkClicked();
                        }
                    }
                });
        Dialog dialog = builder.create();
        return dialog;
    }
}
