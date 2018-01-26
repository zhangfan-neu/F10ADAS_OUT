package com.neusoft.oddc.oddc.restclient;

import android.os.AsyncTask;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.neusoft.oddc.oddc.model.Notification;

/**
 * Created by yzharchuk on 8/17/2017.
 */


public class PostNotificationTask  extends AsyncTask<Notification, Void, Void>
{
    private String url;

    PostNotificationTask(String url)
    {
        this.url = url;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }

    @Override
    protected Void doInBackground(Notification... notification)
    {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            //HttpHeaders headers = new HttpHeaders();
            restTemplate.postForObject(url, notification[0], Notification.class);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            return null;
        }
        return null;
    }

    protected void onPostExecute()
    {
    }
}