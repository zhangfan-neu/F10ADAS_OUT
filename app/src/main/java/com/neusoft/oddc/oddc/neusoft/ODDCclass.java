package com.neusoft.oddc.oddc.neusoft;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.TimeZone;
import android.os.Process;
import android.util.Log;

import org.springframework.http.HttpStatus;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.neusoft.oddc.oddc.neusoft.DBschema;
import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.model.DataPackage;
import com.neusoft.oddc.oddc.model.DataPackageType;
import com.neusoft.oddc.oddc.model.Envelope;
import com.neusoft.oddc.oddc.model.EventType;
import com.neusoft.oddc.oddc.model.ODDCJob;
import com.neusoft.oddc.oddc.model.ODDCTask;
import com.neusoft.oddc.oddc.model.Video;
import com.neusoft.oddc.oddc.restclient.RESTController;
import com.neusoft.oddc.oddc.utilities.Utilities;


public class ODDCclass implements ODDCinterface {
    private Context mContext;
    private File mVideoFolder; // for NeuSoft set value
    private String baseUrl;

    RESTController controller;
    ArrayList<ODDCJob> jobList;
    String currentVideoFile = "NA";
    String completedVideoFile = "NA";

    public ODDCdbHelper dbh = null;
    public SQLiteDatabase db = null;
    public TimeZone tz;

    private NeusoftHandler listener;
    private JobManager jobManager;
    private int loopCount = 0;

    public static UUID curSession;

    public ODDCclass(String url, Context context, File folder){
        //tz = TimeZone.getDefault();
        this.mContext = context;
        this.mVideoFolder = folder;
        this.baseUrl = url;
        jobList = new ArrayList<ODDCJob>();

        dbh = new ODDCdbHelper(this.mContext);
        db = dbh.getWritableDatabase();
        db.execSQL(SQL_CREATE_TABLE);

        Log.d("ODDC THREAD ","ODDCclass TID="+String.valueOf(Process.myTid()));
    }


    public void setListener(NeusoftHandler listener){this.listener = listener;}
    public void setJobManager(JobManager jobManager) { this.jobManager = jobManager; }

    // return value of false indicates some condition exists which should prevent startup
    // otherwise true indicate OK to startup
    public boolean ok2Startup(){

        controller = new RESTController(baseUrl);

        //RMS NOTE: Temporary fix
        //ODDCTask tempTask = JobManager.getDummyTask();
        //jobList = controller.getJobList(tempTask); // FIXME parse jobList and perform tasks

        long fsStat = checkFileSpace();
        return fsStat == -1 ? false : true;
    }

    // NeuSoft waits for boolean return value from reqShutdown before shutdown
    public boolean reqShutdown(){return true;} // FIXME TBD


    // TESTING ONLY
    public int getRowCount(){
        long cnt  = DatabaseUtils.queryNumEntries(db, DBschema.TABLE_NAME);
        return (int)cnt;
    }


    private class ODDCdbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 3;


        public ODDCdbHelper(Context context) {
            super(context, Constants.ODDCApp.DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_DROP_TABLE);
            db.execSQL(SQL_CREATE_TABLE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_TABLE);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

    }

    public void dropTable(){
        db.execSQL(SQL_DROP_TABLE);
        db.execSQL(SQL_CREATE_TABLE);
        Log.d("ODDC","ODDCclass.dropTable");
    }


    public ArrayList<PlaybackList> getPlaybackList(){
        Cursor c = db.rawQuery("select MediaURI,sum(GShockEvent),sum(FCWEvent),sum(LDWEvent),MediaDeleted from oddc where MediaURI not in ( select MediaURI from oddc where  MediaDeleted = 1 ) group by MediaURI having ( sum(GShockEvent) > 0 or sum(FCWEvent) > 0 or sum(LDWEvent) > 0 )",null);
        int nrows = c.getCount();
        Log.d("ODDC GETPBLIST", "Cursor.nrows="+nrows);
        if (nrows > 0) {
            ArrayList<PlaybackList> pbList = new ArrayList<PlaybackList>();
            while (c.moveToNext()) {
                PlaybackList pb = new PlaybackList();
                pb.MediaURI = c.getString(0);
                pb.GShockEvent = c.getInt(1);
                pb.FCWEvent = c.getInt(2);
                pb.LDWEvent = c.getInt(3);
                pb.MediaDeleted = c.getInt(4);
                pbList.add(pb);
            }
            c.close();
            return pbList;
        }
        c.close();
        return null;
    }

    public EventType getEventType(int gse,int fcwe,int ldwe){
        if (gse  > 0) return EventType.GSHOCK;
        if (fcwe > 0) return EventType.FCW;
        if (ldwe > 0) return EventType.LDW;
        return EventType.NONE;
    }


    public ArrayList<LogData> getLog(DataPackageType t){
        String selectSTR = "";
        //String eType = "NO EVENT";

        switch(t){
            case CONTINUOUS:
                selectSTR = "select sessionID,GShockEvent,FCWEvent,LDWEvent,GPStimeStamp,MediaURI,MediaDeleted,MediaUploaded,DataUploaded from oddc where dataUploaded = 1";
                break;
            case EVENT:
                selectSTR = "select sessionID,GShockEvent,FCWEvent,LDWEvent,GPStimeStamp,MediaURI,MediaDeleted,MediaUploaded,DataUploaded from oddc where GShockEvent = 1 or FCWEvent = 1 or LDWEvent = 1";
                break;
            case SELECTIVE:
                selectSTR = "select sessionID,GShockEvent,FCWEvent,LDWEvent,GPStimeStamp,MediaURI,MediaDeleted,MediaUploaded,DataUploaded from oddc where MediaUploaded = 1";
                break;
            case ALL:
                selectSTR = "select sessionID,GShockEvent,FCWEvent,LDWEvent,GPStimeStamp,MediaURI,MediaDeleted,MediaUploaded,DataUploaded from oddc";
                break;
        }

        Cursor c = db.rawQuery(selectSTR,null);
        int nrows = c.getCount();
        if (nrows > 0) {
            ArrayList<LogData> logList = new ArrayList<LogData>();
            while (c.moveToNext()) {
                LogData log = new LogData();
                log.sessionID  = c.getString(0);
                int gse  = c.getInt(1);
                int fcwe = c.getInt(2);
                int ldwe = c.getInt(3);
                log.eventType = getEventType(gse,fcwe,ldwe);
                log.timeStamp     = c.getString(4);
                log.filename      = c.getString(5);
                log.mediaDeleted  = c.getInt(6);
                log.mediaUploaded = c.getInt(7);
                log.dataUploaded  = c.getInt(8);
                logList.add(log);
            }
            c.close();
            return logList;
        }
        c.close();
        return null;
    }



    public boolean onContinuousData(ContinuousData data){
        // Fujitsu processing of continuous data received from NeuSoft
        long fsStat = 0;

        Log.d("ODDC","ODDCclass.onContinuousData "+String.valueOf(Constants.ODDCApp.FRAMES_PER_MIN)+" "+loopCount+" CDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCD");
        insertSQLite(data);

        if (currentVideoFile == "NA") currentVideoFile = data.mediaURI;
        else {
            if (currentVideoFile != data.mediaURI) {
                completedVideoFile = currentVideoFile;
                currentVideoFile = data.mediaURI;
                SendToFLA fla = new SendToFLA(DataPackageType.CONTINUOUS,curSession,data.mediaURI);
                fla.start();
            }
        }
        return fsStat == -1 ? false : true;
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DBschema.TABLE_NAME + " (" +
                    DBschema._ID      + " INTEGER PRIMARY KEY," +
                    DBschema.ID       + " CHAR(48)," +
                    DBschema.SID      + " CHAR(48)," +
                    DBschema.VIN      + " CHAR(18)," +




                    DBschema.GPS_LON  + " FLOAT(10,6)," +
                    DBschema.GPS_LAT  + " FLOAT(10,6)," +

                    DBschema.SPEED    + " FLOAT(5,2)," +
                    DBschema.SPEED_DT + " INT," +





                    DBschema.ACC_X    + " FLOAT(10,6)," +
                    DBschema.ACC_Y    + " FLOAT(10,6)," +
                    DBschema.ACC_Z    + " FLOAT(10,6)," +

                    DBschema.GS_E     + " BOOLEAN," +
                    DBschema.GS_T     + " FLOAT(10,6)," +


                    DBschema.FCW_EFV  + " BOOLEAN," +
                    DBschema.FCW_CI   + " BOOLEAN," +
                    DBschema.FCW_TTC  + " INT," +
                    DBschema.FCW_DFV  + " FLOAT(5,2)," +
                    DBschema.FCW_RSFV + " FLOAT(5,2)," +
                    DBschema.FCW_E    + " BOOLEAN," +
                    DBschema.FCW_ET   + " FLOAT(5,2)," +


                    DBschema.LDW_DLL  + " FLOAT(5,2)," +
                    DBschema.LDW_DRL  + " FLOAT(5,2)," +
                    DBschema.LDW_E    + " BOOLEAN," +

                    DBschema.M_URI    + " VARCHAR(32)," +
                    DBschema.M_D      + " BOOLEAN," +
                    /*DBschema.M_P      + " BOOLEAN," +*/
                    DBschema.M_U      + " BOOLEAN," +
                    DBschema.D_U      + " BOOLEAN )";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + DBschema.TABLE_NAME;

    public boolean insertSQLite(ContinuousData data){
        ContentValues values = new ContentValues();

        values.put(DBschema.SID,data.sessionID.toString());



        values.put(DBschema.VIN, data.vehicleID);

        values.put(DBschema.TS, data.timestamp);
        values.put(DBschema.GPS_LON, data.longitude);
        values.put(DBschema.GPS_LAT, data.latitude);

        values.put(DBschema.SPEED, data.speed);
        values.put(DBschema.SPEED_DT, data.speedDetectionType);

        values.put(DBschema.ACC_X, data.accelerationX);
        values.put(DBschema.ACC_Y, data.accelerationY);
        values.put(DBschema.ACC_Z, data.accelerationZ);







        values.put(DBschema.GS_E, data.gShockEvent);
        values.put(DBschema.GS_T, data.gShockEventThreshold);

        values.put(DBschema.FCW_EFV, data.fcwExistFV);
        values.put(DBschema.FCW_CI, data.fcwCutIn);
        values.put(DBschema.FCW_DFV, data.fcwDistanceToFV);
        values.put(DBschema.FCW_RSFV, data.fcwRelativeSpeedToFV);        values.put(DBschema.FCW_E, data.fcwEvent);
        values.put(DBschema.FCW_ET, data.fcwEventThreshold);

        values.put(DBschema.LDW_DLL, data.ldwDistanceToLeftLane);
        values.put(DBschema.LDW_DRL, data.ldwDistanceToRightLane);
        values.put(DBschema.LDW_E, data.ldwEvent);

        values.put(DBschema.M_URI, data.mediaURI);
        values.put(DBschema.M_D, false);
        /*values.put(DBschema.M_P, false);*/
        values.put(DBschema.M_U, false);
        values.put(DBschema.D_U, false);
        values.put(DBschema.FCW_RSFV, data.fcwRelativeSpeedToFV);
        long rid = db.insert(DBschema.TABLE_NAME, null, values);
        //Log.d("ODDC INSERTSQL","mediaURI="+values.get(DBschema.M_URI));

        return  true;
    }


    // YYMMDD_HHMMSS.mp4
    public long checkFileSpace(){
        long availSpace = mVideoFolder.getUsableSpace();

        //Log.d("ODDC CHECKFILESPACE","MIN_AVAIL_FS="+String.valueOf(Constants.ODDCApp.MIN_AVAIL_FS)+" availSpace="+String.valueOf(availSpace));

        if (availSpace > Constants.ODDCApp.MIN_AVAIL_FS) return availSpace;
        else  {
            String[] columns = new String[]{DBschema.TS,DBschema.GS_E,DBschema.FCW_E,DBschema.FCW_CI,DBschema.LDW_E,DBschema.M_URI,DBschema.M_D,DBschema            String selection = new String("MediaURI NOT IN ( select MediaURI from oddc where GShockEvent = 1 or FCWEvent = 1 or LDWEvent = 1 or MediaDeleted = 1)");
            String limit = new String("2");
            Cursor c = db.query (true,
                    DBschema.TABLE_NAME,
                    columns,
                    selection,
                    null,
                    null,
                    null,
                    null,
                    limit);

            int nrows = c.getCount();
            Log.d("ODDC CHECKFILESPACE", "Cursor.nrows="+nrows);
            if (nrows > 0) {
                int i = 0;
                while (c.moveToNext()) {
                    String fname = c.getString(5);
                    File f = new File(mVideoFolder.getAbsolutePath() + File.separatorChar + fname);
                    Log.d("ODDC CHECKFILESPACE","f="+f.toString()+" f.exists="+f.exists());
                    if (f.exists()) {
                        f.delete();
                        Log.d("ODDC CHECKFILESPACE","file DELETED "+f.toString());
                        String sqlStmt = "update oddc set MediaDeleted = 1 where MediaURI = \'"+fname+"\' ";
                        db.execSQL(sqlStmt);
                    }
                    Log.d("ODDC CHECKFILESPACE", "DELETEFILE "+c.getString(0) + " GS_E="+c.getInt(1)+" FCW_E="+c.getInt(2)+" FCW_CI="+c.getInt(3)+" LDW_E="+c.getInt(4)+" MediaURI=" + c.getString(5) + " MediaDeleted=" + c.getInt(6) + " MediaUploaded=" + c.getInt(7));
                }
                c.close();
            }
            return -1; // delete some files
        }
    }


    public class SendToFLA extends Thread {
        DataPackageType ptype;
        String uuidSID;
        String mediaURI;
        boolean cdEvent = false;
        public SendToFLA(DataPackageType pt, UUID sid,String muri){
            super ("SendToFLA");
            this.ptype = pt;
            this.uuidSID = sid.toString();
            this.mediaURI = muri;
        }
        public void run(){
            Log.d("ODDC","ODDCClass.SendtoFLA.runnnnnnnnnnnnnnnnn sessionID="+uuidSID);
            String selection;
            String[] selectionArgs;
            HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
            String[] columns = new String[]{
                    DBschema.ID,
                    DBschema.SID,
                    DBschema.VIN,
                    DBschema.TS,
                    DBschema.GPS_LON,
                    DBschema.GPS_LAT,
                    DBschema.SPEED,
                    DBschema.SPEED_DT,
                    DBschema.ACC_X,
                    DBschema.ACC_Y,
                    DBschema.ACC_Z,
                    DBschema.GS_E,
                    DBschema.GS_T,
                    DBschema.FCW_EFV,
                    DBschema.FCW_CI,
                    DBschema.FCW_DFV,
                    DBschema.FCW_RSFV,
                    DBschema.FCW_E,
                    DBschema.FCW_ET,
                    DBschema.LDW_DLL,
                    DBschema.LDW_DRL,
                    DBschema.LDW_E,
                    DBschema.M_URI,
                    DBschema.M_D,
                    /*DBschema.M_P,*/
                    DBschema.M_U,
                    DBschema.D_U};















            selectionArgs = new String[]{String.valueOf(curSession), completedVideoFile, String.valueOf(Constants.ODDCApp.SAMPLE_FREQ), String.valueOf(curSession), completedVideoFile};

                selection = new String("select distinct * from oddc where ( sessionID = ? and MediaURI = ? and rowid % ? = 0 ) or ( sessionID = ? and MediaURI = ? and ( GShockEvent = 1 or FCWEvent = 1 or LDWEvent = 1 ) ) ");

            Log.d("ODDC","ODDCClass.SendtoFLA.runnnnnnnnnnnnnnnnn query="+selection);

                Cursor c = db.rawQuery(selection,selectionArgs);


            int nrows = c.getCount();
            Log.d("ODDC","ODDCClass.SendtoFLA.runnnnnnnnnnnnnnnnn sessionID="+uuidSID+" nrows="+String.valueOf(nrows));
            if (nrows > 0)
            {
                int i = 0;
                ArrayList<ContinuousData> dataCollection = new ArrayList<ContinuousData>();
                ContinuousData cd = null;
                while (c.moveToNext()){
                    cd = new ContinuousData();

                    cd.sessionID = UUID.fromString(c.getString( c.getColumnIndex("sessionID") ));
                    cd.vehicleID = c.getString(c.getColumnIndex("vehicleID"));







                    cd.timestamp = c.getString(c.getColumnIndex("timestamp"));
                    cd.longitude = c.getFloat(c.getColumnIndex("longitude"));
                    cd.latitude = c.getFloat(c.getColumnIndex("latitude"));
                    cd.speed = c.getFloat(c.getColumnIndex("Speed"));
                    cd.speedDetectionType = c.getInt(c.getColumnIndex("SpeedDetectionType"));

                    cd.accelerationX = c.getFloat(c.getColumnIndex("AccelerationX"));
                    cd.accelerationX = c.getFloat(c.getColumnIndex("AccelerationY"));
                    cd.accelerationX = c.getFloat(c.getColumnIndex("AccelerationZ"));

                    cd.gShockEvent = ( c.getInt(c.getColumnIndex("GShockEvent")) != 0 ); if (cd.gShockEvent) cdEvent = true;
   
                    cd.fcwExistFV = ( c.getInt(c.getColumnIndex("FCWExistFV")) != 0 );
                    cd.fcwCutIn = ( c.getInt(c.getColumnIndex("FCWCutIn")) != 0 );
                    cd.fcwDistanceToFV = c.getFloat(c.getColumnIndex("FCWDistanceToFV"));
                    cd.fcwRelativeSpeedToFV = c.getFloat(c.getColumnIndex("FCWRelativeSpeedToFV"));
                    cd.fcwEvent = ( c.getInt(c.getColumnIndex("FCWEvent")) != 0 ); if (cd.fcwEvent) cdEvent = true;
                    cd.fcwEventThreshold = c.getFloat(c.getColumnIndex("FCWTEventThreshold"));



                    cd.ldwDistanceToLeftLane = c.getFloat(c.getColumnIndex("LDWDistanceToLeftLane"));
                    cd.ldwDistanceToRightLane = c.getFloat(c.getColumnIndex("LDWDistanceToRightLane"));
                    cd.ldwEvent = ( c.getInt(c.getColumnIndex("LDWEvent")) != 0 ); if (cd.ldwEvent) cdEvent = true;

                    cd.mediaURI = c.getString(c.getColumnIndex("MediaURI"));
                    cd.mediaDeleted = ( c.getInt(c.getColumnIndex("MediaDeleted")) != 0 );
                    /*cd.mediaProtected = ( c.getInt(c.getColumnIndex("MediaProtected")) != 0 );*/
                    cd.mediaUploaded = ( c.getInt(c.getColumnIndex("MediaUploaded")) != 0 );
                    cd.dataUploaded = ( c.getInt(c.getColumnIndex("DataUploaded")) != 0 );
                    dataCollection.add(cd);
                    //Log.d("ODDC SENDTOFLA",cd.sessionID+" "+cd.gpsTimeStamp+" "+cd.gpsTimeStamp+" "+cd.latitude+" "+cd.speed+" "+cd.gShockEvent+" "+cd.fcwEvent+" "+cd.ldwEvent+" "+cd.mediaURI+" "+cd.mediaDeleted+" "+cd.mediaUploaded+" "+cd.dataUploaded);
                   }
                c.close();

                ptype = cdEvent ? DataPackageType.EVENT : DataPackageType.CONTINUOUS;

                DataPackage dataPackage = new DataPackage(); //yz
                Envelope env = new Envelope();
                env.setSessionID(curSession);
                env.setVehicleID(cd.vehicleID);                dataPackage.setEnvelope(env);
                dataPackage.setContinuousData(dataCollection); //yz
                dataPackage.setPackageType(ptype);
                status = controller.postDataPackage(dataPackage); //yz
                if (status == null) listener.sentToFLA(-1);
                else {
                    if (status != HttpStatus.OK) {
                        listener.sentToFLA(-1);
                        Log.e("ODDC ERR","SendToFLA HttpStatus NOT OK");
                    }
                    else {
                        ContentValues cv = new ContentValues();
                        cv.put(DBschema.D_U,true);
                        String whereClause = new String("( sessionID = ? and MediaURI = ? and rowid % ? = 0 ) or ( sessionID = ? and MediaURI = ? and ( GShockEvent = 1 or FCWEvent = 1 or LDWEvent = 1 ) ) ");
                        Log.d("ODDC SENDTOFLA","whereClause="+whereClause);
                        db.update(DBschema.TABLE_NAME,cv,whereClause,selectionArgs);
                	}





                }
                Log.d("ODDC","ODDCClass.SendToFLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
            }
        }
    }

    int selectiveUpload(ODDCTask task){
        HashMap<String, Object> parameters = task.getParameters();
        if (parameters.size() == 0) return -1;

        ArrayList<String> fnames = new ArrayList<String>();
        ArrayList<Video> videos = new ArrayList<Video>();
        for (HashMap.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            if (key.contains("mp4")){
                if (key.compareTo(currentVideoFile) < 0) {
                    fnames.add(key);
                    File evFile = new File(mVideoFolder,key);
                    try {
                        byte[] vData = FileUtils.readFileToByteArray(evFile);
                        videos.add(Video.createDummyVideo(key,vData));
                    } catch (IOException ioe) {
                        Log.e("ODDC", "IOException FileUtils.readFileToByteArray " + key);
                        return -2;
                    }
                }
            }
        }

        DataPackage dataPackage = new DataPackage(); //yz
        dataPackage.setVideos(videos);
        Envelope env = new Envelope(ODDCclass.curSession, listener.getVIN());
        //env.setVehicleID(cd.vehicleID);        dataPackage.setEnvelope(env);
        dataPackage.setPackageType(DataPackageType.SELECTIVE);

        HttpStatus status = controller.postDataPackage(dataPackage); //yz
        if (status == null) listener.sentToFLA(-1);
        else {
            if (status != HttpStatus.OK) {
                listener.sentToFLA(-1);
                Log.e("ODDC ERR","SendToFLA HttpStatus NOT OK");
            }
            else {
                for (String s : fnames) {
                    String sqlStmt = "update oddc set mediaUploaded = 1 where mediaURI = " + s;
                    db.execSQL(sqlStmt);
                }
            }
        }
        return status.value();
    }







}
