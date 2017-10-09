package com.neusoft.oddc.multimedia.util;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;

import java.util.List;

public class FocusHelper {

    //These device may have some problem when auto focus
    private final static String[] AUTO_FOCUS_NOT_SUPPORTED = new String[]{"LG-D838", "LT29i"};

    public static boolean isFocusSupported(List<String> supportedList) {
        boolean ret = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ret = isSupported(Camera.Parameters.FOCUS_MODE_AUTO, supportedList);
        }

        for (int i = 0; i < AUTO_FOCUS_NOT_SUPPORTED.length; i++) {
            if (AUTO_FOCUS_NOT_SUPPORTED[i].equalsIgnoreCase(Build.MODEL)) {
                ret = false;
                break;
            }
        }

        return ret;
    }

    public static boolean isSupported(String value, List<String> supported) {
        return null == supported ? false : supported.indexOf(value) >= 0;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean isFocusAreaSupported(Camera.Parameters params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return (params.getMaxNumFocusAreas() > 0
                    && isSupported(Camera.Parameters.FOCUS_MODE_AUTO, params.getSupportedFocusModes()));
        }
        return false;
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth,
                                     int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (left, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }
}
