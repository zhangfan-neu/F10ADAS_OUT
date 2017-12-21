package com.neusoft.oddc.oddc.neusoft;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.util.Util;
import com.neusoft.oddc.MyApplication;
import com.neusoft.oddc.activity.PreviewActivity;
import com.neusoft.oddc.adas.ADASHelper;
import com.neusoft.oddc.db.dbentity.VehicleProfileEntity;
import com.neusoft.oddc.db.gen.VehicleProfileEntityDao;
import com.neusoft.oddc.oddc.model.Envelope;
import com.neusoft.oddc.oddc.model.ODDCJob;
import com.neusoft.oddc.oddc.model.ODDCTask;
import com.neusoft.oddc.oddc.model.TaskType;
import com.neusoft.oddc.oddc.restclient.RESTController;
import com.neusoft.oddc.oddc.utilities.Utilities;
import com.neusoft.oddc.NeusoftHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by yzharchuk on 8/8/2017.
 */

public class JobManager
{
    private RESTController restController;
    //TODO: Remove fixed VIN information
    private Envelope envelope = new Envelope(ODDCclass.curSession, Utilities.getVehicleID());
    private boolean isProcessingJobs = false;
    private Timer pingTimer;
    private Timer singlePingTimer;
    private int pingFrequency = 8000; // 10000
    private int pingSessionFrequency = 5000;
    private ODDCclass oddc;
    private NeusoftHandler nsh;

    public void setAdasEnabled(boolean adasEnabled)
    {
        this.adasEnabled = adasEnabled;
    }

    private boolean adasEnabled = false;

    private static JobManager instance;
    private String baseUrl;

    public JobManager(String url)
    {
        instance = this;
        baseUrl = url;
        restController = new RESTController(url);
    }

    public boolean isAdasEnabled()
    {
        return adasEnabled;
    }
    public void setODDC(ODDCclass oddc)
    {
        this.oddc = oddc;
    }
    public void setNSH(NeusoftHandler n){this.nsh = n;}
    public void setPingFrequency(int value)
    {
        pingFrequency = value;
    }
    public static JobManager getInstance()
    {
        return instance;
    }

    private ArrayList<ODDCJob> getJobsFromServer(ODDCTask task)
    {
        //Utilities.showToastMessage("Reqesting Job List");
        Log.w("ODDC","JobManager.getJobsFromServer");
        ArrayList<ODDCJob>jobs = restController.getJobList(task);

        return jobs;
    }

    public ArrayList<ODDCJob> getJobList(ODDCTask task)
    {
        Log.w("ODDC","JobManager.getJobList");
        return getJobsFromServer(task);
    }

    public void processJobs(ArrayList<ODDCJob> jobs)
    {
        if(isProcessingJobs)
        {
            return;
        }

        isProcessingJobs = true;

        if(jobs.size() > 0)
        {
            Map<String, Object> parameters = jobs.get(0).getTasks().get(0).getParameters();
            ODDCclass.curSession = UUID.fromString((String)parameters.get("session"));
            envelope.setSessionID(ODDCclass.curSession);

            for(ODDCJob job: jobs)
            {
                processJob(job);
            }

            isProcessingJobs = false;
        }
    }

    private void processJob(ODDCJob job)
    {
        ArrayList <ODDCTask> tasks = job.getTasks();

        if(tasks.size() > 0)
        {
            for(ODDCTask task : tasks)
            {
                processTask(task);
            }
        }
    }

    public void processTask(ODDCTask task)
    {
        Log.w("ODDC","JobManager.processTask "+task.getType());
        switch (task.getType())
        {
            case SELECTIVE_UPLOAD:
                processSelectiveUploadTask(task);
                break;
            case UPLOAD:
                processUploadTask();
                break;
            case STOP:
                processStopTask();
                break;
            case TERMINATE:
                processTerminateTask();
                break;
            case RESUME:
                processResumedTask();
                break;
            default:
                Log.d("processTask", "No tasks from Server.");
        }
    }

    private void processResumedTask()
    {
        //TODO FTCA implementation here
        Log.d("JobManager", "processResumedTask");
        Utilities.showToastMessage("Process Task: Resume");

        if(oddc == null)
        {
            Log.d("processResumedTask", "ODDC is not initialized.");
            return;
        }

//        nsh.resume();
    }

    private void processStopTask()
    {
        Log.d("JobManager", "processStopTask");
        Utilities.showToastMessage("Process Task: Stop");

        adasEnabled = false;

        PreviewActivity previewActivity = PreviewActivity.getInstance();
        if(previewActivity != null)
        {
            previewActivity.endRecording();
        }
    }

    private void processTerminateTask()
    {
        //TODO FTCA implementation here
        Log.d("JobManager", "processTerminateTask");

        if(oddc == null)
        {
            Log.d("processTerminateTask", "ODDC is not initialized.");
            return;
        }
    }

    private void processUploadTask()
    {
        //TODO FTCA implementation here
        Log.d("JobManager", "processUploadTask");

        Utilities.showToastMessage("Process Task: Upload");
        PreviewActivity pa = PreviewActivity.getInstance();
        if (pa != null) pa.onAnimate(PreviewActivity.IconType.IT_UL, PreviewActivity.IconState.IS_SND_OK);

        adasEnabled = true;

        //USE CASE: In case user is already in the PreviewActivity screen, start recording.
        PreviewActivity previewActivity = PreviewActivity.getInstance();
        if(previewActivity != null)
        {
            previewActivity.beginRecording();
        }
    }

    private void processParametersTask()
    {
        //TODO FTCA implementation here
        Log.d("JobManager", "processParametersTask");

        if(oddc == null)
        {
            Log.d("processParametersTask", "ODDC is not initialized.");
            return;
        }

        //TODO: Parse the parameter list

        //TODO: Call ODDC to modify parameter value based on the parameter name...
    }

    private void processPropertiesTask()
    {
        //TODO FTCA implementation here
        Log.d("JobManager", "processPropertiesTask");

        if(oddc == null)
        {
            Log.d("processPropertiesTask", "ODDC is not initialized.");
            return;
        }

        //TODO: Parse the Properties list and corresponding value(s)
        //      Part of the parameters for this command should include the file name of the media file.

        //TODO: Call ODDC to move the file(s) to/from protected folder based on boolean value
        //      True - Move to Protected Folder: Files in this folder will not be deleted during File Manager clean up
        //      False - Move to Normal Folder: Files in this folder can be deleted during File Manager clean up
    }

    private void processSelectiveUploadTask(ODDCTask task)
    {
        Utilities.showToastMessage("Process Task: Selective");

        PreviewActivity pa = PreviewActivity.getInstance();
        if (pa != null) pa.onAnimate(PreviewActivity.IconType.IT_SEL, PreviewActivity.IconState.IS_SND_OK);

        oddc.selectiveUpload(task);
    }

    private ArrayList<ODDCTask> postCommandCheck(Envelope envelope)
    {
        // pingTimer.cancel(); //TODO for testing only. Comment it
        RESTController controller = new RESTController(baseUrl);
        ArrayList<ODDCTask> tasks = controller.postCommandCheck(envelope);
        return tasks;
    }

    public Envelope getEnvelope()
    {
        return envelope;
    }



    public void requestInitialSessionId()
    {
        singlePingTimer = new Timer();
        singlePingTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                //The following retrieves data from VehicleProfileEntityDao and the VIN from OBD-2.
                String vin = Utilities.getVehicleID();

                String csStr = ODDCclass.curSession == null ? "null" : ODDCclass.curSession.toString();
                Log.w("ODDC","JobManager.requestInitialSessionId.run BoM curSession=" + csStr + " vin="+vin);

                if(!vin.isEmpty())
                {
                    envelope = new Envelope(ODDCclass.curSession, vin);

                    ODDCTask task = ODDCTask.createJobRequestTask(envelope);
                    getJobList(task);

                    //TODO: Delete this later when functionality has been verified bug free.
//                    if(jobs != null)
//                    {
//                        Map<String, Object> parameters = jobs.get(0).getTasks().get(0).getParameters();
//                        ODDCclass.curSession = UUID.fromString((String)parameters.get("session"));
//                        envelope.setSessionID(ODDCclass.curSession);
//
//                        processJobs(jobs);
//                    }

                    if(singlePingTimer != null)
                    {
                        singlePingTimer.cancel();
                        singlePingTimer.purge();
                    }
                    Log.w("ODDC","JobManager.requestInitialSessionId.run CALLing startPingTimer");
                    startPingTimer();
                }

                //TODO: Delete this later when functionality has been verified bug free.
//                if(!vin.isEmpty())
//                {
//                    ODDCTask task = ODDCTask.createMockTask(envelope);
//                    ArrayList<ODDCJob> jobs = getJobList(task);
//
//                    if(jobs != null)
//                    {
//                        Map<String, Object> parameters = jobs.get(0).getTasks().get(0).getParameters();
//                        ODDCclass.session = UUID.fromString((String)parameters.get("session"));
//                        envelope.setSessionID(ODDCclass.session);
//
//                        processJobs(jobs);
//                    }
//
//                    if(singlePingTimer != null)
//                    {
//                        singlePingTimer.cancel();
//                        singlePingTimer.purge();
//                    }
//
////                    startPingTimer();
//                }

                //NOTE: Continue to check for VIN from OBD-2 or Vehicle Profile DAO...
            }
        }, 100, pingSessionFrequency);
    }



    public void startPingTimer()
    {
        Log.w("ODDC","JobManager.startPingTimer BoM");

        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {

            @Override
            public void run()
            {
                Utilities.showToastMessage("Requesting Job List");

                PreviewActivity pa = PreviewActivity.getInstance();

                ArrayList<ODDCTask> tasks = postCommandCheck(envelope); // checking Server for new tasks
                if (tasks != null && !tasks.isEmpty() && tasks.size() > 0)
                {
                    pa = PreviewActivity.getInstance();
                    if (pa != null) pa.onAnimate(PreviewActivity.IconType.IT_JM, PreviewActivity.IconState.IS_RCV);

                    ODDCTask task = tasks.get(0);
                    if(task != null)
                    {
                        try {
                            String sessionId;
                            Object obj = task.getParameters();
                            Map<String, Object> parameters = ((Map<String, Object>) obj);

                            String taskERR = parameters.get("taskERR").toString();
                            Log.w("ODDC", "JobManager.PingTimer.run taskERR=" + taskERR);
                            Log.w("ODDC", "JobManager.PingTimer.run taskERR.compareTo2="+taskERR.compareTo("2"));

                            if (taskERR.compareTo("1") == 0 || taskERR.compareTo("2") == 0) {
                                if (pa != null) pa.onAnimate(PreviewActivity.IconType.IT_JM, PreviewActivity.IconState.IS_SND_ERR);
                            } else {
                                if (pa != null) pa.onAnimate(PreviewActivity.IconType.IT_JM, PreviewActivity.IconState.IS_SND_OK);
                                Map<String, Object> map = (Map<String, Object>) parameters.get("envelope");
                                if (map != null) {
                                    sessionId = map.get("sessionID").toString();
                                    ODDCclass.curSession = UUID.fromString(sessionId);
                                    envelope.setSessionID(ODDCclass.curSession);
                                    Log.w("ODDC", "JobManager.PingTimer.run sessionID=" + sessionId);
                                }


                                for (ODDCTask taskItem : tasks) {
                                    processTask(taskItem);
                                }
//                            if(taskEnvelope != null)
//                            {
//                                ODDCclass.curSession = UUID.fromString(sessionId);
////                                envelope.setSessionID(ODDCclass.curSession);
//
//                                for (ODDCTask taskItem : tasks)
//                                {
//                                    processTask(taskItem);
//                                }
//                            }
//                            else
//                            {
//                                Log.e("startPingTimer - ","Envelope is NULL.");
//                            }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        Log.e("startPingTimer - ","Parameter is NULL.");
                    }
                }
            }
        }, 1000, pingFrequency);
    }

    public void stopPingTimer()
    {
        if(pingTimer != null)
        {
            pingTimer.cancel();
            pingTimer.purge();
        }
    }
}
