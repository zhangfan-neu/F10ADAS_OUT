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

    @Override
    protected ArrayList<ODDCTask> doInBackground(Envelope... data)
    {
        ArrayList<ODDCTask> ret = null;
        RestTemplate restTemplate = new RestTemplate();
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HttpEntity<Envelope> request = new HttpEntity<>(data[0]);
            ResponseEntity<Object> result = restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
            returnStatus = result.getStatusCode();

            Log.w("ODDC","PostCommandCheckTask.doInBackground returnStatus="+returnStatus.toString());

            if (result != null && (ArrayList<LinkedHashMap>)result.getBody() != null)
            {
                ArrayList<LinkedHashMap> tmp = (ArrayList<LinkedHashMap>)result.getBody();
                ret = new ArrayList<ODDCTask>();

                for (LinkedHashMap map: tmp)
                {
                    if(map != null)
                    {
                        ODDCTask task = convertToTask(map);
                        HashMap<String, Object> hmap = task.getParameters();
                        hmap.put("taskERR",0);
                        task.setParameters(hmap);
                        ret.add(task);
                    }
                }
            }
            else {
                ret = new ArrayList<ODDCTask>();
                ODDCTask task = new ODDCTask();
                HashMap<String, Object>	hmap = new HashMap<String, Object>();
                hmap.put("taskERR",1);
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
            hmap.put("taskERR",2);
            task.setParameters(hmap);
            ret.add(task);
        }
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
