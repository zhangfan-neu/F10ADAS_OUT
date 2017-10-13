package com.neusoft.oddc.oddc.neusoft;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by yzharchuk on 8/8/2017.
 */

public class JobManager
{
    private RESTController restController;
    private Envelope envelope = new Envelope(ODDCclass.session,Constants.ODDCApp.VIN);

    private boolean isProcessingJobs = false;
    private Timer pingTimer;
    Timer singlePingTimer;
    private int pingFrequency = 10000;
    private ODDCclass oddc;

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

        //TODO: Clean this up....
//        ODDCTask task = ODDCTask.createMockTask(envelope);
//        ArrayList<ODDCJob> jobs = getJobList(task);
//        Map<String, Object> parameters = jobs.get(0).getTasks().get(0).getParameters();
//        String vehicleId = (String)parameters.get("vehicleid");
//        ODDCclass.session = UUID.fromString((String)parameters.get("session"));
//        envelope.setSessionID(ODDCclass.session);
    }
    public boolean isAdasEnabled()
    {
        return adasEnabled;
    }
    public void setODDC(ODDCclass oddc)
    {
        this.oddc = oddc;
    }
    public void setPingFrequency(int value)
    {
        pingFrequency = value;
    }
    public static JobManager getInstance()
    {
        return instance;
    }

    public static ODDCTask getJobRequestTask()
    {
        ODDCTask task = new ODDCTask();

        task.setType(TaskType.JOB_REQUEST);
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        //TODO: Will need to pull real GPS coordinates...
        parameters.put("longitude", -118.308784484863);
        parameters.put("latitude", 33.8489532470703);

        //Use VIN stored on the device.
        String vin = instance.getVehicleID();
        if(vin != null && !vin.isEmpty())
        {
            parameters.put("vehicleID", vin);
        }
        else
        {
            //NOTE: Only used for testing purposes.
            parameters.put("vehicleID", Constants.ODDCApp.VIN);

            //TODO: Uncomment this when we are no longer testing...
            //return null;
        }

        task.setVehicleID(vin);
        return task;
    }

    private String getVehicleID()
    {
        String vin = "";
        VehicleProfileEntity entity;
        ADASHelper nsfh = new ADASHelper();
        VehicleProfileEntityDao vehicleProfileEntityDao= ((MyApplication) MyApplication.currentActivity.getApplication()).getDaoSession().getVehicleProfileEntityDao();
        ArrayList<VehicleProfileEntity> list = (ArrayList<VehicleProfileEntity>) vehicleProfileEntityDao.queryBuilder()
                .where(VehicleProfileEntityDao.Properties.Key_user.eq("")).list();
        if (null != list && list.size() > 0)
        {
            entity = list.get(0);
            vin = entity.getVin();
        }
        else
        {
            // Set vin read from odb2
            vin = nsfh.getvin();
        }

        return vin;
    }

    private ArrayList<ODDCJob> getJobsFromServer(ODDCTask task)
    {
        Utilities.showToastMessage("Reqesting Job List");

        ArrayList<ODDCJob>jobs = restController.getJobList(task);
        return jobs;
    }

    public ArrayList<ODDCJob> getJobList(ODDCTask task)
    {
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
            ODDCclass.session = UUID.fromString((String)parameters.get("session"));
            envelope.setSessionID(ODDCclass.session);

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
        //ODDCclass.session = job.getSessionId();

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
//                nsh.resume();
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

        adasEnabled = true;

        //NOTE: In case user is already in the PreviewActivity screen, start recording.
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

        oddc.selectiveUpload(task);
    }

    public void getStartUpJobList()
    {
        ArrayList<ODDCTask> tasks = postCommandCheck(envelope); // checking Server for new tasks
        if (tasks != null && !tasks.isEmpty() && tasks.size() > 0)
        {
            for (ODDCTask task : tasks)
            {
                processTask(task);
            }
        }

        if(tasks != null && tasks.size() > 0)
        {
            Log.d("ODDC STARTUP","getStartUpJobList REPLY "+tasks.size()+" tasks");
        }


        //TODO: Pull Job List from ODDC Server
//        ArrayList<ODDCJob> jobs = null;
//        ODDCTask tempTask = getJobRequestTask();
//        if(tempTask != null)
//        {
//            jobs = getJobsFromServer(tempTask);
//        }
//        else
//        {
//            Utilities.showToastMessage("Invalid VIN - No job list.");
//        }
//
//        //TODO: Process each Job
//        if(jobs != null && jobs.size() > 0)
//        {
//            Log.d("ODDC PINGTIMER","getJobsFromServer REPLY "+jobs.size()+" jobs");
//
//            processJobs(jobs);
//        }
    }

    private ArrayList<ODDCTask> postCommandCheck(Envelope envelope)
    {
        // pingTimer.cancel(); //TODO for testting only. Comment it
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
                ODDCTask task = ODDCTask.createMockTask(envelope);
                ArrayList<ODDCJob> jobs = getJobList(task);

                if(jobs != null)
                {
                    Map<String, Object> parameters = jobs.get(0).getTasks().get(0).getParameters();
                    ODDCclass.session = UUID.fromString((String)parameters.get("session"));
                    envelope.setSessionID(ODDCclass.session);

                    processJobs(jobs);
                }


                if(singlePingTimer != null)
                {
                    singlePingTimer.cancel();
                    singlePingTimer.purge();
                }

                startPingTimer();
            }
        }, 100, pingFrequency);
    }

    public void startPingTimer()
    {
        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                Log.d("ODDC THREAD ","JobManager.PingTimer TID="+String.valueOf(Process.myTid()));
                Utilities.showToastMessage("Requesting Job List");

                ArrayList<ODDCTask> tasks = postCommandCheck(envelope); // checking Server for new tasks
                if (tasks != null && !tasks.isEmpty() && tasks.size() > 0)
                {
                    for (ODDCTask task : tasks)
                    {
                        processTask(task);
                    }
                }

                //TODO: Pull Job List from ODDC Server
//                ArrayList<ODDCJob> jobs = null;
//                ODDCTask tempTask = getJobRequestTask();
//                if(tempTask != null)
//                {
//                    jobs = getJobsFromServer(tempTask);
//                }
//                else
//                {
//                    Utilities.showToastMessage("Invalid VIN - No job list.");
//                }
//
//
                if(tasks != null && tasks.size() > 0)
                {
                    Log.d("ODDC PINGTIMER","startPingTimer REPLY "+tasks.size()+" tasks");
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
