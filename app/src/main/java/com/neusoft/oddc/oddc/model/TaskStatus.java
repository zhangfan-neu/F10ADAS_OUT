package com.neusoft.oddc.oddc.model;

/**
 * Created by rsamonte on 9/27/2017.
 */

import java.util.HashMap;

public enum TaskStatus
{
    UNKNOWN (0),
    REQUESTED (1),
    RECEIVED (2),
    COMPLETE (3),
    CANCELED (4),
    FAILED (5);

    private int value;
    private static HashMap<Object, Object> map = new HashMap<>();

    private TaskStatus(int value) {
        this.value = value;
    }

    static {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            map.put(taskStatus.value, taskStatus);
        }
    }

    public static TaskStatus valueOf(int taskStatus) {
        return (TaskStatus)map.get(taskStatus);
    }

    public int getValue() {
        return value;
    }
}