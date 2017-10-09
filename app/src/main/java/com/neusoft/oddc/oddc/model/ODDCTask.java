/**
 * Created by yzharchuk on 8/2/2017.
 */

package com.neusoft.oddc.oddc.model;

import com.neusoft.oddc.oddc.utilities.Utilities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ODDCTask
{
    private String id = null;
    private String vehicleID;
    private TaskType type;
    private TaskStatus status;
    private double latitude;
    private double longitude;
    private Timestamp timestamp;
    private HashMap<String, Object> parameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    public static ODDCTask createMockTask(Envelope envelope)
    {
        ODDCTask task = new ODDCTask();
        //task.id = (UUID.randomUUID().toString());
        //task.vehicleID = Utilities.getAlphaNumericString(17);
        task.type = TaskType.JOB_REQUEST;
        task.status = TaskStatus.UNKNOWN;
        task.latitude = -122.413009;
        task.longitude = 37.774851;
        task.vehicleID = envelope.getVehicleID();
        HashMap<String, Object>	parameters = new HashMap<String, Object>();
//        List<String> fileNames = new ArrayList<String>();
//        for (int i = 0; i < 5; i++)
//            fileNames.add(Utilities.getRandomFileName(".MP4"));
//        parameters.put("selectedFileNames", fileNames);
        parameters.put("envelope", envelope);
        task.parameters = parameters;
        return task;
    }
}