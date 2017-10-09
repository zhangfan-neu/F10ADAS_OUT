/**
 * Created by yzharchuk on 8/1/2017.
 */

package com.neusoft.oddc.oddc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neusoft.oddc.oddc.neusoft.Constants;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;


public class ContinuousData
{
    public UUID id = null;;
    public UUID sessionID = null;;
    public String vehicleID = null;;
    public String driverID = null;;
    public String submitterID = null;;
    public int tzOffset = 0;
    public String gpsTimeStamp = null;;
    public double longitude = 0.0;
    public double latitude = 0.0;
    public double speed;
    public int speedDetectionType = 0;
    public String accelerationTimeStamp = null;
    public double accelerationX = 0.0;
    public double accelerationY = 0.0;
    public double accelerationZ = 0.0;

    public String gShockTimeStamp = null;;
    public boolean gShockEvent = false;
    public double gShockEventValue = 0.0;
    public double gShockEventThreshold = 0.0;

    public String fcwTimeStamp = null;;
    public boolean fcwExistFV = false;
    public boolean fcwCutIn = false;
    public double fcwDistanceToFV = 0.0;
    public double fcwRelativeSpeedToFV = 0.0;
    public boolean fcwEvent = false;
    public double fcwEventThreshold = 0.0;

    public String ldwTimeStamp = null;;
    public double ldwDistanceToLeftLane = 0.0;
    public double ldwDistanceToRightLane = 0.0;
    public int ldwEvent = 0;
    public String mediaURI = null;

    public boolean mediaDeleted = false;
    public boolean mediaUploaded = false;
    public boolean dataUploaded = false;

    public void ContinuousData()
    {
        //TODO: Initialize with default values...
    }

    public boolean isEvent(){
        if (gShockEvent || fcwEvent || (ldwEvent > 0)) return true;
        else                                           return false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    //public Timestamp getGpsTimeStamp() {return gpsTimeStamp;}
    //public void setGpsTimeStamp(Timestamp gpsTimeStamp) {this.gpsTimeStamp = gpsTimeStamp;}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getSpeedDetectionType() {
        return speedDetectionType;
    }

    public void setSpeedDetectionType(int speedDetectionType) {
        this.speedDetectionType = speedDetectionType;
    }

    //public Timestamp getAccelerationTimeStamp() {return accelerationTimeStamp;}
    //public void setAccelerationTimeStamp(Timestamp accelerationTimeStamp) {this.accelerationTimeStamp = accelerationTimeStamp;}

    public double getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(double accelerationX) {
        this.accelerationX = accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(double accelerationY) {
        this.accelerationY = accelerationY;
    }

    public double getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(double accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    //public Timestamp getgShockTimeStamp() {return gShockTimeStamp;}
    //public void setgShockTimeStamp(Timestamp gShockTimeStamp) {this.gShockTimeStamp = gShockTimeStamp;}

    public boolean isgShockEvent() {
        return gShockEvent;
    }

    public void setgShockEvent(boolean gShockEvent) {
        this.gShockEvent = gShockEvent;
    }

    public double getgShockEventThreshold() {
        return gShockEventThreshold;
    }

    public void setgShockEventThreshold(double gShockEventThreshold) {
        this.gShockEventThreshold = gShockEventThreshold;
    }

    //public Timestamp getFcwTimeStamp() {return fcwTimeStamp;}
    //public void setFcwTimeStamp(Timestamp fcwTimeStamp) {this.fcwTimeStamp = fcwTimeStamp;}

    public boolean isFcwExistFV() {
        return fcwExistFV;
    }

    public void setFcwExistFV(boolean fcwExistFV) {
        this.fcwExistFV = fcwExistFV;
    }

    public boolean isFcwCutIn() {
        return fcwCutIn;
    }

    public void setFcwCutIn(boolean fcwCutIn) {
        this.fcwCutIn = fcwCutIn;
    }

    public double getFcwDistanceToFV() {
        return fcwDistanceToFV;
    }

    public void setFcwDistanceToFV(double fcwDistanceToFV) {
        this.fcwDistanceToFV = fcwDistanceToFV;
    }

    public double getFcwRelativeSpeedToFV() {
        return fcwRelativeSpeedToFV;
    }

    public void setFcwRelativeSpeedToFV(double fcwRelativeSpeedToFV) {
        this.fcwRelativeSpeedToFV = fcwRelativeSpeedToFV;
    }

    public boolean isFcwEvent() {
        return fcwEvent;
    }

    public void setFcwEvent(boolean fcwEvent) {
        this.fcwEvent = fcwEvent;
    }

    public double getFcwEventThreshold() {
        return fcwEventThreshold;
    }

    public void setFcwEventThreshold(double fcwEventThreshold) {
        this.fcwEventThreshold = fcwEventThreshold;
    }

    //public Timestamp getLdwTimeStamp() {return ldwTimeStamp;}
    //public void setLdwTimeStamp(Timestamp ldwTimeStamp) {this.ldwTimeStamp = ldwTimeStamp;}

    public double getLdwDistanceToLeftLane() {
        return ldwDistanceToLeftLane;
    }

    public void setLdwDistanceToLeftLane(double ldwDistanceToLeftLane) {
        this.ldwDistanceToLeftLane = ldwDistanceToLeftLane;
    }

    public double getLdwDistanceToRightLane() {
        return ldwDistanceToRightLane;
    }

    public void setLdwDistanceToRightLane(double ldwDistanceToRightLane) {
        this.ldwDistanceToRightLane = ldwDistanceToRightLane;
    }

    public int isLdwEvent() {
        return ldwEvent;
    }

    public void setLdwEvent(int ldwEvent) {
        this.ldwEvent = ldwEvent;
    }

    public String getMediaURI() {
        return mediaURI;
    }

    public void setMediaURI(String mediaURI) {
        this.mediaURI = mediaURI;
    }

    public static String getTimestamp(){
        SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.ODDCApp.dateTimeFormat );
        Date date = new Date();
        return dateFormat.format(date);
    }
}
