package com.neusoft.oddc.widget;

import android.util.Log;

import com.neusoft.oddc.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    public static String getADASKey() {
        String key = "";
        String keyInDownload = "/sdcard/Download/license.txt";
        String fileName = "/sdcard/oddc/license.txt";

        File keyInDownloadFile = new File(keyInDownload);
        File fin = new File(fileName);
        if (keyInDownloadFile.exists()) {
            key = readFiles(keyInDownload);
        } else if (fin.exists()) {
            key = readFiles(fileName);
        } else {
            if (BuildConfig.DEBUG) {
                // Read key from property.
                key = PropertyUtil.getADASKey();
            }
        }
        Log.d(TAG, "ADAS key = " + key);
        return key;
    }

    private static String readFiles(String fileName) {
        String content = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(fileName));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            content = outputStream.toString();
            fileInputStream.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

}
