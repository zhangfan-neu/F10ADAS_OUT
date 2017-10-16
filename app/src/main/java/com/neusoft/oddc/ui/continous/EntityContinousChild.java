package com.neusoft.oddc.ui.continous;


import java.util.ArrayList;

public class EntityContinousChild {

    private int index;
    private String sessionId;
    private String fileName;
    private ArrayList<Integer> eventTypes;
    private boolean mediaUploaded;
    private boolean mediaDeleted;
    private boolean dataUploaded;
    private String time;

    public EntityContinousChild() {

    }

    public EntityContinousChild(int index, String sessionId, String fileName, ArrayList<Integer> eventTypes, boolean mediaUploaded, boolean mediaDeleted, boolean dataUploaded, String time) {
        this.index = index;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.eventTypes = eventTypes;
        this.mediaUploaded = mediaUploaded;
        this.mediaDeleted = mediaDeleted;
        this.dataUploaded = dataUploaded;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Integer> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(ArrayList<Integer> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public boolean isMediaUploaded() {
        return mediaUploaded;
    }

    public void setMediaUploaded(boolean mediaUploaded) {
        this.mediaUploaded = mediaUploaded;
    }

    public boolean isMediaDeleted() {
        return mediaDeleted;
    }

    public void setMediaDeleted(boolean mediaDeleted) {
        this.mediaDeleted = mediaDeleted;
    }

    public boolean isDataUploaded() {
        return dataUploaded;
    }

    public void setDataUploaded(boolean dataUploaded) {
        this.dataUploaded = dataUploaded;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
