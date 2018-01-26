package com.neusoft.oddc.oddc.restclient;

/**
 * Created by yzharchuk on 8/8/2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.neusoft.oddc.oddc.model.VideoCollection;



public class PostMediaDataTask extends AsyncTask<VideoCollection, Void, Void>
{
    private String url;

    PostMediaDataTask(String url)
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
    protected Void doInBackground(VideoCollection... data)
    {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            //HttpHeaders headers = new HttpHeaders();
            restTemplate.postForObject(url, data[0], VideoCollection.class);
        }
        catch (Exception e)
        {
            Log.e("POST MediaData failed: ", e.getMessage());
        }
        return null;
    }

    protected void onPostExecute()
    {
    }
}