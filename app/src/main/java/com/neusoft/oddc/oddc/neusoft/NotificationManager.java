package com.neusoft.oddc.oddc.neusoft;

import android.content.Context;

import com.neusoft.oddc.oddc.restclient.RESTController;
import com.neusoft.oddc.widget.PropertyUtil;

import java.util.ArrayList;

/**
 * Created by yzharchuk on 8/17/2017.
 */

public class NotificationManager
{
    private final String baseUrl = Constants.ODDCApp.BASE_URL;
    private RESTController restController;
    private ArrayList<String> notification;

    public NotificationManager()
    {
        restController = new RESTController(baseUrl);
        notification = new ArrayList<String>();
    }

    private void postStartEngineNotification(ArrayList<String> notification)
    {
//        restController.postStartEngineNotification(notification);
    }
    private void postStopEngineNotification(ArrayList<String> notification)
    {
//        restController.postStopEngineNotification(notification);
    }
}
