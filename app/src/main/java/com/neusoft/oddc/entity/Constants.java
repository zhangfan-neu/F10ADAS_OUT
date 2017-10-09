package com.neusoft.oddc.entity;


public class Constants {

    public static final String APP_TAG = "oddc_app";
    public static final String TRACE_LIFECYCLE_FOR_CAMERA = "Lifecycle for camera : ";
    public static final String TRACE_RECORDER = "Trace recorder -> ";
    public static final String TRACE_RECORDER_CAMERA_PART = TRACE_RECORDER + " Camera part -> ";
    public static final String TRACE_RECORDER_MEDIA_RECORDER_PART = TRACE_RECORDER + " MediaRecorder part -> ";

    public static final long RECORD_VIDEO_MAX_FILE_SIZE = 1024 * 1024 * 2000; // 2000MB *Max long value is 2147483647
    public static final long RECORD_VIDEO_MAX_DURATION = 1000 * 60 * 1; // 1Min

    public static final int DESIRED_FRAME_RATE = 30;

    public static final String FILE_PATH = "/sdcard/oddc/";

    public static final double G_THRESH_VALUE = 2.0;

    // ADAS Authorization
    public static final String IMEI_1 = "351602004807283";
    public static final String IMEI_2 = "351602004807275";
    public static final String IMEI_3 = "004402454292917";
    public static final String IMEI_4 = "004402454292925";
    public static final String IMEI_5 = "866046027858352";
    public static final String IMEI_6 = "866046027858360";
    public static final String IMEI_7 = "354765082938713";
    public static final String IMEI_8 = "354766082938711";
    public static final String ADAS_KEY_1 = "kCrE0SXNg5qCXK819ZAC3w=="; // 351602004807283
    public static final String ADAS_KEY_2 = "adxbi/mJeL468tTz1UIroJ=="; // 351602004807275
    public static final String ADAS_KEY_3 = "rqo/QFhH5P63/wvJ2kw7qw=="; // 004402454292917
    public static final String ADAS_KEY_4 = "73UfprUW1jT3+pMRns6Pjg=="; // 004402454292925
    public static final String ADAS_KEY_5 = "2GrdgKST33t63i8+oIubRw=="; // 866046027858352
    public static final String ADAS_KEY_6 = "xuJeP0azb2CBfN0zceROvg=="; // 866046027858360
    public static final String ADAS_KEY_7 = "AN+XPOtTFYgICpsCsdt7gM=="; // 354765082938713
    public static final String ADAS_KEY_8 = "7ChY26W0j+E2DD14EvSUMg=="; // 354766082938711
/*
    {" IMEI ":"358958081304732"," key ":"AEdMuGNje3znUxAbzgVVvJ=="},
    {" IMEI ":"358959081304730"," key ":"tlVc6LbB9+ESV/kHN1NvXw=="},
    {" IMEI ":"351602004807283"," key ":"kCrE0SXNg5qCXK819ZAC3w=="},
    {" IMEI ":"351602004807275"," key ":"adxbi/mJeL468tTz1UIroJ=="},
    {" IMEI ":"004402454292917"," key ":"rqo/QFhH5P63/wvJ2kw7qw=="},
    {" IMEI ":"004402454292925"," key ":"73UfprUW1jT3+pMRns6Pjg=="},
    {" IMEI ":"866046027858352"," key ":"2GrdgKST33t63i8+oIubRw=="},
    {" IMEI ":"866046027858360"," key ":"xuJeP0azb2CBfN0zceROvg=="},
    {" IMEI ":"358330084884906"," key ":"H3V22jiINdHBtC71qpRftg=="},
    {" IMEI ":"354765082938713"," key ":"AN+XPOtTFYgICpsCsdt7gM=="},
    {" IMEI ":"354766082938711"," key ":"7ChY26W0j+E2DD14EvSUMg=="},
*/

    // Cars default info
    public static final int DEFAULT_CAR_LENGTH = 4850;
    public static final int DEFAULT_CAR_WIDTH = 1840;
    public static final int DEFAULT_CAR_HEIGHT = 1450;

    public static final int DEFAULT_CAMERA_HEIGHT = 1100;
    public static final int DEFAULT_CAMERA_OFFSET = 0;
    public static final int DEFAULT_CAMERA_DISTANCEFROMHEAD = 1400;

    public static final int DEFAULT_LDW_SENSITIVIY = 2;
    public static final int DEFAULT_FCW_SENSITIVIY = 2;

    // DVR Playback Events
    public static final int ADAS_FORWARD_VEHICLE_WARNING = 0; // adasForwardVehicleWarning
    public static final int ADAS_LANE_DEPARTURE_LEFT = 1; // adasLaneDepartureLeft
    public static final int ADAS_LANE_DEPARTURE_RIGHT = 2; // adasLaneDepartureRight
    public static final int ADAS_SUDDEN_BRAKING = 3; // adasSuddenBraking
    public static final int ADAS_VEHICLE_CUT_IN = 4; // adasVehicleCutIn

}
