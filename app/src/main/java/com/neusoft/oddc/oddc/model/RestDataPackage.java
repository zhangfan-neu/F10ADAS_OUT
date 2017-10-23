package com.neusoft.oddc.oddc.model;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RestDataPackage
{
    @JsonProperty("packageType")
    private DataPackageType packageType;
    @JsonProperty("continuousData")
    private ArrayList<RestContinuousData> continuousData;
    @JsonProperty("videos")
    private ArrayList<Video> videos;
    @JsonProperty("envelope")
    private Envelope envelope;

    public RestDataPackage()
    {
    }

    public RestDataPackage(DataPackageType packageType, ArrayList<RestContinuousData> continuousData, ArrayList<Video> videos, Envelope envelope) {
        this.packageType = packageType;
        this.continuousData = continuousData;
        this.videos = videos;
        this.envelope = envelope;
    }
    public DataPackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(DataPackageType packageType) {
        this.packageType = packageType;
    }

    public ArrayList<RestContinuousData> getContinuousData() {
        return continuousData;
    }

    public void setContinuousData(ArrayList<RestContinuousData> continuousData) {
        this.continuousData = continuousData;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

	@Override
	public String toString()
	{
		return "DataPackage [packageType=" + packageType + ", continuousData=" + continuousData + ", videos=" + videos + ", envelope=" + envelope + "]";
	}
}