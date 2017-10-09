/**
 * Created by yzharchuk on 8/2/2017.
 */

package com.neusoft.oddc.oddc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Envelope
{
    @JsonProperty("sessionID")
    private UUID sessionID;
    @JsonProperty("vehicleID")
    private String vehicleID;
    @JsonProperty("driverID")
    private String driverID;
    @JsonProperty("submitterID")
    private String submitterID;

    public Envelope() {}
    public Envelope(UUID sessionID, String vehicleID, String driverID, String submitterID) {
        this.sessionID = sessionID;
        this.vehicleID = vehicleID;
        this.driverID = driverID;
        this.submitterID = submitterID;
    }

    public UUID getSessionID()
    {
        return sessionID;
    }
    public void setSessionID(UUID sessionID)
    {
        this.sessionID = sessionID;
    }
    public String getVehicleID()
    {
        return vehicleID;
    }
    public void setVehicleID(String vehicleID)
    {
        this.vehicleID = vehicleID;
    }
    public String getDriverID()
    {
        return driverID;
    }
    public void setDriverID(String driverID)
    {
        this.driverID = driverID;
    }
    public String getSubmitterID()
    {
        return submitterID;
    }
    public void setSubmitterID(String submitterID)
    {
        this.submitterID = submitterID;
    }
}



