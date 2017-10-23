package com.neusoft.oddc.multimedia.util;


import com.neusoft.oddc.entity.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaFileUtil {

    private static final String TAG = MediaFileUtil.class.getSimpleName();

    public static final String FILE_PATH = Constants.FILE_PATH;

    public static String getVideoFilePath() {
        String filePath = "";
        try {
            File directory = new File(FILE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
            String dateStr = simpleDateFormat.format(date);
            File file = new File(FILE_PATH, dateStr + ".mp4");
            if (!file.exists()) {
                file.createNewFile();
            }
            filePath = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }

}
