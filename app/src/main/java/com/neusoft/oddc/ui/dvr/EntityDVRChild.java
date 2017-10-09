package com.neusoft.oddc.ui.dvr;


import java.util.ArrayList;

public class EntityDVRChild {

    private int index;
    private String time;
    private String pic;
    private ArrayList<Integer> eventCodes;

    public EntityDVRChild() {

    }

    public EntityDVRChild(int index, String time, String pic, ArrayList<Integer> eventCodes) {
        this.index = index;
        this.time = time;
        this.pic = pic;
        this.eventCodes = eventCodes;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public ArrayList<Integer> getEventCodes() {
        return eventCodes;
    }

    public void setEventCodes(ArrayList<Integer> eventCodes) {
        this.eventCodes = eventCodes;
    }

}
