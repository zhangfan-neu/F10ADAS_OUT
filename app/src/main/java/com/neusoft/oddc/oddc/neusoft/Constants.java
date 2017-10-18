package com.neusoft.oddc.oddc.neusoft;

/**
 * Created by rsamonte on 9/6/2017.
 */

public class Constants
{
    public static class ODDCApp
    {
//        public static final String BASE_URL = "http://13.57.134.47:8080//ODDCServer/";
        public static final String BASE_URL = "http://13.57.134.47:8080//";
        public static final String VIN = "1B3LC46R38N557181";
//        public static final String BASE_URL = "http://10.0.2.2:8080//";
        public static final int PING_TIME = 10000;   //10 seconds
        //public static final String dateTimeFormat = "MM-dd-yyyy HH:mm:ss.SSS";
        public static final String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        public static final String sampleFileURI = "http://techslides.com/demos/sample-videos/small.mp4";
        public static final int timerFrequency = 20;
        public static final int pingFrequency = 5;
        public static String DATABASE_NAME = "oddc.db";

        public static int MIN_AVAIL_FS = 1024 * 1024 * 1024;
        public static int FRAME_RATE = 30;
        public static int SAMPLE_FREQ = 30;
        public static int FRAMES_PER_MIN = FRAME_RATE * 60;
        public static int SENDCOUNT = FRAMES_PER_MIN / SAMPLE_FREQ;

    }
}
