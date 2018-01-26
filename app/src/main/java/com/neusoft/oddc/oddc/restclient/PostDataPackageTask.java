package com.neusoft.oddc.oddc.restclient;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.neusoft.oddc.oddc.model.DataPackage;
import com.neusoft.oddc.oddc.model.RestDataPackage;

/**
 * Created by yzharchuk on 8/29/2017.
 */

class PostDataPackageTask extends AsyncTask<RestDataPackage, Void, HttpStatus>
{
    private String url;
    private HttpStatus returnStatus;

    public PostDataPackageTask(String url)
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
    protected HttpStatus doInBackground(RestDataPackage... data)
    {
        //TODO: Remove these out when verified...
//        Envelope envelope = data[0].getEnvelope();
//        String vin = envelope.getVehicleID();
//        UUID sessionId = envelope.getSessionID();
//        data[0] = RestDataPackage.createDummyDataPackage(vin, sessionId, DataPackageType.CONTINUOUS, 60);

        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        try
        {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            //HttpHeaders headers = new HttpHeaders();
            HttpEntity<RestDataPackage> request = new HttpEntity<>(data[0]);
            ResponseEntity<DataPackage> result = restTemplate.exchange(url, HttpMethod.POST, request, DataPackage.class);
            returnStatus = result.getStatusCode();
            Log.w("ODDC","PostDataPackageTask.doInBackground returnStatus="+returnStatus);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            Log.w("ODDC","PostDataPackageTask.doInBackground EXCEPTION returnStatus="+returnStatus+ " "+message);
            return returnStatus;
        }
        return returnStatus;
    }

    @Override
    protected void onPostExecute(HttpStatus status)
    {
    }
}