package com.neusoft.oddc.widget;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;

public class StorageUtil {

    private static final String TAG = StorageUtil.class.getSimpleName();
    private static final int MIN_SIZE = 50;
    private static final int MEGA = 1048576; //1024 * 1024

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean enoughSpace() {
        boolean isOk = false;
        try {
            String externalPath;
            if (isExternalStorageAvailable()) {
                externalPath = Environment.getExternalStorageDirectory().toString();
                StatFs statFs = new StatFs(externalPath);
                long availableSize = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
                Log.d(TAG, "availableSize = " + availableSize);
                Log.d(TAG, "availableSize in MB = " + availableSize / MEGA);
                if ((availableSize / MEGA) < MIN_SIZE) {
                    isOk = false;
                } else {
                    isOk = true;
                }
            } else {
                isOk = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Get external available size error!");
            isOk = false;
        }
        return isOk;
    }

    public static final boolean isExternalStorageAvailable() {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_CHECKING.equals(state);
    }

    public static void deleteVideoFile(Context context, String path, Uri uri) {
        try {
            // Delete the last video file.
            String filePath = path;
            File recorderFile = new File(filePath);
            if (recorderFile.exists()) {
                if (recorderFile.delete()) {
                    Log.i(TAG, "Delete the last video file path = " + filePath);
                } else {
                    Log.e(TAG, "Delete the last video file failed! path is " + filePath);
                }
            }
            // Delete in database
            ContentResolver contentResolver = context.getContentResolver();
            if (null != contentResolver) {
                int rowId = contentResolver.delete(uri, null, null);
                if (-1 == rowId) {
                    Log.e(TAG, "Delete the last video file in database failed! uri is " + uri);
                } else {
                    Log.i(TAG, "Delete the last video file in database uri = " + uri);
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Delete the last video file error!");
        }
    }


}
