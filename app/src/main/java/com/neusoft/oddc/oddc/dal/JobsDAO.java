package com.neusoft.oddc.oddc.dal;
import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.oddc.model.ODDCJob;
import java.util.ArrayList;

/**
 * Created by yzharchuk on 8/8/2017.
 */

public class JobsDAO
{
    private DBHelper dbHelper;

    public JobsDAO()
    {
        dbHelper = new DBHelper(NeusoftHandler.getContext());
    }

    public void insertJobs(ArrayList<ODDCJob> jobs)
    {
        dbHelper.insertJobs(jobs);
    }
    public ODDCJob getJob(String jobId)
    {
        ODDCJob job = dbHelper.getJob(jobId);
        return job;
    }
    public ArrayList<ODDCJob> getJobs(ArrayList<String> jobIDs)
    {
        ArrayList<ODDCJob> jobs = dbHelper.getJobs(jobIDs);
        return jobs;
    }




}
