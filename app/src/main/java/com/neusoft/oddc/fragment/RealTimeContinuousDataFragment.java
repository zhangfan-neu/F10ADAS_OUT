package com.neusoft.oddc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.oddc.model.ContinuousData;

import java.sql.Timestamp;

public class RealTimeContinuousDataFragment extends Fragment {

    private TextView real_time_continuous_data_param1;
    private TextView real_time_continuous_data_param2;
    private TextView real_time_continuous_data_param3;
    private TextView real_time_continuous_data_param4;
    private TextView real_time_continuous_data_param5;
    private TextView real_time_continuous_data_param6;
    private TextView real_time_continuous_data_param7;
    private TextView real_time_continuous_data_param8;
    private TextView real_time_continuous_data_param9;
    private TextView real_time_continuous_data_param10;
    private TextView real_time_continuous_data_param11;
    private TextView real_time_continuous_data_param12;
    private TextView real_time_continuous_data_param13;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_real_time_continuous_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        real_time_continuous_data_param1 = (TextView) view.findViewById(R.id.real_time_continuous_data_param1);
        real_time_continuous_data_param2 = (TextView) view.findViewById(R.id.real_time_continuous_data_param2);
        real_time_continuous_data_param3 = (TextView) view.findViewById(R.id.real_time_continuous_data_param3);
        real_time_continuous_data_param4 = (TextView) view.findViewById(R.id.real_time_continuous_data_param4);
        real_time_continuous_data_param5 = (TextView) view.findViewById(R.id.real_time_continuous_data_param5);
        real_time_continuous_data_param6 = (TextView) view.findViewById(R.id.real_time_continuous_data_param6);
        real_time_continuous_data_param7 = (TextView) view.findViewById(R.id.real_time_continuous_data_param7);
        real_time_continuous_data_param8 = (TextView) view.findViewById(R.id.real_time_continuous_data_param8);
        real_time_continuous_data_param9 = (TextView) view.findViewById(R.id.real_time_continuous_data_param9);
        real_time_continuous_data_param10 = (TextView) view.findViewById(R.id.real_time_continuous_data_param10);
        real_time_continuous_data_param11 = (TextView) view.findViewById(R.id.real_time_continuous_data_param11);
        real_time_continuous_data_param12 = (TextView) view.findViewById(R.id.real_time_continuous_data_param12);
        real_time_continuous_data_param13 = (TextView) view.findViewById(R.id.real_time_continuous_data_param13);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void updateUI(ContinuousData continuousData) {
        //String accelerationTimeStamp = continuousData.accelerationTimeStamp;
        double x = continuousData.getAccelerationX();
        double y = continuousData.getAccelerationY();
        double z = continuousData.getAccelerationZ();
        //String fcwTimeStamp = continuousData.fcwTimeStamp;
        double fcwDistanceToFV = continuousData.getFcwDistanceToFV();
        double fcwEventThreshold = continuousData.getFcwEventThreshold();
        double fcwRelativeSpeedToFV = continuousData.getFcwRelativeSpeedToFV();
        //String gShockTimeStamp = continuousData.gShockTimeStamp;
        double gShockEventThreshold = continuousData.getgShockEventThreshold();
        //String gpsTimeStamp = continuousData.gpsTimeStamp;
        double latitude = continuousData.getLatitude();
        double longitude = continuousData.getLongitude();
        //String LdwTimeStamp = continuousData.ldwTimeStamp;
        double LdwDistanceToLeftLane = continuousData.getLdwDistanceToLeftLane();
        double LdwDistanceToRightLane = continuousData.getLdwDistanceToRightLane();
        String MediaURI = continuousData.getMediaURI();
        double Speed = continuousData.getSpeed();
        double SpeedDetectionType = continuousData.getSpeedDetectionType();

        //real_time_continuous_data_param1.setText("accelerationTimeStamp \n" + accelerationTimeStamp.toString());
        real_time_continuous_data_param2.setText("acceleration x/y/z \n" + x + "\n" + y + "\n" + z);
        //real_time_continuous_data_param3.setText("fcwTimeStamp \n" + fcwTimeStamp.toString());
        real_time_continuous_data_param4.setText("fcwDistanceToFV " + fcwDistanceToFV);
        real_time_continuous_data_param5.setText("fcwEventThreshold " + fcwEventThreshold);
        real_time_continuous_data_param6.setText("fcwRelativeSpeedToFV " + fcwRelativeSpeedToFV);
        //real_time_continuous_data_param7.setText("gShockTimeStamp\n" + gShockTimeStamp + " \ngShockEventThreshold " + gShockEventThreshold);
        //real_time_continuous_data_param8.setText("gpsTimeStamp \n" + gpsTimeStamp.toString() + "\nlatitude/longitude  " + latitude + "/" + longitude);
        //real_time_continuous_data_param9.setText("LdwTimeStamp \n" + LdwTimeStamp);
        real_time_continuous_data_param10.setText("LdwDistanceToLeftLane " + LdwDistanceToLeftLane);
        real_time_continuous_data_param11.setText("LdwDistanceToRightLane " + LdwDistanceToRightLane);
        real_time_continuous_data_param12.setText("MediaURI " + MediaURI);
        real_time_continuous_data_param13.setText("Speed " + Speed + "\nSpeedDetectionType " + SpeedDetectionType);

    }
}

