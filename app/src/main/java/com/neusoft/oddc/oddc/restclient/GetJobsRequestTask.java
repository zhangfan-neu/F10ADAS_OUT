package com.neusoft.oddc.oddc.restclient;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import com.neusoft.oddc.oddc.model.ODDCJob;
import com.neusoft.oddc.oddc.model.ODDCJobCollection;
import com.neusoft.oddc.oddc.model.ODDCTask;

/**
 * Created by yzharchuk on 8/1/2017.
 */
public class GetJobsRequestTask extends AsyncTask<ODDCTask, Void, ArrayList<ODDCJob>>
{
    private String url;
    private HttpStatus returnStatus;

    GetJobsRequestTask(String url)
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
    protected ArrayList<ODDCJob> doInBackground(ODDCTask... data)
    {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        ArrayList<ODDCJob> jobs = null;
        ResponseEntity<Object> result = null;

        Log.w("ODDC","GetJobsRequestTask.doInBackground BoM");
        try
        {
            Log.w("ODDC","GetJobsRequestTask.doInBackground.try");

            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Log.w("ODDC","GetJobsRequestTask.doInBackground.try restTemplate.getMessageConverters");

            HttpEntity<ODDCTask> request = new HttpEntity<>(data[0]);
            Log.w("ODDC","GetJobsRequestTask.doInBackground.try new HttpEntity");

            result = restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
            Log.w("ODDC","GetJobsRequestTask.doInBackground.try restTemplate.exchange");

            returnStatus = result.getStatusCode();
            Log.w("ODDC","GetJobsRequestTask.doInBackground.try  returnStatus="+returnStatus);

            //jobs = result.getBody().getJobs();
        }
        catch (Exception e)
        {
            Log.e("GET Jobs failed: ", e.getMessage());
        }
        Log.w("ODDC","GetJobsRequestTask.doInBackground EoM");
        return jobs;
    }

    protected void onPostExecute(ArrayList<ODDCJob> jobs)
    {
    }
}