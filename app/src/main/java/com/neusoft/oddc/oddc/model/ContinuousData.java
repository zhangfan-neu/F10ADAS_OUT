/**
 * Created by yzharchuk on 8/1/2017.
 */

package com.neusoft.oddc.oddc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.neusoft.oddc.oddc.neusoft.Constants;
import com.neusoft.oddc.oddc.utilities.Utilities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;


public class ContinuousData
{
    @JsonProperty("id")
    public UUID id = null;
    @JsonProperty("sessionID")
    public UUID sessionID = null;
    @JsonProperty("vehicleID")
    public String vehicleID = null;

    @JsonProperty("timestamp")
    public String timestamp = Utilities.getTimestamp();

    @JsonProperty("longitude")
    public double longitude = 0.0;
    @JsonProperty("latitude")
    public double latitude = 0.0;
    @JsonProperty("speed")
    public double speed = 0.0;
    @JsonProperty("speedDetectionType")
    public int speedDetectionType = 0;
	@JsonProperty("accelerationX")
	public double accelerationX = 0.0;
    @JsonProperty("accelerationY")
    public double accelerationY = 0.0;
    @JsonProperty("accelerationZ")
    public double accelerationZ = 0.0;

    @JsonProperty("gShockEvent")
    public boolean gShockEvent = false;
    @JsonProperty("gShockEventThreshold")
    public double gShockEventThreshold = 2.0;

    @JsonProperty("fcwExistFV")
    public boolean fcwExistFV = false;
    @JsonProperty("fcwCutIn")
    public boolean fcwCutIn = false;
    @JsonProperty("fcwDistanceToFV")
    public double fcwDistanceToFV = 0.0;
    @JsonProperty("fcwRelativeSpeedToFV")
    public double fcwRelativeSpeedToFV = 0.0;
    @JsonProperty("fcwEvent")
    public boolean fcwEvent = false;
    @JsonProperty("fcwEventThreshold")
    public double fcwEventThreshold = 0.0;

    @JsonProperty("ldwDistanceToLeftLane")
    public double ldwDistanceToLeftLane = 0.0;
    @JsonProperty("ldwDistanceToRightLane")
    public double ldwDistanceToRightLane = 0.0;
    @JsonProperty("ldwEvent")
    public boolean ldwEvent = false;

    @JsonProperty("mediaURI")
    public String mediaURI = " ";

    //yz I did not touch them
    @JsonIgnore
    public boolean mediaDeleted = false;
    /*@JsonIgnore
    public boolean mediaProtected = false;*/
    @JsonIgnore
    public boolean mediaUploaded = false;
    @JsonIgnore
    public boolean dataUploaded = false;

    public boolean isEvent(){
        if (gShockEvent || fcwEvent || ldwEvent)
            return true;
        else
            return false;
    }



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public boolean isLdwEvent() {
        return ldwEvent;
    }

    public void setLdwEvent(boolean ldwEvent) {
        this.ldwEvent = ldwEvent;
    }

    public String getMediaURI() {
        return mediaURI;
    }

    public void setMediaURI(String mediaURI) {
        this.mediaURI = mediaURI;
    }
}
