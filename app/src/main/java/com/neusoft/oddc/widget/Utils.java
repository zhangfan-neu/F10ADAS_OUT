package com.neusoft.oddc.widget;

import java.text.DecimalFormat;

public class Utils {

    public static final String formatValueToStr(float value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String str = decimalFormat.format(value);
        return str;
    }

    public static final String formatValueToStr(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String str = decimalFormat.format(value);
        return str;
    }
}
