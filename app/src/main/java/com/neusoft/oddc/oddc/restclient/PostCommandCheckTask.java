package com.neusoft.oddc.oddc.restclient;

import android.os.AsyncTask;
import android.util.Log;

import com.neusoft.oddc.oddc.model.Envelope;
import com.neusoft.oddc.oddc.model.EventType;
import com.neusoft.oddc.oddc.model.ODDCTask;
import com.neusoft.oddc.oddc.model.TaskType;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by yzharchuk on 8/24/2017.
 */

class PostCommandCheckTask extends AsyncTask<Envelope, Void, ArrayList<ODDCTask>>
{
    private String url;
    private HttpStatus returnStatus;

    public PostCommandCheckTask(String url)
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
    protected ArrayList<ODDCTask> doInBackground(Envelope... data)
    {
        boolean hasMap = false;
        ArrayList<ODDCTask> ret = null;
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HttpEntity<Envelope> request = new HttpEntity<>(data[0]);
            ResponseEntity<Object> result = restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
            returnStatus = result.getStatusCode();

            Log.w("ODDC","PostCommandCheckTask.doInBackground returnStatus="+returnStatus.toString());

            //if (result != null && (ArrayList<LinkedHashMap>)result.getBody() != null)
            if (result != null) {
                Log.w("ODDC","PostCommandCheckTask.doInBackground result !NULL");

                ArrayList<LinkedHashMap> tmp = (ArrayList<LinkedHashMap>)result.getBody();
                if (tmp != null) {
                    Log.w("ODDC","PostCommandCheckTask.doInBackground getBody !NULL");
                    ret = new ArrayList<ODDCTask>();
                    for (LinkedHashMap map : tmp) {
                        if (map != null) {
                            hasMap =  true;
                            Log.w("ODDC","PostCommandCheckTask.doInBackground hasMap=TRUE");
                            ODDCTask task = convertToTask(map);
                            HashMap<String, Object> hmap = task.getParameters();
                            hmap.put("taskERR", returnStatus);
                            task.setParameters(hmap);
                            ret.add(task);
                        }
                    }
                    if (! hasMap){
                        Log.w("ODDC","PostCommandCheckTask.doInBackground hasMap=FALSE");
                        ret = new ArrayList<ODDCTask>();
                        ODDCTask task = new ODDCTask();
                        task.setType(TaskType.TASK_ERROR);
                        HashMap<String, Object>	hmap = new HashMap<String, Object>();
                        hmap.put("taskERR",returnStatus);
                        task.setParameters(hmap);
                        ret.add(task);
                    }
                } else {
                    Log.w("ODDC","PostCommandCheckTask.doInBackground getBody=NULL");
                    ret = new ArrayList<ODDCTask>();
                    ODDCTask task = new ODDCTask();
                    HashMap<String, Object>	hmap = new HashMap<String, Object>();
                    hmap.put("taskERR",8);
                    task.setParameters(hmap);
                    ret.add(task);
                }
            }
            else {
                Log.w("ODDC","PostCommandCheckTask.doInBackground RestTemplate.result=NULL");
                ret = new ArrayList<ODDCTask>();
                ODDCTask task = new ODDCTask();
                HashMap<String, Object>	hmap = new HashMap<String, Object>();
                hmap.put("taskERR",9);
                task.setParameters(hmap);
                ret.add(task);
            }
        }
        catch (Exception e)
        {
            Log.e("PostCommandCheckTask", "EXCEPTION retrieving commands. " + e.getMessage() );
            ret = new ArrayList<ODDCTask>();
            ODDCTask task = new ODDCTask();
            HashMap<String, Object>	hmap = new HashMap<String, Object>();
            hmap.put("taskERR",9);
            task.setParameters(hmap);
            ret.add(task);
        }

        //ODDCTask task = ret.get(0);
        //Log.w("ODDC","PostCommandCheckTask.doInBackground FINAL RETURN task0="+task);
        return ret;
    }

    private ODDCTask convertToTask(LinkedHashMap map)
    {
        ODDCTask task = new ODDCTask();
        task.setId((String)map.get("id"));
        String type = (String)map.get("type");
        task.setType(TaskType.valueOf((String)map.get("type")));

        HashMap<String, Object> parameters = (HashMap<String, Object>)map.get("parameters");
        task.setParameters(parameters);
        return task;
    }

    @Override
    protected void onPostExecute(ArrayList<ODDCTask> tasks)
    {
    }
}
