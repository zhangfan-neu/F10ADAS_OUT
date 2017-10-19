package com.neusoft.oddc.oddc.model;

import java.util.ArrayList;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neusoft.oddc.oddc.utilities.Utilities;

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
	
	public static void main(String[] args)
	{
		//DataPackage dataPackage = DataPackage.createDummyDataPackage("1B3LC46R38N557181", UUID.fromString("aa73c820-ab4f-47ec-b5cf-9b52d3cded4a"), DataPackageType.CONTINUOUS, 60);
		
	}
	

	static String testVideoFileString;
	public static RestDataPackage createDummyDataPackage(String vin, UUID session, DataPackageType dataPackageType, int numRecords)
    {
        RestDataPackage dataPackage = new RestDataPackage();
        dataPackage.setPackageType(dataPackageType);
        dataPackage.setEnvelope(new Envelope(session, vin));

        if (dataPackageType == DataPackageType.CONTINUOUS || dataPackageType == DataPackageType.EVENT)
		{
        	ArrayList<RestContinuousData> continuousData = new ArrayList<RestContinuousData>(numRecords);
            UUID packID = UUID.randomUUID();
            
            for (int i = 0; i < numRecords; i++)
                continuousData.add(RestContinuousData.createDummyContinuousData(vin, session, packID, dataPackageType == DataPackageType.EVENT ? true : false ));
            
            dataPackage.setContinuousData(continuousData);          
		}       
//        else if(dataPackageType == DataPackageType.SELECTIVE)
//        {
//        	byte[] videoBytes = Utilities.downloadFile(testVideoFileString);
//            int videoRecords = (int) (Math.random() * (5)) + 1;
//            ArrayList<Video> videos = new ArrayList<Video>(videoRecords);
//
//            for (int i = 0; i < videoRecords; i++)
//                videos.add(Video.createDummyVideo(videoBytes));
//
//            dataPackage.setVideos(videos);
//        }
        return dataPackage;
    }
}