package com.neusoft.oddc.oddc.neusoft;

import com.neusoft.oddc.oddc.model.EventType;

import java.sql.Timestamp;


public class LogList {
    public String sessionID;
    public EventType eventType;
    public Timestamp timeStamp;
    public String filename;
    public boolean mediaDeleted;
    public boolean mediaUploaded;
    public boolean dataUploaded;
}
