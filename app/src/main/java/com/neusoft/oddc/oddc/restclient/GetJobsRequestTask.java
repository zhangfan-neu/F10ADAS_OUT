package com.neusoft.oddc.oddc.restclient;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Override
    protected ArrayList<ODDCJob> doInBackground(ODDCTask... data)
    {
        RestTemplate restTemplate = new RestTemplate();
        ArrayList<ODDCJob> jobs = null;

        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HttpEntity<ODDCTask> request = new HttpEntity<>(data[0]);
            ResponseEntity<ODDCJobCollection> result = restTemplate.exchange(url, HttpMethod.POST, request, ODDCJobCollection.class);
            jobs = result.getBody().getJobs();
            returnStatus = result.getStatusCode();
        }
        catch (Exception e)
        {
            Log.e("GET Jobs failed: ", e.getMessage());
        }
        return jobs;
    }

    protected void onPostExecute(ArrayList<ODDCJob> jobs)
    {
    }
}