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

    public Envelope() {}
    public Envelope(UUID sessionID, String vehicleID) {
        this.sessionID = sessionID;
        this.vehicleID = vehicleID;
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
}



