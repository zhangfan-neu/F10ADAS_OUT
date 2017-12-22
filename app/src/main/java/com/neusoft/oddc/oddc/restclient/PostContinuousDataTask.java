package com.neusoft.oddc.oddc.restclient;

/*
  Created by yzharchuk on 8/2/2017.
 */

import android.os.AsyncTask;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.neusoft.oddc.oddc.model.ContinuousDataCollection;


public class PostContinuousDataTask  extends AsyncTask<ContinuousDataCollection, Void, HttpStatus> {
    private String url;
    private HttpStatus returnStatus;

    public HttpStatus getReturnStatus() {
        return returnStatus;
    }

    PostContinuousDataTask(String url )
    {
        super();
        this.url = url;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }

    @Override
    protected HttpStatus doInBackground(ContinuousDataCollection... dataCollection) {
        RestTemplate restTemplate = new RestTemplate();
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
          //HttpHeaders headers = new HttpHeaders();
          //restTemplate.postForObject(url, dataCollection[0], ContinuousDataCollection.class);
            HttpEntity<ContinuousDataCollection> request = new HttpEntity<>(dataCollection[0]);
            ResponseEntity<ContinuousDataCollection> result = restTemplate.exchange(url, HttpMethod.POST, request, ContinuousDataCollection.class);
            returnStatus = result.getStatusCode();
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            return returnStatus;
        }
        return returnStatus;
    }

    @Override
    protected void onPostExecute(HttpStatus status)
    {

    }

}