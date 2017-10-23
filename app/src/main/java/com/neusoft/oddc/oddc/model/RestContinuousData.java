package com.neusoft.oddc.oddc.model;

import com.neusoft.oddc.oddc.utilities.Utilities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
/**
 * The persistent class for the continuousdata database table.
 * 
 */

public class RestContinuousData implements Serializable
{
	private UUID id;
	private UUID sessionid;
	private UUID packageid;
	private String vehicleid;
	private double longitude;
	private double latitude;
	private double speed;
	private int speeddetectiontype;
	private double accelerationx;
	private double accelerationy;
	private double accelerationz;
	private boolean gshockevent;
	private double gshockeventthreshold;
	private boolean fcwexistfv;
	private boolean fcwcutin;
	private double fcwdistancetofv;
	private double fcwrelativespeedtofv;
	private boolean fcwevent;
	private double fcwteventthreshold;
	private double ldwdistancetoleftlane;
	private double ldwdistancetorightlane;
	private boolean ldwevent;
	private String mediauri;
	private String timestamp;

	public RestContinuousData()
	{
	}

	public RestContinuousData(UUID id, UUID sessionid, UUID packageid, String vehicleid, double longitude, double latitude, double speed, int speeddetectiontype, double accelerationx, double accelerationy, double accelerationz, boolean gshockevent, double gshockeventthreshold, boolean fcwexistfv,
			boolean fcwcutin, double fcwdistancetofv, double fcwrelativespeedtofv, boolean fcwevent, double fcwteventthreshold, double ldwdistancetoleftlane, double ldwdistancetorightlane, boolean ldwevent, String mediauri, String timestamp)
	{
		super();
		this.id = id;
		this.sessionid = sessionid;
		this.packageid = packageid;
		this.vehicleid = vehicleid;
		this.longitude = longitude;
		this.latitude = latitude;
		this.speed = speed;
		this.speeddetectiontype = speeddetectiontype;
		this.accelerationx = accelerationx;
		this.accelerationy = accelerationy;
		this.accelerationz = accelerationz;
		this.gshockevent = gshockevent;
		this.gshockeventthreshold = gshockeventthreshold;
		this.fcwexistfv = fcwexistfv;
		this.fcwcutin = fcwcutin;
		this.fcwdistancetofv = fcwdistancetofv;
		this.fcwrelativespeedtofv = fcwrelativespeedtofv;
		this.fcwevent = fcwevent;
		this.fcwteventthreshold = fcwteventthreshold;
		this.ldwdistancetoleftlane = ldwdistancetoleftlane;
		this.ldwdistancetorightlane = ldwdistancetorightlane;
		this.ldwevent = ldwevent;
		this.mediauri = mediauri;
		this.timestamp = timestamp;
	}

	public UUID getPackageid()
	{
		return packageid;
	}

	public void setPackageid(UUID packageid)
	{
		this.packageid = packageid;
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public UUID getSessionid()
	{
		return sessionid;
	}

	public void setSessionid(UUID sessionid)
	{
		this.sessionid = sessionid;
	}

	public String getVehicleid()
	{
		return vehicleid;
	}

	public void setVehicleid(String vehicleid)
	{
		this.vehicleid = vehicleid;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public double getSpeed()
	{
		return speed;
	}

	public void setSpeed(double speed)
	{
		this.speed = speed;
	}

	public int getSpeeddetectiontype()
	{
		return speeddetectiontype;
	}

	public void setSpeeddetectiontype(int speeddetectiontype)
	{
		this.speeddetectiontype = speeddetectiontype;
	}

	public double getAccelerationx()
	{
		return accelerationx;
	}

	public void setAccelerationx(double accelerationx)
	{
		this.accelerationx = accelerationx;
	}

	public double getAccelerationy()
	{
		return accelerationy;
	}

	public void setAccelerationy(double accelerationy)
	{
		this.accelerationy = accelerationy;
	}

	public double getAccelerationz()
	{
		return accelerationz;
	}

	public void setAccelerationz(double accelerationz)
	{
		this.accelerationz = accelerationz;
	}

	public boolean isGshockevent()
	{
		return gshockevent;
	}

	public void setGshockevent(boolean gshockevent)
	{
		this.gshockevent = gshockevent;
	}

	public double getGshockeventthreshold()
	{
		return gshockeventthreshold;
	}

	public void setGshockeventthreshold(double gshockeventthreshold)
	{
		this.gshockeventthreshold = gshockeventthreshold;
	}

	public boolean isFcwexistfv()
	{
		return fcwexistfv;
	}

	public void setFcwexistfv(boolean fcwexistfv)
	{
		this.fcwexistfv = fcwexistfv;
	}

	public boolean isFcwcutin()
	{
		return fcwcutin;
	}

	public void setFcwcutin(boolean fcwcutin)
	{
		this.fcwcutin = fcwcutin;
	}

	public double getFcwdistancetofv()
	{
		return fcwdistancetofv;
	}

	public void setFcwdistancetofv(double fcwdistancetofv)
	{
		this.fcwdistancetofv = fcwdistancetofv;
	}

	public double getFcwrelativespeedtofv()
	{
		return fcwrelativespeedtofv;
	}

	public void setFcwrelativespeedtofv(double fcwrelativespeedtofv)
	{
		this.fcwrelativespeedtofv = fcwrelativespeedtofv;
	}

	public boolean isFcwevent()
	{
		return fcwevent;
	}

	public void setFcwevent(boolean fcwevent)
	{
		this.fcwevent = fcwevent;
	}

	public double getFcwteventthreshold()
	{
		return fcwteventthreshold;
	}

	public void setFcwteventthreshold(double fcwteventthreshold)
	{
		this.fcwteventthreshold = fcwteventthreshold;
	}

	public double getLdwdistancetoleftlane()
	{
		return ldwdistancetoleftlane;
	}

	public void setLdwdistancetoleftlane(double ldwdistancetoleftlane)
	{
		this.ldwdistancetoleftlane = ldwdistancetoleftlane;
	}

	public double getLdwdistancetorightlane()
	{
		return ldwdistancetorightlane;
	}

	public void setLdwdistancetorightlane(double ldwdistancetorightlane)
	{
		this.ldwdistancetorightlane = ldwdistancetorightlane;
	}

	public boolean isLdwevent()
	{
		return ldwevent;
	}

	public void setLdwevent(boolean ldwevent)
	{
		this.ldwevent = ldwevent;
	}

	public String getMediauri()
	{
		return mediauri;
	}

	public void setMediauri(String mediauri)
	{
		this.mediauri = mediauri;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(String timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public String toString()
	{
		return "ContinuousData [id=" + id + ", sessionid=" + sessionid + ", packageid=" + packageid + ", vehicleid=" + vehicleid + ", longitude=" + longitude + ", latitude=" + latitude + ", speed=" + speed + ", speeddetectiontype=" + speeddetectiontype + ", accelerationx=" + accelerationx
				+ ", accelerationy=" + accelerationy + ", accelerationz=" + accelerationz + ", gshockevent=" + gshockevent + ", gshockeventthreshold=" + gshockeventthreshold + ", fcwexistfv=" + fcwexistfv + ", fcwcutin=" + fcwcutin + ", fcwdistancetofv=" + fcwdistancetofv + ", fcwrelativespeedtofv="
				+ fcwrelativespeedtofv + ", fcwevent=" + fcwevent + ", fcwteventthreshold=" + fcwteventthreshold + ", ldwdistancetoleftlane=" + ldwdistancetoleftlane + ", ldwdistancetorightlane=" + ldwdistancetorightlane + ", ldwevent=" + ldwevent + ", mediauri=" + mediauri + ", timestamp="
				+ timestamp + "]";
	}
}
