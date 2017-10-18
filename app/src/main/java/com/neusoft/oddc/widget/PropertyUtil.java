package com.neusoft.oddc.widget;


import android.util.Log;

import com.neusoft.oddc.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertyUtil {

    private static final String TAG = PropertyUtil.class.getSimpleName();

    private static final String PROP_FILE_PATH = "/sdcard/oddc/";
    private static final String PROP_FILE_NAME = "oddc_config.properties";
    private static final String PROP_FILE_FULL_PATH = PROP_FILE_PATH + PROP_FILE_NAME;

    // For adas
    private static final String PROP_KEY_ADAS_KEY = "adas_key";
    private static final String PROP_VALUE_ADAS_KEY_DEFAULT = "AN+XPOtTFYgICpsCsdt7gM==";
    // For oddc
    private static final String PROP_KEY_ODDC_SERVER = "oddc_server";
    private static final String PROP_VALUE_ODDC_SERVER_DEFAULT = "http://52.52.124.97:8080/ODDCServer/";


    public static void initCommonProperties() {
        FileOutputStream out = null;
        try {
            File path = new File(PROP_FILE_PATH);
            if (!path.exists()) {
                path.mkdirs();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "initCommonProperties -> path.mkdirs : " + path.getAbsolutePath());
                }
            }
            File file = new File(PROP_FILE_PATH, PROP_FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "initCommonProperties -> file.createNewFile : " + file.getAbsolutePath());
                }
                out = new FileOutputStream(file);
                Properties props = new Properties();
                //Initialise the default value for the very first time
                props.setProperty(PROP_KEY_ADAS_KEY, PROP_VALUE_ADAS_KEY_DEFAULT);
                props.setProperty(PROP_KEY_ODDC_SERVER, PROP_VALUE_ODDC_SERVER_DEFAULT);
                props.store(out, null);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "config.properties mkdirs Exception", e);
                e.printStackTrace();
            }
        } finally {
            try {
                if (null != out) {
                    out.close();
                    out = null;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final String getADASKey() {
        // Create property file if not exist
        initCommonProperties();

        String value = "";
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(PROP_FILE_FULL_PATH);
            props.load(in);
            value = props.getProperty(PROP_KEY_ADAS_KEY);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "PROP_KEY_ADAS_KEY value = " + value);
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "config.properties catch Exception", e);
                e.printStackTrace();
            }
        } finally {
            try {
                in.close();
                in = null;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    public static final String getServerUrl() {
        // Create property file if not exist
        initCommonProperties();

        String value = "";
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(PROP_FILE_FULL_PATH);
            props.load(in);
            value = props.getProperty(PROP_KEY_ODDC_SERVER);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "PROP_KEY_ODDC_SERVER value = " + value);
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "config.properties catch Exception", e);
                e.printStackTrace();
            }
        } finally {
            try {
                in.close();
                in = null;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }


}
