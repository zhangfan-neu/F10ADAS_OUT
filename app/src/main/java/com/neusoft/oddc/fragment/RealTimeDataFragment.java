package com.neusoft.oddc.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.DateHelper;

public class RealTimeDataFragment extends Fragment {

    private TextView real_time_data_param1;
    private TextView real_time_data_param2;
    private TextView real_time_data_param3;
    private TextView real_time_data_param4;
    private TextView real_time_data_param5;
    private TextView real_time_data_param6;
    private TextView real_time_data_param7;
    private TextView real_time_data_param8;
    private TextView real_time_data_param9;
    private TextView real_time_data_param10;
    private TextView real_time_data_param11;
    private TextView real_time_data_param12;
    private TextView real_time_data_param13;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_real_time_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        real_time_data_param1 = (TextView) view.findViewById(R.id.real_time_data_param1);
        real_time_data_param2 = (TextView) view.findViewById(R.id.real_time_data_param2);
        real_time_data_param3 = (TextView) view.findViewById(R.id.real_time_data_param3);
        real_time_data_param4 = (TextView) view.findViewById(R.id.real_time_data_param4);
        real_time_data_param5 = (TextView) view.findViewById(R.id.real_time_data_param5);
        real_time_data_param6 = (TextView) view.findViewById(R.id.real_time_data_param6);
        real_time_data_param7 = (TextView) view.findViewById(R.id.real_time_data_param7);
        real_time_data_param8 = (TextView) view.findViewById(R.id.real_time_data_param8);
        real_time_data_param9 = (TextView) view.findViewById(R.id.real_time_data_param9);
        real_time_data_param10 = (TextView) view.findViewById(R.id.real_time_data_param10);
        real_time_data_param11 = (TextView) view.findViewById(R.id.real_time_data_param11);
        real_time_data_param12 = (TextView) view.findViewById(R.id.real_time_data_param12);
        real_time_data_param13 = (TextView) view.findViewById(R.id.real_time_data_param13);

        updateDateUI();
        updateTimeUI();

    }

    public void updateDateUI() {
        real_time_data_param1.setText(DateHelper.getCurrentTime());
    }

    public void updateTimeUI() {
        real_time_data_param2.setText(DateHelper.getCurrentTime2());
    }

    public void updateLatitudeAndLongitude(String latitude, String longitude) {
        real_time_data_param3.setText(latitude);
        real_time_data_param4.setText(longitude);
    }

    public void updateSpeed(String str) {
        real_time_data_param5.setText(str);
    }


    public void updateMagnetic(String Pitch, String Roll, String yaw) {
        String Magnetic = Pitch + "," + Roll + "," + yaw;
        real_time_data_param5.setText(Magnetic);
    }

    public void updateGSensor(String accX, String accY, String accZ) {
        real_time_data_param6.setText(accX);
        real_time_data_param7.setText(accY);
        real_time_data_param8.setText(accZ);
    }

    public void updateRECTime(String str) {
        real_time_data_param9.setText(str);
    }

    public void updateVehicleDistance(String str) {
        real_time_data_param10.setText(str);
    }

    public void updateLanDistance(String left, String right) {
        real_time_data_param11.setText(right);
        real_time_data_param12.setText(left);
    }

    public void updateEvents(String str) {
        real_time_data_param13.setText(str);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

