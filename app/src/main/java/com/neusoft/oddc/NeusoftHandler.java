package com.neusoft.oddc;

import android.content.Context;
import android.icu.util.TimeZone;
import android.location.Location;
import android.util.Log;

import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.model.DataPackageType;
import com.neusoft.oddc.oddc.neusoft.JobManager;
import com.neusoft.oddc.oddc.neusoft.LogData;
import com.neusoft.oddc.oddc.neusoft.NeuSoftInterface;
import com.neusoft.oddc.oddc.neusoft.ODDCclass;
import com.neusoft.oddc.oddc.neusoft.PlaybackList;
import com.neusoft.oddc.widget.eventbus.EventStartDataCollection;
import com.neusoft.oddc.widget.eventbus.EventStopDataCollection;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NeusoftHandler implements NeuSoftInterface {

    private static final String TAG = ADASHelper.class.getSimpleName();

    private static Context mContext;
    private static ODDCclass oddCclass;
    private static JobManager jobManager;
    public static boolean isOddcOk = false;

    private ADASHelper adasHelper;

    // for data transfer between Neusoft and ODDC
    public void onFLAparam(int param) {
    } // example only at this time, param(s) TBD

    public NeusoftHandler(Context context) {
        mContext = context;
    }

    public NeusoftHandler() {
    }

    public static Context getContext() {
        return mContext;
    }

    public void sentToFLA(int param) {
        Log.d(TAG, "SentToFLA return " + param);
    }

    public void resume() {
        EventBus.getDefault().post(new EventStartDataCollection());
    }

    @Override
    public String getVIN() {
        return ADASHelper.getvin(); // VIN
    }

    @Override
    public Location getLatLong() {
        if (null == adasHelper) {
            return null;
        }
        return adasHelper.getCoarseLocation();
    }

    public void stop() {
        EventBus.getDefault().post(new EventStopDataCollection());
    }


    public void init(Context context) {
        //RMS NOTE: Need to pull server info from one source.  There was a problem where the JobManager was using the
        //          proper URL but the initialization of ODDCclass was using an older one.
        //          I'll see if I have time to put it in the config file and make sure everything points to it.

//        String url = context.getString(R.string.base_url);
        //String url = PropertyUtil.getServerUrl();
        String url = com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL;
        File videodir = new File(Constants.FILE_PATH);
        oddCclass = new ODDCclass(url, context, videodir);
        oddCclass.setListener(this);
        adasHelper = new ADASHelper(context);

        //Initialize Job Manager
        jobManager = new JobManager(com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL);
        jobManager.setODDC(oddCclass);
        jobManager.setNSH(this);
        oddCclass.setJobManager(jobManager);

        //Start Job Manager ping functionality
        jobManager.startPingTimer();
    }

    public boolean startupOddcClass() {
        if (null != oddCclass) {
            isOddcOk = oddCclass.ok2Startup();
        }
        return isOddcOk;
    }

    public boolean postContinuousData(ContinuousData data) {
        if (null == oddCclass) {
            return false;
        }
        return oddCclass.onContinuousData(data);
    }

    public boolean shutdownOddc() {
        boolean isShutDownSuccess = false;
        if (null != oddCclass) {
            isShutDownSuccess = oddCclass.reqShutdown();
            oddCclass = null;
            isOddcOk = false;

            //Null jobManager instance
            if (jobManager != null) {
                jobManager.stopPingTimer();
                jobManager = null;
            }
        }
        return isShutDownSuccess;
    }

//    public List<String> getContinuousLog() {
//        List<String> list;
//        if (null == oddCclass || !isOddcOk) {
//            Log.e(TAG, "oddc is not ok!");
//            list = new ArrayList<String>();
//        } else {
//            list = oddCclass.getContinuousLog();
//            if (null != list) {
//                Log.d(TAG, "oddc trace -> continuous begin --------------- ");
//                for (String contiuousItem : list) {
//                    Log.d(TAG, "oddc trace -> contiuousItem = " + contiuousItem);
//                }
//                Log.d(TAG, "oddc trace -> continuous end --------------- ");
//            }
//        }
//        return list;
//    }

//    public List<String> getEventLog() {
//        List<String> list;
//        if (null == oddCclass || !isOddcOk) {
//            Log.e(TAG, "oddc is not ok!");
//            list = new ArrayList<String>();
//        } else {
//            list = oddCclass.getEventLog();
//            if (null != list) {
//                Log.d(TAG, "oddc trace -> event begin --------------- ");
//                for (String eventItem : list) {
//                    Log.d(TAG, "oddc trace -> eventItem = " + eventItem);
//                }
//                Log.d(TAG, "oddc trace -> event end --------------- ");
//            }
//        }
//        return list;
//    }


//    public List<String> getOnDemandLog() {
//        List<String> list;
//        if (null == oddCclass || !isOddcOk) {
//            Log.e(TAG, "oddc is not ok!");
//            list = new ArrayList<String>();
//        } else {
//            list = oddCclass.getOnDemandLog();
//            if (null != list) {
//                Log.d(TAG, "oddc trace -> onDemand begin --------------- ");
//                for (String onDemandItem : list) {
//                    Log.d(TAG, "oddc trace -> onDemandItem = " + onDemandItem);
//                }
//                Log.d(TAG, "oddc trace -> onDemand end --------------- ");
//            }
//        }
//        return list;
//    }

    public ArrayList<PlaybackList> getPlaybackList() {
        ArrayList<PlaybackList> list;
        if (null == oddCclass || !isOddcOk) {
            Log.e(TAG, "oddc is not ok!");
            list = new ArrayList<PlaybackList>();
        } else {
            list = oddCclass.getPlaybackList();
            Log.d(TAG, "oddCclass.getPlaybackList over");
            if (null != list && BuildConfig.DEBUG) {
                Log.d(TAG, "oddc trace -> playbacklist begin --------------- ");
                Log.d(TAG, "list size is " + list.size());
                for (PlaybackList mPlaybackList : list) {
                    Log.d(TAG, "oddc trace playbacklist-> MediaURI = " + mPlaybackList.MediaURI);
                    Log.d(TAG, "oddc trace playbacklist-> GShockEvent = " + mPlaybackList.GShockEvent);
                    Log.d(TAG, "oddc trace playbacklist-> FCWEvent = " + mPlaybackList.FCWEvent);
                    Log.d(TAG, "oddc trace playbacklist-> LDWEvent = " + mPlaybackList.LDWEvent);
                    Log.d(TAG, "oddc trace playbacklist-> MediaDeleted = " + mPlaybackList.MediaDeleted);
                }
                Log.d(TAG, "oddc trace -> playbacklist end --------------- ");
            }
        }
        return list;
    }

    public ArrayList<LogData> getLogList(DataPackageType t) {
        ArrayList<LogData> list;
        if (null == oddCclass || !isOddcOk) {
            Log.e(TAG, "oddc is not ok!");
            list = new ArrayList<LogData>();
        } else {
            Log.d(TAG, "oddCclass.getLogData DataPackageType = " + t);
            list = oddCclass.getLog(t);
            Log.d(TAG, "oddCclass.getLogData over");
            if (null != list && BuildConfig.DEBUG) {
                Log.d(TAG, "oddc trace -> LogData begin --------------- ");
                Log.d(TAG, "list size is " + list.size());
                for (LogData mLogData : list) {
                    Log.d(TAG, "oddc trace LogData-> filename = " + mLogData.filename);
                    Log.d(TAG, "oddc trace LogData-> sessionID = " + mLogData.sessionID);
                    Log.d(TAG, "oddc trace LogData-> timeStamp = " + mLogData.timeStamp);
                    Log.d(TAG, "oddc trace LogData-> dataUploaded = " + mLogData.dataUploaded);
                    Log.d(TAG, "oddc trace LogData-> eventType = " + mLogData.eventType);
                    Log.d(TAG, "oddc trace LogData-> mediaDeleted = " + mLogData.mediaDeleted);
                    Log.d(TAG, "oddc trace LogData-> mediaUploaded = " + mLogData.mediaUploaded);
                }
                Log.d(TAG, "oddc trace -> LogData end --------------- ");
            }
        }
        return list;
    }

    public static ContinuousData mkContinuousData(String currentFileName, double accelerationX, double accelerationY, double accelerationZ) {
        String dateTime = new String(getTimestamp());

        // NeuSoft prepares data for transfer somewhere in their code
        ContinuousData cd = new ContinuousData();

        cd.sessionID = ODDCclass.curSession;
        cd.vehicleID = ADASHelper.getvin(); // VIN

        cd.timestamp = dateTime; // from OS not GPS

        Location location = ADASHelper.getCoarseLocation();
        if (null != location) {
            double mLongitude = location.getLongitude();
            double mLatitude = location.getLatitude();
            cd.longitude = mLongitude;
            cd.latitude = mLatitude;
            //cd.gpsTimeStamp = new Timestamp(location.getTime());
        } else {
            cd.longitude = 0;
            cd.latitude = 0;
        }

        cd.speed = ADASHelper.getspd();
        cd.speedDetectionType = 0; // always be ZERO

        cd.accelerationX = accelerationX;
        cd.accelerationX = accelerationX;
        cd.accelerationZ = accelerationZ;

        cd.gShockEvent = false;
        //cd.gShockEventThreshold /* default for now */

        cd.fcwExistFV = false;
        //cd.fcwTimeToCollision = 0;
        cd.fcwDistanceToFV = 0;
        cd.fcwRelativeSpeedToFV = 0;

        cd.fcwEvent = false;
        cd.fcwEventThreshold = 0;

        cd.ldwDistanceToLeftLane = 0;
        cd.ldwDistanceToRightLane = 0;
        cd.ldwEvent = false;
        cd.mediaURI = currentFileName;

        return cd;
    }

    private static String getTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.dateTimeFormat);
        Date date = new Date();
        return dateFormat.format(date);
    }

}
