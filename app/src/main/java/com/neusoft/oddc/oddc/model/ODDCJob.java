/**
 * Created by yzharchuk on 8/2/2017.
 */

package com.neusoft.oddc.oddc.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

public class ODDCJob
{
    public ODDCJob() {}
    private String id;
    private UUID sessionId;
    private Timestamp jobTimeStamp;
    private String transportTrigger;
    private String activationTrigger;
    private ArrayList <ODDCTask> tasks;
    private JobStatus status;

    public UUID getSessionId()
    {
        return sessionId;
    }
    public void setSessionId(UUID sessionId)
    {
        this.sessionId = sessionId;
    }
    public Timestamp getJobTimestamp()
    {
        return jobTimeStamp;
    }
    public void setJobTimeStamp(Timestamp jobTimeStamp)
    {
        this.jobTimeStamp = jobTimeStamp;
    }
    public JobStatus getStatus()
    {
        return status;
    }
    public void setStatus(JobStatus status)
    {
        this.status = status;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getTransportTrigger()
    {
        return transportTrigger;
    }
    public void setTransportTrigger(String transportTrigger)
    {
        this.transportTrigger = transportTrigger;
    }
    public String getActivationTrigger()
    {
        return activationTrigger;
    }
    public void setActivationTrigger(String activationTrigger)
    {
        this.activationTrigger = activationTrigger;
    }
    public ArrayList<ODDCTask> getTasks()
    {
        return tasks;
    }
    public void setTasks(ArrayList<ODDCTask> tasks)
    {
        this.tasks = tasks;
    }
}