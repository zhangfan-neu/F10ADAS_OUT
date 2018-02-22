package com.neusoft.oddc.oddc.neusoft;


import android.content.Context;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.neusoft.oddc.activity.MainActivity;

import java.util.Set;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.lang.reflect.Method;



public class OBDManager {
    private static Context mContext;
    private static OBDManager instance;

    SensorManager smgr;
    LocationManager lmgr;
    LocationListener gpsListener;



    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "OBDManager";
    private static final UUID obdUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ConnectThread obdThread;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice obdDevice = null;
    private boolean btConnected = false;
    private boolean streaming = true;
    private boolean needReadVIN = true;
    private boolean mbtOK = true;
    private boolean mgpsOK = true;
    private long pOBD = 125;
    private long pGPS = 250;

    private float[] mMagnetic;
    private float[] mGravity;

    private float accX = 0.0f;
    private float accY = 0.0f;
    private float accZ = 0.0f;
    private int rpm = 0;
    private int spd = 0;
    private String vinBuf = "NA";
    private static Location mLastLocation;

    private byte[] dbuf;

    public String errMsg = "";
    public boolean newMsg = false;

    //final byte[] vinCmd = "0902\r".getBytes();
    final byte[] ate0 = "ATE0\r".getBytes();
    final byte[] ats0 = "ATS0\r".getBytes();
    final byte[] atl0 = "ATL0\r".getBytes();
    final byte[] atl1 = "ATL1\r".getBytes();
    final byte[] rpmCmd = "010C\r".getBytes();
    final byte[] spdCmd = "010D\r".getBytes();


    public static OBDManager getInstance()
    {
        return instance;
    }

    public void setOBDinterval(long p){
        pOBD = p;}

    public String getVIN(){
        return vinBuf;
    }

    public int getSPD(){return spd;}
    public int getRPM(){return rpm;}
    public boolean btOK(){return mbtOK;}

    public static Location getLocation() {
        Log.d(TAG, "getLocation mLastLocation = " + mLastLocation);
        return mLastLocation;
    }

    public float getAccelerometerX() {
        return accX;
    }
    public float getAccelerometerY() {
        return accY;
    }
    public float getAccelerometerZ() {
        return accZ;
    }

    public float[] getMagnetic(){return mMagnetic;}
    public float[] getGravity(){return mGravity;}

    public OBDManager(Context ctx){
        instance = this;
        mContext = ctx;

        smgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        setupBluetooth();
        setupGPS();
        setupACC();
        //setupMAG();
        //setupGVT();
    }

    private boolean setupBluetooth(){
        dbuf = new byte[64];
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.w(TAG,"OBDManager Bluetooth not available");
            errMsg = "ERROR Bluetooth not available";
            newMsg = true;
            mbtOK = false;
        }
        else {
            /*if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }*/
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.w(TAG,"BLUETOOTH Device "+deviceHardwareAddress+" "+deviceName);
                    if (deviceName.startsWith("OBDLink")) obdDevice = device;
                }
            }
        }
        if (obdDevice != null) {
            obdThread = new ConnectThread(obdDevice);
            if (mbtOK){
                Log.w(TAG, "CALLing ConnectThread.run");
                errMsg = "BT OK, CALLing obdThread.start";
                newMsg = true;
                obdThread.start();
            }
            else {
                Log.e(TAG, "BT not OK, NOT CALLing ConnectThread.run");
                errMsg = "ERROR BT not OK";
                newMsg = true;
                mbtOK = false;
            }
        }
        else {
            Log.e(TAG, "OBDLink not found");
            errMsg = "ERROR OBDLink not found";
            newMsg = true;
            mbtOK = false;
        }
        return mbtOK;
    }

    private boolean setupGPS(){
        lmgr = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        if (lmgr == null){
            mgpsOK = false;
            return false;
        }
        if (! lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            mgpsOK = false;
            return false;
        }
        gpsListener = new GPSLocationListener();
        try {
            lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, pGPS, 1, gpsListener);
        }
        catch (SecurityException e) {
            mgpsOK = false;
            Log.e(TAG,e.toString());
            return false;
        }
        return true;
    }

    private class GPSLocationListener extends Thread implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {mLastLocation = loc;}

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private void setupACC(){
        //SensorManager smgr;
        Sensor accSensor;
        SensorEventListener accListener;
        Log.w(TAG,"setupACC");

        //smgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        accSensor = smgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accListener = new ACCListener();
        smgr.registerListener(accListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private class ACCListener extends Thread implements SensorEventListener {
        double gx, gy, gz;
        double ngx, ngy, ngz;
        double acosD, natanD;

        @Override
        public void onSensorChanged(SensorEvent event) {
            accX = event.values[0]; //ngx = gx / 9.81;
            accY = event.values[1]; //ngy = gy / 9.81;
            accZ = event.values[2]; //ngz = gz / 9.81;
            /*gForce = Math.sqrt((gx * gx) + (gy * gy) + (gz * gz));
            ngForce = Math.sqrt((ngx * ngx) + (ngy * ngy) + (ngz * ngz));
            if (gForce > maxG) maxG = gForce;
            if (ngForce > nmaxG) nmaxG = ngForce;*/
            //Log.w(TAG,"onSensorChanged "+accX);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    private void setupMAG(){
        Sensor magSensor;
        SensorEventListener magListener;
        Log.w(TAG,"setupMAG");

        magSensor = smgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magListener = new MAGListener();
        smgr.registerListener(magListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private class MAGListener extends Thread implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mMagnetic = event.values.clone();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    private void setupGVT(){
        Sensor gvtSensor;
        SensorEventListener gvtListener;
        Log.w(TAG,"setupMAG");

        gvtSensor = smgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gvtListener = new GVTListener();
        smgr.registerListener(gvtListener, gvtSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private class GVTListener extends Thread implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mGravity = event.values.clone();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectThread(BluetoothDevice dev) {
            BluetoothSocket sock = null;
            BluetoothSocket sockFallback = null;

            Log.w(TAG, "OBDManager.ConnectThread establishing Bluetooth connection..");
            try {
                sock = dev.createRfcommSocketToServiceRecord(obdUUID);
                sock.connect();
            } catch (Exception e1) {
                Log.e(TAG, "There was an error while establishing Bluetooth connection. Falling back..", e1);
                errMsg = "ERROR " + e1.toString();
                newMsg = true;
                Class<?> clazz = sock.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                try {
                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};
                    sockFallback = (BluetoothSocket) m.invoke(sock.getRemoteDevice(), params);
                    sockFallback.connect();
                    sock = sockFallback;
                } catch (Exception e2) {
                    Log.e(TAG, "Fallback failed establishing Bluetooth connection.", e2);
                    mbtOK = false;
                    errMsg = "ERROR " + e2.toString();
                    newMsg = true;
                    //throw new IOException(e2.getMessage());
                }
            }
            if (mbtOK) {
                mmSocket = sock;
                btConnected = true;
                Log.w(TAG, "BluetoothSocket created");
                errMsg = "BluetoothSocket created";
                newMsg = true;
            }
        }

        public void run() {
            errMsg = "ConnectThread.run";
            Log.w(TAG,errMsg);
            newMsg = true;
            setupStreams();
            writeELM(ate0);
            writeELM(ats0);

            Log.w(TAG,"ConnectThread.run BoF");
            while (streaming){
                //Log.w(TAG,"ConnectThread.run needReadVIN="+needReadVIN);
                if (needReadVIN){
                    writeELM(atl1);
                    vinBuf = readVIN();
                    writeELM(atl0);
                    if (vinBuf != "NA"){
                        needReadVIN = false;
                        MainActivity.getInstance().setVIN();
                    }
                    //needReadVIN = false;
                    Log.w(TAG,"ConnectThread.run.while needReadVIN="+vinBuf);
                }
                else {

                    //rpm = hex2val(0x0c, writeELM(rpmCmd));
                    spd = hex2val(0x0d, writeELM(spdCmd));
                    Log.i(TAG,"ConnectThread.run rpm spd="+spd);
                }
                SystemClock.sleep(pOBD);
            }
            Log.d(TAG,"ConnectThread.run AFTER streaming");
            cancel();
        }

        public void setupStreams(){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                errMsg = "setupStreams ERROR " + e.toString();
                newMsg = true;
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                errMsg = "setupStreams ERROR " + e.toString();
                newMsg = true;
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        String readVIN(){
            final byte[] vinCmd = "0902\r".getBytes();
            int[][] vInts = {{7,8},{9,10},{11,12},
                    {17,18},{19,20},{21,22},{23,24},{25,26},{27,28},{29,30},
                    {35,36},{37,38},{39,40},{41,42},{43,44},{45,46},{47,48}};

            byte vbuf[] = new byte[64];
            byte cbuf[] = new byte[4];
            byte nbyte;
            final char[] cs = new char[18];
            int i = 0;
            int startFound = 0;
            int aBytes = 0;

            String s;
            char c;
            Log.w(TAG,"ConnectThread.readVIN");
            try {
                mmOutStream.write(vinCmd);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread.readVIN send ERROR", e);
                errMsg = "readVIN ERROR " + e.toString();
                newMsg = true;
                return new String("NA");
            }
            vbuf[0] = 0x00;
            while (true) {
                try {
                    aBytes = mmInStream.available();
                    if (aBytes == 0) continue;
                    nbyte = (byte)(mmInStream.read());
                    //Log.d(TAG,"readVIN "+i+" WHILE "+Integer.toString(nbyte,16));
                    if (nbyte != -1) {
                        if (startFound != 2) {
                            if (nbyte == 0x30){
                                startFound = 1;
                                continue;
                            }
                            if (startFound == 1 && nbyte == 0x3a){
                                startFound = 2;
                            }
                            else continue;
                        }
                        vbuf[i] = nbyte;
                        if (nbyte == 0x3e) break; /* > dec62*/
                        i++;
                        if (i == 64) break;
                    }
                    else break;
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    errMsg = "readVIN ERROR " + e.toString();
                    newMsg = true;
                    return new String("NA");
                }
            }
            //Log.w(TAG,"ConnectThread.readVIN "+cbuf);
            for (i=0;i<17;i++){
                cbuf[0] = vbuf[vInts[i][0]];
                cbuf[1] = vbuf[vInts[i][1]];
                cbuf[2] = 0x0;
                try {
                    cs[i] = (char) Integer.parseInt(new String(cbuf).substring(0, 2), 16);
                }
                catch (java.lang.NumberFormatException nfe){
                    Log.e(TAG,"readVIN ERROR "+nfe.getMessage());
                    errMsg = "readVIN ERROR " + nfe.toString();
                    newMsg = true;
                    return new String("NA");
                }
            }
            errMsg = "readVIN VIN="+cs[0]+cs[1]+cs[2]+cs[3]+cs[4]+cs[5]+cs[6];
            Log.w(TAG,errMsg);
            newMsg = true;
            return new String(cs);
        }

        byte[] writeELM(final byte[] cmd){
            byte nbyte;
            int s;
            int i = 0;
            byte[] rbuf = new byte[64];
            dbuf[0] = 0x00;
            boolean pbyte;
            String ch;
            byte[] b = new byte[2];

            Log.i(TAG,"writeELM "+cmd[0]+" "+cmd[1]+" "+cmd[2]+" "+cmd[3]);
            try {
                mmOutStream.write(cmd);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                errMsg = "writeELM ERROR " + e.toString();
                newMsg = true;
                return dbuf;
            }
            dbuf[0] = 0x00;
            if (cmd[0] == 'A'){
                while (true) {
                    try {
                        nbyte = (byte) (mmInStream.read());
                        if (nbyte == -1) break;
                        if (nbyte == 0x3e) break;
                        rbuf[i] = nbyte; i++;
                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        errMsg = "writeELM ERROR " + e.toString();
                        newMsg = true;
                        break;
                    }
                }
                rbuf[i] = 0x00;
                return rbuf;
            }
            else {
                while (true) {
                    try {
                        nbyte = (byte) (mmInStream.read());
                        if (nbyte != -1) {
                            rbuf[i] = nbyte;
                            if (nbyte == 0x13) rbuf[i] = 0x00;
                            if (nbyte == 0x3e) { /* > dec62*/
                                s = 0;
                                if (cmd[0] != 'A') {
                                    while (rbuf[s + 4] != 0x0d) {
                                        dbuf[s] = rbuf[s + 4];
                                        s++;
                                        if (s == 16) break;
                                    }
                                    dbuf[s] = 0x00;
                                    if (cmd[3] == 67) errMsg = "writeELM RETURN " + dbuf[0]+" "+dbuf[1]+" "+dbuf[2]+" "+dbuf[3];
                                    else              errMsg = "writeELM RETURN " + dbuf[0]+" "+dbuf[1];
                                    newMsg = true;
                                    return dbuf;
                                }
                                break;
                            }
                            i++;
                            if (i == 64) break;
                        } else break;
                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        errMsg = "writeELM ERROR " + e.toString();
                        newMsg = true;
                        break;
                    }
                }
            }
            Log.i(TAG,"writeELM EoF should return before this");
            return dbuf;
        }

        int hex2val(int pid,byte[] str){
            int val = -1;
            String obdStr = new String(str);
            try {
                switch (pid) {
                    case 0x0c:
                        val = Integer.parseInt(obdStr.substring(0,4), 16);
                        return (int) (val / 4);
                    case 0x0d:
                        val = Integer.parseInt(obdStr.substring(0,2), 16);
                        return (int) val;
                }
                return val;
            }
            catch(NumberFormatException nfe){
                Log.e(TAG, "ERROR parseInt " +nfe.toString());
                /*for (int i = 0;i <  obdStr.length(); i++) {
                    Log.e(TAG,"ERROR parseInt "+ i + " obdStr:"+obdStr.charAt(i)+" "+ (int)obdStr.charAt(i));
                }*/
            }
            return 0;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

}
