package com.neusoft.oddc.entity;


import java.io.Serializable;

public class EntityMainButton implements Serializable {

    private int index;
    private int buttonStrId;
    private Class clazz;

    public EntityMainButton(int index, int buttonText, Class clazz) {
        this.index = index;
        this.buttonStrId = buttonText;
        this.clazz = clazz;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getButtonStrId() {
        return buttonStrId;
    }

    public void setButtonStrId(int buttonStrId) {
        this.buttonStrId = buttonStrId;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

}
