package com.neusoft.oddc.oddc.neusoft;

import android.provider.BaseColumns;



public class DBschema implements BaseColumns {
    public static final String TABLE_NAME = "oddc";

    public static final String ID  = "id";
    public static final String SID = "sessionID";
    public static final String VIN = "vehicleID";

    public static final String GPS_TS = "GPStimeStamp";    public static final String GPS_LON = "longitude";
    public static final String GPS_LAT = "latitude";

    public static final String SPEED = "Speed";
    public static final String SPEED_DT = "SpeedDetectionType";

    public static final String ACC_X = "AccelerationX";
    public static final String ACC_Y = "AccelerationY";
    public static final String ACC_Z = "AccelerationZ";

    public static final String GS_TS = "GShockTimeStamp";    public static final String GS_E = "GShockEvent";
    public static final String GS_T = "GShockEventThreshold";

    public static final String FCW_EFV = "FCWExistFV";
    public static final String FCW_CI = "FCWCutIn";
    public static final String FCW_TTC = "FCWTimeToCollision";
    public static final String FCW_DFV = "FCWDistanceToFV";
    //public static final String FCW_RSFV = "FCWRelativeSpeedToFV";
    public static final String FCW_E = "FCWEvent";
    public static final String FCW_ET = "FCWTEventThreshold";
    public static final String LDW_TS = "LDWTimeStamp";
    public static final String LDW_DLL = "LDWDistanceToLeftLane";
    public static final String LDW_DRL = "LDWDistanceToRightLane";
    public static final String LDW_E = "LDWEvent";

    public static final String M_URI = "MediaURI";
    public static final String M_D = "MediaDeleted";
    public static final String M_P = "MediaProtected";
    public static final String M_U = "MediaUploaded";
    public static final String D_U = "DataUploaded";
}


