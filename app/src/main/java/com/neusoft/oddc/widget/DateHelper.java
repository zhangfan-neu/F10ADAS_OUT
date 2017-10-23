package com.neusoft.oddc.widget;


import com.neusoft.oddc.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static final String getCurrentTime() {
        String timeStr = "";
        try {
            long time = System.currentTimeMillis(); //long now = android.os.SystemClock.uptimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(time);
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }


    public static final String getCurrentTime2() {
        String timeStr = "";
        try {
            long time = System.currentTimeMillis(); //long now = android.os.SystemClock.uptimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date(time);
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }


    public static String formatTime(String original) {
        String timeStr = "";
        try {
            Date date = new SimpleDateFormat("HHmmss").parse(original);
            SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }

    public static String formateDate(String original) {
        String timeStr = "";
        try {
            Date date = new SimpleDateFormat("yyMMdd").parse(original);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }

    public static String formatLogTimeStampToDate(String original) {
        String timeStr = "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(original);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }


    public static final String formatLogTimeStampToTime(String original) {
        String timeStr = "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(original);
            SimpleDateFormat format = new SimpleDateFormat("hh:mm aa");
            timeStr = format.format(date);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return timeStr;
    }

}
