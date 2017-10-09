package com.neusoft.oddc.oddc.neusoft;

import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.model.DataPackageType;
import com.neusoft.oddc.oddc.neusoft.PlaybackList;

import java.util.ArrayList;



public interface ODDCinterface {
    public boolean onContinuousData(ContinuousData data);
    public ArrayList<PlaybackList> getPlaybackList();
    public ArrayList<LogData> getLog(DataPackageType t);
    public boolean ok2Startup();
    public boolean reqShutdown();

}